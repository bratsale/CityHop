package project.pj25.algorithm;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa koja predstavlja kompletnu putanju od početnog do krajnjeg grada,
 * sastavljenu od niza segmenata putovanja (<code>RouteSegment</code>).
 * <p>
 * Sadrži kumulativne podatke kao što su ukupna cijena, vrijeme putovanja
 * i broj presjedanja. Ova klasa se koristi u algoritmima za pretragu
 * najoptimalnije rute.
 * </p>
 *
 * @author Tvoje Ime
 * @version 1.0
 * @see RouteSegment
 * @see NodeState
 */
public class Path implements Comparable<Path> {

    /**
     * Lista segmenata putovanja koji čine putanju.
     */
    private List<RouteSegment> segments;

    /**
     * Vrijeme polaska prve dionice.
     */
    private LocalTime startTime;

    /**
     * Vrijeme dolaska na finalnu destinaciju.
     */
    private LocalTime endTime;

    /**
     * Ukupna kumulativna cijena putovanja.
     */
    private double totalCost;

    /**
     * Ukupan broj presjedanja na ruti.
     */
    private int transfers;

    /**
     * Ukupno vrijeme putovanja (uključujući vrijeme čekanja).
     */
    private Duration totalTravelTime;

    /**
     * Prazan konstruktor za kreiranje nove, prazne putanje.
     */
    public Path() {
        this.segments = new ArrayList<>();
        this.totalCost = 0.0;
        this.transfers = 0;
        this.totalTravelTime = Duration.ZERO;
        this.startTime = LocalTime.MIDNIGHT;
        this.endTime = LocalTime.MIDNIGHT;
    }

    /**
     * Konstruktor za kopiranje.
     * <p>Kreira duboku kopiju postojeće putanje.</p>
     *
     * @param other Putanja koja se kopira.
     */
    public Path(Path other) {
        this.segments = new ArrayList<>(other.segments);
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.totalCost = other.totalCost;
        this.transfers = other.transfers;
        this.totalTravelTime = other.totalTravelTime;
    }

    /**
     * Dodaje novi segment putovanja u putanju i ažurira sve kumulativne
     * vrijednosti (vrijeme, cijena, presjedanja).
     *
     * @param segment Segment putovanja koji se dodaje.
     */
    public void addSegment(RouteSegment segment) {
        if (this.segments.isEmpty()) {
            this.startTime = segment.getActualDepartureTime();
            this.totalTravelTime = segment.getSegmentDuration();
        } else {
            LocalTime prevArrivalTime = this.endTime;
            LocalTime currentDepartureTime = segment.getActualDepartureTime();
            Duration waitingDuration = calculateWaitingDuration(prevArrivalTime, currentDepartureTime);
            this.totalTravelTime = this.totalTravelTime.plus(waitingDuration).plus(segment.getSegmentDuration());
            this.transfers++;
        }

        this.segments.add(segment);
        this.endTime = segment.getActualArrivalTime();
        this.totalCost += segment.getDeparture().getPrice();
    }

    /**
     * Pomoćna metoda za izračunavanje vremena čekanja između dva vremena
     * ({@link LocalTime}), uzimajući u obzir prelazak preko ponoći.
     *
     * @param prevArrivalTime Vrijeme dolaska prethodnog segmenta.
     * @param currentDepartureTime Vrijeme polaska tekućeg segmenta.
     * @return Trajanje čekanja kao {@link Duration}.
     */
    private Duration calculateWaitingDuration(LocalTime prevArrivalTime, LocalTime currentDepartureTime) {
        long prevArrivalMinutes = prevArrivalTime.toSecondOfDay() / 60;
        long currentDepartureMinutes = currentDepartureTime.toSecondOfDay() / 60;

        long waitingMinutes;
        if (currentDepartureMinutes < prevArrivalMinutes) {
            waitingMinutes = (currentDepartureMinutes + (24 * 60)) - prevArrivalMinutes;
        } else {
            waitingMinutes = currentDepartureMinutes - prevArrivalMinutes;
        }
        return Duration.ofMinutes(waitingMinutes);
    }

    // --- Getteri sa Javadoc komentarima ---

    /**
     * Vraća listu segmenata putovanja.
     * @return Lista {@link RouteSegment} objekata.
     */
    public List<RouteSegment> getSegments() {
        return segments;
    }

    /**
     * Vraća vrijeme polaska prve dionice putovanja.
     * @return Vrijeme polaska kao {@link LocalTime}.
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Vraća vrijeme dolaska na finalnu destinaciju.
     * @return Vrijeme dolaska kao {@link LocalTime}.
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Vraća ukupnu cijenu putovanja.
     * @return Ukupna cijena.
     */
    public double getTotalCost() {
        return totalCost;
    }

    /**
     * Vraća ukupan broj presjedanja.
     * @return Broj presjedanja.
     */
    public int getTransfers() {
        return transfers;
    }

    /**
     * Vraća ukupno vrijeme putovanja.
     * @return Ukupno vrijeme kao {@link Duration}.
     */
    public Duration getTotalTravelTime() {
        return totalTravelTime;
    }

    /**
     * Vraća string reprezentaciju putanje sa svim detaljima.
     * @return Formatirani string sa sumarnim podacima i detaljima segmenata.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Ukupno vrijeme: %s, Ukupna cijena: %.2f KM, Presjedanja: %d\n",
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
                    .append(", Cijena: ").append(String.format("%.2f KM", s.getDeparture().getPrice())).append("\n");
            if (i < segments.size() - 1) {
                LocalTime arrivalPrev = segments.get(i).getActualArrivalTime();
                LocalTime departureNext = segments.get(i+1).getActualDepartureTime();
                Duration wait = calculateWaitingDuration(arrivalPrev, departureNext);
                sb.append("     [Čekanje na presjedanje: ").append(formatDuration(wait)).append("]\n");
            }
        }
        return sb.toString();
    }

    /**
     * Pomoćna metoda za formatiranje trajanja putovanja.
     *
     * @param duration Trajanje koje se formatira.
     * @return Formatirani string u formatu "Xč Ymin".
     */
    private String formatDuration(Duration duration) {
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%dč %dmin", hours, minutes);
    }

    /**
     * Poredi ovu putanju sa drugom putanjom na osnovu ukupnog vremena putovanja.
     *
     * @param other Druga putanja za poređenje.
     * @return Negativan, nula, ili pozitivan broj ako je ova putanja kraća,
     * jednaka, ili duža od druge putanje.
     */
    @Override
    public int compareTo(Path other) {
        return this.totalTravelTime.compareTo(other.totalTravelTime);
    }
}