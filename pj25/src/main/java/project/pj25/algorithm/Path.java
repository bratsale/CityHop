// project.pj25.model.Path.java
package project.pj25.algorithm;

import project.pj25.model.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Path implements Comparable<Path> {
    private List<RouteSegment> segments;
    private LocalTime startTime; // Vreme polaska sa prve stanice
    private LocalTime endTime;   // Vreme dolaska na poslednju stanicu
    private double totalCost;
    private int transfers; // Broj presedanja
    private Duration totalTravelTime; // Ukupno vreme putovanja od polaska do dolaska

    public Path() {
        this.segments = new ArrayList<>();
        this.totalCost = 0.0;
        this.transfers = 0;
        this.totalTravelTime = Duration.ZERO;
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

    public void addSegment(RouteSegment segment) {
        if (segments.isEmpty()) {
            this.startTime = segment.getActualDepartureTime();
        }
        this.segments.add(segment);
        this.endTime = segment.getActualArrivalTime();
        this.totalCost += segment.getDeparture().getPrice();
        // Broj presedanja je broj segmenata minus 1, ako ih ima više od 0
        this.transfers = segments.size() > 0 ? segments.size() - 1 : 0;
        // Računanje ukupnog vremena putovanja
        if (startTime != null && endTime != null) {
            this.totalTravelTime = Duration.between(startTime, endTime);
            // Rešavanje slučaja preko ponoći
            if (totalTravelTime.isNegative()) {
                this.totalTravelTime = totalTravelTime.plusDays(1);
            }
        }
    }

    // Getteri
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
        for (RouteSegment segment : segments) {
            sb.append("  ").append(segment).append("\n");
        }
        return sb.toString();
    }

    // Pomoćna metoda za formatiranje Duration
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%dč %dmin", hours, minutes);
    }

    // Metoda za poređenje putanja - koristi se u PriorityQueue za Dijkstra
    @Override
    public int compareTo(Path other) {
        // Trenutno poređenje po ukupnom vremenu. Kasnije ćemo dodati kriterijume.
        return this.totalTravelTime.compareTo(other.totalTravelTime);
    }
}