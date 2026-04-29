package com.atservis;

import com.atservis.view.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * AT-Servis ML Recommendation System — Desktop Application
 * Ishga tushirish: mvn javafx:run
 */
public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MainWindow window = new MainWindow();
        window.show(primaryStage);
    }
}
