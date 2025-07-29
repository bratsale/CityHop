package project.pj25.algorithm;

import project.pj25.model.*;
import java.time.Duration;
import java.time.LocalTime;

public class RouteSegment {
    private Departure departure;
    private Station startStation;
    private Station endStation;
    private LocalTime actualDepartureTime;
    private LocalTime actualArrivalTime;

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

    /**
     * Računa trajanje ovog pojedinačnog segmenta putovanja.
     * Uzima u obzir prelazak preko ponoći ako je vreme dolaska ranije od vremena polaska.
     * @return Trajanje segmenta kao Duration.
     */
    public Duration getSegmentDuration() {
        // Izračunaj trajanje između stvarnog vremena polaska i stvarnog vremena dolaska za ovaj segment
        Duration duration = Duration.between(actualDepartureTime, actualArrivalTime);

        // Ako je vrijeme dolaska hronološki ranije od vremena polaska, to znači da segment prelazi preko ponoći
        if (duration.isNegative()) {
            // Dodaj 24 sata da bi se ispravno uračunalo prelaženje preko ponoći
            duration = duration.plusDays(1);
        }
        return duration;
    }

    /**
     * Vraća naziv grada polazne stanice za ovaj segment.
     * @return Naziv grada polazne stanice.
     */
    public String getDepartureStationCityName() {
        return startStation.getCity().getName();
    }

    /**
     * Vraća naziv grada dolazne stanice za ovaj segment.
     * @return Naziv grada dolazne stanice.
     */
    public String getArrivalStationCityName() {
        return endStation.getCity().getName();
    }

    // Geteri za table view

    public String getDepartureType() {
        return departure.getType();
    }

    public String getDepartureStationId() {
        return departure.getDepartureStationId();
    }

    public String getArrivalStationId() {
        return departure.getArrivalStationId();
    }

    public double getPrice() {
        return departure.getPrice();
    }



    // Možda nam sad i ne treba to string kako smo prešli na tabelarni prikaz al neka ga
    @Override
    public String toString() {
        // Koristimo getDepartureStationCityName() i getArrivalStationCityName()
        return String.format("%s: %s (%s) -> %s (%s) | Polazak: %s, Dolazak: %s, Cena: %.2f",
                departure.getType(),
                getDepartureStationCityName(),
                startStation.getId(),
                getArrivalStationCityName(),
                endStation.getId(),
                actualDepartureTime,
                actualArrivalTime,
                departure.getPrice());
    }
}