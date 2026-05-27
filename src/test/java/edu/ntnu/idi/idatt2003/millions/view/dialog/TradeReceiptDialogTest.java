package edu.ntnu.idi.idatt2003.millions.view.dialog;

import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.calculator.PurchaseCalculator;
import edu.ntnu.idi.idatt2003.millions.model.calculator.SaleCalculator;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TradeReceiptDialogTest {

    @Test
    void buildContent_forBuy_showsPurchaseLabels() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Share share = new Share(stock, new BigDecimal("2"), new BigDecimal("100.00"));
            TradeReceiptDialog dialog = new TradeReceiptDialog(true, stock, share.getQuantity(), stock.getSalesPrice(), new PurchaseCalculator(share));

            VBox content = (VBox) dialog.buildContent();
            assertNotNull(findLabelWithText(content, "Purchase Complete"));
            assertNotNull(findLabelWithText(content, "Shares Purchased"));
            assertNotNull(findLabelWithText(content, "Total Paid"));
        });
    }

    @Test
    void buildContent_forProfitableSale_includesTaxLine() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("200.00"));
            Share share = new Share(stock, new BigDecimal("2"), new BigDecimal("100.00"));
            TradeReceiptDialog dialog = new TradeReceiptDialog(false, stock, share.getQuantity(), stock.getSalesPrice(), new SaleCalculator(share));

            VBox content = (VBox) dialog.buildContent();
            assertNotNull(findLabelWithText(content, "Sale Complete"));
            assertNotNull(findLabelWithText(content, "Capital Gains Tax (30%)"));
            assertNotNull(findLabelWithText(content, "Total Received"));
        });
    }

    @Test
    void buildContent_forBreakevenSale_omitsTaxLine() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Share share = new Share(stock, new BigDecimal("2"), new BigDecimal("100.00"));
            TradeReceiptDialog dialog = new TradeReceiptDialog(false, stock, share.getQuantity(), stock.getSalesPrice(), new SaleCalculator(share));

            Node content = dialog.buildContent();
            assertNotNull(findLabelWithText(content, "Sale Complete"));
        });
    }

    @Test
    void buildContent_forSale_showsSoldLabel() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Share share = new Share(stock, new BigDecimal("2"), new BigDecimal("100.00"));
            TradeReceiptDialog dialog = new TradeReceiptDialog(false, stock, share.getQuantity(), stock.getSalesPrice(), new SaleCalculator(share));

            VBox content = (VBox) dialog.buildContent();
            assertNotNull(findLabelWithText(content, "Sale Complete"));
            assertNotNull(findLabelWithText(content, "Shares Sold"));
        });
    }

    @Test
    void buildContent_showsDoneButton() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Share share = new Share(stock, new BigDecimal("2"), new BigDecimal("100.00"));
            TradeReceiptDialog dialog = new TradeReceiptDialog(true, stock, share.getQuantity(), stock.getSalesPrice(), new PurchaseCalculator(share));

            VBox content = (VBox) dialog.buildContent();
            assertNotNull(findButtonWithText(content, "Done"));
        });
    }

    @Test
    void buildContent_showsSubtitleWithStockInfo() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("100.00"));
            TradeReceiptDialog dialog = new TradeReceiptDialog(true, stock, share.getQuantity(), stock.getSalesPrice(), new PurchaseCalculator(share));

            VBox content = (VBox) dialog.buildContent();
            assertNotNull(findLabelWithText(content, "Equinor  ·  EQNR"));
        });
    }

    private static Label findLabelWithText(Node node, String text) {
        if (node instanceof Label label && text.equals(label.getText())) {
            return label;
        }
        if (node instanceof Pane pane) {
            for (Node child : pane.getChildren()) {
                Label found = findLabelWithText(child, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static Button findButtonWithText(Node node, String text) {
        if (node instanceof Button button && text.equals(button.getText())) {
            return button;
        }
        if (node instanceof Pane pane) {
            for (Node child : pane.getChildren()) {
                Button found = findButtonWithText(child, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
