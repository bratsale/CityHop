package project.pj25.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Duration;

/**
 * <p>Prilagođeni Jackson serijalizator za konvertovanje objekata tipa {@link Duration}
 * u JSON vrijednosti.</p>
 *
 * <p>Jackson biblioteka po defaultu ne zna kako da serijalizuje {@link Duration} objekte.
 * Ova klasa omogućava da se {@link Duration} vrijednost pravilno konvertuje u
 * numeričku vrijednost (ukupan broj sekundi) u JSON fajlu,
 * što olakšava skladištenje i prenos podataka o vremenskim intervalima.</p>
 *
 * @author Tvoje Ime
 * @version 1.0
 * @see DurationDeserializer
 */
public class DurationSerializer extends JsonSerializer<Duration> {

    /**
     * Serijalizuje objekat {@link Duration} u JSON format.
     * <p>
     * Vrijednost se serijalizuje kao ukupan broj sekundi.
     * </p>
     *
     * @param value Objekat {@link Duration} koji se serijalizuje.
     * @param gen JSON generator.
     * @param serializers Kontekst serijalizacije.
     * @throws IOException Ako dođe do greške prilikom pisanja u JSON fajl.
     */
    @Override
    public void serialize(Duration value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            // Serijalizuj Duration kao ukupan broj sekundi
            gen.writeNumber((double) value.getSeconds());
        }
    }
}