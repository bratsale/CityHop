package project.pj25.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import project.pj25.model.*; // Import svih neophodnih model klasa

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataLoader {

    public static TransportMap loadTransportData(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        TransportMap transportMap = null; // Inicijalizovan na null, biće postavljen ispod

        try {
            File file = new File(filePath);
            // Učitavamo JSON u generičku Mapu, kako bismo ručno popunili TransportMap
            Map<String, Object> rawData = objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {});

            // KORAK 1: Preuzimanje osnovnih dimenzija i inicijalizacija TransportMap
            // Sada koristimo stvarne podatke iz JSON-a za kreiranje TransportMap
            int numRows = (Integer) rawData.get("numRows");
            int numCols = (Integer) rawData.get("numCols");
            transportMap = new TransportMap(numRows, numCols); // TransportMap je SADA inicijalizovan

            // KORAK 2: Učitavanje gradova i inicijalizacija stanica (autobuske i železničke)
            // Ove podatke čitamo iz 'cities' dela JSON-a
            List<List<Map<String, Object>>> citiesRaw = (List<List<Map<String, Object>>>) rawData.get("cities");
            if (citiesRaw != null) {
                for (int r = 0; r < numRows; r++) {
                    for (int c = 0; c < numCols; c++) {
                        Map<String, Object> cityData = citiesRaw.get(r).get(c);
                        int id = (Integer) cityData.get("id");
                        int x = (Integer) cityData.get("x");
                        int y = (Integer) cityData.get("y");
                        String name = (String) cityData.get("name");

                        City city = new City(id, x, y);
                        transportMap.addCity(x, y, city);

                        // Inicijalizacija i dodavanje stanica za svaki grad
                        // OVO JE KRITIČNO: Stanice se kreiraju i dodaju u transportMap.stations mapu
                        // PRE nego što pokušamo da im dodamo polaske.
                        BusStation bs = new BusStation(city);
                        TrainStation ts = new TrainStation(city);
                        transportMap.addStation(bs);
                        transportMap.addStation(ts);
                    }
                }
                System.out.println("Gradovi i osnovne stanice inicijalizovani na osnovu JSON podataka.");
            } else {
                System.err.println("Upozorenje: Nije pronađen 'cities' deo u JSON-u. TransportMap neće biti potpuno inicijalizovan.");
            }


            // KORAK 3: Učitavanje svih polazaka i dodavanje postojećim stanicama
            // Sada kada je transportMap inicijalizovan i sadrži sve stanice, možemo dodavati polaske
            Map<String, Map<String, Object>> allStationsRaw = (Map<String, Map<String, Object>>) rawData.get("allStations");

            if (allStationsRaw != null) {
                int departuresCount = 0;
                for (Map.Entry<String, Map<String, Object>> entry : allStationsRaw.entrySet()) {
                    String stationId = entry.getKey();
                    Map<String, Object> stationData = entry.getValue();

                    List<Map<String, Object>> departuresListRaw = (List<Map<String, Object>>) stationData.get("departures");

                    if (departuresListRaw != null) {
                        // Sada bi transportMap.getStation(stationId) trebalo da vrati ispravnu stanicu
                        Station currentStation = transportMap.getStation(stationId);

                        if (currentStation != null) {
                            for (Map<String, Object> departureData : departuresListRaw) {
                                // Deserijalizacija Departure objekta
                                String type = (String) departureData.get("type");
                                String depStationId = (String) departureData.get("departureStationId");
                                String arrStationId = (String) departureData.get("arrivalStationId");

                                List<Integer> depTimeList = (List<Integer>) departureData.get("departureTime");
                                LocalTime departureTime = LocalTime.of(depTimeList.get(0), depTimeList.get(1));

                                List<Integer> arrTimeList = (List<Integer>) departureData.get("arrivalTime");
                                LocalTime arrivalTime = LocalTime.of(arrTimeList.get(0), arrTimeList.get(1));

                                double price = ((Number) departureData.get("price")).doubleValue();

                                Duration minTransferTime = Duration.ZERO;
                                Object minTransferObj = departureData.get("minTransferTime");
                                if (minTransferObj instanceof Number) {
                                    minTransferTime = Duration.ofMinutes(((Number) minTransferObj).longValue());
                                }

                                Departure departure = new Departure(
                                        type, depStationId, arrStationId,
                                        departureTime, arrivalTime, price, minTransferTime
                                );
                                currentStation.addDeparture(departure);
                                departuresCount++;
                                // Ostavio sam System.out.println() zakomentarisano zbog velike količine ispisa.
                                // System.out.println("Processing departure: " + departure.getDepartureStationId() + " -> " + departure.getArrivalStationId() + " (" + departure.getType() + ")");
                            }
                        } else {
                            // Ovo upozorenje bi trebalo da bude vrlo retko ili nikada, ako su stanice ispravno inicijalizovane
                            System.err.println("Upozorenje: Stanica sa ID-em '" + stationId + "' nije pronađena u transportnoj mapi prilikom dodavanja polazaka.");
                        }
                    }
                }
                System.out.println("Ukupno učitano polazaka: " + departuresCount);
            } else {
                System.err.println("Upozorenje: Nije pronađen 'allStations' deo u JSON-u.");
            }

        } catch (IOException e) {
            System.err.println("Greška prilikom čitanja JSON fajla: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassCastException e) {
            System.err.println("Neočekivana greška prilikom parsiranja podataka: " + e.getMessage());
            System.err.println("Problematična klasa: " + e.getClass().getName());
            System.err.println("Poruka: " + e.getMessage());
            e.printStackTrace();
        }
        return transportMap;
    }

    public static void main(String[] args) {
        String filePath = "transport_data.json";

        TransportMap transportMap = loadTransportData(filePath);

        if (transportMap != null) {
            System.out.println("Transportna mapa uspešno učitana: " + transportMap.toString());
            int totalDepartures = 0;
            if (transportMap.getAllStations() != null) {
                for (Station station : transportMap.getAllStations().values()) {
                    totalDepartures += station.getDepartures().size();
                }
            }
            System.out.println("Ukupan broj polazaka u mapi: " + totalDepartures);

            // --- Dodatne provere učitanih podataka ---
            System.out.println("\n--- Detaljna provera učitanih podataka ---");

            // 1. Provera grada G_0_0
            City city00 = transportMap.getCity(0, 0);
            if (city00 != null) {
                System.out.println("Grad na (0,0): " + city00);
            } else {
                System.out.println("Grad na (0,0) nije pronađen.");
            }

            // 2. Provera stanica za grad G_0_0
            Station busStation00 = transportMap.getStation("A_0_0");
            Station trainStation00 = transportMap.getStation("Z_0_0");

            if (busStation00 != null) {
                System.out.println("Autobuska stanica A_0_0: " + busStation00.toString());
                System.out.println("Broj polazaka sa A_0_0: " + busStation00.getDepartures().size());
                if (!busStation00.getDepartures().isEmpty()) {
                    System.out.println("Prvi polazak sa A_0_0: " + busStation00.getDepartures().get(0));
                }
            } else {
                System.out.println("Autobuska stanica A_0_0 nije pronađena.");
            }

            if (trainStation00 != null) {
                System.out.println("Železnička stanica Z_0_0: " + trainStation00.toString());
                System.out.println("Broj polazaka sa Z_0_0: " + trainStation00.getDepartures().size());
                if (!trainStation00.getDepartures().isEmpty()) {
                    System.out.println("Prvi polazak sa Z_0_0: " + trainStation00.getDepartures().get(0));
                }
            } else {
                System.out.println("Železnička stanica Z_0_0 nije pronađena.");
            }

            // Provera neke druge stanice, npr. Z_2_2
            Station trainStation22 = transportMap.getStation("Z_2_2");
            if (trainStation22 != null) {
                System.out.println("Železnička stanica Z_2_2: " + trainStation22.toString());
                System.out.println("Broj polazaka sa Z_2_2: " + trainStation22.getDepartures().size());
                if (!trainStation22.getDepartures().isEmpty()) {
                    System.out.println("Prvi polazak sa Z_2_2: " + trainStation22.getDepartures().get(0));
                    System.out.println("Drugi polazak sa Z_2_2: " + trainStation22.getDepartures().get(1));
                    System.out.println("Treći polazak sa Z_2_2: " + trainStation22.getDepartures().get(2));
                }
            } else {
                System.out.println("Železnička stanica Z_2_2 nije pronađena.");
            }

        } else {
            System.out.println("Neuspešno učitavanje transportne mape.");
        }
    }
}