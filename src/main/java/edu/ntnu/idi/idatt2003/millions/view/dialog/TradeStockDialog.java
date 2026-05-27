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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Base dialog for trade operations that share quantity input and total calculation UI.
 */
public abstract class TradeStockDialog extends MillionsDialog {

    /**
     * Controller used to execute and evaluate trade operations.
     */
    protected final ExchangeController controller;
    /**
     * Stock currently displayed in the dialog.
     */
    protected final Stock stock;
    /**
     * Callback executed after a successful trade.
     */
    protected final Runnable onTradeComplete;

    /**
     * Input field for user-entered share quantity.
     */
    protected TextField quantityField;
    /**
     * Label displaying the computed trade total.
     */
    protected Label totalValue;
    /**
     * Inline error label shown at the bottom of the dialog.
     */
    protected Label errorLabel;

    /**
     * Creates a trade dialog for the given controller and stock.
     *
     * @param controller the exchange controller used to calculate and execute trades
     * @param stock the stock shown in the dialog
     * @param onTradeComplete callback invoked after a successful trade (may be null)
     */
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

        StockPriceChart chart = new StockPriceChart();
        chart.setPrefHeight(140);
        chart.setMaxWidth(Double.MAX_VALUE);
        chart.setPrices(stock.getHistoricalPrices());

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

        errorLabel = new Label();
        errorLabel.getStyleClass().add("trade-error-label");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        content.getChildren().addAll(header, chart, pricePanel, sharesLabel, quantityBox, totalRow, actions, errorLabel);

        cancel.setOnAction(event -> close());
        confirm.setOnAction(event -> handleConfirm());
        quantityField.setOnAction(event -> handleConfirm());
        quantityField.textProperty().addListener((obs, oldValue, newValue) -> {
            clearError();
            updateTotals();
        });
        updateTotals();

        return content;
    }

    /**
     * Returns the title shown at the top of the dialog.
     *
     * @return dialog title text
     */
    protected abstract String dialogTitle();

    /**
     * Returns the label used for the total row.
     *
     * @return total row label text
     */
    protected abstract String totalRowLabel();

    /**
     * Returns the text used for the primary confirmation button.
     *
     * @return confirmation button text
     */
    protected abstract String confirmButtonText();

    /**
     * Validates user input and executes the trade when the dialog is confirmed.
     */
    protected abstract void handleConfirm();

    /**
     * Recalculates and updates totals shown in the dialog based on current input.
     */
    protected abstract void updateTotals();

    /**
     * Shows an inline validation error at the bottom of the dialog.
     *
     * @param message validation message shown to the user
     */
    protected void showValidationError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Creates a text formatter filter that only allows numeric quantity input.
     *
     * @return change filter for quantity fields
     */
    protected static UnaryOperator<TextFormatter.Change> numericFilter() {
        return change -> {
            String nextText = change.getControlNewText();
            return nextText.matches("\\d*(\\.\\d{0,4})?") ? change : null;
        };
    }

    /**
     * Parses user-entered quantity text to a BigDecimal.
     *
     * @param rawQuantity quantity text from UI input
     * @return parsed quantity, or null when input is blank/invalid
     */
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

    /**
     * Formats a price value for display with two decimals and dollar sign.
     *
     * @param price price to format
     * @return formatted price string
     */
    protected static String formatPrice(BigDecimal price) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "$" + format.format(price);
    }
}
