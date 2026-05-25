package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.calculator.PurchaseCalculator;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.UnaryOperator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Modal dialog for buying shares of a stock.
 */
public final class BuyStockDialog {

    private static final String STYLESHEET_PATH = "/styles/dashboard.css";

    private final ExchangeController controller;
    private final Stock stock;
    private final Runnable onTradeComplete;

    private Stage stage;
    private TextField quantityField;
    private Label totalValue;

    public BuyStockDialog(ExchangeController controller, Stock stock, Runnable onTradeComplete) {
        this.controller = controller;
        this.stock = stock;
        this.onTradeComplete = onTradeComplete;
    }

    public void show(Window owner) {
        if (controller == null || stock == null) {
            return;
        }

        stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }

        StackPane root = buildRoot();
        Scene scene;
        if (owner != null) {
            scene = new Scene(root, owner.getWidth(), owner.getHeight());
            stage.setX(owner.getX());
            stage.setY(owner.getY());
        } else {
            scene = new Scene(root);
        }
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(resolveStylesheet());

        stage.setScene(scene);
        stage.setResizable(false);
        if (owner == null) {
            stage.sizeToScene();
            centerStage();
        }
        stage.showAndWait();
    }

    private StackPane buildRoot() {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("trade-overlay");
        overlay.setPadding(new Insets(40));

        StackPane card = new StackPane();
        card.getStyleClass().add("trade-dialog-card");
        card.setMaxWidth(520);
        card.setPrefWidth(520);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setPrefHeight(Region.USE_COMPUTED_SIZE);
        card.setMinHeight(Region.USE_PREF_SIZE);
        card.setOnMouseClicked(event -> event.consume());

        VBox content = new VBox(12);
        content.setPadding(new Insets(18));
        content.setFillWidth(true);

        VBox header = new VBox(4);
        Label title = new Label("Buy " + stock.getSymbol());
        title.getStyleClass().add("trade-title");
        Label subtitle = new Label(stock.getCompanyName());
        subtitle.getStyleClass().add("trade-subtitle");
        header.getChildren().addAll(title, subtitle);

        VBox pricePanel = new VBox(2);
        pricePanel.getStyleClass().add("trade-price-panel");
        Label priceLabel = new Label("Current Price");
        priceLabel.getStyleClass().add("trade-price-label");
        Label priceValue = new Label(formatPrice(stock.getSalesPrice()));
        priceValue.getStyleClass().add("trade-price-value");
        pricePanel.getChildren().addAll(priceLabel, priceValue);

        Label sharesLabel = new Label("Number of Shares");
        sharesLabel.getStyleClass().add("trade-field-label");

        quantityField = new TextField();
        quantityField.setPromptText("0");
        quantityField.getStyleClass().add("trade-input-field");
        quantityField.setTextFormatter(new TextFormatter<>(numericFilter()));

        HBox quantityBox = new HBox(quantityField);
        quantityBox.getStyleClass().add("trade-input-container");
        HBox.setHgrow(quantityField, Priority.ALWAYS);

        HBox totalRow = new HBox(10);
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label totalLabel = new Label("Total (incl. fees & tax)");
        totalLabel.getStyleClass().add("trade-price-label");
        totalValue = new Label("--");
        totalValue.getStyleClass().add("trade-price-value");
        Region totalSpacer = new Region();
        HBox.setHgrow(totalSpacer, Priority.ALWAYS);
        totalRow.getChildren().addAll(totalLabel, totalSpacer, totalValue);

        HBox actions = new HBox(10);
        actions.getStyleClass().add("trade-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("secondary-button");
        Button confirm = new Button("Confirm Purchase");
        confirm.getStyleClass().add("primary-button");
        confirm.setDefaultButton(true);

        actions.getChildren().addAll(cancel, confirm);

        content.getChildren().addAll(header, pricePanel, sharesLabel, quantityBox, totalRow, actions);
        card.getChildren().add(content);

        cancel.setOnAction(event -> stage.close());
        confirm.setOnAction(event -> handleConfirm());
        quantityField.setOnAction(event -> handleConfirm());
        quantityField.textProperty().addListener((obs, oldValue, newValue) -> updateTotals());
        updateTotals();

        overlay.setOnMouseClicked(event -> stage.close());

        overlay.getChildren().add(card);
        return overlay;
    }

    private void centerStage() {
        stage.centerOnScreen();
    }

    private void updateTotals() {
        if (totalValue == null || stock == null) {
            return;
        }

        BigDecimal quantity = parseQuantity(quantityField.getText());
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            totalValue.setText("--");
            return;
        }

        Share share = new Share(stock, quantity, stock.getSalesPrice());
        PurchaseCalculator calculator = new PurchaseCalculator(share);
        totalValue.setText(formatPrice(calculator.getTotal()));
    }

    private void handleConfirm() {
        String rawQuantity = quantityField.getText() == null ? "" : quantityField.getText().trim();
        if (rawQuantity.isEmpty()) {
            showValidationError("Please enter the number of shares to buy.");
            return;
        }

        BigDecimal quantity;
        try {
            quantity = new BigDecimal(rawQuantity);
        } catch (NumberFormatException exception) {
            showValidationError("Share quantity must be a valid number.");
            return;
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            showValidationError("Share quantity must be greater than zero.");
            return;
        }

        try {
            controller.buy(stock.getSymbol(), quantity);
            if (onTradeComplete != null) {
                onTradeComplete.run();
            }
            stage.close();
        } catch (MillionsException exception) {
            showValidationError(exception.getMessage());
        }
    }

    private static UnaryOperator<TextFormatter.Change> numericFilter() {
        return change -> {
            String nextText = change.getControlNewText();
            return nextText.matches("\\d*(\\.\\d{0,4})?") ? change : null;
        };
    }

    private static BigDecimal parseQuantity(String rawQuantity) {
        if (rawQuantity == null) {
            return null;
        }

        String trimmed = rawQuantity.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String formatPrice(BigDecimal price) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "$" + format.format(price);
    }

    private static String resolveStylesheet() {
        var url = BuyStockDialog.class.getResource(STYLESHEET_PATH);
        if (url == null) {
            throw new IllegalStateException("Missing stylesheet: " + STYLESHEET_PATH);
        }
        return url.toExternalForm();
    }

    private static void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Buy Shares");
        alert.setHeaderText("Unable to complete the purchase");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
