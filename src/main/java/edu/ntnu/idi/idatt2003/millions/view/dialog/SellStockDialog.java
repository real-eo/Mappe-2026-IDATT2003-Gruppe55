package edu.ntnu.idi.idatt2003.millions.view.dialog;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.calculator.SaleCalculator;
import java.math.BigDecimal;
import java.util.Optional;
import javafx.application.Platform;
import javafx.stage.Window;

/**
 * Dialog for selling owned shares of a selected stock.
 */
public final class SellStockDialog extends TradeStockDialog {

    /**
     * Creates a sell dialog bound to the given controller and stock.
     *
     * @param controller the exchange controller used to execute sales
     * @param stock the stock that will be sold
     * @param onTradeComplete callback invoked after a successful trade (may be null)
     */
    public SellStockDialog(ExchangeController controller, Stock stock, Runnable onTradeComplete) {
        super(controller, stock, onTradeComplete);
    }

    @Override
    protected String dialogTitle() {
        return "Sell " + stock.getSymbol();
    }

    @Override
    protected String totalRowLabel() {
        return "Total (after fees & tax)";
    }

    @Override
    protected String confirmButtonText() {
        return "Confirm Sale";
    }

    @Override
    protected void updateTotals() {
        if (totalValue == null || controller == null || stock == null) {
            return;
        }

        BigDecimal quantity = parseQuantity(quantityField.getText());
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            totalValue.setText("--");
            return;
        }

        Optional<Share> owned = controller.getOwnedShare(stock);
        if (owned.isEmpty() || quantity.compareTo(owned.get().getQuantity()) > 0) {
            totalValue.setText("--");
            return;
        }

        Optional<BigDecimal> total = controller.calculateSellTotal(stock, quantity);
        totalValue.setText(total.map(SellStockDialog::formatPrice).orElse("--"));
    }

    @Override
    protected void handleConfirm() {
        String rawQuantity = quantityField.getText() == null ? "" : quantityField.getText().trim();
        if (rawQuantity.isEmpty()) {
            showValidationError("Enter the number of shares you'd like to sell.");
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

        Optional<Share> share = controller.getOwnedShare(stock);
        if (share.isEmpty()) {
            showValidationError("You don't own any " + stock.getSymbol() + " shares.");
            return;
        }

        if (quantity.compareTo(share.get().getQuantity()) > 0) {
            showValidationError("You only have " + share.get().getQuantity() + " shares of "
                + stock.getSymbol() + " available to sell.");
            return;
        }

        try {
            Share saleShare = new Share(stock, quantity, share.get().getPurchasePrice());
            SaleCalculator calc = new SaleCalculator(saleShare);
            BigDecimal pricePerShare = stock.getSalesPrice();
            controller.sell(share.get(), quantity);
            Window owner = stage.getOwner();
            close();
            Platform.runLater(() -> {
                new TradeReceiptDialog(false, stock, quantity, pricePerShare, calc).show(owner);
                if (onTradeComplete != null) {
                    onTradeComplete.run();
                }
            });
        } catch (MillionsException exception) {
            showValidationError(exception.getMessage());
        }
    }
}
