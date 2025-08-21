package project.pj25.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.Duration;

/**
 * <p>Prilagođeni Jackson deserializator za konvertovanje JSON vrijednosti u objekte
 * klase {@link Duration}.</p>
 *
 * <p>Jackson biblioteka po defaultu ne zna kako da deserializuje objekte tipa
 * {@link Duration}, pa ova klasa omogućava da se {@code double} vrijednosti
 * iz JSON-a, koje predstavljaju trajanje u sekundama, pravilno konvertuju
 * u {@link Duration} objekte.</p>
 *
 * @author Tvoje Ime
 * @version 1.0
 * @see LocalTimeDeserializer
 */
public class DurationDeserializer extends JsonDeserializer<Duration> {

    /**
     * Deserijalizuje {@code double} vrijednost iz JSON-a u objekat {@link Duration}.
     *
     * @param p Kontekst parsiranja JSON-a.
     * @param ctxt Kontekst deserializacije.
     * @return {@link Duration} objekat kreiran na osnovu broja sekundi.
     * @throws IOException Ako dođe do greške pri čitanju JSON-a.
     */
    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // JSON sadrži double koji predstavlja sekunde
        double seconds = p.getDoubleValue();
        return Duration.ofSeconds((long) seconds);
    }
}