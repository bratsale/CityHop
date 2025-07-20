package project.pj25.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.List; // Ovo je za Jackson
import project.pj25.util.DurationDeserializer; // Kreiraćemo ovu klasu
import project.pj25.util.LocalTimeDeserializer; // Kreiraćemo ovu klasu

public class Departure {
    private String type; // "autobus" ili "voz"
    private String departureStationId; // ID stanice polaska (B_X_Y ili T_X_Y)
    private String arrivalStationId;   // ID stanice dolaska (može biti u drugom gradu ili ista stanica/grad za transfer)
    @JsonDeserialize(using = LocalTimeDeserializer.class) // Custom deserializer
    private LocalTime departureTime;

    @JsonDeserialize(using = LocalTimeDeserializer.class) // Custom deserializer
    private LocalTime arrivalTime;

    private double price;              // Cijena karte
    @JsonDeserialize(using = DurationDeserializer.class) // Custom deserializer
    private Duration minTransferTime;

    // Glavni konstruktor - OVAKO TREBA DA IZGLEDA
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

    // Prazan konstruktor - OVAKO TREBA DA IZGLEDA (za Jackson)
    public Departure() {
        // Prazan konstruktor za Jackson
    }

    // --- Getteri i Setteri ---
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDepartureStationId() { return departureStationId; }
    public void setDepartureStationId(String departureStationId) { this.departureStationId = departureStationId; }

    public String getArrivalStationId() { return arrivalStationId; }
    public void setArrivalStationId(String arrivalStationId) { this.arrivalStationId = arrivalStationId; }

    public LocalTime getDepartureTime() { return departureTime; }
    // Jackson će koristiti custom deserializer, ali ako želiš setter za druge svrhe, možeš ga ostaviti
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }

    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Duration getMinTransferTime() { return minTransferTime; }
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
                ", minTransfer=" + (minTransferTime != null ? minTransferTime.toMinutes() + "min" : "N/A") +
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