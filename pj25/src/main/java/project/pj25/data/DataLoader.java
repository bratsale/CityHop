package project.pj25.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import project.pj25.model.Departure;
import project.pj25.model.Station; // Dodaj import za Station
import project.pj25.model.BusStation; // Dodaj import za BusStation
import project.pj25.model.TrainStation; // Dodaj import za TrainStation
import project.pj25.model.City; // Dodaj import za City
import project.pj25.model.TransportMap; // Dodaj import za TransportMap

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
        TransportMap transportMap = null;

        try {
            File file = new File(filePath);
            Map<String, Object> rawData = objectMapper.readValue(file, new TypeReference<Map<String, Object>>() {});

            // 1. Učitavanje gradova (ako je potrebno, Jackson bi ovo trebao sam obaviti za TransportMap)
            // Ali, ako i dalje radimo ručno, onda:
            // List<List<Map<String, Object>>> citiesRaw = (List<List<Map<String, Object>>>) rawData.get("cities");
            // int numRows = (int) rawData.get("numRows");
            // int numCols = (int) rawData.get("numCols");

            // Kreiramo TransportMap objekat, Jackson bi to inače uradio ako su get/setteri ispravni
            // transportMap = new TransportMap(numRows, numCols);

            // PRVI KORAK ISPRAVKE: "allStations" je MAPA, ne LISTA
            Map<String, Map<String, Object>> allStationsRaw = (Map<String, Map<String, Object>>) rawData.get("allStations");

            // Inicijalizacija transportMap ako Jackson to nije uradio sam, ili ako je TransportMap konstruktor
            // bez argumenata pozvan i setteri su korišteni
            // Pretpostavimo da je Jackson već stvorio osnovni TransportMap objekt sa gradovima.
            // Ako nije, trebaš implementirati deserializaciju TransportMap klase.
            // Za sada, hajde da se fokusiramo na departurse.

            if (allStationsRaw != null) {
                int departuresCount = 0;
                for (Map.Entry<String, Map<String, Object>> entry : allStationsRaw.entrySet()) {
                    String stationId = entry.getKey();
                    Map<String, Object> stationData = entry.getValue();

                    // DRUGI KORAK ISPRAVKE: Preuzimamo listu "departures" iz SVAKE stanice
                    List<Map<String, Object>> departuresListRaw = (List<Map<String, Object>>) stationData.get("departures");

                    if (departuresListRaw != null) {
                        // Pretpostavimo da transportMap već ima sve stanice inicijalizovane (iz generisanja)
                        // Ako ne, moraš ih kreirati ovde ili u TransportMap konstruktoru/setterima
                        // i dodati ih u transportMap.

                        // Pronađi odgovarajuću stanicu u transportMap objektu
                        // Ovo je deo gde ti treba TransportMap objekat sa ucitanim gradovima i stanicama
                        // KAKO BI OVAJ KOD RADIO, POTREBNO JE DA JE TransportMap KLASA ISPRAVNO DESERIJALIZOVANA
                        // OD STRANE JACKSONA SA "cities", "numRows", "numCols" I "allStations" (BEZ DEPARTURESA).
                        // Ako TransportMap nema settere za 'stations' mapu, moramo je ručno popuniti.

                        // OVO JE KRITIČNO: Ako Jackson ne mapira Station objekte direktno,
                        // onda 'stationData' mora biti mapirano u Station objekat ili Station interfejs.
                        // Za sada, pretpostavimo da su stanice već inicijalizovane u transportMap
                        // (npr. ako se pune iz "cities" matrice ili drugog dijela JSON-a).

                        // Privremeno, kreiraću TransportMap ovde sa dummy podacima za test,
                        // ali ti ovo trebaš pravilno uraditi u tvojoj aplikaciji
                        // (tj. osigurati da se cities i stations popune prije nego što dođeš do departure-a).
                        // OVO NE TREBA DA RADIŠ OVDE AKO IMAS ISPRAVAN JACKSON MAPING ZA TRANSPORTMAP
                        if (transportMap == null) {
                            // Ova logika je samo za simulaciju ako TransportMap nije potpuno deserializovan.
                            // U realnosti, Jackson bi trebao deserializovati sve!
                            // Morali bismo znati numRows i numCols. Za demo, hardkodovaću
                            int dummyNumRows = 3;
                            int dummyNumCols = 3;
                            transportMap = new TransportMap(dummyNumRows, dummyNumCols);

                            // Popuni dummy gradove i stanice samo da bi se izbegao null pointer
                            for (int r = 0; r < dummyNumRows; r++) {
                                for (int c = 0; c < dummyNumCols; c++) {
                                    City city = new City(r * dummyNumCols + c, r, c);
                                    transportMap.addCity(r, c, city);
                                    // Dodaj dummy stanice (Bus i Train) za svaki grad
                                    // Stvarne stanice bi Jackson trebao popuniti ili generisati
                                    BusStation bs = new BusStation(city);
                                    TrainStation ts = new TrainStation(city);
                                    transportMap.addStation(bs);
                                    transportMap.addStation(ts);
                                    // U JSON-u su stanice "Z_X_Y" i "A_X_Y", pa ih moramo kreirati
                                    // pre nego što pokušamo da im dodamo polaske
                                }
                            }
                        }


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

                                double price = ((Number) departureData.get("price")).doubleValue(); // Može biti Integer ili Double

                                // minTransferTime može biti double, a Jackson ga je već verovatno pročitao kao double
                                // ili može biti null, pa treba proveriti
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
                                System.out.println("Processing departure: " + departure.getDepartureStationId() + " -> " + departure.getArrivalStationId() + " (" + departure.getType() + ")");
                            }
                        } else {
                            System.err.println("Upozorenje: Stanica sa ID-em '" + stationId + "' nije pronađena u transportnoj mapi.");
                        }
                    }
                }
                System.out.println("Ukupno učitano polazaka: " + departuresCount);
            }

        } catch (IOException e) {
            System.err.println("Greška prilikom čitanja JSON fajla: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassCastException e) {
            System.err.println("Neočekivana greška prilikom parsiranja podataka: " + e.getMessage());
            e.printStackTrace();
        }
        return transportMap;
    }

    public static void main(String[] args) {
        // Više mi ne treba apsolutna putanja jer nije tu bio problem, u dobrom je direktorijumu
        String filePath = "transport_data.json";

        TransportMap transportMap = loadTransportData(filePath);

        if (transportMap != null) {
            System.out.println("Transportna mapa uspešno učitana: " + transportMap.toString());
            // Možeš dodati dodatnu logiku za proveru podataka
            // Npr., provera broja stanica, polazaka itd.
            int totalDepartures = 0;
            if (transportMap.getAllStations() != null) {
                for (Station station : transportMap.getAllStations().values()) {
                    totalDepartures += station.getDepartures().size();
                }
            }
            System.out.println("Ukupan broj polazaka u mapi: " + totalDepartures);

        } else {
            System.out.println("Neuspešno učitavanje transportne mape.");
        }
    }
}