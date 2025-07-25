package project.pj25.algorithm; // Proveri da li je ispravan paket

import project.pj25.model.*;
import java.time.LocalTime;
import java.util.Objects;

public class NodeState implements Comparable<NodeState> {
    private Station station;
    private LocalTime arrivalTime;
    private Path currentPath;
    private String optimizationCriterion; // <--- NOVO: Polje za kriterijum optimizacije

    // Ažuriran konstruktor da prihvati kriterijum
    public NodeState(Station station, LocalTime arrivalTime, Path currentPath, String optimizationCriterion) {
        this.station = station;
        this.arrivalTime = arrivalTime;
        this.currentPath = currentPath;
        this.optimizationCriterion = optimizationCriterion; // Inicijalizacija novog polja
    }

    // Getteri (dodaj getter za optimizationCriterion ako već nemaš)
    public Station getStation() { return station; }
    public LocalTime getArrivalTime() { return arrivalTime; }
    public Path getCurrentPath() { return currentPath; }
    public String getOptimizationCriterion() { return optimizationCriterion; } // <-- Novi getter

    // *** KLJUČNA IZMENA: compareTo metoda sada koristi kriterijum ***
    @Override
    public int compareTo(NodeState other) {
        // Logika poređenja zavisi od izabranog kriterijuma
        switch (this.optimizationCriterion.toLowerCase()) { // Koristi toLowerCase() za robustnost
            case "time": // Odgovara stringu koji dolazi iz GUI/RouteFinder-a
                // Primarni kriterijum: vreme putovanja. Sekundarni: cena. Tercijarni: presedanja.
                int timeCmp = this.currentPath.getTotalTravelTime().compareTo(other.currentPath.getTotalTravelTime());
                if (timeCmp == 0) {
                    int costCmp = Double.compare(this.currentPath.getTotalCost(), other.currentPath.getTotalCost());
                    if (costCmp == 0) {
                        return Integer.compare(this.currentPath.getTransfers(), other.currentPath.getTransfers());
                    }
                    return costCmp;
                }
                return timeCmp;

            case "price": // Odgovara stringu koji dolazi iz GUI/RouteFinder-a
                // Primarni kriterijum: cena. Sekundarni: vreme putovanja. Tercijarni: presedanja.
                int costCmp = Double.compare(this.currentPath.getTotalCost(), other.currentPath.getTotalCost());
                if (costCmp == 0) {
                    int timeCmpSecondary = this.currentPath.getTotalTravelTime().compareTo(other.currentPath.getTotalTravelTime());
                    if (timeCmpSecondary == 0) {
                        return Integer.compare(this.currentPath.getTransfers(), other.currentPath.getTransfers());
                    }
                    return timeCmpSecondary;
                }
                return costCmp;

            case "transfers": // Odgovara stringu koji dolazi iz GUI/RouteFinder-a
                // Primarni kriterijum: broj presedanja. Sekundarni: vreme putovanja. Tercijarni: cena.
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeState nodeState = (NodeState) o;
        // Za Dijkstra, NodeState se obično smatra istim ako je ista stanica (i eventualno vreme dolaska za time-dependent)
        // Pošto ti u RouteFinderu koristiš Map<Station, Path> i 'comparePaths' za validaciju,
        // ovde je dovoljno da jednakost bude samo po stanici.
        return Objects.equals(station.getId(), nodeState.station.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(station.getId());
    }
}