package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.io.StockCsvLoader;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Launches the Millions UI.
 */
public class Main extends Application {

    private static final String DASHBOARD_STYLESHEET = "/styles/dashboard.css";
    private static final String STARTPAGE_STYLESHEET = "/styles/startpage.css";
    private static final String STOCK_RESOURCE = "data/sp500.csv";

    @Override
    public void start(Stage stage) {
        StartPage startPage = new StartPage();
        Scene scene = new Scene(
            startPage.createRoot((name, startingMoney) ->
                launchDashboard(stage, name, startingMoney)),
            1024,
            768
        );
        scene.getStylesheets().add(resolveStylesheet(STARTPAGE_STYLESHEET));
        stage.setTitle("Millions - Stock Trading Game");
        stage.setScene(scene);
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

    private void launchDashboard(Stage stage, String name, BigDecimal startingMoney) {
        Player player;
        try {
            player = new Player(name, startingMoney);
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
            return;
        }

        List<Stock> stocks;
        StockCsvLoader loader = new StockCsvLoader();
        try {
            stocks = loader.loadFromResource(STOCK_RESOURCE);
        } catch (IOException exception) {
            showError("Failed to load stock data: " + exception.getMessage());
            return;
        }

        Exchange exchange = new Exchange("S&P 500", stocks);
        ExchangeController controller = new ExchangeController(exchange, player);

        DashboardPage dashboard = new DashboardPage(controller);
        Parent root = dashboard.createRoot();
        Scene scene = stage.getScene();
        if (scene == null) {
            showError("No active scene available to load the dashboard.");
            return;
        }
        scene.setRoot(root);
        scene.getStylesheets().setAll(resolveStylesheet(DASHBOARD_STYLESHEET));
        stage.setMaximized(true);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Start Game");
        alert.setHeaderText("Unable to start the game");
        alert.setContentText(message);
        alert.showAndWait();
    }
}


