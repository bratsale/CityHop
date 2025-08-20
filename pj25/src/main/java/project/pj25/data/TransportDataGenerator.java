package project.pj25.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import project.pj25.model.*;
import project.pj25.util.LocalTimeSerializer;
import project.pj25.util.DurationSerializer;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TransportDataGenerator {
    private static final int DEFAULT_SIZE_N = 5;
    private static final int DEFAULT_SIZE_M = 5;
    // Povećavamo broj polazaka između stanica kako bismo stvorili gušći graf.
    private static final int DEPARTURES_PER_STATION_TYPE_PER_DESTINATION = 5; // Povećano sa 3 na 5
    private static final int MIN_STATIONS_PER_CITY = 1;
    private static final int MAX_STATIONS_PER_CITY = 3;
    private static final Random random = new Random();

    private final int n;
    private final int m;

    public TransportDataGenerator(int n, int m) {
        this.n = n;
        this.m = m;
    }

    public static void main(String[] args) {
        TransportDataGenerator generator = new TransportDataGenerator(DEFAULT_SIZE_N, DEFAULT_SIZE_M);
        TransportMap transportMap = generator.generateData();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        SimpleModule customModule = new SimpleModule();
        customModule.addSerializer(LocalTime.class, new LocalTimeSerializer());
        customModule.addSerializer(Duration.class, new DurationSerializer());
        mapper.registerModule(customModule);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            mapper.writeValue(new File("transport_data.json"), transportMap);
            System.out.println("Podaci su generisani i sacuvani kao transport_data.json");
            System.out.println("\n--- Pokusavam da ucitam generisani JSON sa DataLoaderom ---");
            project.pj25.data.DataLoader.loadTransportData("transport_data.json");
            System.out.println("--- Ucitavanje zavrseno ---");

        } catch (IOException e) {
            System.err.println("Greska prilikom cuvanja podataka u JSON fajl: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public TransportMap generateData() {
        TransportMap transportMap = new TransportMap(n, m);

        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                int cityId = x * m + y;
                City city = new City(cityId, x, y);
                transportMap.addCity(x, y, city);
            }
        }

        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                City currentCity = transportMap.getCity(x, y);

                int numBusStations = MIN_STATIONS_PER_CITY + random.nextInt(MAX_STATIONS_PER_CITY - MIN_STATIONS_PER_CITY + 1);
                for (int i = 0; i < numBusStations; i++) {
                    String busStationId = "A_" + x + "_" + y + "_" + i;
                    BusStation busStation = new BusStation(busStationId, currentCity);
                    currentCity.addStation(busStation);
                    transportMap.addStation(busStation);
                }

                int numTrainStations = MIN_STATIONS_PER_CITY + random.nextInt(MAX_STATIONS_PER_CITY - MIN_STATIONS_PER_CITY + 1);
                for (int i = 0; i < numTrainStations; i++) {
                    String trainStationId = "Z_" + x + "_" + y + "_" + i;
                    TrainStation trainStation = new TrainStation(trainStationId, currentCity);
                    currentCity.addStation(trainStation);
                    transportMap.addStation(trainStation);
                }
            }
        }

        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                City currentCity = transportMap.getCity(x, y);
                List<Station> currentCityStations = currentCity.getStations();

                List<BusStation> currentBusStations = currentCityStations.stream()
                        .filter(s -> s instanceof BusStation)
                        .map(s -> (BusStation) s)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

                List<TrainStation> currentTrainStations = currentCityStations.stream()
                        .filter(s -> s instanceof TrainStation)
                        .map(s -> (TrainStation) s)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

                if (!currentBusStations.isEmpty() || !currentTrainStations.isEmpty()) {
                    for (BusStation busStationA : currentBusStations) {
                        for (BusStation busStationB : currentBusStations) {
                            if (busStationA.equals(busStationB)) continue;
                            busStationA.addDeparture(generateDeparture(
                                    "autobus", busStationA.getId(), busStationB.getId(), true
                            ));
                        }
                    }

                    for (TrainStation trainStationA : currentTrainStations) {
                        for (TrainStation trainStationB : currentTrainStations) {
                            if (trainStationA.equals(trainStationB)) continue;
                            trainStationA.addDeparture(generateDeparture(
                                    "voz", trainStationA.getId(), trainStationB.getId(), true
                            ));
                        }
                    }

                    if (!currentBusStations.isEmpty() && !currentTrainStations.isEmpty()) {
                        for (BusStation busStation : currentBusStations) {
                            for (int i = 0; i < DEPARTURES_PER_STATION_TYPE_PER_DESTINATION; i++) {
                                TrainStation randomTrainStation = currentTrainStations.get(random.nextInt(currentTrainStations.size()));
                                busStation.addDeparture(generateDeparture(
                                        "autobus", busStation.getId(), randomTrainStation.getId(), true
                                ));
                            }
                        }
                        for (TrainStation trainStation : currentTrainStations) {
                            for (int i = 0; i < DEPARTURES_PER_STATION_TYPE_PER_DESTINATION; i++) {
                                BusStation randomBusStation = currentBusStations.get(random.nextInt(currentBusStations.size()));
                                trainStation.addDeparture(generateDeparture(
                                        "voz", trainStation.getId(), randomBusStation.getId(), true
                                ));
                            }
                        }
                    }
                }

                List<City> neighborCities = getNeighborCities(x, y, transportMap);
                for (City neighborCity : neighborCities) {
                    List<Station> neighborCityStations = neighborCity.getStations();
                    List<BusStation> neighborBusStations = neighborCityStations.stream()
                            .filter(s -> s instanceof BusStation)
                            .map(s -> (BusStation) s)
                            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

                    List<TrainStation> neighborTrainStations = neighborCityStations.stream()
                            .filter(s -> s instanceof TrainStation)
                            .map(s -> (TrainStation) s)
                            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

                    if (!currentBusStations.isEmpty() && !neighborBusStations.isEmpty()) {
                        for (BusStation currentBusStation : currentBusStations) {
                            // Izmena: Petlja ide do N umesto do 3
                            for (int i = 0; i < DEPARTURES_PER_STATION_TYPE_PER_DESTINATION; i++) {
                                BusStation randomNeighborBusStation = neighborBusStations.get(random.nextInt(neighborBusStations.size()));
                                currentBusStation.addDeparture(generateDeparture(
                                        "autobus", currentBusStation.getId(), randomNeighborBusStation.getId(), false
                                ));
                            }
                        }
                    }

                    if (!currentTrainStations.isEmpty() && !neighborTrainStations.isEmpty()) {
                        for (TrainStation currentTrainStation : currentTrainStations) {
                            // Izmena: Petlja ide do N umesto do 3
                            for (int i = 0; i < DEPARTURES_PER_STATION_TYPE_PER_DESTINATION; i++) {
                                TrainStation randomNeighborTrainStation = neighborTrainStations.get(random.nextInt(neighborTrainStations.size()));
                                currentTrainStation.addDeparture(generateDeparture(
                                        "voz", currentTrainStation.getId(), randomNeighborTrainStation.getId(), false
                                ));
                            }
                        }
                    }
                }
            }
        }
        return transportMap;
    }

    // Izmenjena metoda generateDeparture
    private Departure generateDeparture(String type, String fromStationId, String toStationId, boolean isLocalTransfer) {
        int hour = random.nextInt(24);
        int minute = random.nextInt(4) * 15;
        LocalTime departureTime = LocalTime.of(hour, minute);

        int durationMinutes;
        double price;

        if (isLocalTransfer) {
            // Smanjujemo raspon za unutar-gradske transfere da budu predvidljiviji
            durationMinutes = 10 + random.nextInt(10); // Od 10 do 19 minuta
            price = 5.0 + random.nextDouble() * 5.0; // Od 5.0 do 10.0 KM
        } else {
            // Povećavamo broj ruta i smanjujemo razlike u ceni i vremenu da bi se dobilo više alternativa
            durationMinutes = 30 + random.nextInt(61); // Od 30 do 90 minuta
            price = 100.0 + random.nextDouble() * 400.0; // Od 100.0 do 500.0 KM
        }

        Duration duration = Duration.ofMinutes(durationMinutes);
        LocalTime arrivalTime = departureTime.plus(duration);

        price = Math.round(price * 100.0) / 100.0;

        int minTransferMinutes = isLocalTransfer ? 0 : (5 + random.nextInt(16)); // Smanjen raspon
        Duration minTransferTime = Duration.ofMinutes(minTransferMinutes);

        return new Departure(type, fromStationId, toStationId, departureTime, arrivalTime, price, minTransferTime);
    }

    private List<City> getNeighborCities(int x, int y, TransportMap map) {
        List<City> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

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