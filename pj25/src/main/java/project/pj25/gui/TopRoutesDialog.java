package project.pj25.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory; // Potrebno za TableView
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import project.pj25.model.*;// Proveri paket za Path i RouteSegment
import project.pj25.algorithm.*;

import java.time.Duration;
import java.time.LocalTime; // Potrebno za kolone vremena
import java.util.List;

public class TopRoutesDialog extends Stage {

    private final List<Path> routes;

    public TopRoutesDialog(Stage owner, List<Path> routes, String criterion) {
        this.routes = routes;
        setTitle("Top ruta po: " + formatCriterionName(criterion));
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        // Glavni VBox koji će držati sve prikaze pojedinačnih ruta
        VBox rootContent = new VBox(20); // Povećaj razmak između ruta
        rootContent.setPadding(new Insets(20));

        if (routes.isEmpty()) {
            Label noRoutesLabel = new Label("Nema pronađenih dodatnih ruta za zadati kriterijum.");
            noRoutesLabel.setStyle("-fx-font-size: 1.2em; -fx-text-fill: gray;");
            rootContent.getChildren().add(noRoutesLabel);
            // Postavi jednostavniju scenu ako nema ruta
            Scene scene = new Scene(rootContent, 400, 150);
            setScene(scene);
        } else {
            // Kreiramo VBox za svaku rutu, koji sadrži labelu sa sažetkom, TableView i dugme
            for (int i = 0; i < routes.size(); i++) {
                Path path = routes.get(i);
                VBox routeDisplayBox = new VBox(10); // Razmak unutar jednog prikaza rute
                routeDisplayBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10; -fx-background-color: #f9f9f9; -fx-border-radius: 5;");
                routeDisplayBox.setMaxWidth(Double.MAX_VALUE);

                // Sažetak rute
                Label summaryLabel = new Label(
                        String.format("Ruta %d: Vreme: %s, Cena: %.2f KM, Presedanja: %d",
                                i + 1, // Numeracija rute
                                formatDuration(path.getTotalTravelTime()),
                                path.getTotalCost(),
                                path.getTransfers()));
                summaryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 1.1em; -fx-text-fill: #333;");

                // TableView za segmente ove rute
                TableView<RouteSegment> segmentsTable = new TableView<>();
                segmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                segmentsTable.setPlaceholder(new Label("Nema segmenata za prikaz."));

                // Definisanje kolona za segmente (isto kao u TransportApp-u)
                TableColumn<RouteSegment, String> typeCol = new TableColumn<>("Tip");
                typeCol.setCellValueFactory(new PropertyValueFactory<>("departureType"));
                typeCol.setPrefWidth(50); // Možete prilagoditi širinu

                TableColumn<RouteSegment, String> fromCol = new TableColumn<>("Od (stanica)");
                fromCol.setCellValueFactory(new PropertyValueFactory<>("departureStationId"));
                fromCol.setPrefWidth(80);

                TableColumn<RouteSegment, String> fromCityCol = new TableColumn<>("Od (grad)");
                fromCityCol.setCellValueFactory(new PropertyValueFactory<>("departureStationCityName"));
                fromCityCol.setPrefWidth(80);

                TableColumn<RouteSegment, String> toCol = new TableColumn<>("Do (stanica)");
                toCol.setCellValueFactory(new PropertyValueFactory<>("arrivalStationId"));
                toCol.setPrefWidth(80);

                TableColumn<RouteSegment, String> toCityCol = new TableColumn<>("Do (grad)");
                toCityCol.setCellValueFactory(new PropertyValueFactory<>("arrivalStationCityName"));
                toCityCol.setPrefWidth(80);

                TableColumn<RouteSegment, LocalTime> depTimeCol = new TableColumn<>("Polazak");
                depTimeCol.setCellValueFactory(new PropertyValueFactory<>("actualDepartureTime"));
                depTimeCol.setPrefWidth(70);

                TableColumn<RouteSegment, LocalTime> arrTimeCol = new TableColumn<>("Dolazak");
                arrTimeCol.setCellValueFactory(new PropertyValueFactory<>("actualArrivalTime"));
                arrTimeCol.setPrefWidth(70);

                TableColumn<RouteSegment, Double> priceCol = new TableColumn<>("Cena");
                priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
                priceCol.setPrefWidth(60);

                segmentsTable.getColumns().addAll(typeCol, fromCol, fromCityCol, toCol, toCityCol, depTimeCol, arrTimeCol, priceCol);

                // Postavi podatke za tabelu
                ObservableList<RouteSegment> segments = FXCollections.observableArrayList(path.getSegments());
                segmentsTable.setItems(segments);
                // Dinamička visina tabele bazirana na broju segmenata, ali sa maksimalnim ograničenjem
                segmentsTable.setPrefHeight(Math.min(segments.size() * 25 + 30, 250)); // Visina reda ~25, dodaj za zaglavlje
                segmentsTable.setMaxHeight(250); // Maksimalna visina tabele

                // Dugme za kupovinu za ovu specifičnu rutu
                Button buyButton = new Button("Kupi kartu za rutu " + (i + 1));
                buyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 1.0em;");
                buyButton.setOnAction(e -> {
                    System.out.println("Kupujem kartu za rutu: " + path.toString());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Kupovina karte");
                    alert.setHeaderText("Karta uspešno kupljena!");
                    alert.setContentText("Račun za rutu je generisan. Detalji:\n" + summaryLabel.getText());
                    alert.showAndWait();
                    // Ovde pozvati pravu logiku za generisanje računa
                });
                buyButton.setMaxWidth(Double.MAX_VALUE); // Da se dugme raširi

                routeDisplayBox.getChildren().addAll(summaryLabel, segmentsTable, buyButton);
                rootContent.getChildren().add(routeDisplayBox);
            }

            // Stavi glavni sadržaj unutar ScrollPane-a da bi bio skrolabilan ako ima mnogo ruta
            ScrollPane scrollPane = new ScrollPane(rootContent);
            scrollPane.setFitToWidth(true); // Omogući da se sadržaj širi po širini ScrollPane-a
            scrollPane.setPadding(new Insets(0)); // Nema potrebe za dodatnim paddingom ovde, VBox već ima

            Scene scene = new Scene(scrollPane, 850, 700); // Povećaj dimenzije prozora da stane više
            setScene(scene);
        }
    }

    // Pomoćna metoda za formatiranje Duration
    private String formatDuration(Duration duration) {
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%dč %dmin", hours, minutes);
    }

    // Pomoćna metoda za formatiranje naziva kriterijuma za prikaz u naslovu prozora
    private String formatCriterionName(String criterion) {
        if (criterion == null) return "N/A";
        switch (criterion.toLowerCase()) {
            case "time": return "Najkraće vreme putovanja";
            case "price": return "Najniža cena";
            case "transfers": return "Najmanji broj presedanja";
            default: return criterion;
        }
    }
}