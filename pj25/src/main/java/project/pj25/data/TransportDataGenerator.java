package project.pj25.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.module.SimpleModule; // Važan import!
import project.pj25.model.*;
import project.pj25.util.LocalTimeSerializer;
import project.pj25.util.DurationSerializer;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TransportDataGenerator {
    private static final int DEFAULT_SIZE_N = 5;
    private static final int DEFAULT_SIZE_M = 5;
    private static final int DEPARTURES_PER_STATION_TYPE_PER_DESTINATION = 3;
    private static final int MIN_STATIONS_PER_CITY = 1; // Minimalno stanica po gradu
    private static final int MAX_STATIONS_PER_CITY = 3; // Maksimalno stanica po gradu (3 kao što si tražio)
    private static final Random random = new Random();

    private final int n; // Broj redova
    private final int m; // Broj kolona

    public TransportDataGenerator(int n, int m) {
        this.n = n;
        this.m = m;
    }

    public static void main(String[] args) {
        TransportDataGenerator generator = new TransportDataGenerator(DEFAULT_SIZE_N, DEFAULT_SIZE_M);
        TransportMap transportMap = generator.generateData();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Registruj standardni modul za java.time
        // (koristiće se za tipove koje mi ne override-ujemo)

        // Registrujemo custom serializere koji će override-ovati defaultno ponašanje JavaTimeModule-a
        // za LocalTime i Duration
        SimpleModule customModule = new SimpleModule();
        customModule.addSerializer(LocalTime.class, new LocalTimeSerializer());
        customModule.addSerializer(Duration.class, new DurationSerializer());
        mapper.registerModule(customModule); // Registruj custom modul

        mapper.enable(SerializationFeature.INDENT_OUTPUT); // Za ljepši ispis JSON-a
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // TEST1

        try {
            // Serijalizacija TransportMap objekta direktno
            mapper.writeValue(new File("transport_data.json"), transportMap);
            System.out.println("Podaci su generisani i sacuvani kao transport_data.json");

            // Opciono: Testiraj učitavanje podataka odmah nakon generisanja
            System.out.println("\n--- Pokusavam da ucitam generisani JSON sa DataLoaderom ---");
            // Uvjeri se da DataLoader.loadTransportData() metoda postoji i da je javno dostupna
            project.pj25.data.DataLoader.loadTransportData("transport_data.json");
            System.out.println("--- Ucitavanje zavrseno ---");

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

                // Generisanje nasumičnog broja autobuskih stanica (1 do MAX_STATIONS_PER_CITY)
                int numBusStations = MIN_STATIONS_PER_CITY + random.nextInt(MAX_STATIONS_PER_CITY - MIN_STATIONS_PER_CITY + 1);
                for (int i = 0; i < numBusStations; i++) {
                    // Generiši jedinstveni ID za autobusku stanicu u datom gradu
                    // Npr. A_X_Y_Index (A_0_0_0, A_0_0_1, itd.)
                    String busStationId = "A_" + x + "_" + y + "_" + i;
                    BusStation busStation = new BusStation(busStationId, currentCity); // Ažuriran konstruktor
                    currentCity.addStation(busStation); // Dodaj stanicu u grad
                    transportMap.addStation(busStation); // Dodaj stanicu u globalnu mapu transporta
                }

                // Generisanje nasumičnog broja železničkih stanica (1 do MAX_STATIONS_PER_CITY)
                int numTrainStations = MIN_STATIONS_PER_CITY + random.nextInt(MAX_STATIONS_PER_CITY - MIN_STATIONS_PER_CITY + 1);
                for (int i = 0; i < numTrainStations; i++) {
                    // Generiši jedinstveni ID za železničku stanicu u datom gradu
                    // Npr. Z_X_Y_Index (Z_0_0_0, Z_0_0_1, itd.)
                    String trainStationId = "Z_" + x + "_" + y + "_" + i;
                    TrainStation trainStation = new TrainStation(trainStationId, currentCity); // Ažuriran konstruktor
                    currentCity.addStation(trainStation); // Dodaj stanicu u grad
                    transportMap.addStation(trainStation); // Dodaj stanicu u globalnu mapu transporta
                }
            }
        }

        // 3. Generisanje polazaka
        // Ova logika će morati biti kompleksnija jer sada imamo više stanica po gradu.
        // Morate odabrati nasumične stanice unutar gradova za polaske.
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                City currentCity = transportMap.getCity(x, y);
                List<Station> currentCityStations = currentCity.getStations();

                // Filtriraj autobuske i železničke stanice za tekući grad
                List<BusStation> currentBusStations = currentCityStations.stream()
                        .filter(s -> s instanceof BusStation)
                        .map(s -> (BusStation) s)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll); // Koristimo ArrayList::new, ArrayList::add, ArrayList::addAll za toList() kompatibilnost

                List<TrainStation> currentTrainStations = currentCityStations.stream()
                        .filter(s -> s instanceof TrainStation)
                        .map(s -> (TrainStation) s)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll); // Koristimo ArrayList::new, ArrayList::add, ArrayList::addAll za toList() kompatibilnost


                // Generisanje polazaka unutar istog grada (između autobuskih i železničkih)
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


                    // Polasci između autobuskih stanica (između gradova)
                    if (!currentBusStations.isEmpty() && !neighborBusStations.isEmpty()) {
                        for (BusStation currentBusStation : currentBusStations) {
                            for (int i = 0; i < DEPARTURES_PER_STATION_TYPE_PER_DESTINATION; i++) {
                                BusStation randomNeighborBusStation = neighborBusStations.get(random.nextInt(neighborBusStations.size()));
                                currentBusStation.addDeparture(generateDeparture(
                                        "autobus", currentBusStation.getId(), randomNeighborBusStation.getId(), false
                                ));
                            }
                        }
                    }

                    // Polasci između železničkih stanica (između gradova)
                    if (!currentTrainStations.isEmpty() && !neighborTrainStations.isEmpty()) {
                        for (TrainStation currentTrainStation : currentTrainStations) {
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

    // Metoda generateDeparture ostaje ista jer ona vraća Departure OBJEKAT,
    // a Jackson serijalizator će se pobrinuti za formatiranje.
    private Departure generateDeparture(String type, String fromStationId, String toStationId, boolean isLocalTransfer) {
        int hour = random.nextInt(24);
        int minute = random.nextInt(4) * 15;
        LocalTime departureTime = LocalTime.of(hour, minute);

        int durationMinutes = isLocalTransfer ? (10 + random.nextInt(20)) : (30 + random.nextInt(151));
        Duration duration = Duration.ofMinutes(durationMinutes);

        LocalTime arrivalTime = departureTime.plus(duration);

        double price = isLocalTransfer ? (5.0 + random.nextDouble() * 15.0) : (100.0 + random.nextDouble() * 900.0);
        price = Math.round(price * 100.0) / 100.0;

        int minTransferMinutes = isLocalTransfer ? 0 : (5 + random.nextInt(26));
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