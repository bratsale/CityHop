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

    public Path findBestRoute(City startCity, City endCity, String optimizationCriterion) {
        Map<Station, Path> shortestPaths = new HashMap<>();
        // Set<Station> settledStations = new HashSet<>(); // Više nije striktno potrebno ako se validacija radi sa comparePaths

        // PriorityQueue će sada sortirati na osnovu NodeState.compareTo() koji koristi optimizationCriterion
        PriorityQueue<NodeState> pq = new PriorityQueue<>();

        // 1. Inicijalizacija: Dodaj sve početne stanice u PriorityQueue
        for (Station startStation : getStationsInCity(startCity)) {
            Path initialPath = new Path();
            LocalTime initialDepartureTime = LocalTime.MIDNIGHT; // Vreme polaska iz početnog grada

            // <--- KLJUČNA IZMENA: Prosledi optimizationCriterion konstruktoru NodeState-a
            NodeState initialState = new NodeState(startStation, initialDepartureTime, initialPath, optimizationCriterion);
            pq.add(initialState);
            shortestPaths.put(startStation, initialPath); // Nulta putanja do početne stanice
        }

        // 2. Glavni Dijkstra algoritam
        while (!pq.isEmpty()) {
            NodeState currentNodeState = pq.poll();
            Station currentStation = currentNodeState.getStation();
            LocalTime currentArrivalTime = currentNodeState.getArrivalTime();
            Path currentPath = currentNodeState.getCurrentPath();

            // Sada je ova provera još važnija jer PQ uvek izbacuje "najbolji" NodeState po izabranom kriterijumu
            // Ako smo već pronašli bolju putanju do ove stanice, preskoči
            if (shortestPaths.containsKey(currentStation) && comparePaths(currentPath, shortestPaths.get(currentStation), optimizationCriterion) > 0) {
                continue;
            }

            // Ako smo stigli do jedne od odredišnih stanica, vratimo putanju
            // Sada je ovaj rani povratak još pouzdaniji, jer PQ garantuje da je ovo "najbolja" ruta
            // po izabranom kriterijumu.
            if (currentStation.getCity().equals(endCity)) {
                return currentPath;
            }

            // settledStations.add(currentStation); // Više nije neophodno ako je gornja provera efikasna

            // Iteriraj kroz sve polaske (Departure) sa trenutne stanice
            for (Departure departure : currentStation.getDepartures()) {
                Station nextStation = transportMap.getStation(departure.getArrivalStationId());

                if (nextStation == null) {
                    System.err.println("Upozorenje: Polazak sa ID-jem dolazne stanice " + departure.getArrivalStationId() + " ne vodi nigde.");
                    continue;
                }

                Duration minTransferNeeded = DEFAULT_MIN_TRANSFER_TIME;
                if (currentStation.getCity().equals(startCity)) {
                    minTransferNeeded = Duration.ZERO;
                }

                LocalTime earliestReadyToDepart = currentArrivalTime.plus(minTransferNeeded);
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
                newPath.addSegment(newSegment); // Path.addSegment će ažurirati totalCost, totalTravelTime, transfers

                LocalTime newArrivalTime = newPath.getEndTime(); // Vreme dolaska na nextStation

                // Ako je nova putanja do 'nextStation' bolja od one koja je trenutno zapisana
                // (upoređujući po izabranom kriterijumu optimizacije: vreme, cena ili transferi)
                if (!shortestPaths.containsKey(nextStation) || comparePaths(newPath, shortestPaths.get(nextStation), optimizationCriterion) < 0) {
                    shortestPaths.put(nextStation, newPath);
                    // <--- KLJUČNA IZMENA: Prosledi optimizationCriterion konstruktoru NodeState-a
                    pq.add(new NodeState(nextStation, newArrivalTime, newPath, optimizationCriterion));
                }
            }
        }

        // Ako se PriorityQueue isprazni, a nismo našli odredišni grad, znači da nema rute.
        return null;
    }

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
     * Ova metoda ostaje, ali sada služi prvenstveno za ažuriranje `shortestPaths` mape.
     * Glavno poređenje u PQ se dešava u `NodeState.compareTo`.
     */
    private int comparePaths(Path path1, Path path2, String criterion) {
        if (path1 == null && path2 == null) return 0;
        if (path1 == null) return 1; // path2 je bolja (postoji)
        if (path2 == null) return -1; // path1 je bolja (postoji)

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