package project.pj25.algorithm; // Ili project.pj25.algorithm; ako si premestio Path tamo

import project.pj25.model.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Path implements Comparable<Path> { // Path treba biti u project.pj25.model paketu!
    private List<RouteSegment> segments;
    private LocalTime startTime;      // Vreme polaska sa prve stanice
    private LocalTime endTime;        // Vreme dolaska na poslednju stanicu (LocalTime)
    private double totalCost;
    private int transfers;            // Broj presedanja
    private Duration totalTravelTime; // Ukupno vreme putovanja od polaska do dolaska (akumulirano)

    public Path() {
        this.segments = new ArrayList<>();
        this.totalCost = 0.0;
        this.transfers = 0;
        this.totalTravelTime = Duration.ZERO; // Inicijalizacija na nulu
    }

    // Konstruktor za kopiranje (korisno za Dijkstra, kada se gradi putanja)
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
            this.totalTravelTime = segment.getSegmentDuration(); // Ukupno vreme je inicijalno samo trajanje prvog segmenta
        } else {
            // Za svaki sledeći segment, izračunaj vreme čekanja
            LocalTime prevArrivalTime = this.endTime; // Vreme dolaska prethodnog segmenta
            LocalTime currentDepartureTime = segment.getActualDepartureTime(); // Vreme polaska tekućeg segmenta

            // Izračunaj trajanje čekanja, uzimajući u obzir prelazak preko ponoći
            Duration waitingDuration = calculateWaitingDuration(prevArrivalTime, currentDepartureTime);

            // Dodaj vreme čekanja i trajanje tekućeg segmenta na ukupno vreme putovanja
            this.totalTravelTime = this.totalTravelTime.plus(waitingDuration).plus(segment.getSegmentDuration());
        }

        // Dodaj segment u listu
        this.segments.add(segment);
        // Ažuriraj vreme dolaska cele putanje (LocalTime, samo vreme u danu)
        this.endTime = segment.getActualArrivalTime();
        // Ažuriraj ukupnu cenu
        this.totalCost += segment.getDeparture().getPrice();
        // Broj presedanja je broj segmenata minus 1
        this.transfers = segments.size() - 1;
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
            // Ako je vreme polaska (npr. 07:00) manje od vremena dolaska (npr. 09:00),
            // to znači da se polazak dešava sledećeg dana.
            // Zato dodajemo 24 sata (1440 minuta) na vreme polaska da bismo ga uporedili na istoj vremenskoj osi.
            waitingMinutes = (currentDepartureMinutes + (24 * 60)) - prevArrivalMinutes;
        } else {
            // Polazak je istog dana ili kasnije istog dana
            waitingMinutes = currentDepartureMinutes - prevArrivalMinutes;
        }
        return Duration.ofMinutes(waitingMinutes);
    }

    // --- Getteri (ovo je ostalo isto) ---
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
        sb.append(String.format("Ukupno vreme: %s, Ukupna cena: %.2f, Presedanja: %d\n",
                formatDuration(totalTravelTime), totalCost, transfers));
        for (int i = 0; i < segments.size(); i++) { // Dodao sam for petlju da bi ispisao čekanje
            RouteSegment s = segments.get(i);
            sb.append("  ").append(s.getDeparture().getType())
                    .append(": ").append(s.getDeparture().getDepartureStationId())
                    .append(" (grad ").append(s.getDepartureStationCityName()).append(")")
                    .append(" -> ").append(s.getDeparture().getArrivalStationId())
                    .append(" (grad ").append(s.getArrivalStationCityName()).append(")")
                    .append(" | Polazak: ").append(s.getActualDepartureTime())
                    .append(", Dolazak: ").append(s.getActualArrivalTime())
                    .append(", Cena: ").append(String.format("%.2f", s.getDeparture().getPrice())).append("\n");
            if (i < segments.size() - 1) {
                // Dodaj informacije o čekanju između segmenata
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