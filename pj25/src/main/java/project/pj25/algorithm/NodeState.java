package project.pj25.algorithm;

import project.pj25.model.*;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Pomoćna klasa koja predstavlja stanje čvora u algoritmu pretrage rute.
 *
 * <p>Ova klasa čuva trenutno stanje tokom pretrage: stanicu, vrijeme dolaska
 * na tu stanicu i putanju do te tačke. Implementira interfejs {@link Comparable}
 * kako bi se omogućilo poređenje stanja na osnovu različitih kriterijuma
 * (vrijeme, cijena, presjedanja). Ovo je ključno za rad algoritma sa prioritetnim redom
 * kao što je A* ili Dijkstra.</p>
 *
 * @author bratsale
 * @version 1.0
 * @see RouteFinder
 * @see Path
 */
public class NodeState implements Comparable<NodeState> {
    /**
     * Trenutna stanica na kojoj se stanje nalazi.
     */
    private Station station;
    /**
     * Vrijeme dolaska na ovu stanicu.
     */
    private LocalTime arrivalTime;
    /**
     * Kumulativna putanja do trenutnog stanja.
     */
    private Path currentPath;
    /**
     * Kriterijum optimizacije koji se koristi za poređenje stanja.
     */
    private String optimizationCriterion;

    /**
     * Konstruktor za kreiranje novog stanja čvora.
     *
     * @param station Trenutna stanica.
     * @param arrivalTime Vrijeme dolaska.
     * @param currentPath Kumulativna putanja do trenutnog stanja.
     * @param optimizationCriterion Kriterijum optimizacije (npr. "time", "price", "transfers").
     */
    public NodeState(Station station, LocalTime arrivalTime, Path currentPath, String optimizationCriterion) {
        this.station = station;
        this.arrivalTime = arrivalTime;
        this.currentPath = currentPath;
        this.optimizationCriterion = optimizationCriterion;
    }

    // Getteri i setteri

    /**
     * Vraća stanicu trenutnog stanja.
     * @return {@link Station} objekat.
     */
    public Station getStation() { return station; }

    /**
     * Vraća putanju do trenutnog stanja.
     * @return {@link Path} objekat.
     */
    public Path getCurrentPath() { return currentPath; }

    /**
     * Vraća numeričku vrijednost troška putanje na osnovu kriterijuma optimizacije.
     * Ova metoda je ključna za rad prioritetskog reda, jer daje jedinstvenu
     * metriku za poređenje putanja.
     *
     * @return Trošak putanje kao double vrijednost.
     * @throws IllegalArgumentException ako je kriterijum optimizacije nepoznat.
     */
    public double getCost() {
        switch (this.optimizationCriterion.toLowerCase()) {
            case "time":
                return this.currentPath.getTotalTravelTime().toMinutes();
            case "price":
                return this.currentPath.getTotalCost();
            case "transfers":
                return this.currentPath.getTransfers();
            default:
                throw new IllegalArgumentException("Nepoznat kriterijum optimizacije: " + optimizationCriterion);
        }
    }


    /**
     * Poredi dva objekta {@code NodeState} na osnovu definisanog kriterijuma optimizacije.
     *
     * <p>Kriterijumi poređenja su:
     * <ul>
     * <li><b>"time"</b>: Primarno se poredi ukupno vrijeme putovanja. Ako je jednako,
     * porede se cijene, a zatim broj presjedanja.</li>
     * <li><b>"price"</b>: Primarno se poredi ukupna cijena. Ako je jednaka,
     * porede se vremena putovanja, a zatim broj presjedanja.</li>
     * <li><b>"transfers"</b>: Primarno se poredi broj presjedanja. Ako je jednak,
     * porede se vremena putovanja, a zatim cijene.</li>
     * </ul>
     * </p>
     *
     * @param other Drugi objekat {@code NodeState} za poređenje.
     * @return Negativan cijeli broj, nula, ili pozitivan cijeli broj ako je ovaj
     * objekat manji od, jednak, ili veći od navedenog objekta.
     * @throws IllegalArgumentException ako je kriterijum optimizacije nepoznat.
     */
    @Override
    public int compareTo(NodeState other) {
        switch (this.optimizationCriterion.toLowerCase()) {
            case "time":
                int timeCmp = this.currentPath.getTotalTravelTime().compareTo(other.currentPath.getTotalTravelTime());
                if (timeCmp == 0) {
                    int costCmp = Double.compare(this.currentPath.getTotalCost(), other.currentPath.getTotalCost());
                    if (costCmp == 0) {
                        return Integer.compare(this.currentPath.getTransfers(), other.currentPath.getTransfers());
                    }
                    return costCmp;
                }
                return timeCmp;

            case "price":
                int costCmp = Double.compare(this.currentPath.getTotalCost(), other.currentPath.getTotalCost());
                if (costCmp == 0) {
                    int timeCmpSecondary = this.currentPath.getTotalTravelTime().compareTo(other.currentPath.getTotalTravelTime());
                    if (timeCmpSecondary == 0) {
                        return Integer.compare(this.currentPath.getTransfers(), other.currentPath.getTransfers());
                    }
                    return timeCmpSecondary;
                }
                return costCmp;

            case "transfers":
                int transfersCmp = Integer.compare(this.currentPath.getTransfers(), other.currentPath.getTransfers());
                if (transfersCmp == 0) {
                    int timeCmpSecondary = this.currentPath.getTotalTravelTime().compareTo(other.currentPath.getTotalTravelTime());
                    if (timeCmpSecondary == 0) {
                        return Double.compare(this.currentPath.getTotalCost(), other.currentPath.getTotalCost());
                    }
                    return timeCmpSecondary;
                }
                return transfersCmp;

            default:
                throw new IllegalArgumentException("Nepoznat kriterijum optimizacije: " + optimizationCriterion);
        }
    }

    /**
     * Poredi ovaj objekat stanja sa drugim objektom.
     * <p>Dva objekta {@code NodeState} su jednaka ako se odnose na istu stanicu (na osnovu ID-a).</p>
     * @param o Drugi objekat za poređenje.
     * @return {@code true} ako su objekti jednaki, {@code false} inače.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeState nodeState = (NodeState) o;
        return Objects.equals(station.getId(), nodeState.station.getId());
    }

    /**
     * Generiše hash kod za objekat stanja čvora.
     * <p>Hash kod je baziran isključivo na ID-u stanice, što je u skladu sa metodom {@code equals()}.</p>
     * @return Hash kod objekta.
     */
    @Override
    public int hashCode() {
        return Objects.hash(station.getId());
    }
}