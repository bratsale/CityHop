package project.pj25.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.LocalTime;

/**
 * <p>Prilagođeni Jackson deserializator za konvertovanje JSON niza u objekte
 * klase {@link LocalTime}.</p>
 *
 * <p>Jackson biblioteka po defaultu ne podržava deserializaciju {@link LocalTime}
 * objekata iz formata koji se koristi u ovom projektu (niz sa [sat, minut]).
 * Ova klasa preuzima JSON niz sa dva elementa (sat i minut) i kreira
 * odgovarajući {@link LocalTime} objekat, omogućavajući ispravno učitavanje
 * podataka o vremenu iz JSON fajla.</p>
 *
 * @author Tvoje Ime
 * @version 1.0
 * @see DurationDeserializer
 */
public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {

    /**
     * Deserijalizuje JSON niz u objekat {@link LocalTime}.
     *
     * <p>Metoda očekuje JSON niz u formatu {@code [sat, minut]}.
     * Na primjer, {@code [10, 30]} će biti konvertovano u {@link LocalTime} objekat
     * koji predstavlja 10:30.</p>
     *
     * @param p Kontekst parsiranja JSON-a.
     * @param ctxt Kontekst deserializacije.
     * @return {@link LocalTime} objekat kreiran na osnovu vrijednosti sata i minuta.
     * @throws IOException Ako format JSON čvora nije očekivani niz.
     */
    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isArray() && node.size() == 2) {
            int hour = node.get(0).asInt();
            int minute = node.get(1).asInt();
            return LocalTime.of(hour, minute);
        }
        throw new IOException("Expected an array of [hour, minute] for LocalTime, but got: " + node);
    }
}