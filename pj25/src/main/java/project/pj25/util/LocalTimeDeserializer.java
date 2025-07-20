package project.pj25.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.LocalTime;

public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
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