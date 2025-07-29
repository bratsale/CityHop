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
        Map<Station, Path> shortestPaths = new HashMap<>(); // Čuvamo najbolju putanju do svake stanice
        PriorityQueue<NodeState> pq = new PriorityQueue<>();

        List<Path> foundRoutes = new ArrayList<>(); // Lista za čuvanje svih validnih ruta do odredišnog grada

        // 1. Inicijalizacija: Dodaj sve početne stanice u PriorityQueue
        for (Station startStation : getStationsInCity(startCity)) {
            Path initialPath = new Path();
            LocalTime initialDepartureTime = LocalTime.MIDNIGHT; // Pretpostavka da je polazak u ponoć ako nije specifikovano

            NodeState initialState = new NodeState(startStation, initialDepartureTime, initialPath, optimizationCriterion);
            pq.add(initialState);
            shortestPaths.put(startStation, initialPath);
        }

        // 2. Glavni Dijkstra algoritam (modifikovan da ne prestaje odmah)
        while (!pq.isEmpty()) {
            NodeState currentNodeState = pq.poll();
            Station currentStation = currentNodeState.getStation();
            // LocalTime currentArrivalTime = currentNodeState.getArrivalTime(); // Nije nam direktno potrebno ovde, uzimamo iz currentPath
            Path currentPath = currentNodeState.getCurrentPath();

            // Važno: Provera da li je trenutna putanja bolja od one koja je već u shortestPaths mapi.
            // Ovo je ključno za pravilno funkcionisanje Dijkstre, čak i kada želimo više ruta.
            // Ako smo već pronašli bolju putanju do ove stanice (prema izabranom kriterijumu), preskoči.
            if (shortestPaths.containsKey(currentStation) && comparePaths(currentPath, shortestPaths.get(currentStation), optimizationCriterion) > 0) {
                continue;
            }

            // Ako smo stigli do odredišnog grada, dodaj putanju u listu pronađenih ruta
            if (currentStation.getCity().equals(endCity)) {
                foundRoutes.add(currentPath);
                // NE VRAĆAMO ODMAH! Nastavljamo petlju da bismo pronašli i druge "dobre" rute.
            }

            // Iteriraj kroz sve polaske (Departure) sa trenutne stanice
            for (Departure departure : currentStation.getDepartures()) {
                Station nextStation = transportMap.getStation(departure.getArrivalStationId());

                if (nextStation == null) {
                    System.err.println("Upozorenje: Polazak sa ID-jem dolazne stanice " + departure.getArrivalStationId() + " ne vodi nigde.");
                    continue;
                }

                Duration minTransferNeeded = DEFAULT_MIN_TRANSFER_TIME;
                // Specijalan slučaj za prvi segment putovanja - nema vremena čekanja na transfer
                // (ako se polazi sa početne stanice, pretpostavljamo da nema transfera pre prvog polaska)
                // OVA LINIJA JE VEOMA VAŽNA ZA PRVI SEGMENT PUTOVANJA!
                if (currentPath.getSegments().isEmpty()) { // Ako je ovo prvi segment u putanji
                    minTransferNeeded = Duration.ZERO;
                }


                LocalTime earliestReadyToDepart = currentPath.getEndTime().plus(minTransferNeeded); // Koristi vreme dolaska prethodnog segmenta
                LocalTime nextDepartureScheduledTime = departure.getDepartureTime();

                boolean canTakeDeparture = false;
                long earliestReadyMinutes = earliestReadyToDepart.toSecondOfDay() / 60;
                long nextDepartureMinutes = nextDepartureScheduledTime.toSecondOfDay() / 60;

                // Provera da li možemo stići na sledeći polazak, uzimajući u obzir prelazak preko ponoći
                if (nextDepartureMinutes >= earliestReadyMinutes) {
                    canTakeDeparture = true;
                } else {
                    // Ako je sledeći polazak sledećeg dana
                    long normalizedNextDepartureMinutes = nextDepartureMinutes + (24 * 60);
                    if (normalizedNextDepartureMinutes >= earliestReadyMinutes) {
                        canTakeDeparture = true;
                    }
                }

                if (!canTakeDeparture) {
                    continue; // Ne možemo uzeti ovaj polazak
                }

                // Kreiraj novi segment rute
                RouteSegment newSegment = new RouteSegment(
                        departure,
                        currentStation,
                        nextStation,
                        nextDepartureScheduledTime, // Stvarno vreme polaska za ovaj segment
                        departure.getArrivalTime()   // Stvarno vreme dolaska za ovaj segment
                );

                // Kreiraj novu putanju produžujući trenutnu putanju
                Path newPath = new Path(currentPath); // Kreiraj kopiju trenutne putanje
                newPath.addSegment(newSegment); // Dodaj novi segment u kopiju putanje

                LocalTime newArrivalTime = newPath.getEndTime(); // Vreme dolaska na nextStation (nakon dodavanja segmenta)

                // Ako je nova putanja do 'nextStation' bolja od one koja je trenutno zapisana
                // (upoređujući po izabranom kriterijumu optimizacije: vreme, cena ili transferi)
                if (!shortestPaths.containsKey(nextStation) || comparePaths(newPath, shortestPaths.get(nextStation), optimizationCriterion) < 0) {
                    shortestPaths.put(nextStation, newPath); // Ažuriraj najbolju putanju do ove stanice
                    pq.add(new NodeState(nextStation, newArrivalTime, newPath, optimizationCriterion)); // Dodaj u PQ
                }
            }
        }

        // 3. Filtriranje i sortiranje pronađenih ruta
        // Sortiraj sve pronađene rute po izabranom kriterijumu
        foundRoutes.sort((p1, p2) -> comparePaths(p1, p2, optimizationCriterion));

        // Vrati samo top 'limit' ruta
        return foundRoutes.stream()
                .limit(limit) // Ograniči na top N ruta
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