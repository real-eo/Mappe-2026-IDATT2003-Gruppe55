package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.io.StockCsvLoader;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.GameState;
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
        Scene scene = new Scene(new StartPage().createRoot(null, null), 1024, 768);
        stage.setTitle("Millions - Stock Trading Game");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        showStartPage(stage);
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

    private void showStartPage(Stage stage) {
        StartPage startPage = new StartPage();
        Parent root = startPage.createRoot(
            (name, startingMoney) -> launchDashboard(stage, name, startingMoney),
            () -> showLoadPage(stage)
        );
        Scene scene = stage.getScene();
        if (scene == null) {
            showError("No active scene available to load the start page.");
            return;
        }
        scene.setRoot(root);
        scene.getStylesheets().setAll(resolveStylesheet(STARTPAGE_STYLESHEET));
        stage.setMaximized(true);
    }

    private void showLoadPage(Stage stage) {
        LoadGamePage loadGamePage = new LoadGamePage(
            state -> launchDashboard(stage, state),
            () -> showStartPage(stage)
        );
        Parent root = loadGamePage.createRoot();
        Scene scene = stage.getScene();
        if (scene == null) {
            showError("No active scene available to load the save list.");
            return;
        }
        scene.setRoot(root);
        scene.getStylesheets().setAll(resolveStylesheet(STARTPAGE_STYLESHEET));
        stage.setMaximized(true);
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
        showDashboard(stage, controller);
    }

    private void launchDashboard(Stage stage, GameState state) {
        if (state == null) {
            showError("Unable to load the selected save.");
            return;
        }
        ExchangeController controller = new ExchangeController(state.getExchange(), state.getPlayer());
        showDashboard(stage, controller);
    }

    private void showDashboard(Stage stage, ExchangeController controller) {
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


