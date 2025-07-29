package project.pj25.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonTypeInfo; // <-- DODAJ OVO
import com.fasterxml.jackson.annotation.JsonSubTypes; // <-- DODAJ OVO
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
@JsonTypeInfo( // <-- DODAJ OVU ANOTACIJU
        use = JsonTypeInfo.Id.NAME, // Koristi naziv tipa za identifikaciju
        include = JsonTypeInfo.As.PROPERTY, // Dodaj svojstvo u JSON
        property = "type" // Naziv svojstva koje sadrži informaciju o tipu (ovo je tvoje "type" polje!)
)
@JsonSubTypes({ // <-- DODAJ OVU ANOTACIJU
        @JsonSubTypes.Type(value = BusStation.class, name = "autobus"), // Mapiraj BusStation kada je "type": "autobus"
        @JsonSubTypes.Type(value = TrainStation.class, name = "voz")    // Mapiraj TrainStation kada je "type": "voz"
})
public abstract class Station {
    protected String id;
    protected City city;
    protected String type; // Ovo polje će sada biti korišćeno za tipovanje od strane Jacksona
    protected List<Departure> departures;

    // Glavni konstruktor - Koristi ga TransportDataGenerator
    public Station(String id, City city, String type) {
        this.id = id;
        this.city = city;
        this.type = type;
        this.departures = new ArrayList<>();
    }

    // Prazan konstruktor - Koristi ga Jackson za deserializaciju
    public Station() {
        this.departures = new ArrayList<>();
    }

    // --- Getteri i Setteri ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<Departure> getDepartures() { return departures; }
    public void setDepartures(List<Departure> departures) {
        this.departures = departures;
    }

    public void addDeparture(Departure departure) {
        if (this.departures == null) {
            this.departures = new ArrayList<>();
        }
        this.departures.add(departure);
    }

    @Override
    public String toString() {
        return "Station{" +
                "id='" + id + '\'' +
                ", cityId=" + (city != null ? city.getId() : "N/A") +
                ", type='" + type + '\'' +
                ", departuresCount=" + (departures != null ? departures.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return Objects.equals(id, station.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}