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
import java.util.Random; // Nije više neophodan, ali može ostati
import java.util.stream.Collectors;

public class GraphRenderer {
    private Canvas canvas;
    private GraphicsContext gc;
    private TransportMap transportMap;

    private Map<Station, Point2D> stationDisplayCoordinates;
    private Map<City, CityGateways> cityGatewayCoordinates;

    // Fiksne konstante - ove sada postaju VIŠE "odnosne" nego apsolutne veličine
    private static final double STATION_RADIUS_RATIO = 0.03; // Udeo radijusa stanice u veličini gradske kutije
    private static final double INTER_STATION_SPACING_RATIO = 0.15; // Udeo razmaka stanica u veličini gradske kutije
    private static final double CANVAS_MARGIN_RATIO = 0.05; // Udeo margine u ukupnoj dimenziji Canvasa
    private static final double CITY_BOX_DIM_BASE = 150.0; // Bazna dimenzija stranice "kvadrata" grada - REFERENTNA VREDNOST

    // Dinamički izračunati faktori i dimenzije
    private double overallScaleFactor; // Sada manje bitno, jer se dimenzije direktno računaju
    private double currentStationRadius;
    private double currentCityBoxDim; // Dinamička dimenzija kutije grada
    private double currentCanvasMargin;
    private double currentInterStationSpacingInCity;

    private double horzCitySpacing; // Stvarni razmak između centara gradova
    private double vertCitySpacing;

    // Pomoćne klase za koordinate
    private static class Point2D {
        double x, y;
        Point2D(double x, double y) { this.x = x; this.y = y; }
    }

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

    public GraphRenderer(Canvas canvas, TransportMap transportMap) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.transportMap = transportMap;
        this.stationDisplayCoordinates = new HashMap<>();
        this.cityGatewayCoordinates = new HashMap<>();

        // Inicijalizacija default vrednosti (ove će se odmah prepisati)
        this.overallScaleFactor = 1.0;
        this.currentStationRadius = 0; // Inicijalizacija na 0 jer će se izračunati
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

    private void calculateAndLayoutGraph() {
        stationDisplayCoordinates.clear();
        cityGatewayCoordinates.clear();

        int numRows = transportMap.getNumRows();
        int numCols = transportMap.getNumCols();

        // Osnovne dimenzije Canvasa
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        // Izračunaj dinamičku marginu baziranu na dimenzijama Canvasa
        currentCanvasMargin = Math.min(canvasWidth, canvasHeight) * CANVAS_MARGIN_RATIO;

        // Dostupni prostor za mrežu gradova (Canvas minus margine)
        double usableWidth = canvasWidth - (2 * currentCanvasMargin);
        double usableHeight = canvasHeight - (2 * currentCanvasMargin);

        // Izračunaj optimalnu dimenziju jedne gradske kutije
        // Ako je samo jedan grad, on zauzima ceo usableSpace
        if (numCols == 0 || numRows == 0) { // Ne bi trebalo da se desi, ali za sigurnost
            currentCityBoxDim = Math.min(usableWidth, usableHeight);
            horzCitySpacing = currentCityBoxDim; // Nema razmaka, samo dimenzija
            vertCitySpacing = currentCityBoxDim;
        } else {
            // Izračunaj maksimalnu dimenziju kutije po širini i visini
            // (N-1) je broj "razmaka" između N gradova
            // Pretpostavljamo da želimo da `currentCityBoxDim` bude jednaka i horizontalno i vertikalno
            // `(numCols - 1)` * (razmak_izmedju_centara) + `currentCityBoxDim` = `usableWidth`
            // `currentCityBoxDim` + `razmak_procenat_od_kutije` * `currentCityBoxDim`
            // Pokušajmo da svaki grad + razmak bude isti "segment"
            double segmentWidth = usableWidth / numCols;
            double segmentHeight = usableHeight / numRows;

            // Određujemo dimenziju kutije kao manju od ova dva segmenta
            currentCityBoxDim = Math.min(segmentWidth, segmentHeight);

            // Prilagodite currentCityBoxDim da omogući i razmake.
            // Npr., neka kutija zauzima 80% prostora segmenta, a 20% je razmak
            currentCityBoxDim = currentCityBoxDim * 0.8; // Prilagodite ovaj faktor (npr. 0.7-0.9)

            // Razmak između centara gradova će biti dimenzija segmenta
            horzCitySpacing = usableWidth / numCols;
            vertCitySpacing = usableHeight / numRows;

            // Ako je samo 1 grad, horz/vertCitySpacing treba da bude jednak currentCityBoxDim
            if (numCols == 1) horzCitySpacing = currentCityBoxDim;
            if (numRows == 1) vertCitySpacing = currentCityBoxDim;
        }


        // Ažuriraj dinamičke dimenzije na osnovu izračunatog currentCityBoxDim
        currentStationRadius = currentCityBoxDim * STATION_RADIUS_RATIO;
        currentInterStationSpacingInCity = currentCityBoxDim * INTER_STATION_SPACING_RATIO;

        // Podesi početnu X i Y poziciju da centriras mrežu gradova
        // Totalna širina/visina koju graf stvarno zauzima (od početka prve kutije do kraja poslednje)
        double actualGraphWidth = (numCols - 1) * horzCitySpacing + currentCityBoxDim;
        double actualGraphHeight = (numRows - 1) * vertCitySpacing + currentCityBoxDim;

        // Centriranje unutar raspoloživog prostora Canvasa
        double startX = currentCanvasMargin + (usableWidth - actualGraphWidth) / 2;
        double startY = currentCanvasMargin + (usableHeight - actualGraphHeight) / 2;


        // Rasporedi gradove, stanice i kapije
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                City city = transportMap.getCity(i, j);
                if (city != null) {
                    // Centralna tačka grada u skaliranom prostoru
                    double cityCenterX = startX + j * horzCitySpacing + currentCityBoxDim / 2;
                    double cityCenterY = startY + i * vertCitySpacing + currentCityBoxDim / 2;

                    Point2D cityCenter = new Point2D(cityCenterX, cityCenterY);

                    // Gornji levi ugao gradske kutije
                    double cityBoxX = cityCenterX - currentCityBoxDim / 2;
                    double cityBoxY = cityCenterY - currentCityBoxDim / 2;

                    // Pozicije kapija (na ivicama gradskog okvira)
                    Point2D busGateway = new Point2D(cityCenterX, cityBoxY); // Gornja ivica
                    Point2D trainGateway = new Point2D(cityCenterX, cityBoxY + currentCityBoxDim); // Donja ivica

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

                    // Raspored autobuskih stanica (npr. gornji red, horizontalno centrirano)
                    double busY = cityBoxY + currentInterStationSpacingInCity;
                    double busStartX = cityCenterX - (busStations.size() - 1) * currentInterStationSpacingInCity / 2;
                    if (busStartX < cityBoxX + currentStationRadius) { // Osiguraj da ne izađe iz kutije
                        busStartX = cityBoxX + currentStationRadius;
                    }

                    for (int k = 0; k < busStations.size(); k++) {
                        Station station = busStations.get(k);
                        stationDisplayCoordinates.put(station, new Point2D(busStartX + k * currentInterStationSpacingInCity, busY));
                    }

                    // Raspored železničkih stanica (npr. donji red, horizontalno centrirano)
                    double trainY = cityBoxY + currentCityBoxDim - currentInterStationSpacingInCity;
                    double trainStartX = cityCenterX - (trainStations.size() - 1) * currentInterStationSpacingInCity / 2;
                    if (trainStartX < cityBoxX + currentStationRadius) { // Osiguraj da ne izađe iz kutije
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
                        // Smanjivanje veličine fonta za imena gradova za bolju preglednost
                        gc.setFont(new Font("Arial", 20 * (currentCityBoxDim / CITY_BOX_DIM_BASE))); // Faktor skale je sada vezan za kutiju
                        gc.setTextAlign(TextAlignment.CENTER);
                        // Pomeranje labela malo niže unutar kutije ako je to potrebno, ali za sad ostavljamo ovako
                        gc.fillText(city.getName(), gateways.cityCenter.x, cityBoxY - 5 * (currentCityBoxDim / CITY_BOX_DIM_BASE));
                    }
                }
            }
        }

        // 2. Crtanje međugradskih veza (između "kapija")
        gc.setStroke(Color.LIGHTGRAY.darker());
        gc.setLineWidth(1.5 * overallScaleFactor);

        for (int i = 0; i < transportMap.getNumRows(); i++) {
            for (int j = 0; j < transportMap.getNumCols(); j++) {
                City currentCity = transportMap.getCity(i, j);
                if (currentCity == null) continue;

                CityGateways currentCityGateways = cityGatewayCoordinates.get(currentCity);
                if (currentCityGateways == null) continue;

                int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Gore, dole, levo, desno
                for (int[] dir : directions) {
                    int neighborX = i + dir[0];
                    int neighborY = j + dir[1];

                    if (neighborX >= 0 && neighborX < transportMap.getNumRows() &&
                            neighborY >= 0 && neighborY < transportMap.getNumCols()) {

                        City neighborCity = transportMap.getCity(neighborX, neighborY);
                        if (neighborCity == null) continue;

                        CityGateways neighborCityGateways = cityGatewayCoordinates.get(neighborCity);
                        if (neighborCityGateways == null) continue;

                        // Provera postojanja autobuske veze
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

                        // Provera postojanja železničke veze
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

            // NE CRTAMO station.getId() na drawInitialMap() da smanjimo pretrpanost.
            // ID-evi će biti prikazani samo kada je ruta istaknuta.
        }
    }

    // Ukloni @Override ako GraphRenderer ne implementira interfejs
    // ili ne proširuje klasu koja deklariše highlightRoute metodu.
    // Ako postoji interfejs, npr. MapRenderer, onda dodaj 'implements MapRenderer'
    // na deklaraciju klase GraphRenderer.
    // @Override
    public void highlightRoute(Path route) {
        // Ponovo nacrtaj početnu mapu bez ID-eva stanica
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
                System.err.println("Upozorenje: Koordinate za stanicu " + startStation.getId() + " ili " + endStation.getId() + " nisu pronađene prilikom highlighta rute.");
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
                    System.err.println("Upozorenje: Koordinate kapija grada " + startCity.getName() + " ili " + endCity.getName() + " nisu pronađene prilikom highlighta rute.");
                }
            }
        }

        // Ponovno crtanje svih stanica, ali ovoga puta sa isticanjem i prikazom ID-a za stanice na ruti
        for (Station station : transportMap.getAllStations().values()) {
            Point2D p = stationDisplayCoordinates.get(station);
            if (p == null) continue;

            boolean isOnRoute = route.getSegments().stream().anyMatch(s ->
                    s.getStartStation().equals(station) || s.getEndStation().equals(station)
            );

            if (isOnRoute) {
                gc.setFill(Color.GREEN); // Zelena za stanice na ruti
            } else {
                // Originalne boje za stanice koje nisu na ruti (nacrtane u drawInitialMap(), ali se ponavljaju da se osigura redosled)
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


            if (isOnRoute) { // SAMO AKO JE STANICA NA RUTI, PRIKAŽI ID
                gc.setFill(Color.BLACK);
                gc.setFont(new Font("Arial", 12 * overallScaleFactor));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(station.getId(), p.x, p.y + currentStationRadius + 8 * overallScaleFactor);
            }
        }
    }
}