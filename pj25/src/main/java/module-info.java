module project.pj25 {
    // Standardne JavaFX zavisnosti
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires javafx.media;
    requires javafx.swing;

    // Jackson biblioteke za JSON obradu
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310; // Za java.time objekte
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    // EXPORTS: Omogućava da tvoji paketi budu dostupni drugim modulima (npr. JavaFX-u za lansiranje)
    exports project.pj25.gui; // OBRATITI PAŽNJU NA PROVJERU OVDJE ZA SVE PAKETE KOJE SE IZVOZE
    exports project.pj25.model;
    exports project.pj25.data;
    exports project.pj25.algorithm;
    //exports project.pj25.graph; // Izvozi graph paket
    //exports project.pj25.util;  // Izvozi util paket

    // OPENS: Omogućava REFLEKSIVNI pristup specifičnim modulima (kao što je Jackson)
    // PROMENA: 'gui' paket mora biti 'otvoren' za 'javafx.graphics' modul, jer on instancira tvoju Application klasu putem refleksije.
    opens project.pj25.gui to javafx.graphics, javafx.fxml; // <--- KLJUČNA IZMENA!
    // Stara linija 'opens project.pj25 to javafx.fxml, com.fasterxml.jackson.databind;'
    // ti verovatno ne treba, osim ako nemaš FXML fajlove koji se nalaze direktno u project.pj25 paketu.
    // Ako se FXML fajlovi i Application klasa nalaze isključivo u 'project.pj25.gui', onda ti ova linija ne treba.
    // Ostavljam je zakomentarisanu u primeru ispod da ti bude jasnije.
    // Ako imaš još neke JavaFX specifične stvari, pazi na njih
    // Npr. ako je fxml fajl u resources/project.pj25, onda samo opens project.pj25 to javafx.fxml
}