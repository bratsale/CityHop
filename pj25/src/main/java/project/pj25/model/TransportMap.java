package project.pj25.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Klasa koja predstavlja kompletnu transportnu mapu sistema.
 * <p>
 * Sadrži dvodimenzionalni niz gradova (<code>{@link City}[][]</code>) koji
 * reprezentuje geografski raspored, kao i mapu svih stanica u sistemu radi
 * lakšeg pristupa po ID-u. Ova klasa služi kao centralni model za sve
 * podatke o transportu.
 * </p>
 *
 * @author bratsale
 * @version 1.0
 * @see City
 * @see Station
 */
public class TransportMap {

    /**
     * Dvodimenzionalni niz objekata tipa {@link City} koji predstavlja mrežu gradova.
     */
    private City[][] cities;

    /**
     * Mapa svih stanica u transportnoj mreži, gdje je ključ ID stanice,
     * a vrijednost je objekat stanice.
     */
    private Map<String, Station> stations;

    /**
     * Broj redova u mreži gradova.
     */
    private int numRows;

    /**
     * Broj kolona u mreži gradova.
     */
    private int numCols;

    /**
     * Prazan konstruktor.
     * <p>Inicijalizuje mapu stanica. Koristi ga Jackson za deserializaciju.</p>
     */
    public TransportMap() {
        this.stations = new HashMap<>();
    }

    /**
     * Konstruktor koji kreira mapu sa zadatim dimenzijama.
     *
     * @param numRows Broj redova.
     * @param numCols Broj kolona.
     */
    public TransportMap(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.cities = new City[numRows][numCols];
        this.stations = new HashMap<>();
    }

    // Getteri i setteri

    /**
     * Vraća dvodimenzionalni niz gradova.
     * @return Niz gradova.
     */
    public City[][] getCities() { return cities; }

    /**
     * Vraća mapu svih stanica.
     * @return Mapa stanica.
     */
    public Map<String, Station> getAllStations() { return stations; }

    /**
     * Vraća broj redova u mreži gradova.
     * @return Broj redova.
     */
    public int getNumRows() { return numRows; }

    /**
     * Vraća broj kolona u mreži gradova.
     * @return Broj kolona.
     */
    public int getNumCols() { return numCols; }

    /**
     * Dodaje grad na određene koordinate u mreži.
     *
     * @param x X koordinata grada.
     * @param y Y koordinata grada.
     * @param city Objekat grada.
     */
    public void addCity(int x, int y, City city) {
        if (x >= 0 && x < numRows && y >= 0 && y < numCols) {
            this.cities[x][y] = city;
        } else {
            System.err.println("Error: City coordinates (" + x + ", " + y + ") out of bounds.");
        }
    }

    /**
     * Vraća grad sa zadatih koordinata.
     *
     * @param x X koordinata.
     * @param y Y koordinata.
     * @return Objekat grada ili {@code null} ako koordinate nisu validne.
     */
    public City getCity(int x, int y) {
        if (x >= 0 && x < numRows && y >= 0 && y < numCols) {
            return this.cities[x][y];
        }
        return null;
    }

    /**
     * Dodaje stanicu u globalnu mapu svih stanica.
     *
     * @param station Objekat stanice.
     */
    public void addStation(Station station) {
        if (station != null) {
            this.stations.put(station.getId(), station);
        }
    }

    /**
     * Vraća stanicu na osnovu njenog ID-a.
     *
     * @param stationId ID stanice.
     * @return Objekat stanice ili {@code null} ako stanica nije pronađena.
     */
    public Station getStation(String stationId) {
        return this.stations.get(stationId);
    }

    /**
     * Vraća string reprezentaciju transportne mape.
     * @return Formatirani string sa sumarnim podacima o mapi.
     */
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