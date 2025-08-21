module project.pj25 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires javafx.media;
    requires javafx.swing;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    exports project.pj25.gui;
    exports project.pj25.model;
    exports project.pj25.data;
    exports project.pj25.algorithm;
    exports project.pj25.util;


    opens project.pj25.gui to javafx.graphics, javafx.fxml;

    opens project.pj25.model to com.fasterxml.jackson.databind;

    opens project.pj25.data to com.fasterxml.jackson.databind;

    opens project.pj25.util to com.fasterxml.jackson.databind;

}