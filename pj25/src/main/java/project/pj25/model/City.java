package project.pj25.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Klasa koja predstavlja grad unutar transportne mape.
 * <p>
 * Svaki grad ima jedinstveni ID, koordinate (x, y) i listu saobraćajnih stanica
 * koje mu pripadaju. Klasa koristi Jackson anotacije
 * (<code>@JsonIdentityInfo</code>) kako bi se osigurala pravilna serijalizacija
 * i deserializacija objekata, sprječavajući duplikate referenci u JSON fajlu.
 * </p>
 *
 * @author bratsale
 * @version 1.0
 */
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class City {
    /**
     * Jedinstveni identifikator grada.
     */
    private int id;
    /**
     * X koordinata grada na mapi.
     */
    private int x;
    /**
     * Y koordinata grada na mapi.
     */
    private int y;
    /**
     * Naziv grada.
     */
    private String name;
    /**
     * Lista saobraćajnih stanica koje pripadaju gradu.
     * @see Station
     */
    private List<Station> stations;

    /**
     * Konstruktor za kreiranje novog objekta grada.
     *
     * @param id Jedinstveni ID grada.
     * @param x X koordinata grada.
     * @param y Y koordinata grada.
     */
    public City(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.name = "G_" + x + "_" + y;
        this.stations = new ArrayList<>();
    }

    /**
     * Prazan konstruktor.
     * <p>
     * Neophodan za rad Jackson biblioteke prilikom deserializacije objekata iz JSON-a.
     * </p>
     */
    public City() {}

    /**
     * Vraća ID grada.
     * @return ID grada.
     */
    public int getId() { return id; }

    /**
     * Vraća X koordinatu grada.
     * @return X koordinata.
     */
    public int getX() { return x; }

    /**
     * Vraća Y koordinatu grada.
     * @return Y koordinata.
     */
    public int getY() { return y; }

    /**
     * Vraća naziv grada.
     * @return Naziv grada.
     */
    public String getName() { return name; }

    /**
     * Postavlja naziv grada.
     * @param name Novi naziv grada.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Vraća listu stanica u gradu.
     * @return Lista {@link Station} objekata.
     */
    public List<Station> getStations() {
        return stations;
    }

    /**
     * Postavlja listu stanica u gradu.
     * <p>
     * Ova metoda se primarno koristi od strane Jackson deserializatora.
     * </p>
     * @param stations Nova lista stanica.
     */
    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    /**
     * Dodaje novu stanicu u listu stanica koje pripadaju gradu.
     *
     * @param station Objekat stanice koji treba dodati.
     */
    public void addStation(Station station) {
        if (this.stations == null) {
            this.stations = new ArrayList<>();
        }
        this.stations.add(station);
    }

    /**
     * Vraća string reprezentaciju objekta grada.
     * @return Formatirani string sa detaljima grada.
     */
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

    /**
     * Poredi ovaj objekat grada sa drugim objektom. Gradovi se smatraju jednakim
     * ako imaju isti ID.
     *
     * @param o Drugi objekat za poređenje.
     * @return {@code true} ako su objekti jednaki, {@code false} inače.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return id == city.id;
    }

    /**
     * Generiše hash kod za objekat grada na osnovu ID-a.
     * @return Hash kod objekta.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}