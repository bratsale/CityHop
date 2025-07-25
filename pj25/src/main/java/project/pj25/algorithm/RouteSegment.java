package project.pj25.algorithm; // Proveri da li je ovo ispravan paket. Ako je model, trebalo bi biti project.pj25.model.

import project.pj25.model.*; // Važno je da su ovi modeli dostupni
import java.time.Duration; // Dodaj import za Duration
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

    /**
     * Računa trajanje ovog pojedinačnog segmenta putovanja.
     * Uzima u obzir prelazak preko ponoći ako je vreme dolaska ranije od vremena polaska.
     * @return Trajanje segmenta kao Duration.
     */
    public Duration getSegmentDuration() {
        // Izračunaj trajanje između stvarnog vremena polaska i stvarnog vremena dolaska za ovaj segment
        Duration duration = Duration.between(actualDepartureTime, actualArrivalTime);

        // Ako je vreme dolaska hronološki ranije od vremena polaska, to znači da segment prelazi preko ponoći
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

    // geteri za table view

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



    // Možda i toString za lakši prikaz
    @Override
    public String toString() {
        // Koristimo getDepartureStationCityName() i getArrivalStationCityName()
        return String.format("%s: %s (%s) -> %s (%s) | Polazak: %s, Dolazak: %s, Cena: %.2f",
                departure.getType(),
                getDepartureStationCityName(), // Pozivamo novu metodu
                startStation.getId(),
                getArrivalStationCityName(),   // Pozivamo novu metodu
                endStation.getId(),
                actualDepartureTime,
                actualArrivalTime,
                departure.getPrice());
    }
}