package project.pj25.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import project.pj25.model.*;
import project.pj25.util.DurationDeserializer;
import project.pj25.util.LocalTimeDeserializer;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;

/**
 * <p>Klasa zadužena za učitavanje podataka o transportnoj mapi iz JSON fajla.</p>
 *
 * <p>Koristi Jackson biblioteku za deserializaciju JSON podataka u hijerarhiju
 * Java objekata (<code>{@link TransportMap}</code>, <code>{@link City}</code>,
 * <code>{@link Station}</code>, <code>{@link Departure}</code>). Uključuje
 * prilagođene deserializatore za ispravno rukovanje tipovima <code>{@link LocalTime}</code>
 * i <code>{@link Duration}</code>. Ova klasa sada služi isključivo kao pomoćni alat
 * i ne pokreće se samostalno.</p>
 *
 * @author bratsale
 * @version 1.1
 * @see TransportMap
 * @see project.pj25.util.LocalTimeDeserializer
 * @see project.pj25.util.DurationDeserializer
 */
public class DataLoader {

    /**
     * Učitava podatke o transportnoj mapi iz navedenog JSON fajla.
     * <p>
     * Metoda konfiguriše {@link ObjectMapper} da koristi prilagođene module za
     * deserializaciju objekata vezanih za vrijeme (<code>java.time</code> paket),
     * a zatim učitava cijeli JSON sadržaj u jedan objekat klase
     * <code>{@link TransportMap}</code>.
     * </p>
     *
     * @param filePath Puna putanja do JSON fajla.
     * @return Učitani {@link TransportMap} objekat, ili {@code null} ako dođe
     * do greške prilikom čitanja fajla.
     */

    public static TransportMap loadTransportData(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());
        SimpleModule customModule = new SimpleModule();
        customModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
        customModule.addDeserializer(Duration.class, new DurationDeserializer());
        objectMapper.registerModule(customModule);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            File file = new File(filePath);
            TransportMap transportMap = objectMapper.readValue(file, TransportMap.class);
            System.out.println("Podaci uspješno učitani iz " + filePath);
            System.out.println("Učitana transportna mapa: " + transportMap.toString());
            return transportMap;

        } catch (IOException e) {
            System.err.println("Greška prilikom čitanja JSON fajla: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}