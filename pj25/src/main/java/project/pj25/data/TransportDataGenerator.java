package project.pj25.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import project.pj25.model.*;
import project.pj25.util.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <p>Klasa za generisanje podataka za transportnu mrežu.</p>
 *
 * <p>Generiše grid gradova, nasumično kreira autobuske i vozne stanice unutar svakog
 * grada, a zatim generiše nasumične polaske između stanica unutar istog grada,
 * kao i između susjednih gradova. Dimenzije mape (n x m) se prosljeđuju
 * kroz konstruktor. Ova klasa služi isključivo za generisanje podataka u memoriji.</p>
 *
 * @author bratsale
 * @version 1.1
 */
public class TransportDataGenerator {
    /** Broj polazaka po tipu stanice i destinaciji. */
    private static final int DEPARTURES_PER_STATION_TYPE_PER_DESTINATION = 5;
    /** Minimalan broj stanica po gradu. */
    private static final int MIN_STATIONS_PER_CITY = 1;
    /** Maksimalan broj stanica po gradu. */
    private static final int MAX_STATIONS_PER_CITY = 3;
    /** Random generator za nasumične vrijednosti. */
    private static final Random random = new Random();

    /** Broj redova u gridu. */
    private final int n;
    /** Broj kolona u gridu. */
    private final int m;

    /**
     * Konstruktor za {@code TransportDataGenerator}.
     *
     * @param n Broj redova.
     * @param m Broj kolona.
     */
    public TransportDataGenerator(int n, int m) {
        this.n = n;
        this.m = m;
    }

    /**
     * Generiše kompletnu transportnu mapu.
     * <p>
     * Kreira gradove, stanice u svakom gradu i polaske između stanica na osnovu
     * dimenzija proslijeđenih u konstruktoru.
     * </p>
     *
     * @return Generisani objekat {@link TransportMap}.
     */
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

    /**
     * Generiše nasumični polazak sa realnim podacima o vremenu, cijeni i trajanju.
     *
     * @param type            Tip prevoza ("autobus" ili "voz").
     * @param fromStationId   ID polazne stanice.
     * @param toStationId     ID dolazne stanice.
     * @param isLocalTransfer Da li je transfer unutar istog grada.
     * @return Generisani objekat {@link Departure}.
     */
    private Departure generateDeparture(String type, String fromStationId, String toStationId, boolean isLocalTransfer) {
        int hour = random.nextInt(24);
        int minute = random.nextInt(4) * 15;
        LocalTime departureTime = LocalTime.of(hour, minute);

        int durationMinutes;
        double price;

        if (isLocalTransfer) {
            durationMinutes = 10 + random.nextInt(10);
            price = 5.0 + random.nextDouble() * 5.0;
        } else {
            durationMinutes = 30 + random.nextInt(61);
            price = 100.0 + random.nextDouble() * 400.0;
        }

        Duration duration = Duration.ofMinutes(durationMinutes);
        LocalTime arrivalTime = departureTime.plus(duration);
        price = Math.round(price * 100.0) / 100.0;

        int minTransferMinutes = isLocalTransfer ? 0 : (5 + random.nextInt(16));
        Duration minTransferTime = Duration.ofMinutes(minTransferMinutes);

        return new Departure(type, fromStationId, toStationId, departureTime, arrivalTime, price, minTransferTime);
    }

    /**
     * Pomoćna metoda za pronalaženje susjednih gradova u gridu.
     *
     * @param x   X koordinata grada.
     * @param y   Y koordinata grada.
     * @param map Transportna mapa.
     * @return Lista susjednih gradova.
     */
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