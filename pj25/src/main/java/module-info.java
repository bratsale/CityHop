module project.pj25 {
    // Standardne JavaFX zavisnosti
    requires javafx.controls;
    requires javafx.fxml;

    // Jackson biblioteke za JSON obradu
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310; // Za java.time objekte

    // EXPORTS: Omogućava da tvoji paketi budu dostupni drugim modulima (npr. JavaFX-u za lansiranje)
    exports project.pj25; // Glavni paket za JavaFX
    exports project.pj25.model; // Izvozi model paket
    exports project.pj25.data;  // Izvozi data paket
    //exports project.pj25.graph; // Izvozi graph paket
    //exports project.pj25.util;  // Izvozi util paket

    // OPENS: Omogućava REFLEKSIVNI pristup specifičnim modulima (kao što je Jackson)
    // Ovo je ključno za Jackson da može da čita i piše tvoje objekte!
    opens project.pj25 to javafx.fxml, com.fasterxml.jackson.databind; // Glavni paket za FXML i Jackson
    opens project.pj25.model to com.fasterxml.jackson.databind; // OMOGUĆAVA JACKSONU PRISTUP TVOJIM MODELIMA!
    opens project.pj25.data to com.fasterxml.jackson.databind;  // Ako će Jackson serijalizovati/deserijalizovati stvari iz data paketa
    //opens project.pj25.graph to com.fasterxml.jackson.databind; // Ako će Jackson serijalizovati/deserijalizovati stvari iz graph paketa

    // Ako imaš još neke JavaFX specifične stvari, pazi na njih
    // Npr. ako je fxml fajl u resources/project.pj25, onda samo opens project.pj25 to javafx.fxml
}