package project.pj25.algorithm; // Ili project.pj25.algorithm; ako si premestio Path tamo

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Path implements Comparable<Path> {
    private List<RouteSegment> segments;
    private LocalTime startTime;
    private LocalTime endTime;
    private double totalCost;
    private int transfers;
    private Duration totalTravelTime;

    public Path() {
        this.segments = new ArrayList<>();
        this.totalCost = 0.0;
        this.transfers = 0;
        this.totalTravelTime = Duration.ZERO;
        // *** KLJUČNA IZMENA: INICIJALIZUJ startTime I endTime ***
        this.startTime = LocalTime.MIDNIGHT; // Podrazumevano početno vreme za praznu putanju
        this.endTime = LocalTime.MIDNIGHT;   // Podrazumevano završno vreme za praznu putanju
    }

    // Konstruktor za kopiranje
    public Path(Path other) {
        this.segments = new ArrayList<>(other.segments);
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.totalCost = other.totalCost;
        this.transfers = other.transfers;
        this.totalTravelTime = other.totalTravelTime;
    }

    /**
     * Dodaje segment putovanja u putanju i ažurira ukupno vreme, cenu i broj presedanja.
     * OVA METODA JE KLJUČNA I TREBA DA JE ZAMENIŠ U TVOJIM KLASAMA.
     *
     * @param segment segment putovanja koji se dodaje.
     */
    public void addSegment(RouteSegment segment) {
        // Ako je ovo prvi segment u putanji
        if (this.segments.isEmpty()) {
            this.startTime = segment.getActualDepartureTime(); // Početak putovanja je polazak prvog segmenta
            // Za prvi segment, totalTravelTime je samo njegovo trajanje.
            // Nema čekanja pre prvog segmenta.
            this.totalTravelTime = segment.getSegmentDuration();
        } else {
            // Za svaki sledeći segment, izračunaj vreme čekanja
            LocalTime prevArrivalTime = this.endTime; // Vreme dolaska prethodnog segmenta
            LocalTime currentDepartureTime = segment.getActualDepartureTime(); // Vreme polaska tekućeg segmenta

            // Izračunaj trajanje čekanja, uzimajući u obzir prelazak preko ponoći
            Duration waitingDuration = calculateWaitingDuration(prevArrivalTime, currentDepartureTime);

            // Dodaj vreme čekanja i trajanje tekućeg segmenta na ukupno vreme putovanja
            this.totalTravelTime = this.totalTravelTime.plus(waitingDuration).plus(segment.getSegmentDuration());

            // Broj presedanja se povećava za svaki novi segment nakon prvog
            this.transfers++;
        }

        // Dodaj segment u listu
        this.segments.add(segment);
        // Ažuriraj vreme dolaska cele putanje (LocalTime, samo vreme u danu)
        this.endTime = segment.getActualArrivalTime();
        // Ažuriraj ukupnu cenu
        this.totalCost += segment.getDeparture().getPrice();
        // Važno: `transfers` se ažurira unutar if/else bloka, ne globalno `segments.size() - 1` ovde.
        // Ako je iznad `transfers++`, onda je ovo ok.
        // Ako je `segments.size() - 1` onda je to samo broj segmenata minus 1, što je isto ok.
        // Bitno je da je definicija `transfers` konzistentna.
        // Pošto si u konstruktoru inicijalizovao na 0, i u else grani dodaješ ++,
        // onda je to ispravno.
    }

    /**
     * Pomoćna metoda za izračunavanje vremena čekanja između dva vremena (LocalTime),
     * uzimajući u obzir prelazak preko ponoći.
     *
     * @param prevArrivalTime Vreme dolaska prethodnog segmenta.
     * @param currentDepartureTime Vreme polaska tekućeg segmenta.
     * @return Trajanje čekanja kao Duration.
     */
    private Duration calculateWaitingDuration(LocalTime prevArrivalTime, LocalTime currentDepartureTime) {
        long prevArrivalMinutes = prevArrivalTime.toSecondOfDay() / 60;
        long currentDepartureMinutes = currentDepartureTime.toSecondOfDay() / 60;

        long waitingMinutes;
        if (currentDepartureMinutes < prevArrivalMinutes) {
            // Ako je vreme polaska sledećeg dana (npr. dolazak 23:00, polazak 01:00)
            waitingMinutes = (currentDepartureMinutes + (24 * 60)) - prevArrivalMinutes;
        } else {
            // Polazak je istog dana ili kasnije istog dana
            waitingMinutes = currentDepartureMinutes - prevArrivalMinutes;
        }
        return Duration.ofMinutes(waitingMinutes);
    }

    // --- Getteri ---
    public List<RouteSegment> getSegments() {
        return segments;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public int getTransfers() {
        return transfers;
    }

    public Duration getTotalTravelTime() {
        return totalTravelTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Ukupno vreme: %s, Ukupna cena: %.2f KM, Presedanja: %d\n", // Dodao KM ovde
                formatDuration(totalTravelTime), totalCost, transfers));
        for (int i = 0; i < segments.size(); i++) {
            RouteSegment s = segments.get(i);
            sb.append("  ").append(s.getDeparture().getType())
                    .append(": ").append(s.getDeparture().getDepartureStationId())
                    .append(" (grad ").append(s.getDepartureStationCityName()).append(")")
                    .append(" -> ").append(s.getDeparture().getArrivalStationId())
                    .append(" (grad ").append(s.getArrivalStationCityName()).append(")")
                    .append(" | Polazak: ").append(s.getActualDepartureTime())
                    .append(", Dolazak: ").append(s.getActualArrivalTime())
                    .append(", Cena: ").append(String.format("%.2f KM", s.getDeparture().getPrice())).append("\n"); // Dodao KM ovde
            if (i < segments.size() - 1) {
                LocalTime arrivalPrev = segments.get(i).getActualArrivalTime();
                LocalTime departureNext = segments.get(i+1).getActualDepartureTime();
                Duration wait = calculateWaitingDuration(arrivalPrev, departureNext);
                sb.append("     [Čekanje na presedanje: ").append(formatDuration(wait)).append("]\n");
            }
        }
        return sb.toString();
    }

    // Pomoćna metoda za formatiranje Duration
    private String formatDuration(Duration duration) {
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%dč %dmin", hours, minutes);
    }

    // Metoda za poređenje putanja - koristi se u PriorityQueue za Dijkstra
    @Override
    public int compareTo(Path other) {
        // Trenutno poređenje po ukupnom vremenu. Kasnije ćemo dodati kriterijume.
        return this.totalTravelTime.compareTo(other.totalTravelTime);
    }
}