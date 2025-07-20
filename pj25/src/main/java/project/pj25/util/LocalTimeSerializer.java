package project.pj25.util; // Prilagodi paketu gdje su ti util klase

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalTime;

public class LocalTimeSerializer extends JsonSerializer<LocalTime> {
    @Override
    public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeStartArray(); // Počinje JSON niz [
            gen.writeNumber(value.getHour()); // Sat
            gen.writeNumber(value.getMinute()); // Minuta
            gen.writeEndArray(); // Završava JSON niz ]
        }
    }
}