package project.pj25.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import project.pj25.model.*; // Uvozimo sve iz model paketa

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TransportDataGenerator {
    private static final int DEFAULT_SIZE_N = 3; // Broj redova gradova
    private static final int DEFAULT_SIZE_M = 3; // Broj kolona gradova
    private static final int DEPARTURES_PER_STATION_TYPE_PER_DESTINATION = 3; // Broj polazaka po tipu stanice do odredišta
    private static final Random random = new Random();

    private final int n; // Broj redova
    private final int m; // Broj kolona

    public TransportDataGenerator(int n, int m) {
        this.n = n;
        this.m = m;
    }

    public static void main(String[] args) {
        // Kreira generator sa defaultnim dimenzijama
        TransportDataGenerator generator = new TransportDataGenerator(DEFAULT_SIZE_N, DEFAULT_SIZE_M);
        TransportMap transportMap = generator.generateData();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Registrujemo modul za java.time objekte
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // Za ljepši ispis JSON-a

        try {
            // Serijalizacija TransportMap objekta direktno
            mapper.writeValue(new File("transport_data.json"), transportMap);
            System.out.println("Podaci su generisani i sacuvani kao transport_data.json");
        } catch (IOException e) {
            System.err.println("Greska prilikom cuvanja podataka u JSON fajl: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Generise sve podatke i vraca TransportMap objekat
    public TransportMap generateData() {
        TransportMap transportMap = new TransportMap(n, m);

        // 1. Generisanje gradova
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                int cityId = x * m + y;
                City city = new City(cityId, x, y);
                transportMap.addCity(x, y, city);
            }
        }

        // 2. Generisanje stanica za svaki grad i dodavanje u TransportMap
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                City currentCity = transportMap.getCity(x, y);

                BusStation busStation = new BusStation(currentCity);
                transportMap.addStation(busStation);

                TrainStation trainStation = new TrainStation(currentCity);
                transportMap.addStation(trainStation);
            }
        }

        // 3. Generisanje polazaka
        // Za svaku stanicu generišemo polaske ka susjednim gradovima i stanicama unutar istog grada
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                City currentCity = transportMap.getCity(x, y);
                BusStation currentBusStation = (BusStation) transportMap.getStation("A_" + x + "_" + y);
                TrainStation currentTrainStation = (TrainStation) transportMap.getStation("Z_" + x + "_" + y);

                // Polasci unutar istog grada (transferi između autobuskih i željezničkih stanica)
                if (currentBusStation != null && currentTrainStation != null) {
                    // Autobus -> Voz
                    for (int i = 0; i < DEPARTURES_PER_STATION_TYPE_PER_DESTINATION; i++) {
                        currentBusStation.addDeparture(generateDeparture(
                                "autobus", currentBusStation.getId(), currentTrainStation.getId(), true
                        ));
                    }
                    // Voz -> Autobus
                    for (int i = 0; i < DEPARTURES_PER_STATION_TYPE_PER_DESTINATION; i++) {
                        currentTrainStation.addDeparture(generateDeparture(
                                "voz", currentTrainStation.getId(), currentBusStation.getId(), true
                        ));
                    }
                }

                // Polasci ka susjednim gradovima
                List<City> neighborCities = getNeighborCities(x, y, transportMap);
                for (City neighborCity : neighborCities) {
                    BusStation neighborBusStation = (BusStation) transportMap.getStation("A_" + neighborCity.getX() + "_" + neighborCity.getY());
                    TrainStation neighborTrainStation = (TrainStation) transportMap.getStation("Z_" + neighborCity.getX() + "_" + neighborCity.getY());

                    if (currentBusStation != null && neighborBusStation != null) {
                        for (int i = 0; i < DEPARTURES_PER_STATION_TYPE_PER_DESTINATION; i++) {
                            currentBusStation.addDeparture(generateDeparture(
                                    "autobus", currentBusStation.getId(), neighborBusStation.getId(), false
                            ));
                        }
                    }
                    if (currentTrainStation != null && neighborTrainStation != null) {
                        for (int i = 0; i < DEPARTURES_PER_STATION_TYPE_PER_DESTINATION; i++) {
                            currentTrainStation.addDeparture(generateDeparture(
                                    "voz", currentTrainStation.getId(), neighborTrainStation.getId(), false
                            ));
                        }
                    }
                }
            }
        }
        return transportMap;
    }

    // Generiše jedan polazak
    private Departure generateDeparture(String type, String fromStationId, String toStationId, boolean isLocalTransfer) {
        // Generisanje vremena polaska (svakih 15 minuta)
        int hour = random.nextInt(24);
        int minute = random.nextInt(4) * 15;
        LocalTime departureTime = LocalTime.of(hour, minute);

        // Generisanje trajanja putovanja (duration)
        // Lokalni transferi su kraći, dalji duži
        int durationMinutes = isLocalTransfer ? (10 + random.nextInt(20)) : (30 + random.nextInt(151));
        Duration duration = Duration.ofMinutes(durationMinutes);

        // Generisanje vremena dolaska (može preći ponoć)
        LocalTime arrivalTime = departureTime.plus(duration);

        // Generisanje cijene
        double price = isLocalTransfer ? (5.0 + random.nextDouble() * 15.0) : (100.0 + random.nextDouble() * 900.0);
        price = Math.round(price * 100.0) / 100.0; // Zaokruživanje na dvije decimale

        // Generisanje minimalnog vremena čekanja za transfer
        int minTransferMinutes = isLocalTransfer ? 0 : (5 + random.nextInt(26)); // Lokalni transferi obično nemaju čekanje
        Duration minTransferTime = Duration.ofMinutes(minTransferMinutes);

        return new Departure(type, fromStationId, toStationId, departureTime, arrivalTime, price, minTransferTime);
    }

    // Pronalazi susjedne gradove (gore, dole, lijevo, desno)
    private List<City> getNeighborCities(int x, int y, TransportMap map) {
        List<City> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Gore, dole, lijevo, desno

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && nx < n && ny >= 0 && ny < m) {
                neighbors.add(map.getCity(nx, ny));
            }
        }
        return neighbors;
    }
}