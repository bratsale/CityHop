package project.pj25.model;

// Uklonite ove importe ako ih ne koristite direktno za anotacije parametara konstruktora
// import com.fasterxml.jackson.annotation.JsonCreator;
// import com.fasterxml.jackson.annotation.JsonProperty;

public class BusStation extends Station {

    // Konstruktor za generisanje podataka (koristi ga TransportDataGenerator)
    public BusStation(String id, City city) {
        super(id, city, "autobus"); // Poziva glavni konstruktor nadklase
    }

    // Prazan konstruktor za Jackson deserializaciju
    public BusStation() {
        super(); // Poziva prazan konstruktor nadklase, koji SADA inicijalizuje 'departures'
        this.type = "autobus"; // Postavlja tip
    }
}