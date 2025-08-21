package project.pj25.data;

import project.pj25.algorithm.Path;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Klasa za upravljanje računima i statistikama prodaje.
 *
 * <p>
 * {@code InvoiceManager} je odgovoran za generisanje i čuvanje računa u
 * tekstualnom formatu. Takođe pruža funkcionalnost za analizu podataka
 * iz sačuvanih računa, kao što je izračunavanje ukupnog broja prodatih karata
 * i ukupnog prihoda.
 * </p>
 *
 * @author Tvoje Ime
 * @version 1.0
 */
public class InvoiceManager {

    /**
     * Naziv foldera u koji se čuvaju generisani računi.
     */
    private static final String INVOICES_FOLDER = "racuni";

    /**
     * Generiše i čuva račun za datu putanju.
     * <p>
     * Kreira tekstualni fajl u folderu "racuni" sa jedinstvenim imenom,
     * koji sadrži detalje o putovanju, ukupnoj cijeni, vremenu i broju presjedanja.
     * </p>
     *
     * @param path          Objekat {@link Path} koji predstavlja kupljenu rutu.
     * @param startCityName Naziv grada polaska.
     * @param endCityName   Naziv grada dolaska.
     */
    public static void generateInvoice(Path path, String startCityName, String endCityName) {
        File folder = new File(INVOICES_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("%s/%s_racun.txt", INVOICES_FOLDER, timestamp);

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("--- Račun za kartu ---\n");
            writer.write("Datum i vrijeme kupovine: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")) + "\n");
            writer.write("Relacija: " + startCityName + " -> " + endCityName + "\n");
            writer.write("-----------------------\n");
            writer.write("Ukupna cijena: " + String.format("%.2f KM", path.getTotalCost()) + "\n");
            writer.write("Ukupno vrijeme putovanja: " + formatDuration(path.getTotalTravelTime()) + "\n");
            writer.write("Broj presjedanja: " + path.getTransfers() + "\n");
            writer.write("-----------------------\n");
            writer.write("Detalji rute:\n");
            writer.write(path.toString());

            System.out.println("Račun uspješno generisan: " + filename);

        } catch (IOException e) {
            System.err.println("Greška pri generisanju računa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Pomoćna metoda za formatiranje trajanja putovanja.
     *
     * @param duration Trajanje putovanja kao {@link java.time.Duration}.
     * @return Formatirani string u formatu "Xč Ymin".
     */
    private static String formatDuration(java.time.Duration duration) {
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%dč %dmin", hours, minutes);
    }

    /**
     * Učitava i sumira podatke o prodaji (broj karata i ukupan prihod)
     * iz svih sačuvanih računa.
     * <p>
     * Metoda skenira sve fajlove u folderu za račune i parsira liniju
     * koja sadrži ukupnu cijenu kako bi izračunala ukupan prihod i ukupan
     * broj prodatih karata.
     * </p>
     *
     * @return Objekat {@link SalesData} sa sumarnim podacima o prodaji.
     */
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
                        if (line.contains("Ukupna cijena:")) {
                            String priceStr = line.replace("Ukupna cijena:", "").trim().replace("KM", "").trim();
                            totalRevenue += Double.parseDouble(priceStr);
                            totalTickets++;
                            break;
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