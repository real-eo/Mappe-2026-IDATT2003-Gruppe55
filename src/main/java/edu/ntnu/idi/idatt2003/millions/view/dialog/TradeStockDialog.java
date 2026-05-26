package edu.ntnu.idi.idatt2003.millions.view.dialog;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.UnaryOperator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public abstract class TradeStockDialog extends MillionsDialog {

    protected final ExchangeController controller;
    protected final Stock stock;
    protected final Runnable onTradeComplete;

    protected TextField quantityField;
    protected Label totalValue;

    protected TradeStockDialog(ExchangeController controller, Stock stock, Runnable onTradeComplete) {
        this.controller = controller;
        this.stock = stock;
        this.onTradeComplete = onTradeComplete;
    }

    @Override
    protected boolean canShow() {
        return controller != null && stock != null;
    }

    @Override
    protected Node buildContent() {
        VBox content = new VBox(12);
        content.setPadding(new Insets(18));
        content.setFillWidth(true);

        VBox header = new VBox(4);
        Label title = new Label(dialogTitle());
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
        Label totalLabel = new Label(totalRowLabel());
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
        Button confirm = new Button(confirmButtonText());
        confirm.getStyleClass().add("primary-button");
        confirm.setDefaultButton(true);

        actions.getChildren().addAll(cancel, confirm);

        content.getChildren().addAll(header, pricePanel, sharesLabel, quantityBox, totalRow, actions);

        cancel.setOnAction(event -> close());
        confirm.setOnAction(event -> handleConfirm());
        quantityField.setOnAction(event -> handleConfirm());
        quantityField.textProperty().addListener((obs, oldValue, newValue) -> updateTotals());
        updateTotals();

        return content;
    }

    protected abstract String dialogTitle();

    protected abstract String totalRowLabel();

    protected abstract String confirmButtonText();

    protected abstract String errorTitle();

    protected abstract String errorHeader();

    protected abstract void handleConfirm();

    protected abstract void updateTotals();

    protected void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(errorTitle());
        alert.setHeaderText(errorHeader());
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected static UnaryOperator<TextFormatter.Change> numericFilter() {
        return change -> {
            String nextText = change.getControlNewText();
            return nextText.matches("\\d*(\\.\\d{0,4})?") ? change : null;
        };
    }

    protected static BigDecimal parseQuantity(String rawQuantity) {
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

    protected static String formatPrice(BigDecimal price) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "$" + format.format(price);
    }
}
