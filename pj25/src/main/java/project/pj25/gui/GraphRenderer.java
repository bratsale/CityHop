package project.pj25.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import project.pj25.algorithm.*;
import project.pj25.model.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class GraphRenderer {
    private Canvas canvas;
    private GraphicsContext gc;
    private TransportMap transportMap;

    private Map<Station, Point2D> stationDisplayCoordinates;

    // Fiksne konstante za RELATIVNE bazne veličine
    private static final double STATION_RADIUS_BASE = 5.0; // Bazni radijus stanice
    private static final double CITY_BOX_PADDING_BASE = 20.0; // Bazni padding oko grada
    private static final double CANVAS_MARGIN_BASE = 40.0; // Bazna margina od ivica Canvasa
    private static final double STATION_OFFSET_MAGNITUDE_BASE = 40.0; // Bazni ofset stanica unutar grada

    // Dinamički izračunati faktori i dimenzije
    private double overallScaleFactor; // Glavni faktor skaliranja za ceo graf
    private double currentStationRadius;
    private double currentCityBoxPadding;
    private double currentCanvasMargin;
    private double currentStationOffsetMagnitude; // Dinamički ofset stanica unutar grada

    // Razmaci između centara gradova
    private double horzCitySpacing;
    private double vertCitySpacing;

    public GraphRenderer(Canvas canvas, TransportMap transportMap) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.transportMap = transportMap;
        this.stationDisplayCoordinates = new HashMap<>();

        // Inicijalizacija default vrednosti pre prve kalkulacije
        this.overallScaleFactor = 1.0;
        this.currentStationRadius = STATION_RADIUS_BASE;
        this.currentCityBoxPadding = CITY_BOX_PADDING_BASE;
        this.currentCanvasMargin = CANVAS_MARGIN_BASE;
        this.currentStationOffsetMagnitude = STATION_OFFSET_MAGNITUDE_BASE;

        // Pridruži listenere za promenu veličine Canvasa
        // Ovo je KLJUČNO! Kada se veličina canvasa promeni, preračunaj koordinate i ponovo nacrtaj.
        this.canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            calculateStationDisplayCoordinates();
            drawInitialMap();
        });
        this.canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            calculateStationDisplayCoordinates();
            drawInitialMap();
        });

        // Početna kalkulacija
        calculateStationDisplayCoordinates();
    }

    private static class Point2D {
        double x, y;
        Point2D(double x, double y) { this.x = x; this.y = y; }
    }

    private void calculateStationDisplayCoordinates() {
        stationDisplayCoordinates.clear();

        int numRows = transportMap.getNumRows();
        int numCols = transportMap.getNumCols();

        // 1. Dinamičko izračunavanje razmaka i dimenzija
        // Minimum je 1 za računanje razmaka, da se izbegne deljenje sa nulom
        double effectiveNumCols = Math.max(1, numCols);
        double effectiveNumRows = Math.max(1, numRows);

        // Bazne dimenzije koje bi zauzeo jedan "kvadrat" grada uključujući njegov padding i ofset stanica
        double baseCitySquareSide = (STATION_OFFSET_MAGNITUDE_BASE * 2 + CITY_BOX_PADDING_BASE * 2);

        // Ukupna širina/visina koju bi *sadržaj* (svi gradovi i njihove stanice) zauzeo
        // kada bi se koristile bazne dimenzije bez ukupnog skaliranja
        double contentWidthNeededBase = (effectiveNumCols * baseCitySquareSide) + (effectiveNumCols - 1) * 0; // Dodali smo 0, jer ćemo razmak izračunati kasnije
        double contentHeightNeededBase = (effectiveNumRows * baseCitySquareSide) + (effectiveNumRows - 1) * 0;

        // Dostupna širina/visina Canvasa umanjena za bazne margine
        double availableWidth = canvas.getWidth() - (2 * CANVAS_MARGIN_BASE);
        double availableHeight = canvas.getHeight() - (2 * CANVAS_MARGIN_BASE);

        // Izračunaj faktore skaliranja na osnovu dostupnog i potrebnog prostora
        // Ako je baseCitySquareSide 0 (npr. nema stanica, ali ne bi trebalo), postavi scale na 1
        double scaleFactorX = (contentWidthNeededBase > 0) ? availableWidth / contentWidthNeededBase : 1.0;
        double scaleFactorY = (contentHeightNeededBase > 0) ? availableHeight / contentHeightNeededBase : 1.0;

        overallScaleFactor = Math.min(scaleFactorX, scaleFactorY);

        // Ograniči minimalni i maksimalni overallScaleFactor
        overallScaleFactor = Math.max(overallScaleFactor, 0.2); // Npr. ne manje od 20% originalne veličine
        overallScaleFactor = Math.min(overallScaleFactor, 1.5); // Npr. ne više od 150% originalne veličine

        // Ažuriraj dinamičke dimenzije na osnovu overallScaleFactor
        currentStationRadius = STATION_RADIUS_BASE * overallScaleFactor;
        currentCityBoxPadding = CITY_BOX_PADDING_BASE * overallScaleFactor;
        currentCanvasMargin = CANVAS_MARGIN_BASE * overallScaleFactor;
        currentStationOffsetMagnitude = STATION_OFFSET_MAGNITUDE_BASE * overallScaleFactor;


        // 2. Izračunaj dinamičke razmake između centara gradova (koji uključuju i veličinu grada)
        // Ukupna širina/visina koju zauzima sadržaj nakon skaliranja
        double scaledContentWidth = numCols * (currentStationOffsetMagnitude * 2 + currentCityBoxPadding * 2);
        double scaledContentHeight = numRows * (currentStationOffsetMagnitude * 2 + currentCityBoxPadding * 2);

        // Preostali prostor za distribuciju kao razmaka
        double remainingWidthForSpacing = canvas.getWidth() - (2 * currentCanvasMargin) - scaledContentWidth;
        double remainingHeightForSpacing = canvas.getHeight() - (2 * currentCanvasMargin) - scaledContentHeight;

        // Podeli preostali prostor ravnomerno između gradova
        horzCitySpacing = (numCols > 1) ? remainingWidthForSpacing / (numCols - 1) : 0;
        vertCitySpacing = (numRows > 1) ? remainingHeightForSpacing / (numRows - 1) : 0;

        // Osiguraj minimalni razmak (opciono, može pomoći da se gradovi ne spoje previše)
        horzCitySpacing = Math.max(horzCitySpacing, currentCityBoxPadding / 2); // Minimalni razmak pola paddinga
        vertCitySpacing = Math.max(vertCitySpacing, currentCityBoxPadding / 2);


        // 3. Izračunaj koordinate za svaki grad i njegove stanice
        Random rand = new Random(42); // Koristi fiksni seed za konzistentan raspored stanica

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                City city = transportMap.getCity(i, j);
                if (city != null) {
                    // Centralna tačka grada u skaliranom prostoru
                    // Pozicija se izračunava na osnovu indeksa (i, j) i dinamičkih razmaka
                    double cityCenterX = currentCanvasMargin + j * (currentStationOffsetMagnitude * 2 + currentCityBoxPadding * 2 + horzCitySpacing) + currentStationOffsetMagnitude + currentCityBoxPadding;
                    double cityCenterY = currentCanvasMargin + i * (currentStationOffsetMagnitude * 2 + currentCityBoxPadding * 2 + vertCitySpacing) + currentStationOffsetMagnitude + currentCityBoxPadding;

                    List<Station> stationsInCity = transportMap.getAllStations().values().stream()
                            .filter(s -> s.getCity().equals(city))
                            .collect(Collectors.toList());

                    int numStations = stationsInCity.size();
                    if (numStations > 0) {
                        // Distribuiraj stanice u krug oko centra grada
                        double angleStep = 2 * Math.PI / numStations;
                        for (int k = 0; k < numStations; k++) {
                            Station station = stationsInCity.get(k);
                            double offsetX = currentStationOffsetMagnitude * Math.cos(k * angleStep);
                            double offsetY = currentStationOffsetMagnitude * Math.sin(k * angleStep);

                            stationDisplayCoordinates.put(station, new Point2D(cityCenterX + offsetX, cityCenterY + offsetY));
                        }
                    }
                }
            }
        }
    }

    public void drawInitialMap() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw city boundaries
        for (int i = 0; i < transportMap.getNumRows(); i++) {
            for (int j = 0; j < transportMap.getNumCols(); j++) {
                City city = transportMap.getCity(i, j);
                if (city != null) {
                    List<Station> stationsInCity = transportMap.getAllStations().values().stream()
                            .filter(s -> s.getCity().equals(city))
                            .collect(Collectors.toList());

                    if (stationsInCity.isEmpty()) continue;

                    double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
                    double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

                    // Pronađi bounding box za stanice unutar grada
                    for (Station station : stationsInCity) {
                        Point2D p = stationDisplayCoordinates.get(station);
                        if (p != null) {
                            minX = Math.min(minX, p.x);
                            minY = Math.min(minY, p.y);
                            maxX = Math.max(maxX, p.x);
                            maxY = Math.max(maxY, p.y);
                        }
                    }

                    // Dodaj dinamički padding na okvir grada
                    minX -= currentCityBoxPadding;
                    minY -= currentCityBoxPadding;
                    maxX += currentCityBoxPadding;
                    maxY += currentCityBoxPadding;

                    gc.setStroke(Color.DARKGRAY);
                    gc.setLineWidth(1.0);
                    gc.strokeRect(minX, minY, maxX - minX, maxY - minY);

                    gc.setFill(Color.BLACK);
                    gc.setFont(new Font("Arial", 12 * overallScaleFactor)); // Skaliraj font
                    gc.setTextAlign(TextAlignment.CENTER);
                    gc.fillText(city.getName(), minX + (maxX - minX) / 2, minY - 5 * overallScaleFactor); // Skaliraj i ofset teksta
                }
            }
        }

        // Draw all connections (lines between stations)
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1.0 * overallScaleFactor); // Skaliraj debljinu linija
        for (Station station : transportMap.getAllStations().values()) {
            Point2D startPoint = stationDisplayCoordinates.get(station);
            if (startPoint == null) continue;

            for (Departure departure : station.getDepartures()) {
                Station endStation = transportMap.getStation(departure.getArrivalStationId());
                if (endStation == null) {
                    System.err.println("Upozorenje: Polazak sa ID-jem dolazne stanice " + departure.getArrivalStationId() + " ne vodi nigde.");
                    continue;
                }
                Point2D endPoint = stationDisplayCoordinates.get(endStation);
                if (endPoint == null) continue;

                gc.strokeLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
            }
        }

        // Draw all stations (nodes)
        for (Station station : transportMap.getAllStations().values()) {
            Point2D p = stationDisplayCoordinates.get(station);
            if (p == null) continue;

            gc.setFill(Color.BLUE);
            gc.fillOval(p.x - currentStationRadius, p.y - currentStationRadius, 2 * currentStationRadius, 2 * currentStationRadius);
            gc.setStroke(Color.DARKBLUE);
            gc.setLineWidth(1.0 * overallScaleFactor); // Skaliraj debljinu okvira
            gc.strokeOval(p.x - currentStationRadius, p.y - currentStationRadius, 2 * currentStationRadius, 2 * currentStationRadius);

            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Arial", 10 * overallScaleFactor)); // Skaliraj font stanice
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(station.getId(), p.x, p.y + currentStationRadius + 10 * overallScaleFactor);
        }
    }

    public void highlightRoute(Path route) {
        drawInitialMap(); // Clear and redraw base map

        gc.setStroke(Color.RED);
        gc.setLineWidth(3.0 * overallScaleFactor); // Skaliraj debljinu highlight linije

        for (RouteSegment segment : route.getSegments()) {
            Station startStation = segment.getStartStation();
            Station endStation = segment.getEndStation();

            Point2D startPoint = stationDisplayCoordinates.get(startStation);
            Point2D endPoint = stationDisplayCoordinates.get(endStation);

            if (startPoint != null && endPoint != null) {
                gc.strokeLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
            }
        }

        // Re-draw stations (and their labels) on top, highlighting path stations
        for (Station station : transportMap.getAllStations().values()) {
            Point2D p = stationDisplayCoordinates.get(station);
            if (p == null) continue;

            boolean isOnRoute = route.getSegments().stream().anyMatch(s ->
                    s.getStartStation().equals(station) || s.getEndStation().equals(station)
            );

            gc.setFill(isOnRoute ? Color.GREEN : Color.BLUE);
            gc.fillOval(p.x - currentStationRadius, p.y - currentStationRadius, 2 * currentStationRadius, 2 * currentStationRadius);
            gc.setStroke(Color.DARKBLUE);
            gc.setLineWidth(1.0 * overallScaleFactor);
            gc.strokeOval(p.x - currentStationRadius, p.y - currentStationRadius, 2 * currentStationRadius, 2 * currentStationRadius);

            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Arial", 10 * overallScaleFactor));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(station.getId(), p.x, p.y + currentStationRadius + 10 * overallScaleFactor);
        }
    }
}