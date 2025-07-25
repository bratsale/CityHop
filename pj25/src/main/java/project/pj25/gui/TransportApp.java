package project.pj25.gui; // PROVERI: ako je tvoj paket project.pj25.gui, onda je OK

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*; // Uključi sve kontrole
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox; // Verovatno ti ne treba ako je samo VBox
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import project.pj25.model.*; // Proveri pakete
import project.pj25.data.*;
import project.pj25.util.*;
import project.pj25.algorithm.*;

import java.time.Duration; // DODAJ OVO
import java.time.LocalTime; // DODAJ OVO
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// importi za grafiku
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TransportApp extends Application {

    private TransportMap transportMap;
    private RouteFinder routeFinder;
    private ComboBox<City> startCityComboBox;
    private ComboBox<City> endCityComboBox;
    private ToggleGroup optimizationCriteriaGroup;
    private Button findRouteButton;
    // private TextArea resultTextArea; // <--- OVO UKLONI!
    private Canvas mapCanvas;
    private GraphRenderer graphRenderer;

    // *** NOVE VARIJABLE ZA TABELARNI PRIKAZ ***
    private TableView<RouteSegment> routeDetailsTable;
    private Label bestRouteSummaryLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CityHop - Pronađi Optimalnu Rutu");

        System.out.println("Učitavam transportne podatke...");
        transportMap = DataLoader.loadTransportData("transport_data.json");

        if (transportMap == null) {
            System.err.println("Greška prilikom učitavanja podataka. Aplikacija se neće pokrenuti.");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Greška pri učitavanju");
            alert.setHeaderText("Nije moguće učitati transportne podatke.");
            alert.setContentText("Molimo proverite da li 'transport_data.json' postoji i da li je ispravan.");
            alert.showAndWait();
            return;
        }
        System.out.println("Podaci uspešno učitani. Ukupan broj gradova: " + (transportMap.getNumRows() * transportMap.getNumCols()));

        this.routeFinder = new RouteFinder(transportMap);

        // ... (KOD ZA COMBOBOXES I RADIOBUTTONS JE ISTI) ...
        startCityComboBox = new ComboBox<>();
        startCityComboBox.setPromptText("Odaberi početni grad");
        populateCityComboBox(startCityComboBox);

        endCityComboBox = new ComboBox<>();
        endCityComboBox.setPromptText("Odaberi odredišni grad");
        populateCityComboBox(endCityComboBox);

        optimizationCriteriaGroup = new ToggleGroup();

        RadioButton timeRadio = new RadioButton("Najkraće vreme putovanja");
        timeRadio.setToggleGroup(optimizationCriteriaGroup);
        timeRadio.setUserData("time");
        timeRadio.setSelected(true);

        RadioButton priceRadio = new RadioButton("Najniža cena");
        priceRadio.setToggleGroup(optimizationCriteriaGroup);
        priceRadio.setUserData("price");

        RadioButton transfersRadio = new RadioButton("Najmanji broj presedanja");
        transfersRadio.setToggleGroup(optimizationCriteriaGroup);
        transfersRadio.setUserData("transfers");

        VBox criteriaBox = new VBox(10, new Label("Kriterijum optimizacije:"), timeRadio, priceRadio, transfersRadio);
        criteriaBox.setPadding(new Insets(10));
        criteriaBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");

        findRouteButton = new Button("Pronađi rutu");
        findRouteButton.setOnAction(e -> findOptimalRoute());

        VBox controlsLayout = new VBox(20);
        controlsLayout.setPadding(new Insets(20));
        controlsLayout.setAlignment(Pos.TOP_LEFT);
        controlsLayout.getChildren().addAll(
                new Label("Početni grad:"), startCityComboBox,
                new Label("Odredišni grad:"), endCityComboBox,
                criteriaBox,
                findRouteButton
        );

        mapCanvas = new Canvas(700, 600); // Početna veličina Canvasa
        graphRenderer = new GraphRenderer(mapCanvas, transportMap);
        graphRenderer.drawInitialMap();

        // *** NOVE LINIJE ZA TABELARNI PRIKAZ NA MESTU GDE JE BIO resultTextArea ***
        bestRouteSummaryLabel = new Label("Najbolja ruta: N/A");
        bestRouteSummaryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 1.1em; -fx-padding: 5 0 5 0;"); // Dodaj stil

        routeDetailsTable = new TableView<>();
        routeDetailsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        routeDetailsTable.setPlaceholder(new Label("Nema segmenata rute za prikaz."));

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

        TableColumn<RouteSegment, Double> priceCol = new TableColumn<>("Cena");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Dodavanje kolona u tabelu
        routeDetailsTable.getColumns().addAll(typeCol, fromCol, fromCityCol, toCol, toCityCol, depTimeCol, arrTimeCol, priceCol);
        // Postavi preferiranu visinu tabele
        routeDetailsTable.setPrefHeight(200); // Možeš prilagoditi ovu vrednost

        // Glavni layout prozora
        VBox mapAndResultsLayout = new VBox(10);
        mapAndResultsLayout.setPadding(new Insets(10));
        // *** OVDE ZAMENJUJEŠ resultTextArea sa novim elementima ***
        mapAndResultsLayout.getChildren().addAll(mapCanvas, bestRouteSummaryLabel, routeDetailsTable);
        VBox.setVgrow(mapCanvas, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setLeft(controlsLayout);
        root.setCenter(mapAndResultsLayout);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ... (populateCityComboBox metoda je ista) ...
    private void populateCityComboBox(ComboBox<City> comboBox) {
        if (transportMap != null && transportMap.getCities() != null) {
            List<City> allCities = new ArrayList<>();
            for (int i = 0; i < transportMap.getNumRows(); i++) {
                for (int j = 0; j < transportMap.getNumCols(); j++) {
                    City city = transportMap.getCity(i, j);
                    if (city != null) {
                        allCities.add(city);
                    }
                }
            }
            allCities.sort(Comparator.comparing(City::getName));

            ObservableList<City> cityList = FXCollections.observableArrayList(allCities);
            comboBox.setItems(cityList);
            comboBox.setCellFactory(lv -> new ListCell<City>() {
                @Override
                protected void updateItem(City item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName());
                }
            });
            comboBox.setButtonCell(new ListCell<City>() {
                @Override
                protected void updateItem(City item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName());
                }
            });
        }
    }

    /**
     * Metoda koja se poziva kada korisnik klikne na "Pronađi rutu".
     */
    private void findOptimalRoute() {
        City startCity = startCityComboBox.getSelectionModel().getSelectedItem();
        City endCity = endCityComboBox.getSelectionModel().getSelectedItem();
        String criteria = (optimizationCriteriaGroup.getSelectedToggle() != null) ?
                (String) optimizationCriteriaGroup.getSelectedToggle().getUserData() : null;

        if (startCity == null || endCity == null) {
            showAlert("Greška pri odabiru", "Molimo odaberite i početni i odredišni grad.");
            return;
        }
        if (startCity.equals(endCity)) {
            showAlert("Greška pri odabiru", "Početni i odredišni grad moraju biti različiti.");
            return;
        }
        if (criteria == null) {
            showAlert("Greška pri odabiru", "Molimo odaberite kriterijum optimizacije.");
            return;
        }

        // resultTextArea.setText("Tražim rutu od " + startCity.getName() + " do " + endCity.getName() + // <--- UKLONI OVO
        //         " po kriterijumu: " + criteria + "...\n"); // <--- UKLONI OVO
        bestRouteSummaryLabel.setText("Tražim rutu od " + startCity.getName() + " do " + endCity.getName() +
                " po kriterijumu: " + criteria + "..."); // Postavi inicijalni tekst pretrage

        Path bestRoute = routeFinder.findBestRoute(startCity, endCity, criteria);

        showRouteDetails(bestRoute, criteria); // <--- POZIVI NOVU METODU OVDE

        if (bestRoute != null) {
            // resultTextArea.appendText("\nOptimalna ruta pronađena:\n"); // <--- UKLONI OVO
            // resultTextArea.appendText(bestRoute.toString()); // <--- UKLONI OVO
            graphRenderer.highlightRoute(bestRoute); // I dalje istakni rutu na grafu
        } else {
            // resultTextArea.appendText("\nNije pronađena ruta od " + startCity.getName() + " do " + endCity.getName() + "."); // <--- UKLONI OVO
            graphRenderer.drawInitialMap();
        }
    }

    // *** NOVA METODA ZA PRIKAZ DETALJA RUTE U TABELI ***
    private void showRouteDetails(Path path, String criterion) {
        if (path != null) {
            bestRouteSummaryLabel.setText(
                    String.format("Najbolja ruta (%s): Vreme: %s, Cena: %.2f, Presedanja: %d",
                            criterion.toLowerCase(),
                            formatDuration(path.getTotalTravelTime()),
                            path.getTotalCost(),
                            path.getTransfers()));

            ObservableList<RouteSegment> segments = FXCollections.observableArrayList(path.getSegments());
            routeDetailsTable.setItems(segments);
        } else {
            bestRouteSummaryLabel.setText("Najbolja ruta: Nema pronađenih ruta.");
            routeDetailsTable.setItems(FXCollections.emptyObservableList()); // Isprazni tabelu
        }
    }

    // *** POMOĆNA METODA ZA FORMATIRANJE DURATION (kopiraj iz Path klase ako već nemaš) ***
    private String formatDuration(Duration duration) {
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%dč %dmin", hours, minutes);
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ... (main metoda je ista) ...
    public static void main(String[] args) {
        launch(args);
    }
}