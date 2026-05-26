package edu.ntnu.idi.idatt2003.millions.view.dialog;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.calculator.PurchaseCalculator;
import java.math.BigDecimal;
import java.util.Optional;
import javafx.stage.Window;

public final class BuyStockDialog extends TradeStockDialog {

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
    protected String errorTitle() {
        return "Buy Shares";
    }

    @Override
    protected String errorHeader() {
        return "Unable to complete the purchase";
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
            PurchaseCalculator calc = new PurchaseCalculator(
                new Share(stock, quantity, stock.getSalesPrice()));
            Window owner = stage.getOwner();
            close();
            new TradeReceiptDialog(true, stock, quantity, stock.getSalesPrice(), calc).show(owner);
            if (onTradeComplete != null) {
                onTradeComplete.run();
            }
        } catch (MillionsException exception) {
            showValidationError(exception.getMessage());
        }
    }
}
