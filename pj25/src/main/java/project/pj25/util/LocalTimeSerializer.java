package project.pj25.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalTime;

/**
 * <p>Prilagođeni Jackson serijalizator za konvertovanje objekata tipa {@link LocalTime}
 * u JSON niz.</p>
 *
 * <p>Ova klasa serijalizuje {@link LocalTime} objekte u formatu JSON niza
 * {@code [sat, minut]}, na primjer {@code [10, 30]}. Ovaj format je specifično
 * definisan za potrebe projekta, i omogućava ispravno serijalizovanje i
 * deserializovanje podataka o vremenu.</p>
 *
 * @author Tvoje Ime
 * @version 1.0
 * @see LocalTimeDeserializer
 */
public class LocalTimeSerializer extends JsonSerializer<LocalTime> {

    /**
     * Serijalizuje objekat {@link LocalTime} u JSON niz.
     * <p>
     * Metoda serijalizuje sat i minut u formatu {@code [sat, minut]}.
     * </p>
     *
     * @param value Objekat {@link LocalTime} koji se serijalizuje.
     * @param gen JSON generator.
     * @param serializers Kontekst serijalizacije.
     * @throws IOException Ako dođe do greške prilikom pisanja u JSON fajl.
     */
    @Override
    public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeStartArray();
            gen.writeNumber(value.getHour());
            gen.writeNumber(value.getMinute());
            gen.writeEndArray();
        }
    }
}