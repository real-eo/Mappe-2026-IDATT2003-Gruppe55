package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

/**
 * Main JavaFX application view for the Millions stock market game.
 */
public class MainApp extends Application {

    private ExchangeController controller;
    private Label weekLabel;
    private Label balanceLabel;
    private ListView<String> stockListView;

    @Override
    public void init() {
        Exchange exchange = new Exchange("Oslo Stock Exchange");
        exchange.addStock(new Stock("EQNR", "Equinor ASA", new BigDecimal("280.00")));
        exchange.addStock(new Stock("DNB", "DNB Bank ASA", new BigDecimal("215.50")));
        exchange.addStock(new Stock("TEL", "Telenor ASA", new BigDecimal("133.20")));
        Player player = new Player("Player 1", new BigDecimal("100000"));
        controller = new ExchangeController(exchange, player);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top: week and balance info
        weekLabel = new Label("Week: 1");
        balanceLabel = new Label("Balance: 100000.00");
        HBox infoBar = new HBox(20, weekLabel, balanceLabel);
        infoBar.setPadding(new Insets(5));
        root.setTop(infoBar);

        // Center: stock list
        stockListView = new ListView<>();
        refreshStockList();
        root.setCenter(stockListView);

        // Bottom: controls
        TextField symbolField = new TextField();
        symbolField.setPromptText("Symbol");
        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");
        Button buyBtn = new Button("Buy");
        Button sellBtn = new Button("Sell");
        Button advanceBtn = new Button("Next Week");

        buyBtn.setOnAction(e -> handleTransaction(symbolField.getText(), qtyField.getText(), true));
        sellBtn.setOnAction(e -> handleTransaction(symbolField.getText(), qtyField.getText(), false));
        advanceBtn.setOnAction(e -> {
            controller.advance();
            refreshStockList();
            weekLabel.setText("Week: " + controller.getExchange().getWeek());
        });

        HBox controls = new HBox(10, symbolField, qtyField, buyBtn, sellBtn, advanceBtn);
        controls.setPadding(new Insets(5));
        root.setBottom(controls);

        primaryStage.setTitle("Millions - Stock Market Game");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    private void handleTransaction(String symbol, String qtyText, boolean isBuy) {
        try {
            int qty = Integer.parseInt(qtyText.trim());
            if (isBuy) {
                controller.buy(symbol.trim(), qty);
            } else {
                controller.sell(symbol.trim(), qty);
            }
            balanceLabel.setText("Balance: " + controller.getPlayer().getMoney());
            refreshStockList();
        } catch (NumberFormatException e) {
            showAlert("Invalid quantity: " + qtyText);
        } catch (MillionsException e) {
            showAlert(e.getMessage());
        }
    }

    private void refreshStockList() {
        stockListView.getItems().clear();
        List<Stock> stocks = controller.findStocks("");
        for (Stock stock : stocks) {
            stockListView.getItems().add(stock.toString());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
