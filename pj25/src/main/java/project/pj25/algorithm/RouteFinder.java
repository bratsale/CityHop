package project.pj25.algorithm;

import project.pj25.model.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Klasa za pronalazak optimalnih ruta unutar transportne mape.
 * <p>
 * Koristi modifikovani algoritam sličan Dijkstrinom za efikasno pronalaženje
 * više optimalnih putanja između dva grada, na osnovu različitih kriterijuma
 * (vrijeme, cijena, broj presjedanja). Umjesto da se zaustavi nakon pronalaska
 * prve rute, algoritam nastavlja da istražuje kako bi pronašao i druge dobre
 * alternative.
 * </p>
 *
 * @author Tvoje Ime
 * @version 1.0
 * @see TransportMap
 * @see Path
 * @see NodeState
 */
public class RouteFinder {

    /**
     * Podrazumijevano minimalno vrijeme za presjedanje.
     */
    private static final Duration DEFAULT_MIN_TRANSFER_TIME = Duration.ofMinutes(15);

    /**
     * Instanca transportne mape na kojoj se vrši pretraga.
     */
    private final TransportMap transportMap;

    /**
     * Konstruktor za kreiranje objekta {@code RouteFinder}.
     *
     * @param transportMap Transportna mapa koja se koristi za pretragu.
     */
    public RouteFinder(TransportMap transportMap) {
        this.transportMap = transportMap;
    }

    /**
     * Pronalazi do N optimalnih ruta između početnog i krajnjeg grada,
     * na osnovu zadatog kriterijuma optimizacije.
     * <p>
     * Algoritam nastavlja pretragu i nakon pronalaska prve rute do odredišta kako bi
     * prikupio više opcija. Putanje se prate u prioritetnom redu, a za svaku stanicu
     * se čuva lista najboljih putanja kako bi se spriječilo preplavljivanje
     * memorije.
     * </p>
     *
     * @param startCity           Početni grad.
     * @param endCity             Odredišni grad.
     * @param optimizationCriterion Kriterijum optimizacije ("time", "price", "transfers").
     * @param limit               Maksimalan broj ruta koje treba vratiti (npr. 5).
     * @return Lista pronađenih {@link Path} objekata, sortirana po kriterijumu.
     */
    public List<Path> findTopNRoutes(City startCity, City endCity, String optimizationCriterion, int limit) {
        Map<Station, List<Path>> foundPathsToStation = new HashMap<>();
        PriorityQueue<NodeState> pq = new PriorityQueue<>();
        List<Path> foundRoutes = new ArrayList<>();

        // 1. Inicijalizacija
        for (Station startStation : getStationsInCity(startCity)) {
            Path initialPath = new Path();
            LocalTime initialDepartureTime = LocalTime.MIDNIGHT;
            NodeState initialState = new NodeState(startStation, initialDepartureTime, initialPath, optimizationCriterion);
            pq.add(initialState);

            List<Path> initialPaths = new ArrayList<>();
            initialPaths.add(initialPath);
            foundPathsToStation.put(startStation, initialPaths);
        }

        // 2. Glavni algoritam pretrage
        while (!pq.isEmpty()) {
            NodeState currentNodeState = pq.poll();
            Station currentStation = currentNodeState.getStation();
            Path currentPath = currentNodeState.getCurrentPath();

            List<Path> pathsToCurrentStation = foundPathsToStation.getOrDefault(currentStation, new ArrayList<>());
            if (!pathsToCurrentStation.isEmpty() && pathsToCurrentStation.size() >= limit * 2) {
                Path worstPath = pathsToCurrentStation.get(pathsToCurrentStation.size() - 1);
                if (comparePaths(currentPath, worstPath, optimizationCriterion) > 0) {
                    continue;
                }
            }

            if (currentStation.getCity().equals(endCity)) {
                foundRoutes.add(currentPath);
                if (foundRoutes.size() >= limit * 2) {
                    break;
                }
            }

            for (Departure departure : currentStation.getDepartures()) {
                Station nextStation = transportMap.getStation(departure.getArrivalStationId());

                if (nextStation == null) {
                    System.err.println("Upozorenje: Polazak sa ID-jem dolazne stanice " + departure.getArrivalStationId() + " ne vodi nigdje.");
                    continue;
                }

                Duration minTransferNeeded = DEFAULT_MIN_TRANSFER_TIME;
                if (currentPath.getSegments().isEmpty()) {
                    minTransferNeeded = Duration.ZERO;
                }

                LocalTime earliestReadyToDepart = currentPath.getEndTime().plus(minTransferNeeded);
                LocalTime nextDepartureScheduledTime = departure.getDepartureTime();

                boolean canTakeDeparture = false;
                long earliestReadyMinutes = earliestReadyToDepart.toSecondOfDay() / 60;
                long nextDepartureMinutes = nextDepartureScheduledTime.toSecondOfDay() / 60;

                if (nextDepartureMinutes >= earliestReadyMinutes) {
                    canTakeDeparture = true;
                } else {
                    long normalizedNextDepartureMinutes = nextDepartureMinutes + (24 * 60);
                    if (normalizedNextDepartureMinutes >= earliestReadyMinutes) {
                        canTakeDeparture = true;
                    }
                }

                if (!canTakeDeparture) {
                    continue;
                }

                RouteSegment newSegment = new RouteSegment(
                        departure,
                        currentStation,
                        nextStation,
                        nextDepartureScheduledTime,
                        departure.getArrivalTime()
                );

                Path newPath = new Path(currentPath);
                newPath.addSegment(newSegment);
                LocalTime newArrivalTime = newPath.getEndTime();

                List<Path> pathsToNextStation = foundPathsToStation.getOrDefault(nextStation, new ArrayList<>());

                if (pathsToNextStation.size() < limit * 2 || comparePaths(newPath, pathsToNextStation.get(pathsToNextStation.size() - 1), optimizationCriterion) < 0) {
                    pathsToNextStation.add(newPath);
                    pathsToNextStation.sort((p1, p2) -> comparePaths(p1, p2, optimizationCriterion));
                    if (pathsToNextStation.size() > limit * 2) {
                        pathsToNextStation = pathsToNextStation.subList(0, limit * 2);
                    }
                    foundPathsToStation.put(nextStation, pathsToNextStation);
                    pq.add(new NodeState(nextStation, newArrivalTime, newPath, optimizationCriterion));
                }
            }
        }

        // 3. Filtriranje, sortiranje i vraćanje rezultata
        foundRoutes.sort((p1, p2) -> comparePaths(p1, p2, optimizationCriterion));

        List<Path> uniqueRoutes = foundRoutes.stream()
                .filter(p -> p.getSegments().size() > 0)
                .collect(Collectors.toMap(
                        path -> path.getSegments().stream()
                                .map(s -> s.getDeparture().getDepartureStationId())
                                .collect(Collectors.joining("-")),
                        path -> path,
                        (p1, p2) -> comparePaths(p1, p2, optimizationCriterion) < 0 ? p1 : p2
                ))
                .values()
                .stream()
                .sorted((p1, p2) -> comparePaths(p1, p2, optimizationCriterion))
                .collect(Collectors.toList());

        return uniqueRoutes.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Vraća listu svih stanica koje se nalaze u datom gradu.
     *
     * @param city Grad čije stanice treba pronaći.
     * @return Lista {@link Station} objekata.
     */
    private List<Station> getStationsInCity(City city) {
        List<Station> cityStations = new ArrayList<>();
        for (Station station : transportMap.getAllStations().values()) {
            if (station.getCity().equals(city)) {
                cityStations.add(station);
            }
        }
        return cityStations;
    }

    /**
     * Pomoćna metoda za upoređivanje dvije putanje na osnovu kriterijuma optimizacije.
     * <p>
     * Ova metoda služi kao unificirani komparator koji se koristi na više mjesta
     * u algoritmu kako bi se osigurala konzistentnost sortiranja i poređenja.
     * </p>
     *
     * @param path1 Prva putanja.
     * @param path2 Druga putanja.
     * @param criterion Kriterijum optimizacije ("time", "price", "transfers").
     * @return Negativan broj ako je path1 bolja, pozitivan ako je path2 bolja, 0 ako su jednake.
     * @throws IllegalArgumentException ako je kriterijum nepoznat.
     */
    private int comparePaths(Path path1, Path path2, String criterion) {
        if (path1 == null && path2 == null) return 0;
        if (path1 == null) return 1;
        if (path2 == null) return -1;

        switch (criterion.toLowerCase()) {
            case "time":
                int timeCmp = path1.getTotalTravelTime().compareTo(path2.getTotalTravelTime());
                if (timeCmp == 0) {
                    int costCmp = Double.compare(path1.getTotalCost(), path2.getTotalCost());
                    if (costCmp == 0) {
                        return Integer.compare(path1.getTransfers(), path2.getTransfers());
                    }
                    return costCmp;
                }
                return timeCmp;
            case "price":
                int costCmp = Double.compare(path1.getTotalCost(), path2.getTotalCost());
                if (costCmp == 0) {
                    int timeCmpSecondary = path1.getTotalTravelTime().compareTo(path2.getTotalTravelTime());
                    if (timeCmpSecondary == 0) {
                        return Integer.compare(path1.getTransfers(), path2.getTransfers());
                    }
                    return timeCmpSecondary;
                }
                return costCmp;
            case "transfers":
                int transfersCmp = Integer.compare(path1.getTransfers(), path2.getTransfers());
                if (transfersCmp == 0) {
                    int timeCmpSecondary = path1.getTotalTravelTime().compareTo(path2.getTotalTravelTime());
                    if (timeCmpSecondary == 0) {
                        return Double.compare(path1.getTotalCost(), path2.getTotalCost());
                    }
                    return timeCmpSecondary;
                }
                return transfersCmp;
            default:
                throw new IllegalArgumentException("Nepoznat kriterijum optimizacije: " + criterion);
        }
    }
}