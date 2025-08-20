package project.pj25.algorithm;

import project.pj25.model.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class RouteFinder {
    private static final Duration DEFAULT_MIN_TRANSFER_TIME = Duration.ofMinutes(15);
    private final TransportMap transportMap;

    public RouteFinder(TransportMap transportMap) {
        this.transportMap = transportMap;
    }

    /**
     * Pronalazi do N optimalnih ruta između dva grada na osnovu zadatog kriterijuma.
     * Algoritam nastavlja pretragu i nakon pronalaska prve rute do odredišta kako bi prikupio više opcija.
     *
     * @param startCity           Početni grad.
     * @param endCity             Odredišni grad.
     * @param optimizationCriterion Kriterijum optimizacije ("time", "price", "transfers").
     * @param limit               Maksimalan broj ruta koje treba vratiti (npr. 5).
     * @return Lista pronađenih ruta (Path objekata), sortirana po kriterijumu.
     */
    public List<Path> findTopNRoutes(City startCity, City endCity, String optimizationCriterion, int limit) {
        // Izmena 1: Umesto da čuvamo samo jednu najbolju putanju do svake stanice, čuvaćemo više njih.
        // Ovo omogućava algoritam da ne odbacuje odmah alternative.
        Map<Station, List<Path>> foundPathsToStation = new HashMap<>();

        PriorityQueue<NodeState> pq = new PriorityQueue<>();

        List<Path> foundRoutes = new ArrayList<>(); // Lista za čuvanje svih validnih ruta do odredišnog grada

        // 1. Inicijalizacija: Dodaj sve početne stanice u PriorityQueue
        for (Station startStation : getStationsInCity(startCity)) {
            Path initialPath = new Path();
            LocalTime initialDepartureTime = LocalTime.MIDNIGHT;

            NodeState initialState = new NodeState(startStation, initialDepartureTime, initialPath, optimizationCriterion);
            pq.add(initialState);

            // Izmena 2: Inicijalizacija liste putanja za početnu stanicu
            List<Path> initialPaths = new ArrayList<>();
            initialPaths.add(initialPath);
            foundPathsToStation.put(startStation, initialPaths);
        }

        // 2. Glavni Dijkstra algoritam (modifikovan da ne prestaje odmah)
        while (!pq.isEmpty()) {
            NodeState currentNodeState = pq.poll();
            Station currentStation = currentNodeState.getStation();
            Path currentPath = currentNodeState.getCurrentPath();

            // Izmena 3: Zadržavamo proveru, ali ne prekidamo uvek.
            // Ako trenutna putanja nije među 'limit * 2' najboljih do te stanice, preskoči.
            // Ovo je heuristika da ne preplavimo memoriju, a da i dalje nađemo alternative.
            List<Path> pathsToCurrentStation = foundPathsToStation.getOrDefault(currentStation, new ArrayList<>());
            if (!pathsToCurrentStation.isEmpty() && pathsToCurrentStation.size() >= limit * 2) {
                // Ako je trenutna putanja gora od najgore putanje u listi za tu stanicu, preskoči
                Path worstPath = pathsToCurrentStation.get(pathsToCurrentStation.size() - 1);
                if (comparePaths(currentPath, worstPath, optimizationCriterion) > 0) {
                    continue;
                }
            }

            // Ako smo stigli do odredišnog grada, dodaj putanju u listu pronađenih ruta
            if (currentStation.getCity().equals(endCity)) {
                foundRoutes.add(currentPath);
                // Nastavljamo petlju da bismo pronašli i druge "dobre" rute.
                // Izmena 4: Dodajmo prag za zaustavljanje pretrage da ne ide u beskonačnost
                if (foundRoutes.size() >= limit * 2) { // Nađi dvostruko više ruta pa ih filtriraj
                    break;
                }
            }

            // Iteriraj kroz sve polaske (Departure) sa trenutne stanice
            for (Departure departure : currentStation.getDepartures()) {
                Station nextStation = transportMap.getStation(departure.getArrivalStationId());

                if (nextStation == null) {
                    System.err.println("Upozorenje: Polazak sa ID-jem dolazne stanice " + departure.getArrivalStationId() + " ne vodi nigde.");
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

                // Kreiraj novi segment rute
                RouteSegment newSegment = new RouteSegment(
                        departure,
                        currentStation,
                        nextStation,
                        nextDepartureScheduledTime,
                        departure.getArrivalTime()
                );

                // Kreiraj novu putanju produžujući trenutnu putanju
                Path newPath = new Path(currentPath);
                newPath.addSegment(newSegment);

                LocalTime newArrivalTime = newPath.getEndTime();

                // Izmena 5: Proveri da li je putanja do sledeće stanice dovoljno dobra da se doda u PQ.
                // Umesto da proveravamo samo najbolju putanju (shortestPaths),
                // proveravamo da li je nova putanja bolja od najgore do te stanice.
                List<Path> pathsToNextStation = foundPathsToStation.getOrDefault(nextStation, new ArrayList<>());

                if (pathsToNextStation.size() < limit * 2 || comparePaths(newPath, pathsToNextStation.get(pathsToNextStation.size() - 1), optimizationCriterion) < 0) {

                    pathsToNextStation.add(newPath);
                    // Održavaj listu sortiranom da bismo uvek mogli da uzmemo "najgoru"
                    pathsToNextStation.sort((p1, p2) -> comparePaths(p1, p2, optimizationCriterion));
                    // Ograniči veličinu liste
                    if (pathsToNextStation.size() > limit * 2) {
                        pathsToNextStation = pathsToNextStation.subList(0, limit * 2);
                    }
                    foundPathsToStation.put(nextStation, pathsToNextStation);

                    pq.add(new NodeState(nextStation, newArrivalTime, newPath, optimizationCriterion));
                }
            }
        }

        // 3. Filtriranje i sortiranje pronađenih ruta
        // Sortiraj sve pronađene rute po izabranom kriterijumu
        foundRoutes.sort((p1, p2) -> comparePaths(p1, p2, optimizationCriterion));

        // Izmena 6: Ukloni duplikate (rute sa identičnom sekvencom stanica) pre limitiranja.
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


        // Vrati samo top 'limit' ruta
        return uniqueRoutes.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Pomoćna metoda za dobijanje stanica u gradu
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
     * Pomoćna metoda za upoređivanje dve putanje na osnovu kriterijuma optimizacije.
     * Vraća negativan broj ako je path1 bolja, pozitivan ako je path2 bolja, 0 ako su jednake.
     * Ova metoda se koristi i u NodeState.compareTo i za konačno sortiranje lista putanja.
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