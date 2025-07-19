package project.pj25.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TransportMap {
    private City[][] cities; // Matrica gradova, n x m
    private Map<String, Station> stations; // Mapa svih stanica, ključ je ID stanice (npr. "A_0_0")
    private int numRows; // n
    private int numCols; // m

    public TransportMap(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.cities = new City[numRows][numCols];
        this.stations = new HashMap<>();
    }

    // Važno: Jackson će trebati no-arg konstruktor i settere za deserijalizaciju
    public TransportMap() {
        // Prazan konstruktor za Jackson
    }

    public void addCity(int x, int y, City city) {
        if (x >= 0 && x < numRows && y >= 0 && y < numCols) {
            this.cities[x][y] = city;
        } else {
            System.err.println("Error: City coordinates (" + x + ", " + y + ") out of bounds.");
        }
    }

    public City getCity(int x, int y) {
        if (x >= 0 && x < numRows && y >= 0 && y < numCols) {
            return this.cities[x][y];
        }
        return null;
    }

    public void addStation(Station station) {
        if (station != null && station.getId() != null) {
            this.stations.put(station.getId(), station);
        }
    }

    public Station getStation(String stationId) {
        return this.stations.get(stationId);
    }

    public City[][] getCities() {
        return cities;
    }

    public Map<String, Station> getAllStations() { // Promijenjeno ime iz getStations da bude jasnije
        return stations;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    // --- Setteri (za Jackson) ---
    // Jackson će možda koristiti ove settere ako ne koristi direktno konstruktor
    public void setCities(City[][] cities) { this.cities = cities; }
    public void setStations(Map<String, Station> stations) { this.stations = stations; } // Jackson će trebati ovo
    public void setNumRows(int numRows) { this.numRows = numRows; }
    public void setNumCols(int numCols) { this.numCols = numCols; }


    @Override
    public String toString() {
        return "TransportMap{" +
                "numRows=" + numRows +
                ", numCols=" + numCols +
                ", totalCities=" + (cities != null ? cities.length * cities[0].length : 0) +
                ", totalStations=" + stations.size() +
                '}';
    }

    // Ovde equals i hashCode mogu biti kompleksni zbog matrica i mapa.
    // Za sada, neka bude jednostavna implementacija, ili se fokusirajte na logiku.
    // @Override
    // public boolean equals(Object o) { ... }
    // @Override
    // public int hashCode() { ... }
}