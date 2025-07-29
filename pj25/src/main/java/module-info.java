module project.pj25 {
    // Standardne JavaFX zavisnosti
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires javafx.media;
    requires javafx.swing; // Ako koristite Swing komponente unutar JavaFX aplikacije

    // Jackson biblioteke za JSON obradu
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310; // Za java.time objekte
    requires com.fasterxml.jackson.core;         // Iako je obično tranzitivna, eksplicitno je korisno
    requires com.fasterxml.jackson.annotation;    // Iako je obično tranzitivna, eksplicitno je korisno

    // EXPORTS: Omogućava da tvoji paketi budu dostupni drugim modulima
    // Ovo je za API vidljivost i kompajliranje/linkovanje
    exports project.pj25.gui;
    exports project.pj25.model;
    exports project.pj25.data;
    exports project.pj25.algorithm;
    exports project.pj25.util; // Dodato: Tvoji serializatori/deserializatori su ovde
    // exports project.pj25.graph; // Zakomentarisano ako ga ne koristiš

    // OPENS: Omogućava REFLEKSIVNI pristup specifičnim modulima (kao što su Jackson i JavaFX)
    // Ovo je ključno za runtime pristup privatnim poljima, metodama itd.

    // Otvara GUI paket za JavaFX runtime (za fxml učitavanje i pokretanje aplikacije)
    opens project.pj25.gui to javafx.graphics, javafx.fxml;

    // Otvara model paket za Jackson (omogućava pristup privatnim poljima i konstruktorima)
    opens project.pj25.model to com.fasterxml.jackson.databind;

    // Otvara data paket za Jackson (ako serijalizujete/deserijalizujete klase direktno iz data paketa)
    opens project.pj25.data to com.fasterxml.jackson.databind;

    // Otvara util paket za Jackson (za custom serijalizatore/deserijalizatore)
    opens project.pj25.util to com.fasterxml.jackson.databind;

    // Ako imate FXML fajlove u root paketu (project.pj25), onda vam treba i ovo:
    // opens project.pj25 to javafx.fxml;
}