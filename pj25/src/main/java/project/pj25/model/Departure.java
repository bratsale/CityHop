package project.pj25.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import project.pj25.util.DurationDeserializer;
import project.pj25.util.LocalTimeDeserializer;

/**
 * Klasa koja predstavlja pojedinačni polazak između dvije stanice.
 *
 * <p>Ovaj objekat sadrži sve relevantne detalje o jednom segmentu putovanja:
 * tip prevoza, stanice polaska i dolaska, vremena, cijenu, kao i minimalno
 * vrijeme presjedanja na dolaznoj stanici. Klasa koristi Jackson anotacije
 * za ispravnu deserializaciju polja koja su tipa {@link LocalTime} i {@link Duration},
 * s obzirom na to da Jackson ne podržava ove tipove po defaultu.</p>
 *
 * @author bratsale
 * @version 1.0
 */
public class Departure {
    /**
     * Tip prevoza ("autobus" ili "voz").
     */
    private String type;
    /**
     * ID stanice polaska.
     */
    private String departureStationId;
    /**
     * ID stanice dolaska.
     */
    private String arrivalStationId;
    /**
     * Vrijeme polaska sa stanice.
     */
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime departureTime;
    /**
     * Vrijeme dolaska na odredišnu stanicu.
     */
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime arrivalTime;
    /**
     * Cijena karte za ovaj segment putovanja.
     */
    private double price;
    /**
     * Minimalno vrijeme presjedanja koje je potrebno na dolaznoj stanici.
     */
    @JsonDeserialize(using = DurationDeserializer.class)
    private Duration minTransferTime;

    /**
     * Glavni konstruktor za kreiranje novog objekta polaska.
     *
     * @param type Tip prevoza.
     * @param departureStationId ID stanice polaska.
     * @param arrivalStationId ID stanice dolaska.
     * @param departureTime Vrijeme polaska.
     * @param arrivalTime Vrijeme dolaska.
     * @param price Cijena karte.
     * @param minTransferTime Minimalno vrijeme za presjedanje na dolaznoj stanici.
     */
    public Departure(String type, String departureStationId, String arrivalStationId,
                     LocalTime departureTime, LocalTime arrivalTime, double price, Duration minTransferTime) {
        this.type = type;
        this.departureStationId = departureStationId;
        this.arrivalStationId = arrivalStationId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.minTransferTime = minTransferTime;
    }

    /**
     * Prazan konstruktor.
     * <p>Neophodan za rad Jackson biblioteke tokom deserializacije.</p>
     */
    public Departure() {}

    // Getteri i setteri

    /**
     * Vraća tip prevoza.
     * @return Tip prevoza.
     */
    public String getType() { return type; }

    /**
     * Vraća ID stanice polaska.
     * @return ID stanice polaska.
     */
    public String getDepartureStationId() { return departureStationId; }

    /**
     * Vraća ID stanice dolaska.
     * @return ID stanice dolaska.
     */
    public String getArrivalStationId() { return arrivalStationId; }

    /**
     * Vraća vrijeme polaska.
     * @return Vrijeme polaska kao {@link LocalTime} objekat.
     */
    public LocalTime getDepartureTime() { return departureTime; }

    /**
     * Vraća vrijeme dolaska.
     * @return Vrijeme dolaska kao {@link LocalTime} objekat.
     */
    public LocalTime getArrivalTime() { return arrivalTime; }

    /**
     * Vraća cijenu karte.
     * @return Cijena karte.
     */
    public double getPrice() { return price; }

    /**
     * Vraća string reprezentaciju objekta polaska.
     * @return Formatirani string sa detaljima polaska.
     */
    @Override
    public String toString() {
        return "Departure{" +
                "type='" + type + '\'' +
                ", from='" + departureStationId + '\'' +
                ", to='" + arrivalStationId + '\'' +
                ", depTime=" + departureTime +
                ", arrTime=" + arrivalTime +
                ", price=" + String.format("%.2f", price) +
                ", minTransfer=" + (minTransferTime != null ? minTransferTime.toMinutes() + "min" : "N/A") +
                '}';
    }

    /**
     * Poredi ovaj objekat polaska sa drugim objektom.
     * Polasci se smatraju jednakim ako sva njihova polja imaju iste vrijednosti.
     *
     * @param o Drugi objekat za poređenje.
     * @return {@code true} ako su objekti jednaki, {@code false} inače.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Departure departure = (Departure) o;
        return Double.compare(price, departure.price) == 0 &&
                Objects.equals(type, departure.type) &&
                Objects.equals(departureStationId, departure.departureStationId) &&
                Objects.equals(arrivalStationId, departure.arrivalStationId) &&
                Objects.equals(departureTime, departure.departureTime) &&
                Objects.equals(arrivalTime, departure.arrivalTime) &&
                Objects.equals(minTransferTime, departure.minTransferTime);
    }

    /**
     * Generiše hash kod za objekat polaska na osnovu svih njegovih polja.
     * @return Hash kod objekta.
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, departureStationId, arrivalStationId, departureTime, arrivalTime, price, minTransferTime);
    }
}