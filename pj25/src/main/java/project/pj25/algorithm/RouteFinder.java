package project.pj25.algorithm;

import project.pj25.model.*;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.util.*;

public class RouteFinder {
    private static final Duration DEFAULT_MIN_TRANSFER_TIME = Duration.ofMinutes(15);
    private final TransportMap transportMap;

    public RouteFinder(TransportMap transportMap) {
        this.transportMap = transportMap;
    }

    /**
     * Pronalazi optimalnu rutu između dva grada na osnovu zadatog kriterijuma.
     *
     * @param startCity           Početni grad.
     * @param endCity             Odredišni grad.
     * @param optimizationCriterion Kriterijum optimizacije ("time", "price", "transfers").
     * @return Najbolja pronađena ruta (Path objekat) ili null ako ruta nije pronađena.
     */
    public Path findBestRoute(City startCity, City endCity, String optimizationCriterion) {
        Map<Station, Path> shortestPaths = new HashMap<>();
        Set<Station> settledStations = new HashSet<>();
        PriorityQueue<NodeState> pq = new PriorityQueue<>();

        // 1. Inicijalizacija: Dodaj sve početne stanice u PriorityQueue
        for (Station startStation : getStationsInCity(startCity)) {
            Path initialPath = new Path();
            LocalTime initialDepartureTime = LocalTime.MIDNIGHT; // Vreme polaska iz početnog grada

            NodeState initialState = new NodeState(startStation, initialDepartureTime, initialPath);
            pq.add(initialState);
            // Nulta putanja do početne stanice (ukupno vreme 0, cena 0, transferi 0)
            shortestPaths.put(startStation, initialPath);
        }

        // 2. Glavni Dijkstra algoritam
        while (!pq.isEmpty()) {
            NodeState currentNodeState = pq.poll();
            Station currentStation = currentNodeState.getStation();
            LocalTime currentArrivalTime = currentNodeState.getArrivalTime();
            Path currentPath = currentNodeState.getCurrentPath();

            // Ako smo već obradili ovu stanicu sa boljim ili jednakim rezultatom, preskoči
            // Ovo je ključno za time-dependent Dijkstru: ako imamo bolju (ili jednaku) putanju
            // do ove stanice (po kriterijumu optimizacije), nema potrebe da je ponovo obrađujemo.
            if (shortestPaths.containsKey(currentStation) && comparePaths(currentPath, shortestPaths.get(currentStation), optimizationCriterion) > 0) {
                continue;
            }

            // Ako smo stigli do jedne od odredišnih stanica, vratimo putanju
            if (currentStation.getCity().equals(endCity)) {
                return currentPath;
            }

            // Označi stanicu kao settled (obrađenu)
            settledStations.add(currentStation);

            // Iteriraj kroz sve polaske (Departure) sa trenutne stanice
            for (Departure departure : currentStation.getDepartures()) {
                Station nextStation = transportMap.getStation(departure.getArrivalStationId());

                // Proveri da li postoji sledeća stanica (sanity check)
                if (nextStation == null) {
                    System.err.println("Upozorenje: Polazak sa ID-jem dolazne stanice " + departure.getArrivalStationId() + " ne vodi nigde.");
                    continue;
                }

                // Izračunaj minimalno vreme čekanja za presedanje
                // Ako je trenutna stanica u istom gradu kao i početni grad rute,
                // onda nema transfera, pa je minTransferNeeded 0.
                // Inače, koristimo DEFAULT_MIN_TRANSFER_TIME (15 minuta).
                Duration minTransferNeeded = DEFAULT_MIN_TRANSFER_TIME;
                if (currentStation.getCity().equals(startCity)) { // Proveravamo da li je ovo prva stanica rute
                    minTransferNeeded = Duration.ZERO;
                }

                LocalTime earliestReadyToDepart = currentArrivalTime.plus(minTransferNeeded);
                LocalTime nextDepartureScheduledTime = departure.getDepartureTime();

                // --- ISPRAVLJENA I POJEDNOSTAVLJENA LOGIKA ZA canTakeDeparture ---
                boolean canTakeDeparture = false;

                // Konvertuj LocalTime u minute od ponoći za lakše linearno poređenje
                long earliestReadyMinutes = earliestReadyToDepart.toSecondOfDay() / 60;
                long nextDepartureMinutes = nextDepartureScheduledTime.toSecondOfDay() / 60;

                // Scenario 1: Planirano vreme polaska je na ili nakon vremena kada smo spremni (istog "dana")
                if (nextDepartureMinutes >= earliestReadyMinutes) {
                    canTakeDeparture = true;
                }
                // Scenario 2: Planirano vreme polaska je numerički ranije
                // (npr. polazak u 01:00, mi spremni u 23:00)
                // To znači da je polazak za "sledeći dan".
                else {
                    // Da bismo polazak "premestili" na sledeći dan u linearnoj skali,
                    // dodajemo 24 sata (1440 minuta) na njegovo vreme.
                    long normalizedNextDepartureMinutes = nextDepartureMinutes + (24 * 60);

                    // Sada proveravamo da li je "normalizovano" vreme polaska na ili nakon kada smo spremni
                    if (normalizedNextDepartureMinutes >= earliestReadyMinutes) {
                        canTakeDeparture = true;
                    }
                    // Ako i nakon normalizacije nije dostupan, onda je to voz koji je propušten
                    // (npr. stigli smo u 10:00, voz je otišao u 09:00 istog dana)
                }

                if (!canTakeDeparture) {
                    continue; // Preskoči ovaj polazak jer ga ne možemo uhvatiti
                }
                // --- KRAJ ISPRAVLJENE LOGIKE ZA canTakeDeparture ---

                // Kreiraj novi segment rute
                RouteSegment newSegment = new RouteSegment(
                        departure,
                        currentStation,
                        nextStation,
                        // Stvarna vremena polaska i dolaska za segment
                        nextDepartureScheduledTime, // Koristimo planirano vreme polaska
                        departure.getArrivalTime()    // Koristimo planirano vreme dolaska
                );

                // Kreiraj novu putanju produžujući trenutnu putanju
                Path newPath = new Path(currentPath); // Kopiraj trenutnu putanju
                newPath.addSegment(newSegment);       // Dodaj novi segment

                // Izračunaj stvarno vreme dolaska za sledeći čvor (stanicu)
                LocalTime newArrivalTime = newPath.getEndTime(); // Vreme dolaska na nextStation

                // Ako je nova putanja do 'nextStation' bolja od one koja je trenutno zapisana
                // (upoređujući po izabranom kriterijumu optimizacije: vreme, cena ili transferi)
                if (!shortestPaths.containsKey(nextStation) || comparePaths(newPath, shortestPaths.get(nextStation), optimizationCriterion) < 0) {
                    shortestPaths.put(nextStation, newPath);
                    // Dodaj novo stanje čvora u PriorityQueue
                    pq.add(new NodeState(nextStation, newArrivalTime, newPath));
                }
            }
        }

        // Ako se PriorityQueue isprazni, a nismo našli odredišni grad
        // To znači da nema rute.
        return null;
    }

    private List<Station> getStationsInCity(City city) {
        List<Station> cityStations = new ArrayList<>();
        // Prođi kroz sve stanice u transportnoj mapi (koja je TransportMap transportMap instanca)
        // i pronađi one koje pripadaju datom gradu
        for (Station station : transportMap.getAllStations().values()) { // <--- Koristi transportMap.getAllStations()
            if (station.getCity().equals(city)) {
                cityStations.add(station);
            }
        }
        return cityStations;
    }

    /**
     * Pomoćna metoda za upoređivanje dve putanje na osnovu kriterijuma optimizacije.
     * Vraća negativan broj ako je path1 bolja, pozitivan ako je path2 bolja, 0 ako su jednake.
     */
    private int comparePaths(Path path1, Path path2, String criterion) {
        if (path1 == null && path2 == null) return 0;
        if (path1 == null) return 1; // path2 je bolja (postoji)
        if (path2 == null) return -1; // path1 je bolja (postoji)

        switch (criterion.toLowerCase()) {
            case "time":
                return path1.getTotalTravelTime().compareTo(path2.getTotalTravelTime());
            case "price":
                return Double.compare(path1.getTotalCost(), path2.getTotalCost());
            case "transfers":
                return Integer.compare(path1.getTransfers(), path2.getTransfers());
            default:
                throw new IllegalArgumentException("Nepoznat kriterijum optimizacije: " + criterion);
        }
    }
}
