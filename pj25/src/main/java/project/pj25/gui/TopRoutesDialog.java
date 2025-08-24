package project.pj25.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import project.pj25.data.InvoiceManager;
import project.pj25.algorithm.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

/**
 * <p>Klasa koja predstavlja dijalog prozor za prikaz top lista ruta,
 * rangiranih po različitim kriterijumima (vreme, cena, presedanja).</p>
 *
 * <p>Prozor prikazuje svaku rutu kao zasebnu cjelinu unutar skrolabilnog panela.
 * Svaka ruta je predstavljena sažetkom, detaljnim pregledom segmenata
 * u {@link TableView} i dugmetom za kupovinu karte.</p>
 *
 * @author bratsale
 * @version 1.0
 */
public class TopRoutesDialog extends Stage {

    /** Lista ruta koje treba prikazati. */
    private final List<Path> routes;

    /**
     * Konstruktor za {@code TopRoutesDialog}.
     *
     * @param owner     Glavni prozor aplikacije (vlasnik dijaloga).
     * @param routes    Lista ruta ({@link Path} objekata) za prikaz.
     * @param criterion Kriterijum po kojem su rute rangirane (npr. "time", "price").
     */
    public TopRoutesDialog(Stage owner, List<Path> routes, String criterion) {
        this.routes = routes;
        setTitle("Top rute po: " + formatCriterionName(criterion));
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        VBox rootContent = new VBox(20);
        rootContent.setPadding(new Insets(20));

        if (routes.isEmpty()) {
            Label noRoutesLabel = new Label("Nema pronađenih dodatnih ruta za zadati kriterijum.");
            noRoutesLabel.setStyle("-fx-font-size: 1.2em; -fx-text-fill: gray;");
            rootContent.getChildren().add(noRoutesLabel);
            Scene scene = new Scene(rootContent, 400, 150);
            setScene(scene);
        } else {
            for (int i = 0; i < routes.size(); i++) {
                Path path = routes.get(i);
                VBox routeDisplayBox = new VBox(10);
                routeDisplayBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: #f9f9f9; -fx-border-radius: 5;");
                routeDisplayBox.setMaxWidth(Double.MAX_VALUE);

                Label summaryLabel = new Label(
                        String.format("Ruta %d: Vrijeme: %s, Cijena: %.2f KM, Presjedanja: %d",
                                i + 1,
                                formatDuration(path.getTotalTravelTime()),
                                path.getTotalCost(),
                                path.getTransfers()));
                summaryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 1.1em; -fx-text-fill: #333;");

                TableView<RouteSegment> segmentsTable = new TableView<>();
                segmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                segmentsTable.setPlaceholder(new Label("Nema segmenata za prikaz."));

                // Definisanje kolona
                TableColumn<RouteSegment, String> typeCol = new TableColumn<>("Tip");
                typeCol.setCellValueFactory(new PropertyValueFactory<>("departureType"));

                TableColumn<RouteSegment, String> fromCol = new TableColumn<>("Od (stanica)");
                fromCol.setCellValueFactory(new PropertyValueFactory<>("departureStationId"));

                TableColumn<RouteSegment, String> fromCityCol = new TableColumn<>("Od (grad)");
                fromCityCol.setCellValueFactory(new PropertyValueFactory<>("departureStationCityName"));

                TableColumn<RouteSegment, String> toCol = new TableColumn<>("Do (stanica)");
                toCol.setCellValueFactory(new PropertyValueFactory<>("arrivalStationId"));

                TableColumn<RouteSegment, String> toCityCol = new TableColumn<>("Do (grad)");
                toCityCol.setCellValueFactory(new PropertyValueFactory<>("arrivalStationCityName"));

                TableColumn<RouteSegment, LocalTime> depTimeCol = new TableColumn<>("Polazak");
                depTimeCol.setCellValueFactory(new PropertyValueFactory<>("actualDepartureTime"));

                TableColumn<RouteSegment, LocalTime> arrTimeCol = new TableColumn<>("Dolazak");
                arrTimeCol.setCellValueFactory(new PropertyValueFactory<>("actualArrivalTime"));

                TableColumn<RouteSegment, Double> priceCol = new TableColumn<>("Cijena");
                priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

                segmentsTable.getColumns().addAll(typeCol, fromCol, fromCityCol, toCol, toCityCol, depTimeCol, arrTimeCol, priceCol);
                ObservableList<RouteSegment> segments = FXCollections.observableArrayList(path.getSegments());
                segmentsTable.setItems(segments);
                segmentsTable.setPrefHeight(Math.min(segments.size() * 25 + 30, 250));
                segmentsTable.setMaxHeight(250);

                Button buyButton = new Button("Kupi kartu za rutu " + (i + 1));
                buyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 1.0em;");
                buyButton.setOnAction(e -> {
                    String startCityName = path.getSegments().get(0).getDepartureStationCityName();
                    String endCityName = path.getSegments().get(path.getSegments().size() - 1).getArrivalStationCityName();
                    InvoiceManager.generateInvoice(path, startCityName, endCityName);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Kupovina karte");
                    alert.setHeaderText("Karta uspješno kupljena!");
                    alert.setContentText("Račun za rutu je generisan i sačuvan u folderu 'racuni'.");
                    alert.showAndWait();
                });
                buyButton.setMaxWidth(Double.MAX_VALUE);

                routeDisplayBox.getChildren().addAll(summaryLabel, segmentsTable, buyButton);
                rootContent.getChildren().add(routeDisplayBox);
            }

            ScrollPane scrollPane = new ScrollPane(rootContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setPadding(new Insets(0));

            Scene scene = new Scene(scrollPane, 850, 700);
            setScene(scene);
        }
    }

    /**
     * Pomoćna metoda za formatiranje trajanja putovanja.
     *
     * @param duration Trajanje putovanja kao {@link Duration}.
     * @return Formatirani string u formatu "Xč Ymin".
     */
    private String formatDuration(Duration duration) {
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%dč %dmin", hours, minutes);
    }

    /**
     * Pomoćna metoda za formatiranje naziva kriterijuma za prikaz.
     *
     * @param criterion Naziv kriterijuma (npr. "time").
     * @return Formatirani string za prikaz u naslovu.
     */
    private String formatCriterionName(String criterion) {
        if (criterion == null) return "N/A";
        switch (criterion.toLowerCase()) {
            case "time": return "Najkraće vrijeme putovanja";
            case "price": return "Najniža cijena";
            case "transfers": return "Najmanji broj presjedanja";
            default: return criterion;
        }
    }
}