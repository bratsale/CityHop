package project.pj25.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>Apstraktna klasa koja predstavlja saobraćajnu stanicu (autobusku ili željezničku)
 * u sistemu. Služi kao osnovna klasa za sve tipove stanica i definiše zajednička
 * svojstva i ponašanje.</p>
 *
 * <p>Klasa koristi Jackson anotacije za serijalizaciju i deserializaciju.
 * {@code @JsonIdentityInfo} omogućava da se objekti stanica serijalizuju samo jednom,
 * a na drugim mjestima se referenciraju preko njihovog ID-a.
 * {@code @JsonTypeInfo} i {@code @JsonSubTypes} osiguravaju da Jackson zna
 * koji konkretan tip stanice ({@link BusStation} ili {@link TrainStation})
 * treba da kreira tokom deserializacije na osnovu polja "type".</p>
 *
 * @author bratsale
 * @version 1.0
 */
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BusStation.class, name = "autobus"),
        @JsonSubTypes.Type(value = TrainStation.class, name = "voz")
})
public abstract class Station {

    /**
     * Jedinstveni identifikator stanice.
     */
    protected String id;

    /**
     * Grad kojem stanica pripada.
     * @see City
     */
    protected City city;

    /**
     * Tip stanice (npr. "autobus" ili "voz"). Koristi se za Jackson deserializaciju.
     */
    protected String type;

    /**
     * Lista polazaka sa ove stanice.
     * @see Departure
     */
    protected List<Departure> departures;

    /**
     * Glavni konstruktor za kreiranje novog objekta stanice.
     * Koristi ga TransportDataGenerator za inicijalizaciju.
     *
     * @param id Jedinstveni ID stanice.
     * @param city Grad kojem stanica pripada.
     * @param type Tip stanice (npr. "autobus", "voz").
     */
    public Station(String id, City city, String type) {
        this.id = id;
        this.city = city;
        this.type = type;
        this.departures = new ArrayList<>();
    }

    /**
     * Prazan konstruktor. Koristi ga Jackson (de)serijalizator za kreiranje
     * objekta prije popunjavanja polja.
     */
    public Station() {
        this.departures = new ArrayList<>();
    }

    // Getteri i setteri

    /**
     * Vraća jedinstveni identifikator stanice.
     * @return ID stanice.
     */
    public String getId() { return id; }

    /**
     * Postavlja jedinstveni identifikator stanice.
     * @param id ID stanice.
     */
    public void setId(String id) { this.id = id; }

    /**
     * Vraća grad kojem stanica pripada.
     * @return Objekat grada.
     */
    public City getCity() { return city; }

    /**
     * Postavlja grad kojem stanica pripada.
     * @param city Objekat grada.
     */
    public void setCity(City city) { this.city = city; }

    /**
     * Vraća tip stanice.
     * @return String koji predstavlja tip ("autobus" ili "voz").
     */
    public String getType() { return type; }

    /**
     * Postavlja tip stanice.
     * @param type String koji predstavlja tip ("autobus" ili "voz").
     */
    public void setType(String type) { this.type = type; }

    /**
     * Vraća listu polazaka sa ove stanice.
     * @return Lista {@link Departure} objekata.
     */
    public List<Departure> getDepartures() { return departures; }

    /**
     * Postavlja listu polazaka za ovu stanicu.
     * @param departures Nova lista polazaka.
     */
    public void setDepartures(List<Departure> departures) {
        this.departures = departures;
    }

    /**
     * Dodaje novi polazak u listu polazaka sa ove stanice.
     *
     * @param departure Objekat polaska koji treba dodati.
     */
    public void addDeparture(Departure departure) {
        if (this.departures == null) {
            this.departures = new ArrayList<>();
        }
        this.departures.add(departure);
    }

    /**
     * Vraća string reprezentaciju objekta stanice.
     * @return Formatirani string sa detaljima stanice.
     */
    @Override
    public String toString() {
        return "Station{" +
                "id='" + id + '\'' +
                ", cityId=" + (city != null ? city.getId() : "N/A") +
                ", type='" + type + '\'' +
                ", departuresCount=" + (departures != null ? departures.size() : 0) +
                '}';
    }

    /**
     * Poredi ovaj objekat stanice sa drugim objektom. Stanice su jednake
     * ako imaju isti ID.
     *
     * @param o Drugi objekat za poređenje.
     * @return {@code true} ako su objekti jednaki, {@code false} inače.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return Objects.equals(id, station.id);
    }

    /**
     * Generiše hash kod za objekat stanice na osnovu ID-a.
     * @return Hash kod objekta.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}