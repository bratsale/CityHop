package project.pj25.model;

import java.util.List;

public interface Station {
    String getId(); // Jedinstveni ID stanice (npr. "A_X_Y" ili "Z_X_Y")
    City getCity(); // Grad kojem stanica pripada
    List<Departure> getDepartures(); // Lista svih polazaka sa ove stanice
    void addDeparture(Departure departure); // Metoda za dodavanje polaska
}