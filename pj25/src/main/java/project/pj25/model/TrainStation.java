package project.pj25.model;

/**
 * Klasa koja predstavlja željezničku stanicu.
 *
 * <p>Ova klasa nasljeđuje sva svojstva i ponašanje od apstraktne klase {@link Station}.
 * Njena uloga je da pruži specifičnu implementaciju stanice za vozove, što je ključno
 * za ispravnu serijalizaciju i deserializaciju pomoću Jackson biblioteke.
 * Jackson prepoznaje ovaj tip stanice na osnovu polja 'type' sa vrijednošću "voz".</p>
 *
 * @author bratsale
 * @version 1.0
 * @see Station
 * @see BusStation
 */
public class TrainStation extends Station {

    /**
     * Konstruktor za kreiranje objekta željezničke stanice sa zadanim ID-om i gradom.
     * Koristi ga {@link project.pj25.data.TransportDataGenerator} za generisanje
     * podataka o transportnoj mapi.
     *
     * @param id Jedinstveni ID stanice (npr. 'Z_1_1').
     * @param city Grad kojem stanica pripada.
     */
    public TrainStation(String id, City city) {
        super(id, city, "voz");
    }

    /**
     * Prazan konstruktor.
     * Neophodan je za ispravan rad Jackson biblioteke prilikom deserializacije
     * objekata iz JSON-a.
     */
    public TrainStation() {
        super();
        this.type = "voz";
    }
}