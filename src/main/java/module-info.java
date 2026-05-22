module com.autoatelier {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.annotation;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    requires java.net.http;
    requires java.prefs;

    opens com.autoatelier to javafx.fxml;
    opens com.autoatelier.controller to javafx.fxml;
    opens com.autoatelier.controller.components to javafx.fxml;
    opens com.autoatelier.controller.client to javafx.fxml;
    opens com.autoatelier.controller.manager to javafx.fxml;
    opens com.autoatelier.controller.admin to javafx.fxml;

    opens com.autoatelier.model to com.fasterxml.jackson.databind;
    opens com.autoatelier.service to com.fasterxml.jackson.databind;

    exports com.autoatelier;
    exports com.autoatelier.model;
}
