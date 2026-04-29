module com.atservis {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires org.apache.poi.ooxml;
    requires com.google.gson;
    requires java.desktop;

    opens com.atservis to javafx.fxml;
    opens com.atservis.model to com.google.gson, javafx.base;
    opens com.atservis.controller to javafx.fxml;

    exports com.atservis;
    exports com.atservis.model;
    exports com.atservis.service;
    exports com.atservis.view;
}
