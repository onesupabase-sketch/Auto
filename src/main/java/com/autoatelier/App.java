package com.autoatelier;

import com.autoatelier.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneManager.init(primaryStage);
        SceneManager.navigate("login");
        primaryStage.setTitle("Ателье Автомобилей");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
