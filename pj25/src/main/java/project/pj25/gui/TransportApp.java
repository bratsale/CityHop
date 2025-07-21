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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import project.pj25.model.*;
import project.pj25.data.*;
import project.pj25.util.*;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransportApp extends Application {

    private TransportMap transportMap; // Učitani podaci o mapi
    private ComboBox<City> startCityComboBox;
    private ComboBox<City> endCityComboBox;
    private ToggleGroup optimizationCriteriaGroup;
    private Button findRouteButton;
    private TextArea resultTextArea; // Za prikaz rute

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CityHop - Pronađi Optimalnu Rutu");

        // 1. Učitavanje podataka
        System.out.println("Učitavam transportne podatke...");
        transportMap = DataLoader.loadTransportData("transport_data.json");

        if (transportMap == null) {
            System.err.println("Greška prilikom učitavanja podataka. Aplikacija se neće pokrenuti.");
            // Prikazati grešku korisniku
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Greška pri učitavanju");
            alert.setHeaderText("Nije moguće učitati transportne podatke.");
            alert.setContentText("Molimo proverite da li 'transport_data.json' postoji i da li je ispravan.");
            alert.showAndWait();
            return;
        }
        System.out.println("Podaci uspešno učitani. Ukupan broj gradova: " + (transportMap.getNumRows() * transportMap.getNumCols()));

        // Konfiguracija GUI komponenti
        // ComboBox za odabir početnog grada
        startCityComboBox = new ComboBox<>();
        startCityComboBox.setPromptText("Odaberi početni grad");
        // Popuni ComboBox sa gradovima
        populateCityComboBox(startCityComboBox);

        // ComboBox za odabir odredišnog grada
        endCityComboBox = new ComboBox<>();
        endCityComboBox.setPromptText("Odaberi odredišni grad");
        populateCityComboBox(endCityComboBox);

        // RadioButtons za kriterijume optimizacije
        optimizationCriteriaGroup = new ToggleGroup();

        RadioButton timeRadio = new RadioButton("Najkraće vreme putovanja");
        timeRadio.setToggleGroup(optimizationCriteriaGroup);
        timeRadio.setUserData("time");
        timeRadio.setSelected(true); // Podrazumevano odabran

        RadioButton priceRadio = new RadioButton("Najniža cena");
        priceRadio.setToggleGroup(optimizationCriteriaGroup);
        priceRadio.setUserData("price");

        RadioButton transfersRadio = new RadioButton("Najmanji broj presedanja");
        transfersRadio.setToggleGroup(optimizationCriteriaGroup);
        transfersRadio.setUserData("transfers");

        VBox criteriaBox = new VBox(10, new Label("Kriterijum optimizacije:"), timeRadio, priceRadio, transfersRadio);
        criteriaBox.setPadding(new Insets(10));
        criteriaBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");

        // Dugme za pronalaženje rute
        findRouteButton = new Button("Pronađi rutu");
        findRouteButton.setOnAction(e -> findOptimalRoute()); // Dodajemo akciju za dugme

        // Layout za kontrole (ComboBoxes, RadioButtons, Button)
        VBox controlsLayout = new VBox(20);
        controlsLayout.setPadding(new Insets(20));
        controlsLayout.setAlignment(Pos.TOP_LEFT);
        controlsLayout.getChildren().addAll(
                new Label("Početni grad:"), startCityComboBox,
                new Label("Odredišni grad:"), endCityComboBox,
                criteriaBox,
                findRouteButton
        );

        // TextArea za prikaz rezultata
        resultTextArea = new TextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setWrapText(true);
        resultTextArea.setPromptText("Optimalna ruta će biti prikazana ovde...");

        // Glavni layout aplikacije
        BorderPane root = new BorderPane();
        root.setLeft(controlsLayout);
        root.setCenter(resultTextArea); // Privremeno, ovde će kasnije biti mapa/vizualizacija

        Scene scene = new Scene(root, 800, 600); // Širina i visina prozora
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Popunjava ComboBox sa gradovima iz transportne mape.
     * @param comboBox ComboBox koji treba popuniti.
     */
    private void populateCityComboBox(ComboBox<City> comboBox) {
        if (transportMap != null && transportMap.getCities() != null) {
            // Sakupljamo sve gradove u jednu listu
            List<City> allCities = new ArrayList<>();
            for (int i = 0; i < transportMap.getNumRows(); i++) {
                for (int j = 0; j < transportMap.getNumCols(); j++) {
                    City city = transportMap.getCity(i, j);
                    if (city != null) {
                        allCities.add(city);
                    }
                }
            }
            // Sortiramo gradove po imenu radi lakšeg pronalaženja
            allCities.sort(Comparator.comparing(City::getName));

            ObservableList<City> cityList = FXCollections.observableArrayList(allCities);
            comboBox.setItems(cityList);
            // Postavljamo CellFactory da prikaže samo ime grada u ComboBoxu
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
     * Ovde će biti implementirana logika za pronalaženje rute.
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

        // OVDE ĆE DOĆI ALGORITAM ZA PRONALAŽENJE RUTE
        // Za sada, samo placeholder poruka
        resultTextArea.appendText("\nLogika za pronalaženje rute još uvek nije implementirana.");
        resultTextArea.appendText("\nOdabrani gradovi: " + startCity.getName() + " -> " + endCity.getName());
        resultTextArea.appendText("\nOdabrani kriterijum: " + criteria);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args); // Pokreće JavaFX aplikaciju
    }
}