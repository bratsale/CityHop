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
 * i <code>{@link Duration}</code>.</p>
 *
 * @author Tvoje Ime
 * @version 1.0
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

    /**
     * Glavna metoda za testiranje učitavanja podataka.
     * <p>
     * Poziva metodu <code>loadTransportData</code> i prikazuje osnovne informacije
     * o učitanoj transportnoj mapi kako bi se potvrdilo da je proces učitavanja
     * bio uspješan.
     * </p>
     *
     * @param args Argumenti komandne linije (ne koriste se).
     */
    public static void main(String[] args) {
        String filePath = "transport_data.json";
        TransportMap transportMap = loadTransportData(filePath);

        if (transportMap != null) {
            System.out.println("Transportna mapa uspješno učitana.");
            int totalDepartures = 0;
            if (transportMap.getAllStations() != null) {
                for (Station station : transportMap.getAllStations().values()) {
                    totalDepartures += station.getDepartures().size();
                }
            }
            System.out.println("Ukupan broj polazaka u mapi: " + totalDepartures);

            System.out.println("\n--- Detaljna provjera učitanih podataka (prvih nekoliko) ---");
            City city00 = transportMap.getCity(0, 0);
            if (city00 != null) {
                System.out.println("Grad na (0,0): " + city00.getName() + " (ID: " + city00.getId() + ")");
                System.out.println("Broj stanica u gradu (0,0): " + city00.getStations().size());
                if (!city00.getStations().isEmpty()) {
                    System.out.println("Prva stanica u gradu (0,0): " + city00.getStations().get(0).getId());
                    System.out.println("Grad prve stanice: " + city00.getStations().get(0).getCity().getName());
                }

                Station sampleBusStation = transportMap.getStation("A_0_0_0");
                if (sampleBusStation != null) {
                    System.out.println("Provjera stanice A_0_0_0:");
                    System.out.println("  ID: " + sampleBusStation.getId());
                    System.out.println("  Tip: " + sampleBusStation.getType());
                    System.out.println("  Grad: " + (sampleBusStation.getCity() != null ? sampleBusStation.getCity().getName() : "N/A"));
                    System.out.println("  Broj polazaka: " + sampleBusStation.getDepartures().size());
                    if (!sampleBusStation.getDepartures().isEmpty()) {
                        System.out.println("  Prvi polazak sa A_0_0_0: " + sampleBusStation.getDepartures().get(0));
                    }
                } else {
                    System.out.println("Stanica A_0_0_0 nije pronađena.");
                }

            } else {
                System.out.println("TransportMap ili grad na (0,0) je null nakon učitavanja.");
            }

        } else {
            System.out.println("Neuspješno učitavanje transportne mape.");
        }
    }
}