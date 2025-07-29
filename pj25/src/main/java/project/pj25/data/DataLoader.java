package project.pj25.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature; // Dodato
import com.fasterxml.jackson.databind.module.SimpleModule; // Dodato
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Dodato

import project.pj25.model.*; // Import svih neophodnih model klasa
import project.pj25.util.DurationDeserializer; // Dodato
import project.pj25.util.LocalTimeDeserializer; // Dodato


import java.io.File;
import java.io.IOException;
import java.time.Duration; // Ne treba direktno ako se Jackson brine
import java.time.LocalTime; // Ne treba direktno ako se Jackson brine
// import java.util.ArrayList; // Ne treba direktno ako se Jackson brine
// import java.util.List; // Ne treba direktno ako se Jackson brine
// import java.util.Map; // Ne treba direktno ako se Jackson brine

public class DataLoader {

    public static TransportMap loadTransportData(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();

        // REGISTRUJ ISTE MODULE KAO U DATA GENERATORU
        objectMapper.registerModule(new JavaTimeModule());

        SimpleModule customModule = new SimpleModule();
        customModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer()); // KORISTI DESERIALIZERE OVDE
        customModule.addDeserializer(Duration.class, new DurationDeserializer());  // KORISTI DESERIALIZERE OVDE
        objectMapper.registerModule(customModule);

        // Opcionalno, ali korisno za debugging i čitanje JSON-a
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            File file = new File(filePath);
            // Jackson će SVE učitati direktno u TransportMap objekat,
            // uključujući sve gradove, stanice i polaske,
            // zahvaljujući @JsonIdentityInfo i pravilnim strukturama klasa.
            TransportMap transportMap = objectMapper.readValue(file, TransportMap.class);

            System.out.println("Podaci uspešno učitani iz " + filePath);
            System.out.println("Učitana transportna mapa: " + transportMap.toString());
            return transportMap;

        } catch (IOException e) {
            System.err.println("Greška prilikom čitanja JSON fajla: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String filePath = "transport_data.json";

        TransportMap transportMap = loadTransportData(filePath);

        if (transportMap != null) {
            System.out.println("Transportna mapa uspešno učitana.");
            int totalDepartures = 0;
            if (transportMap.getAllStations() != null) {
                for (Station station : transportMap.getAllStations().values()) {
                    totalDepartures += station.getDepartures().size();
                }
            }
            System.out.println("Ukupan broj polazaka u mapi: " + totalDepartures);

            // --- Dodatne provere učitanih podataka (primera radi) ---
            System.out.println("\n--- Detaljna provera učitanih podataka (prvih nekoliko) ---");

            // Učitavanje i provera grada na (0,0)
            City city00 = transportMap.getCity(0, 0); // Pretpostavka da getCity radi na osnovu x,y
            if (city00 != null) {
                System.out.println("Grad na (0,0): " + city00.getName() + " (ID: " + city00.getId() + ")");
                System.out.println("Broj stanica u gradu (0,0): " + city00.getStations().size());
                if (!city00.getStations().isEmpty()) {
                    System.out.println("Prva stanica u gradu (0,0): " + city00.getStations().get(0).getId());
                    // Provera da li referenca nazad na grad radi
                    System.out.println("Grad prve stanice: " + city00.getStations().get(0).getCity().getName());
                }

                // Provera polazaka za jednu od stanica u gradu (0,0)
                // Moramo da znamo ID, npr. "A_0_0_0"
                Station sampleBusStation = transportMap.getStation("A_0_0_0");
                if (sampleBusStation != null) {
                    System.out.println("Provera stanice A_0_0_0:");
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
            System.out.println("Neuspešno učitavanje transportne mape.");
        }
    }
}