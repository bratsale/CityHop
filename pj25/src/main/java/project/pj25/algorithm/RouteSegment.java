package project.pj25.algorithm;

import project.pj25.model.*;
import java.time.Duration;
import java.time.LocalTime;

/**
 * Klasa koja predstavlja jedan segment putovanja, odnosno, jednu dionicu
 * od stanice do stanice.
 * <p>
 * Sadrži detaljne informacije o polasku ({@link Departure}), polaznoj i dolaznoj
 * stanici, te stvarnim vremenima polaska i dolaska za taj segment. Ova klasa je
 * osnovni gradivni blok za kreiranje kompletne putanje (<code>{@link Path}</code>).
 * </p>
 *
 * @author Tvoje Ime
 * @version 1.0
 * @see Path
 */
public class RouteSegment {

    /**
     * Objekat polaska koji sadrži informacije o tipu prevoza, cijeni i rasporedu.
     */
    private Departure departure;

    /**
     * Polazna stanica za ovaj segment.
     */
    private Station startStation;

    /**
     * Dolazna stanica za ovaj segment.
     */
    private Station endStation;

    /**
     * Stvarno vrijeme polaska iz polazne stanice.
     */
    private LocalTime actualDepartureTime;

    /**
     * Stvarno vrijeme dolaska na dolaznu stanicu.
     */
    private LocalTime actualArrivalTime;

    /**
     * Konstruktor za kreiranje novog segmenta rute.
     *
     * @param departure Polazak koji se odnosi na ovaj segment.
     * @param startStation Polazna stanica.
     * @param endStation Dolazna stanica.
     * @param actualDepartureTime Stvarno vrijeme polaska.
     * @param actualArrivalTime Stvarno vrijeme dolaska.
     */
    public RouteSegment(Departure departure, Station startStation, Station endStation, LocalTime actualDepartureTime, LocalTime actualArrivalTime) {
        this.departure = departure;
        this.startStation = startStation;
        this.endStation = endStation;
        this.actualDepartureTime = actualDepartureTime;
        this.actualArrivalTime = actualArrivalTime;
    }

    // --- Getteri sa Javadoc komentarima ---

    /**
     * Vraća objekat polaska ({@link Departure}) za ovaj segment.
     * @return Objekat polaska.
     */
    public Departure getDeparture() {
        return departure;
    }

    /**
     * Vraća polaznu stanicu.
     * @return {@link Station} objekat polazne stanice.
     */
    public Station getStartStation() {
        return startStation;
    }

    /**
     * Vraća dolaznu stanicu.
     * @return {@link Station} objekat dolazne stanice.
     */
    public Station getEndStation() {
        return endStation;
    }

    /**
     * Vraća stvarno vrijeme polaska.
     * @return Vrijeme polaska kao {@link LocalTime}.
     */
    public LocalTime getActualDepartureTime() {
        return actualDepartureTime;
    }

    /**
     * Vraća stvarno vrijeme dolaska.
     * @return Vrijeme dolaska kao {@link LocalTime}.
     */
    public LocalTime getActualArrivalTime() {
        return actualArrivalTime;
    }

    /**
     * Računa trajanje ovog pojedinačnog segmenta putovanja.
     * <p>
     * Metoda uzima u obzir prelazak preko ponoći ako je vrijeme dolaska
     * hronološki ranije od vremena polaska (npr. polazak u 23:00, dolazak u 01:00).
     * </p>
     * @return Trajanje segmenta kao {@link Duration}.
     */
    public Duration getSegmentDuration() {
        Duration duration = Duration.between(actualDepartureTime, actualArrivalTime);
        if (duration.isNegative()) {
            duration = duration.plusDays(1);
        }
        return duration;
    }

    /**
     * Vraća naziv grada polazne stanice.
     * @return Naziv grada.
     */
    public String getDepartureStationCityName() {
        return startStation.getCity().getName();
    }

    /**
     * Vraća naziv grada dolazne stanice.
     * @return Naziv grada.
     */
    public String getArrivalStationCityName() {
        return endStation.getCity().getName();
    }

    /**
     * Vraća tip prevoza (npr. "autobus" ili "voz") za ovaj segment.
     * @return Tip prevoza.
     */
    public String getDepartureType() {
        return departure.getType();
    }

    /**
     * Vraća ID polazne stanice.
     * @return ID stanice.
     */
    public String getDepartureStationId() {
        return departure.getDepartureStationId();
    }

    /**
     * Vraća ID dolazne stanice.
     * @return ID stanice.
     */
    public String getArrivalStationId() {
        return departure.getArrivalStationId();
    }

    /**
     * Vraća cijenu za ovaj segment putovanja.
     * @return Cijena.
     */
    public double getPrice() {
        return departure.getPrice();
    }

    /**
     * Vraća string reprezentaciju objekta segmenta rute.
     * @return Formatirani string sa detaljima segmenta.
     */
    @Override
    public String toString() {
        return String.format("%s: %s (%s) -> %s (%s) | Polazak: %s, Dolazak: %s, Cijena: %.2f",
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