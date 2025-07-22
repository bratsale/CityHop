package project.pj25.algorithm;

import project.pj25.model.*;
import java.time.LocalTime;

public class RouteSegment {
    private Departure departure;
    private Station startStation; // Stvarna početna stanica polaska
    private Station endStation;   // Stvarna krajnja stanica polaska
    private LocalTime actualDepartureTime; // Stvarno vreme polaska (može biti kasnije od planiranog zbog transfera)
    private LocalTime actualArrivalTime;   // Stvarno vreme dolaska

    public RouteSegment(Departure departure, Station startStation, Station endStation, LocalTime actualDepartureTime, LocalTime actualArrivalTime) {
        this.departure = departure;
        this.startStation = startStation;
        this.endStation = endStation;
        this.actualDepartureTime = actualDepartureTime;
        this.actualArrivalTime = actualArrivalTime;
    }

    // Getteri
    public Departure getDeparture() {
        return departure;
    }

    public Station getStartStation() {
        return startStation;
    }

    public Station getEndStation() {
        return endStation;
    }

    public LocalTime getActualDepartureTime() {
        return actualDepartureTime;
    }

    public LocalTime getActualArrivalTime() {
        return actualArrivalTime;
    }

    // Možda i toString za lakši prikaz
    @Override
    public String toString() {
        return String.format("%s: %s (%s) -> %s (%s) | Polazak: %s, Dolazak: %s, Cena: %.2f",
                departure.getType(),
                startStation.getCity().getName(),
                startStation.getId(),
                endStation.getCity().getName(),
                endStation.getId(),
                actualDepartureTime,
                actualArrivalTime,
                departure.getPrice());
    }
}
