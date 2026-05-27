package edu.ntnu.idi.idatt2003.millions.view.dialog;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.calculator.PurchaseCalculator;
import java.math.BigDecimal;
import java.util.Optional;
import javafx.application.Platform;
import javafx.stage.Window;

/**
 * Dialog for buying shares of a selected stock.
 */
public final class BuyStockDialog extends TradeStockDialog {

    /**
     * Creates a buy dialog bound to the given controller and stock.
     *
     * @param controller the exchange controller used to execute purchases
     * @param stock the stock that will be purchased
     * @param onTradeComplete callback invoked after a successful trade (may be null)
     */
    public BuyStockDialog(ExchangeController controller, Stock stock, Runnable onTradeComplete) {
        super(controller, stock, onTradeComplete);
    }

    @Override
    protected String dialogTitle() {
        return "Buy " + stock.getSymbol();
    }

    @Override
    protected String totalRowLabel() {
        return "Total (incl. fees & tax)";
    }

    @Override
    protected String confirmButtonText() {
        return "Confirm Purchase";
    }

    @Override
    protected void updateTotals() {
        if (totalValue == null || stock == null) {
            return;
        }

        BigDecimal quantity = parseQuantity(quantityField.getText());
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            totalValue.setText("--");
            return;
        }

        Optional<BigDecimal> total = controller.calculateBuyTotal(stock.getSymbol(), quantity);
        totalValue.setText(total.map(BuyStockDialog::formatPrice).orElse("--"));
    }

    @Override
    protected void handleConfirm() {
        String rawQuantity = quantityField.getText() == null ? "" : quantityField.getText().trim();
        if (rawQuantity.isEmpty()) {
            showValidationError("Enter the number of shares you'd like to buy.");
            return;
        }

        BigDecimal quantity;
        try {
            quantity = new BigDecimal(rawQuantity);
        } catch (NumberFormatException exception) {
            showValidationError("Please enter a valid number of shares.");
            return;
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            showValidationError("Number of shares must be greater than zero.");
            return;
        }

        try {
            controller.buy(stock.getSymbol(), quantity);
            PurchaseCalculator calc = new PurchaseCalculator(
                new Share(stock, quantity, stock.getSalesPrice()));
            Window owner = stage.getOwner();
            close();
            Platform.runLater(() -> {
                new TradeReceiptDialog(true, stock, quantity, stock.getSalesPrice(), calc).show(owner);
                if (onTradeComplete != null) {
                    onTradeComplete.run();
                }
            });
        } catch (MillionsException exception) {
            showValidationError(exception.getMessage());
        }
    }
}
