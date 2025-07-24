package project.pj25.gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import project.pj25.model.*;
import project.pj25.data.*;
import project.pj25.util.*;
import project.pj25.algorithm.*;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// importi za grafiku
// NOVI IMPORTI ZA GRAFIKU
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext; // Iako se ne koristi direktno ovde, obično ide uz Canvas
import javafx.scene.paint.Color; // Za boje (za kasniju upotrebu)
import javafx.scene.text.Font; // Za tekst (za kasniju upotrebu)

public class TransportApp extends Application {

    private TransportMap transportMap; // Učitani podaci o mapi
    private RouteFinder routeFinder;   // Instanca RouteFinder-a
    private ComboBox<City> startCityComboBox;
    private ComboBox<City> endCityComboBox;
    private ToggleGroup optimizationCriteriaGroup;
    private Button findRouteButton;
    private TextArea resultTextArea; // Za prikaz rute
    private Canvas mapCanvas;
    private GraphRenderer graphRenderer;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CityHop - Pronađi Optimalnu Rutu");

        // 1. Učitavanje podataka
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

        // Inicijalizacija RouteFinder-a nakon učitavanja transportMap
        this.routeFinder = new RouteFinder(transportMap); // KLJUČNA IZMENA: Inicijalizacija RouteFinder-a

        // Konfiguracija GUI komponenti
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

        resultTextArea = new TextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setWrapText(true);
        resultTextArea.setPromptText("Optimalna ruta će biti prikazana ovde...");
        resultTextArea.setPrefHeight(200); // Povećao visinu na 200 (sa 150)
        resultTextArea.setPrefWidth(550); // Možeš dodati i preferiranu širinu ako želiš da bude šira od canvasa

        VBox mapAndResultsLayout = new VBox(10);
        mapAndResultsLayout.setPadding(new Insets(10));
        mapAndResultsLayout.getChildren().addAll(mapCanvas, resultTextArea);
        VBox.setVgrow(mapCanvas, Priority.ALWAYS); // Dodaj ovu liniju
// Postavi fiksnu visinu za TextArea
        resultTextArea.setPrefHeight(200);

// Glavni layout prozora
        BorderPane root = new BorderPane();
        root.setLeft(controlsLayout);
        root.setCenter(mapAndResultsLayout);

// Povećaj veličinu prozora
        Scene scene = new Scene(root, 1200, 800); // Povećao sam širinu na 1100, visinu na 750
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void populateCityComboBox(ComboBox<City> comboBox) {
        if (transportMap != null && transportMap.getCities() != null) {
            List<City> allCities = new ArrayList<>();
            // Imaj na umu da transportMap.getCities() može vratiti mapu, ili dvodimenzionalni niz,
            // proveri tačno kako je implementirano u tvom DataLoader-u.
            // Ova petlja pretpostavlja da getCities vraća mapu ili da imaš getCity(row, col).
            for (int i = 0; i < transportMap.getNumRows(); i++) {
                for (int j = 0; j < transportMap.getNumCols(); j++) {
                    City city = transportMap.getCity(i, j); // Ako transportMap ima getCity(row, col)
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

        resultTextArea.setText("Tražim rutu od " + startCity.getName() + " do " + endCity.getName() +
                " po kriterijumu: " + criteria + "...\n");

        Path bestRoute = routeFinder.findBestRoute(startCity, endCity, criteria);

        if (bestRoute != null) {
            resultTextArea.appendText("\nOptimalna ruta pronađena:\n");
            resultTextArea.appendText(bestRoute.toString());
            // Pozovi GraphRenderer da istakne pronađenu rutu
            graphRenderer.highlightRoute(bestRoute);
        } else {
            resultTextArea.appendText("\nNije pronađena ruta od " + startCity.getName() + " do " + endCity.getName() + ".");
            // Ako ruta nije pronađena, vrati mapu na početno stanje (bez isticanja)
            graphRenderer.drawInitialMap();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}