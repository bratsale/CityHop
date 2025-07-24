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

    // Ažurirane konstante za bolji izgled
    private static final double STATION_RADIUS = 5.0;
    private static final double CITY_BOX_PADDING = 20.0; // Malo veći padding oko grada
    private static final double PADDING_CANVAS = 40.0; // Povećan padding od ivica Canvasa

    // Prilagođene konstante za skaliranje
    // Smanjen DISPLAY_SCALE_FACTOR da se gradovi manje rašire
    private static final double DISPLAY_SCALE_FACTOR = 80.0; // Smanjeno sa 100.0
    // Povećan OFFSET za bolje razdvajanje stanica
    private static final double STATION_OFFSET_MAGNITUDE = 40.0; // Povećano sa 30.0

    public GraphRenderer(Canvas canvas, TransportMap transportMap) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.transportMap = transportMap;
        this.stationDisplayCoordinates = new HashMap<>();

        calculateStationDisplayCoordinates();
    }

    private static class Point2D {
        double x, y;
        Point2D(double x, double y) { this.x = x; this.y = y; }
    }

    private void calculateStationDisplayCoordinates() {
        stationDisplayCoordinates.clear();

        double maxCityCoordX = 0;
        double maxCityCoordY = 0;

        // Pronađi STVARNE maksimalne koordinate gradova iz učitanih podataka
        // Ovo je sigurnije nego koristiti numRows/numCols - 1
        for (int i = 0; i < transportMap.getNumRows(); i++) {
            for (int j = 0; j < transportMap.getNumCols(); j++) {
                City city = transportMap.getCity(i, j);
                if (city != null) {
                    if (city.getX() > maxCityCoordX) maxCityCoordX = city.getX();
                    if (city.getY() > maxCityCoordY) maxCityCoordY = city.getY();
                }
            }
        }
        // Dodaj malu marginu na maksimalne koordinate ako su 0 (za pojedinačan grad)
        if (maxCityCoordX == 0 && transportMap.getNumCols() > 1) maxCityCoordX = transportMap.getNumCols() - 1;
        if (maxCityCoordY == 0 && transportMap.getNumRows() > 1) maxCityCoordY = transportMap.getNumRows() - 1;


        // Izračunaj skaliranje na osnovu stvarnog raspona koordinata gradova
        // Dodat DISPLAY_SCALE_FACTOR * 2 da bi se obezbedio prostor za ofset stanica unutar grada
        double drawingWidth = (maxCityCoordX * DISPLAY_SCALE_FACTOR) + (STATION_OFFSET_MAGNITUDE * 2);
        double drawingHeight = (maxCityCoordY * DISPLAY_SCALE_FACTOR) + (STATION_OFFSET_MAGNITUDE * 2);

        // Uzmi u obzir i padding Canvasa
        double scaleX = (canvas.getWidth() - 2 * PADDING_CANVAS) / drawingWidth;
        double scaleY = (canvas.getHeight() - 2 * PADDING_CANVAS) / drawingHeight;
        double finalScale = Math.min(scaleX, scaleY); // Koristi manji faktor skaliranja

        // Izračunaj ofset za centriranje crteža
        double offsetX_center = (canvas.getWidth() - (drawingWidth * finalScale + 2 * PADDING_CANVAS)) / 2;
        double offsetY_center = (canvas.getHeight() - (drawingHeight * finalScale + 2 * PADDING_CANVAS)) / 2;

        if (offsetX_center < 0) offsetX_center = 0; // Osiguraj da ne idemo van levih granica
        if (offsetY_center < 0) offsetY_center = 0; // Osiguraj da ne idemo van gornjih granica


        Random rand = new Random(42);

        for (int i = 0; i < transportMap.getNumRows(); i++) {
            for (int j = 0; j < transportMap.getNumCols(); j++) {
                City city = transportMap.getCity(i, j);
                if (city != null) {
                    // Skalirane koordinate centra grada
                    double cityBaseX = PADDING_CANVAS + city.getX() * DISPLAY_SCALE_FACTOR * finalScale + offsetX_center;
                    double cityBaseY = PADDING_CANVAS + city.getY() * DISPLAY_SCALE_FACTOR * finalScale + offsetY_center;

                    List<Station> stationsInCity = transportMap.getAllStations().values().stream()
                            .filter(s -> s.getCity().equals(city))
                            .collect(Collectors.toList());

                    int numStations = stationsInCity.size();
                    if (numStations > 0) {
                        double angleStep = 2 * Math.PI / numStations;
                        for (int k = 0; k < numStations; k++) {
                            Station station = stationsInCity.get(k);
                            double offsetX = STATION_OFFSET_MAGNITUDE * Math.cos(k * angleStep);
                            double offsetY = STATION_OFFSET_MAGNITUDE * Math.sin(k * angleStep);

                            stationDisplayCoordinates.put(station, new Point2D(cityBaseX + offsetX, cityBaseY + offsetY));
                        }
                    }
                }
            }
        }
    }

    // Metode drawInitialMap i highlightRoute ostaju uglavnom iste,
    // samo će koristiti nove koordinate iz calculateStationDisplayCoordinates()
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

                    for (Station station : stationsInCity) {
                        Point2D p = stationDisplayCoordinates.get(station);
                        if (p != null) {
                            minX = Math.min(minX, p.x);
                            minY = Math.min(minY, p.y);
                            maxX = Math.max(maxX, p.x);
                            maxY = Math.max(maxY, p.y);
                        }
                    }

                    // Add padding to the city box
                    minX -= CITY_BOX_PADDING;
                    minY -= CITY_BOX_PADDING;
                    maxX += CITY_BOX_PADDING;
                    maxY += CITY_BOX_PADDING;

                    gc.setStroke(Color.DARKGRAY);
                    gc.setLineWidth(1.0);
                    gc.strokeRect(minX, minY, maxX - minX, maxY - minY);

                    gc.setFill(Color.BLACK);
                    gc.setFont(new Font("Arial", 12));
                    gc.setTextAlign(TextAlignment.CENTER);
                    gc.fillText(city.getName(), minX + (maxX - minX) / 2, minY - 5);
                }
            }
        }

        // Draw all connections (lines between stations)
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1.0);
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
            gc.fillOval(p.x - STATION_RADIUS, p.y - STATION_RADIUS, 2 * STATION_RADIUS, 2 * STATION_RADIUS);
            gc.setStroke(Color.DARKBLUE);
            gc.setLineWidth(1.0);
            gc.strokeOval(p.x - STATION_RADIUS, p.y - STATION_RADIUS, 2 * STATION_RADIUS, 2 * STATION_RADIUS);

            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Arial", 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(station.getId(), p.x, p.y + STATION_RADIUS + 10);
        }
    }

    public void highlightRoute(Path route) {
        drawInitialMap(); // Clear and redraw base map

        gc.setStroke(Color.RED);
        gc.setLineWidth(3.0);

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
            gc.fillOval(p.x - STATION_RADIUS, p.y - STATION_RADIUS, 2 * STATION_RADIUS, 2 * STATION_RADIUS);
            gc.setStroke(Color.DARKBLUE);
            gc.setLineWidth(1.0);
            gc.strokeOval(p.x - STATION_RADIUS, p.y - STATION_RADIUS, 2 * STATION_RADIUS, 2 * STATION_RADIUS);

            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Arial", 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(station.getId(), p.x, p.y + STATION_RADIUS + 10);
        }
    }
}