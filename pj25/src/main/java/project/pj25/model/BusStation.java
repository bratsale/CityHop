package project.pj25.model;

/**
 * Klasa koja predstavlja autobusku stanicu.
 *
 * <p>Ova klasa nasljeđuje sva svojstva i ponašanje od apstraktne klase {@link Station}.
 * Njena primarna uloga je da definiše specifičan tip stanice za potrebe serijalizacije
 * (Jackson) i logike unutar aplikacije. Po polju 'type' Jackson prepoznaje
 * koji konkretan objekat treba da kreira tokom učitavanja podataka.</p>
 *
 * @author bratsale
 * @version 1.0
 * @see Station
 * @see TrainStation
 */
public class BusStation extends Station {

    /**
     * Konstruktor za kreiranje objekta autobuske stanice sa zadanim ID-om i gradom.
     * Koristi ga TransportDataGenerator prilikom generisanja transportne mape.
     *
     * @param id Jedinstveni ID stanice (npr. 'A_1_1').
     * @param city Grad kojem stanica pripada.
     */
    public BusStation(String id, City city) {
        super(id, city, "autobus");
    }

    /**
     * Prazan konstruktor.
     * Koristi ga Jackson za deserializaciju objekta iz JSON-a, nakon čega
     * popunjava polja.
     */
    public BusStation() {
        super();
        this.type = "autobus";
    }
}