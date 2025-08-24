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
 * @version 1.1
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
     * Algoritam koristi modifikovanu Dijkstrinu pretragu koja čuva K najboljih
     * putanja za svaku posjećenu stanicu, omogućavajući pronalazak više optimalnih ruta.
     * Glavno ažuriranje: Uklonjena je logika za preuranjeno zaustavljanje petlje pretrage
     * i korigovana je logika za uklanjanje duplikata kako bi se osigurala dosljednost.
     * </p>
     *
     * @param startCity Početni grad.
     * @param endCity Odredišni grad.
     * @param optimizationCriterion Kriterijum optimizacije ("time", "price", "transfers").
     * @param limit Maksimalan broj ruta koje treba vratiti (npr. 5).
     * @return Lista pronađenih {@link Path} objekata, sortirana po kriterijumu.
     */
    public List<Path> findTopNRoutes(City startCity, City endCity, String optimizationCriterion, int limit) {
        // Skladište za K najboljih putanja do svake stanice
        Map<Station, List<Path>> kBestPathsToStation = new HashMap<>();

        // Prioritetni red za pretragu
        PriorityQueue<NodeState> pq = new PriorityQueue<>(
                (n1, n2) -> {
                    int cmp = n1.compareTo(n2);
                    if (cmp == 0) {
                        return Integer.compare(n1.hashCode(), n2.hashCode());
                    }
                    return cmp;
                }
        );

        // Lista finalnih ruta do odredišta
        List<Path> foundRoutes = new ArrayList<>();

        // Inicijalizacija: dodavanje svih početnih stanica u red prioriteta
        for (Station startStation : getStationsInCity(startCity)) {
            Path initialPath = new Path();
            LocalTime initialDepartureTime = LocalTime.MIDNIGHT;

            NodeState initialState = new NodeState(startStation, initialDepartureTime, initialPath, optimizationCriterion);
            pq.add(initialState);

            List<Path> initialPaths = new ArrayList<>();
            initialPaths.add(initialPath);
            kBestPathsToStation.put(startStation, initialPaths);
        }

        // Glavna petlja algoritma
        while (!pq.isEmpty()) { // Pretraga se ne prekida preuranjeno
            NodeState currentNodeState = pq.poll();
            Station currentStation = currentNodeState.getStation();
            Path currentPath = currentNodeState.getCurrentPath();

            if (currentPath.getSegments().size() > transportMap.getAllStations().size() * 2) {
                continue;
            }

            if (currentStation.getCity().equals(endCity)) {
                foundRoutes.add(currentPath);
            }

            for (Departure departure : currentStation.getDepartures()) {
                Station nextStation = transportMap.getStation(departure.getArrivalStationId());
                if (nextStation == null) continue;

                Duration minTransferNeeded = DEFAULT_MIN_TRANSFER_TIME;
                if (currentPath.getSegments().isEmpty()) {
                    minTransferNeeded = Duration.ZERO;
                }

                LocalTime earliestReadyToDepart = currentPath.getEndTime().plus(minTransferNeeded);
                LocalTime nextDepartureScheduledTime = departure.getDepartureTime();

                boolean canTakeDeparture = false;
                if (nextDepartureScheduledTime.isAfter(earliestReadyToDepart) || nextDepartureScheduledTime.equals(earliestReadyToDepart)) {
                    canTakeDeparture = true;
                } else if (nextDepartureScheduledTime.isBefore(earliestReadyToDepart) && earliestReadyToDepart.toSecondOfDay() > nextDepartureScheduledTime.toSecondOfDay()) {
                    // Prelazak preko ponoći
                    long departureMinutes = nextDepartureScheduledTime.toSecondOfDay() / 60;
                    long arrivalMinutes = earliestReadyToDepart.toSecondOfDay() / 60;
                    if (departureMinutes + (24 * 60) >= arrivalMinutes) {
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

                List<Path> pathsToNextStation = kBestPathsToStation.getOrDefault(nextStation, new ArrayList<>());

                if (pathsToNextStation.size() < limit || comparePaths(newPath, pathsToNextStation.get(pathsToNextStation.size() - 1), optimizationCriterion) < 0) {
                    pathsToNextStation.add(newPath);
                    pathsToNextStation.sort((p1, p2) -> comparePaths(p1, p2, optimizationCriterion));
                    if (pathsToNextStation.size() > limit) {
                        pathsToNextStation = pathsToNextStation.subList(0, limit);
                    }
                    kBestPathsToStation.put(nextStation, pathsToNextStation);
                    pq.add(new NodeState(nextStation, newPath.getEndTime(), newPath, optimizationCriterion));
                }
            }
        }

        // Sortiranje i uklanjanje duplikata na kraju
        // Koristimo Stream API sa poboljšanom logikom za jedinstvene rute
        List<Path> uniqueRoutes = foundRoutes.stream()
                .filter(p -> p.getSegments() != null && !p.getSegments().isEmpty())
                .collect(Collectors.toMap(
                        path -> path.getRouteKeyWithTime(),
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