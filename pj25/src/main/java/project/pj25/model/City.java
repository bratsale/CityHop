package project.pj25.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo; // DODAJ OVO
import com.fasterxml.jackson.annotation.ObjectIdGenerators; // DODAJ OVO
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIdentityInfo( // <-- DODAJ OVU ANOTACIJU NA KLASU
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id" // Koristi 'id' polje kao jedinstveni identifikator
)
public class City {
    private int id;
    private int x;
    private int y;
    private String name;
    private List<Station> stations;

    public City(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.name = "G_" + x + "_" + y;
        this.stations = new ArrayList<>();
    }

    // Prazan konstruktor za Jackson
    public City() {} // Dodajte ako već nemate

    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) { // Dodaj setter za Jackson deserializaciju
        this.stations = stations;
    }

    public void addStation(Station station) {
        if (this.stations == null) {
            this.stations = new ArrayList<>();
        }
        this.stations.add(station);
    }

    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", numStations=" + (stations != null ? stations.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return id == city.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}