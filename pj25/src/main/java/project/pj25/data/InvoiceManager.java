package project.pj25.data; // Ili project.pj25.util, po tvom izboru

import project.pj25.algorithm.Path;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class InvoiceManager {

    private static final String INVOICES_FOLDER = "racuni";

    // Metoda za generisanje i čuvanje računa
    public static void generateInvoice(Path path, String startCityName, String endCityName) {
        // Obavezno kreiraj folder ako ne postoji
        File folder = new File(INVOICES_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Generiši jedinstveno ime fajla
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("%s/%s_racun.txt", INVOICES_FOLDER, timestamp);

        try (FileWriter writer = new FileWriter(filename)) {
            // Generisanje sadržaja računa
            writer.write("--- Račun za kartu ---\n");
            writer.write("Datum i vreme kupovine: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")) + "\n");
            writer.write("Relacija: " + startCityName + " -> " + endCityName + "\n");
            writer.write("-----------------------\n");
            writer.write("Ukupna cena: " + String.format("%.2f KM", path.getTotalCost()) + "\n");
            writer.write("Ukupno vreme putovanja: " + formatDuration(path.getTotalTravelTime()) + "\n");
            writer.write("Broj presedanja: " + path.getTransfers() + "\n");
            writer.write("-----------------------\n");
            writer.write("Detalji rute:\n");
            writer.write(path.toString()); // Koristimo već postojeću toString() metodu Path klase za detalje

            System.out.println("Račun uspješno generisan: " + filename);

        } catch (IOException e) {
            System.err.println("Greška pri generisanju računa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Pomoćna metoda za formatiranje Duration, ako nije već u Path klasi
    private static String formatDuration(java.time.Duration duration) {
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%dč %dmin", hours, minutes);
    }

    // Metoda za učitavanje ukupnog broja karata i prihoda
    public static SalesData loadSalesData() {
        File folder = new File(INVOICES_FOLDER);
        if (!folder.exists()) {
            return new SalesData(0, 0.0);
        }

        int totalTickets = 0;
        double totalRevenue = 0.0;
        File[] invoiceFiles = folder.listFiles((dir, name) -> name.endsWith("_racun.txt"));

        if (invoiceFiles != null) {
            for (File file : invoiceFiles) {
                try (Scanner scanner = new Scanner(file)) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.contains("Ukupna cena:")) {
                            String priceStr = line.replace("Ukupna cena:", "").trim().replace("KM", "").trim();
                            totalRevenue += Double.parseDouble(priceStr);
                            totalTickets++;
                            break; // Našli smo cenu, prelazimo na sledeći fajl
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Greška pri čitanju računa: " + file.getName() + " - " + e.getMessage());
                }
            }
        }
        return new SalesData(totalTickets, totalRevenue);
    }
}