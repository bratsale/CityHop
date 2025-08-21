package project.pj25.gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Separator;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import project.pj25.model.*;
import project.pj25.data.*;
import project.pj25.algorithm.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.Canvas;

/**
 * <p>Glavna klasa za pokretanje grafičke aplikacije "CityHop".</p>
 *
 * <p>Ova klasa postavlja kompletan korisnički interfejs, uključujući kontrole za
 * odabir početnog i krajnjeg grada, kriterijuma optimizacije, grafički prikaz
 * transportne mreže i tabelarni prikaz pronađenih ruta. Povezuje sve komponente
 * sistema: učitavanje podataka, pronalaženje ruta, vizualizaciju i generisanje računa.</p>
 *
 * @author Tvoje Ime
 * @version 1.0
 */
public class TransportApp extends Application {

    private TransportMap transportMap;
    private RouteFinder routeFinder;
    private ComboBox<City> startCityComboBox;
    private ComboBox<City> endCityComboBox;
    private ToggleGroup optimizationCriteriaGroup;
    private Button findRouteButton;
    private Canvas mapCanvas;
    private GraphRenderer graphRenderer;
    private TableView<RouteSegment> routeDetailsTable;
    private Label bestRouteSummaryLabel;
    private Button showAdditionalRoutesButton;
    private Button buyBestRouteTicketButton;
    private Path currentBestRoute;
    private Label salesInfoLabel;

    /**
     * Glavna metoda za pokretanje JavaFX aplikacije.
     * <p>Kreira i postavlja scenu, te inicijalizuje sve komponente korisničkog interfejsa.</p>
     *
     * @param primaryStage Glavna pozornica (prozor) aplikacije.
     */
    @Override
    public void start(Stage primaryStage) {
        SalesData sales = InvoiceManager.loadSalesData();
        salesInfoLabel = new Label(String.format("Ukupno prodato karata: %d\nUkupan prihod: %.2f KM", sales.totalTickets(), sales.totalRevenue()));
        salesInfoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 1.1em; -fx-padding: 0 0 10 0;");
        salesInfoLabel.setPrefWidth(300);

        primaryStage.setTitle("CityHop - Pronađi Optimalnu Rutu");

        System.out.println("Učitavam transportne podatke...");
        transportMap = DataLoader.loadTransportData("transport_data.json");

        if (transportMap == null) {
            System.err.println("Greška prilikom učitavanja podataka. Aplikacija se neće pokrenuti.");
            showAlert("Greška pri učitavanju", "Nije moguće učitati transportne podatke.\nMolimo provjerite da li 'transport_data.json' postoji i da li je ispravan.");
            return;
        }
        System.out.println("Podaci uspješno učitani. Ukupan broj gradova: " + (transportMap.getNumRows() * transportMap.getNumCols()));

        this.routeFinder = new RouteFinder(transportMap);

        startCityComboBox = new ComboBox<>();
        startCityComboBox.setPromptText("Odaberi početni grad");
        startCityComboBox.setMaxWidth(Double.MAX_VALUE);
        populateCityComboBox(startCityComboBox);

        endCityComboBox = new ComboBox<>();
        endCityComboBox.setPromptText("Odaberi odredišni grad");
        endCityComboBox.setMaxWidth(Double.MAX_VALUE);
        populateCityComboBox(endCityComboBox);

        optimizationCriteriaGroup = new ToggleGroup();
        RadioButton timeRadio = new RadioButton("Najkraće vrijeme putovanja");
        timeRadio.setToggleGroup(optimizationCriteriaGroup);
        timeRadio.setUserData("time");
        timeRadio.setSelected(true);

        RadioButton priceRadio = new RadioButton("Najniža cijena");
        priceRadio.setToggleGroup(optimizationCriteriaGroup);
        priceRadio.setUserData("price");

        RadioButton transfersRadio = new RadioButton("Najmanji broj presjedanja");
        transfersRadio.setToggleGroup(optimizationCriteriaGroup);
        transfersRadio.setUserData("transfers");

        VBox criteriaBox = new VBox(10, new Label("Kriterijum optimizacije:"), timeRadio, priceRadio, transfersRadio);
        criteriaBox.setPadding(new Insets(10));
        criteriaBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");
        criteriaBox.setMaxWidth(Double.MAX_VALUE);

        findRouteButton = new Button("Pronađi rutu");
        findRouteButton.setMaxWidth(Double.MAX_VALUE);
        findRouteButton.setOnAction(e -> findOptimalRoute());

        VBox salesInfoBox = new VBox(5);
        salesInfoBox.getChildren().addAll(salesInfoLabel, new Separator());
        salesInfoBox.setPadding(new Insets(0, 0, 10, 0));

        VBox controlsLayout = new VBox(20);
        controlsLayout.setPadding(new Insets(20));
        controlsLayout.setAlignment(Pos.TOP_LEFT);
        controlsLayout.getChildren().addAll(
                salesInfoBox,
                new Label("Početni grad:"), startCityComboBox,
                new Label("Odredišni grad:"), endCityComboBox,
                criteriaBox,
                findRouteButton
        );
        controlsLayout.setPrefWidth(250);
        controlsLayout.setMaxWidth(300);

        mapCanvas = new Canvas(700, 600);
        graphRenderer = new GraphRenderer(mapCanvas, transportMap);
        graphRenderer.drawInitialMap();

        BorderPane mapPane = new BorderPane();
        mapPane.setCenter(mapCanvas);
        mapCanvas.widthProperty().bind(mapPane.widthProperty());
        mapCanvas.heightProperty().bind(mapPane.heightProperty());
        mapPane.setPrefSize(700, 600);

        bestRouteSummaryLabel = new Label("Najbolja ruta: N/A");
        bestRouteSummaryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 1.1em; -fx-padding: 5 0 5 0;");
        bestRouteSummaryLabel.setAlignment(Pos.CENTER_LEFT);

        routeDetailsTable = new TableView<>();
        routeDetailsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        routeDetailsTable.setPlaceholder(new Label("Nema segmenata rute za prikaz."));

        // Definisanje kolona za tabelu
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

        routeDetailsTable.getColumns().addAll(typeCol, fromCol, fromCityCol, toCol, toCityCol, depTimeCol, arrTimeCol, priceCol);
        routeDetailsTable.setPrefHeight(200);
        routeDetailsTable.setMaxHeight(Double.MAX_VALUE);

        showAdditionalRoutesButton = new Button("Prikaži dodatne rute");
        showAdditionalRoutesButton.setOnAction(e -> {
            City start = startCityComboBox.getSelectionModel().getSelectedItem();
            City end = endCityComboBox.getSelectionModel().getSelectedItem();
            String criterion = getSelectedCriterion();

            if (start != null && end != null && criterion != null) {
                List<Path> topRoutes = routeFinder.findTopNRoutes(start, end, criterion, 5);
                TopRoutesDialog dialog = new TopRoutesDialog(primaryStage, topRoutes, criterion);
                dialog.show();
            } else {
                showAlert("Greška", "Molimo odaberite početni i krajnji grad, te kriterijum optimizacije prije prikaza dodatnih ruta.");
            }
        });

        buyBestRouteTicketButton = new Button("Kupovina karte (za najbolju rutu)");
        buyBestRouteTicketButton.setOnAction(e -> {
            if (currentBestRoute != null) {
                String startCityName = currentBestRoute.getSegments().get(0).getDepartureStationCityName();
                String endCityName = currentBestRoute.getSegments().get(currentBestRoute.getSegments().size() - 1).getArrivalStationCityName();
                InvoiceManager.generateInvoice(currentBestRoute, startCityName, endCityName);

                SalesData updatedSales = InvoiceManager.loadSalesData();
                salesInfoLabel.setText(String.format("Ukupno prodato karata: %d\nUkupan prihod: %.2f KM", updatedSales.totalTickets(), updatedSales.totalRevenue()));

                showAlert("Kupovina karte", "Karta uspješno kupljena!\nRačun za najbolju rutu je generisan i sačuvan u folderu 'racuni'.");
            } else {
                showAlert("Nema rute", "Molimo prvo pronađite rutu.");
            }
        });

        HBox actionButtonsBox = new HBox(15);
        actionButtonsBox.setAlignment(Pos.CENTER);
        actionButtonsBox.setPadding(new Insets(10, 0, 0, 0));
        actionButtonsBox.getChildren().addAll(showAdditionalRoutesButton, buyBestRouteTicketButton);
        actionButtonsBox.setMaxWidth(Double.MAX_VALUE);

        VBox rightSideLayout = new VBox(10);
        rightSideLayout.setPadding(new Insets(10));
        rightSideLayout.getChildren().addAll(mapPane, bestRouteSummaryLabel, routeDetailsTable, actionButtonsBox);
        rightSideLayout.setMaxWidth(Double.MAX_VALUE);
        rightSideLayout.setMaxHeight(Double.MAX_VALUE);

        BorderPane root = new BorderPane();
        root.setLeft(controlsLayout);
        root.setCenter(rightSideLayout);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Popunjava {@link ComboBox} sa listom gradova iz transportne mape.
     * Gradovi se sortiraju po imenu radi lakšeg odabira.
     *
     * @param comboBox ComboBox koji treba popuniti.
     */
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
     * Pronalazi optimalnu rutu na osnovu odabranih kriterijuma i ažurira korisnički interfejs.
     */
    private void findOptimalRoute() {
        City startCity = startCityComboBox.getSelectionModel().getSelectedItem();
        City endCity = endCityComboBox.getSelectionModel().getSelectedItem();
        String criteria = getSelectedCriterion();

        if (startCity == null || endCity == null) {
            showAlert("Greška pri odabiru", "Molimo odaberite i početni i odredišni grad.");
            currentBestRoute = null;
            showRouteDetails(null, criteria);
            return;
        }
        if (startCity.equals(endCity)) {
            showAlert("Greška pri odabiru", "Početni i odredišni grad moraju biti različiti.");
            currentBestRoute = null;
            showRouteDetails(null, criteria);
            return;
        }
        if (criteria == null) {
            showAlert("Greška pri odabiru", "Molimo odaberite kriterijum optimizacije.");
            currentBestRoute = null;
            showRouteDetails(null, criteria);
            return;
        }

        bestRouteSummaryLabel.setText("Tražim rutu od " + startCity.getName() + " do " + endCity.getName() +
                " po kriterijumu: " + formatCriterionNameForDisplay(criteria) + "...");

        List<Path> bestRoutes = routeFinder.findTopNRoutes(startCity, endCity, criteria, 1);
        Path bestRoute = null;
        if (!bestRoutes.isEmpty()) {
            bestRoute = bestRoutes.get(0);
        }

        currentBestRoute = bestRoute;

        showRouteDetails(bestRoute, criteria);

        if (bestRoute != null) {
            graphRenderer.highlightRoute(bestRoute);
        } else {
            graphRenderer.drawInitialMap();
        }
    }

    /**
     * Ažurira tabelarni i tekstualni prikaz rute u korisničkom interfejsu.
     *
     * @param path      Putanja ({@link Path}) koja se prikazuje.
     * @param criterion Kriterijum po kojem je putanja pronađena.
     */
    private void showRouteDetails(Path path, String criterion) {
        if (path != null) {
            bestRouteSummaryLabel.setText(
                    String.format("Najbolja ruta (%s): Vrijeme: %s, Cijena: %.2f KM, Presjedanja: %d",
                            formatCriterionNameForDisplay(criterion),
                            formatDuration(path.getTotalTravelTime()),
                            path.getTotalCost(),
                            path.getTransfers()));

            ObservableList<RouteSegment> segments = FXCollections.observableArrayList(path.getSegments());
            routeDetailsTable.setItems(segments);
        } else {
            bestRouteSummaryLabel.setText("Najbolja ruta: Nema pronađenih ruta.");
            routeDetailsTable.setItems(FXCollections.emptyObservableList());
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
     * Dohvata odabrani kriterijum optimizacije iz {@link ToggleGroup}.
     *
     * @return String koji predstavlja odabrani kriterijum ("time", "price", "transfers").
     */
    private String getSelectedCriterion() {
        Toggle selectedToggle = optimizationCriteriaGroup.getSelectedToggle();
        if (selectedToggle != null) {
            return (String) selectedToggle.getUserData();
        }
        return null;
    }

    /**
     * Pomoćna metoda za formatiranje naziva kriterijuma za prikaz u korisničkom interfejsu.
     *
     * @param criterion Naziv kriterijuma (npr. "time").
     * @return Formatirani string za prikaz.
     */
    private String formatCriterionNameForDisplay(String criterion) {
        if (criterion == null) return "N/A";
        switch (criterion.toLowerCase()) {
            case "time": return "Najkraće vrijeme putovanja";
            case "price": return "Najniža cijena";
            case "transfers": return "Najmanji broj presjedanja";
            default: return criterion;
        }
    }

    /**
     * Prikazuje dijaloški prozor sa upozorenjem.
     *
     * @param title   Naslov prozora.
     * @param message Poruka za prikaz.
     */
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