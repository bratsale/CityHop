// TransportMap.java
package project.pj25.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TransportMap {
    private City[][] cities;
    private Map<String, Station> stations;
    private int numRows;
    private int numCols;

    public TransportMap() {
        this.stations = new HashMap<>(); // Inicijalizuj mapu u no-arg konstruktoru
    }

    // Konstruktor koji prima dimenzije (može biti koristan za rucno inicijalizovanje)
    public TransportMap(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.cities = new City[numRows][numCols];
        this.stations = new HashMap<>();
    }

    public City[][] getCities() { return cities; }
    public void setCities(City[][] cities) { this.cities = cities; } // Setter za Jackson

    public Map<String, Station> getAllStations() { return stations; }
    public void setStations(Map<String, Station> stations) { this.stations = stations; } // Setter za Jackson (možda neće biti direktno popunjen)

    public int getNumRows() { return numRows; }
    public void setNumRows(int numRows) { this.numRows = numRows; } // Setter za Jackson

    public int getNumCols() { return numCols; }
    public void setNumCols(int numCols) { this.numCols = numCols; } // Setter za Jackson

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
        if (station != null) {
            this.stations.put(station.getId(), station);
        }
    }

    public Station getStation(String stationId) {
        return this.stations.get(stationId);
    }

    @Override
    public String toString() {
        return "TransportMap{" +
                "numRows=" + numRows +
                ", numCols=" + numCols +
                ", totalCities=" + (cities != null && cities.length > 0 ? cities.length * cities[0].length : 0) +
                ", totalStations=" + stations.size() +
                '}';
    }
}