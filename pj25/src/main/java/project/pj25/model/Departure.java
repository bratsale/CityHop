package project.pj25.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

public class Departure {
    private String type; // "autobus" ili "voz"
    private String departureStationId; // ID stanice polaska (A_X_Y ili Z_X_Y)
    private String arrivalStationId;   // ID stanice dolaska (može biti u drugom gradu ili ista stanica/grad za transfer)
    private LocalTime departureTime;   // Vrijeme polaska
    private LocalTime arrivalTime;     // Vrijeme dolaska
    private double price;              // Cijena karte
    private Duration minTransferTime;  // Minimalno vrijeme čekanja za presjedanje

    public Departure(String type, String departureStationId, String arrivalStationId,
                     LocalTime departureTime, LocalTime arrivalTime, double price, Duration minTransferTime) {
        this.type = type;
        this.departureStationId = departureStationId;
        this.arrivalStationId = arrivalStationId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.minTransferTime = minTransferTime;
    }

    // Važno: Jackson će trebati no-arg konstruktor za deserijalizaciju ako ne koristite @JsonCreator anotacije
    public Departure() {
        // Prazan konstruktor za Jackson
    }

    // --- Getteri ---
    public String getType() {
        return type;
    }

    public String getDepartureStationId() {
        return departureStationId;
    }

    public String getArrivalStationId() {
        return arrivalStationId;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public double getPrice() {
        return price;
    }

    public Duration getMinTransferTime() {
        return minTransferTime;
    }

    // --- Setteri (Jackson ih koristi za deserijalizaciju) ---
    public void setType(String type) { this.type = type; }
    public void setDepartureStationId(String departureStationId) { this.departureStationId = departureStationId; }
    public void setArrivalStationId(String arrivalStationId) { this.arrivalStationId = arrivalStationId; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
    public void setPrice(double price) { this.price = price; }
    public void setMinTransferTime(Duration minTransferTime) { this.minTransferTime = minTransferTime; }

    @Override
    public String toString() {
        return "Departure{" +
                "type='" + type + '\'' +
                ", from='" + departureStationId + '\'' +
                ", to='" + arrivalStationId + '\'' +
                ", depTime=" + departureTime +
                ", arrTime=" + arrivalTime +
                ", price=" + String.format("%.2f", price) +
                ", minTransfer=" + minTransferTime.toMinutes() + "min" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Departure departure = (Departure) o;
        return Double.compare(price, departure.price) == 0 &&
                Objects.equals(type, departure.type) &&
                Objects.equals(departureStationId, departure.departureStationId) &&
                Objects.equals(arrivalStationId, departure.arrivalStationId) &&
                Objects.equals(departureTime, departure.departureTime) &&
                Objects.equals(arrivalTime, departure.arrivalTime) &&
                Objects.equals(minTransferTime, departure.minTransferTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, departureStationId, arrivalStationId, departureTime, arrivalTime, price, minTransferTime);
    }
}