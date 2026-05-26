package edu.ntnu.idi.idatt2003.millions.view.dialog;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.calculator.SaleCalculator;
import java.math.BigDecimal;
import java.util.Optional;
import javafx.stage.Window;

public final class SellStockDialog extends TradeStockDialog {

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
    protected String errorTitle() {
        return "Sell Shares";
    }

    @Override
    protected String errorHeader() {
        return "Unable to complete the sale";
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
            showValidationError("Please enter the number of shares to sell.");
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

        Optional<Share> share = controller.getOwnedShare(stock);
        if (share.isEmpty()) {
            showValidationError("You do not own any shares of " + stock.getSymbol() + ".");
            return;
        }

        if (quantity.compareTo(share.get().getQuantity()) > 0) {
            showValidationError("You only own " + share.get().getQuantity() + " shares of "
                + stock.getSymbol() + ".");
            return;
        }

        try {
            Share saleShare = new Share(stock, quantity, share.get().getPurchasePrice());
            SaleCalculator calc = new SaleCalculator(saleShare);
            BigDecimal pricePerShare = stock.getSalesPrice();
            controller.sell(share.get(), quantity);
            Window owner = stage.getOwner();
            close();
            new TradeReceiptDialog(false, stock, quantity, pricePerShare, calc).show(owner);
            if (onTradeComplete != null) {
                onTradeComplete.run();
            }
        } catch (MillionsException exception) {
            showValidationError(exception.getMessage());
        }
    }
}
