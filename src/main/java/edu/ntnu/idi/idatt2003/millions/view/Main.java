package edu.ntnu.idi.idatt2003.millions.view;

import java.net.URL;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Launches the Millions UI.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        DashboardPage dashboard = new DashboardPage();
        Parent root = dashboard.createRoot();
        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(resolveStylesheet("/styles/dashboard.css"));
        stage.setTitle("Millions - Stock Trading Game");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static String resolveStylesheet(String path) {
        URL url = Main.class.getResource(path);
        if (url == null) {
            throw new IllegalStateException("Missing stylesheet: " + path);
        }
        return url.toExternalForm();
    }
}


