package project.pj25.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BusStation implements Station {
    private String id; // Npr. A_X_Y
    private City city;
    private List<Departure> departures; // Lista polazaka sa ove stanice

    public BusStation(City city) {
        this.city = city;
        this.id = "A_" + city.getX() + "_" + city.getY();
        this.departures = new ArrayList<>();
    }

    public BusStation(){
        this.departures = new ArrayList<>();
    }

    public void setCity(City city) {
        this.city = city;
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public City getCity() {
        return city;
    }

    @Override
    public List<Departure> getDepartures() {
        return departures;
    }

    @Override
    public void addDeparture(Departure departure) {
        if (departure != null) {
            this.departures.add(departure);
        }
    }

    @Override
    public String toString() {
        return "BusStation{" +
                "id='" + id + '\'' +
                ", city=" + city.getName() +
                ", departuresCount=" + departures.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusStation that = (BusStation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void setId(String s) {
        this.id = s;
    }
}