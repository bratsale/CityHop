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
import java.util.stream.Collectors;

/**
 * Klasa za renderovanje transportne mape na JavaFX {@link Canvas}.
 * <p>
 * Odgovorna je za dinamičko raspoređivanje gradova, stanica i veza,
 * te za crtanje tih elemenata. Podržava dvije glavne funkcije:
 * 1. Crtanje inicijalne mape.
 * 2. Isticanje specifične rute na mapi.
 * Raspored elemenata se skalira i prilagođava dimenzijama platna.
 * </p>
 *
 * @author Tvoje Ime
 * @version 1.0
 */
public class GraphRenderer {

    /** Platno za crtanje. */
    private Canvas canvas;
    /** Kontekst za grafičke operacije. */
    private GraphicsContext gc;
    /** Transportna mapa koja se prikazuje. */
    private TransportMap transportMap;

    /** Mapa koja pohranjuje koordinate stanica na platnu. */
    private Map<Station, Point2D> stationDisplayCoordinates;
    /** Mapa koja pohranjuje koordinate "kapija" gradova (za međugradske linije). */
    private Map<City, CityGateways> cityGatewayCoordinates;

    // Fiksne konstante za proporcije
    private static final double STATION_RADIUS_RATIO = 0.03;
    private static final double INTER_STATION_SPACING_RATIO = 0.15;
    private static final double CANVAS_MARGIN_RATIO = 0.05;
    private static final double CITY_BOX_DIM_BASE = 150.0;

    // Dinamički izračunate dimenzije
    private double overallScaleFactor;
    private double currentStationRadius;
    private double currentCityBoxDim;
    private double currentCanvasMargin;
    private double currentInterStationSpacingInCity;
    private double horzCitySpacing;
    private double vertCitySpacing;

    /** Pomoćna klasa za dvodimenzionalne tačke. */
    private static class Point2D {
        double x, y;
        Point2D(double x, double y) { this.x = x; this.y = y; }
    }

    /** Pomoćna klasa za koordinate ulaznih tačaka grada. */
    private static class CityGateways {
        Point2D busGateway;
        Point2D trainGateway;
        Point2D cityCenter;

        CityGateways(Point2D busGateway, Point2D trainGateway, Point2D cityCenter) {
            this.busGateway = busGateway;
            this.trainGateway = trainGateway;
            this.cityCenter = cityCenter;
        }
    }

    /**
     * Konstruktor za {@code GraphRenderer}.
     * <p>Postavlja slušatelje za promjene dimenzija platna kako bi se graf dinamički prilagodio.</p>
     *
     * @param canvas Platno za crtanje.
     * @param transportMap Transportna mapa koja se prikazuje.
     */
    public GraphRenderer(Canvas canvas, TransportMap transportMap) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.transportMap = transportMap;
        this.stationDisplayCoordinates = new HashMap<>();
        this.cityGatewayCoordinates = new HashMap<>();

        this.overallScaleFactor = 1.0;
        this.currentStationRadius = 0;
        this.currentCityBoxDim = 0;
        this.currentCanvasMargin = 0;
        this.currentInterStationSpacingInCity = 0;

        this.canvas.widthProperty().addListener((obs, oldVal, newVal) -> {
            calculateAndLayoutGraph();
            drawInitialMap();
        });
        this.canvas.heightProperty().addListener((obs, oldVal, newVal) -> {
            calculateAndLayoutGraph();
            drawInitialMap();
        });

        calculateAndLayoutGraph();
    }

    /**
     * Izračunava i postavlja sve koordinate elemenata na platnu na osnovu trenutnih dimenzija.
     * <p>Metoda je odgovorna za dinamičko skaliranje i raspored gradova, stanica i "kapija".</p>
     */
    private void calculateAndLayoutGraph() {
        stationDisplayCoordinates.clear();
        cityGatewayCoordinates.clear();

        int numRows = transportMap.getNumRows();
        int numCols = transportMap.getNumCols();

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        currentCanvasMargin = Math.min(canvasWidth, canvasHeight) * CANVAS_MARGIN_RATIO;
        double usableWidth = canvasWidth - (2 * currentCanvasMargin);
        double usableHeight = canvasHeight - (2 * currentCanvasMargin);

        if (numCols == 0 || numRows == 0) {
            currentCityBoxDim = Math.min(usableWidth, usableHeight);
            horzCitySpacing = currentCityBoxDim;
            vertCitySpacing = currentCityBoxDim;
        } else {
            double segmentWidth = usableWidth / numCols;
            double segmentHeight = usableHeight / numRows;
            currentCityBoxDim = Math.min(segmentWidth, segmentHeight) * 0.8;
            horzCitySpacing = usableWidth / numCols;
            vertCitySpacing = usableHeight / numRows;
            if (numCols == 1) horzCitySpacing = currentCityBoxDim;
            if (numRows == 1) vertCitySpacing = currentCityBoxDim;
        }

        currentStationRadius = currentCityBoxDim * STATION_RADIUS_RATIO;
        currentInterStationSpacingInCity = currentCityBoxDim * INTER_STATION_SPACING_RATIO;

        double actualGraphWidth = (numCols - 1) * horzCitySpacing + currentCityBoxDim;
        double actualGraphHeight = (numRows - 1) * vertCitySpacing + currentCityBoxDim;
        double startX = currentCanvasMargin + (usableWidth - actualGraphWidth) / 2;
        double startY = currentCanvasMargin + (usableHeight - actualGraphHeight) / 2;

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                City city = transportMap.getCity(i, j);
                if (city != null) {
                    double cityCenterX = startX + j * horzCitySpacing + currentCityBoxDim / 2;
                    double cityCenterY = startY + i * vertCitySpacing + currentCityBoxDim / 2;

                    Point2D cityCenter = new Point2D(cityCenterX, cityCenterY);
                    double cityBoxX = cityCenterX - currentCityBoxDim / 2;
                    double cityBoxY = cityCenterY - currentCityBoxDim / 2;

                    Point2D busGateway = new Point2D(cityCenterX, cityBoxY);
                    Point2D trainGateway = new Point2D(cityCenterX, cityBoxY + currentCityBoxDim);

                    cityGatewayCoordinates.put(city, new CityGateways(busGateway, trainGateway, cityCenter));

                    List<Station> stationsInCity = transportMap.getAllStations().values().stream()
                            .filter(s -> s.getCity().equals(city))
                            .collect(Collectors.toList());

                    List<BusStation> busStations = stationsInCity.stream()
                            .filter(s -> s instanceof BusStation)
                            .map(s -> (BusStation) s)
                            .collect(Collectors.toList());

                    List<TrainStation> trainStations = stationsInCity.stream()
                            .filter(s -> s instanceof TrainStation)
                            .map(s -> (TrainStation) s)
                            .collect(Collectors.toList());

                    double busY = cityBoxY + currentInterStationSpacingInCity;
                    double busStartX = cityCenterX - (busStations.size() - 1) * currentInterStationSpacingInCity / 2;
                    if (busStartX < cityBoxX + currentStationRadius) {
                        busStartX = cityBoxX + currentStationRadius;
                    }

                    for (int k = 0; k < busStations.size(); k++) {
                        Station station = busStations.get(k);
                        stationDisplayCoordinates.put(station, new Point2D(busStartX + k * currentInterStationSpacingInCity, busY));
                    }

                    double trainY = cityBoxY + currentCityBoxDim - currentInterStationSpacingInCity;
                    double trainStartX = cityCenterX - (trainStations.size() - 1) * currentInterStationSpacingInCity / 2;
                    if (trainStartX < cityBoxX + currentStationRadius) {
                        trainStartX = cityBoxX + currentStationRadius;
                    }

                    for (int k = 0; k < trainStations.size(); k++) {
                        Station station = trainStations.get(k);
                        stationDisplayCoordinates.put(station, new Point2D(trainStartX + k * currentInterStationSpacingInCity, trainY));
                    }
                }
            }
        }
    }

    /**
     * Crta kompletnu transportnu mapu (gradske okvire, veze i stanice) na platnu.
     * <p>Metoda ne crta ID-eve stanica kako bi se održala preglednost.
     * Stari sadržaj platna se briše prije crtanja.</p>
     */
    public void drawInitialMap() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setLineWidth(1.0 * overallScaleFactor);

        // 1. Crtanje gradskih okvira i labela
        for (int i = 0; i < transportMap.getNumRows(); i++) {
            for (int j = 0; j < transportMap.getNumCols(); j++) {
                City city = transportMap.getCity(i, j);
                if (city != null) {
                    CityGateways gateways = cityGatewayCoordinates.get(city);
                    if (gateways != null) {
                        double cityBoxX = gateways.cityCenter.x - currentCityBoxDim / 2;
                        double cityBoxY = gateways.cityCenter.y - currentCityBoxDim / 2;

                        gc.setStroke(Color.DARKGRAY);
                        gc.strokeRect(cityBoxX, cityBoxY, currentCityBoxDim, currentCityBoxDim);

                        gc.setFill(Color.BLACK);
                        gc.setFont(new Font("Arial", 20 * (currentCityBoxDim / CITY_BOX_DIM_BASE)));
                        gc.setTextAlign(TextAlignment.CENTER);
                        gc.fillText(city.getName(), gateways.cityCenter.x, cityBoxY - 5 * (currentCityBoxDim / CITY_BOX_DIM_BASE));
                    }
                }
            }
        }

        // 2. Crtanje međugradskih veza
        gc.setStroke(Color.LIGHTGRAY.darker());
        gc.setLineWidth(1.5 * overallScaleFactor);
        for (int i = 0; i < transportMap.getNumRows(); i++) {
            for (int j = 0; j < transportMap.getNumCols(); j++) {
                City currentCity = transportMap.getCity(i, j);
                if (currentCity == null) continue;
                CityGateways currentCityGateways = cityGatewayCoordinates.get(currentCity);
                if (currentCityGateways == null) continue;

                int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                for (int[] dir : directions) {
                    int neighborX = i + dir[0];
                    int neighborY = j + dir[1];
                    if (neighborX >= 0 && neighborX < transportMap.getNumRows() &&
                            neighborY >= 0 && neighborY < transportMap.getNumCols()) {
                        City neighborCity = transportMap.getCity(neighborX, neighborY);
                        if (neighborCity == null) continue;
                        CityGateways neighborCityGateways = cityGatewayCoordinates.get(neighborCity);
                        if (neighborCityGateways == null) continue;

                        boolean hasBusConnection = transportMap.getAllStations().values().stream()
                                .filter(s -> s instanceof BusStation && s.getCity().equals(currentCity))
                                .anyMatch(s -> s.getDepartures().stream()
                                        .anyMatch(d -> transportMap.getStation(d.getArrivalStationId()) != null &&
                                                transportMap.getStation(d.getArrivalStationId()).getCity().equals(neighborCity) &&
                                                d.getType().equals("autobus")));
                        if (hasBusConnection) {
                            gc.strokeLine(currentCityGateways.busGateway.x, currentCityGateways.busGateway.y,
                                    neighborCityGateways.busGateway.x, neighborCityGateways.busGateway.y);
                        }

                        boolean hasTrainConnection = transportMap.getAllStations().values().stream()
                                .filter(s -> s instanceof TrainStation && s.getCity().equals(currentCity))
                                .anyMatch(s -> s.getDepartures().stream()
                                        .anyMatch(d -> transportMap.getStation(d.getArrivalStationId()) != null &&
                                                transportMap.getStation(d.getArrivalStationId()).getCity().equals(neighborCity) &&
                                                d.getType().equals("voz")));
                        if (hasTrainConnection) {
                            gc.strokeLine(currentCityGateways.trainGateway.x, currentCityGateways.trainGateway.y,
                                    neighborCityGateways.trainGateway.x, neighborCityGateways.trainGateway.y);
                        }
                    }
                }
            }
        }

        // 3. Crtanje unutar-gradskih transfer linija
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.8 * overallScaleFactor);
        for (Station station : transportMap.getAllStations().values()) {
            Point2D startPoint = stationDisplayCoordinates.get(station);
            if (startPoint == null) continue;
            station.getDepartures().stream()
                    .filter(d -> transportMap.getStation(d.getArrivalStationId()) != null)
                    .map(d -> transportMap.getStation(d.getArrivalStationId()))
                    .filter(endStation -> endStation.getCity().equals(station.getCity()))
                    .forEach(endStation -> {
                        Point2D endPoint = stationDisplayCoordinates.get(endStation);
                        if (endPoint != null) {
                            gc.strokeLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
                        }
                    });
        }

        // 4. Crtanje svih stanica (čvorova)
        for (Station station : transportMap.getAllStations().values()) {
            Point2D p = stationDisplayCoordinates.get(station);
            if (p == null) continue;
            if (station instanceof BusStation) {
                gc.setFill(Color.ORANGE);
            } else if (station instanceof TrainStation) {
                gc.setFill(Color.PURPLE);
            } else {
                gc.setFill(Color.BLUE);
            }
            gc.fillOval(p.x - currentStationRadius, p.y - currentStationRadius, 2 * currentStationRadius, 2 * currentStationRadius);
            gc.setStroke(Color.DARKBLUE);
            gc.setLineWidth(1.0 * overallScaleFactor);
            gc.strokeOval(p.x - currentStationRadius, p.y - currentStationRadius, 2 * currentStationRadius, 2 * currentStationRadius);
        }
    }

    /**
     * Ističe datu rutu na mapi.
     * <p>Ponovo crta mapu, a zatim crta i boji linije i stanice koje su dio date putanje.
     * Također, prikazuje ID-eve stanica koje su na putanji.</p>
     *
     * @param route Putanja ({@link Path}) koju treba istaći.
     */
    public void highlightRoute(Path route) {
        drawInitialMap();
        gc.setLineWidth(3.0 * overallScaleFactor);
        Color intercityColor = Color.RED;
        Color intraCityColor = Color.ORANGE;

        for (int i = 0; i < route.getSegments().size(); i++) {
            RouteSegment segment = route.getSegments().get(i);
            Station startStation = segment.getStartStation();
            Station endStation = segment.getEndStation();

            City startCity = startStation.getCity();
            City endCity = endStation.getCity();

            Point2D actualStartPoint = stationDisplayCoordinates.get(startStation);
            Point2D actualEndPoint = stationDisplayCoordinates.get(endStation);

            if (actualStartPoint == null || actualEndPoint == null) {
                System.err.println("Upozorenje: Koordinate za stanicu " + startStation.getId() + " ili " + endStation.getId() + " nisu pronađene.");
                continue;
            }

            if (startCity.equals(endCity)) {
                gc.setStroke(intraCityColor);
                gc.strokeLine(actualStartPoint.x, actualStartPoint.y, actualEndPoint.x, actualEndPoint.y);
            } else {
                CityGateways currentCityGateways = cityGatewayCoordinates.get(startCity);
                CityGateways nextCityGateways = cityGatewayCoordinates.get(endCity);
                if (currentCityGateways != null && nextCityGateways != null) {
                    Point2D segmentStartPointForDrawing = null;
                    Point2D segmentEndPointForDrawing = null;
                    if (segment.getDeparture() != null) {
                        String departureType = segment.getDeparture().getType();
                        if (departureType.equals("autobus")) {
                            segmentStartPointForDrawing = currentCityGateways.busGateway;
                            segmentEndPointForDrawing = nextCityGateways.busGateway;
                        } else if (departureType.equals("voz")) {
                            segmentStartPointForDrawing = currentCityGateways.trainGateway;
                            segmentEndPointForDrawing = nextCityGateways.trainGateway;
                        }
                    }
                    if (segmentStartPointForDrawing != null && segmentEndPointForDrawing != null) {
                        gc.setStroke(intercityColor);
                        gc.strokeLine(segmentStartPointForDrawing.x, segmentStartPointForDrawing.y, segmentEndPointForDrawing.x, segmentEndPointForDrawing.y);
                    } else {
                        System.err.println("Upozorenje: Nije moguće dobiti tip polaska ili koordinate kapija za segment rute: " + startStation.getId() + " -> " + endStation.getId());
                    }
                } else {
                    System.err.println("Upozorenje: Koordinate kapija grada " + startCity.getName() + " ili " + endCity.getName() + " nisu pronađene.");
                }
            }
        }

        for (Station station : transportMap.getAllStations().values()) {
            Point2D p = stationDisplayCoordinates.get(station);
            if (p == null) continue;
            boolean isOnRoute = route.getSegments().stream().anyMatch(s -> s.getStartStation().equals(station) || s.getEndStation().equals(station));

            if (isOnRoute) {
                gc.setFill(Color.GREEN);
            } else {
                if (station instanceof BusStation) {
                    gc.setFill(Color.ORANGE);
                } else if (station instanceof TrainStation) {
                    gc.setFill(Color.PURPLE);
                } else {
                    gc.setFill(Color.BLUE);
                }
            }
            gc.fillOval(p.x - currentStationRadius, p.y - currentStationRadius, 2 * currentStationRadius, 2 * currentStationRadius);
            gc.setStroke(Color.DARKBLUE);
            gc.setLineWidth(1.0 * overallScaleFactor);
            gc.strokeOval(p.x - currentStationRadius, p.y - currentStationRadius, 2 * currentStationRadius, 2 * currentStationRadius);

            if (isOnRoute) {
                gc.setFill(Color.BLACK);
                gc.setFont(new Font("Arial", 12 * overallScaleFactor));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(station.getId(), p.x, p.y + currentStationRadius + 8 * overallScaleFactor);
            }
        }
    }
}