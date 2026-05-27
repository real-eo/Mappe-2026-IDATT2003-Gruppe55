package edu.ntnu.idi.idatt2003.millions.view.dialog;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuyStockDialogTest {

    @Test
    void canShow_returnsFalse_whenControllerOrStockMissing() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));

        BuyStockDialog noController = new BuyStockDialog(null, stock, null);
        BuyStockDialog noStock = new BuyStockDialog(newController(), null, null);

        assertTrue(!noController.canShow());
        assertTrue(!noStock.canShow());
    }

    @Test
    void buildContent_setsExpectedTexts_andUpdateTotals_handlesInvalidAndValidQuantities() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            ExchangeController controller = newController(stock);
            BuyStockDialog dialog = new BuyStockDialog(controller, stock, null);

            VBox content = (VBox) dialog.buildContent();
            assertNotNull(content);

            TextField quantityField = dialog.quantityField;
            Label totalValue = dialog.totalValue;
            assertNotNull(quantityField);
            assertNotNull(totalValue);

            quantityField.setText("");
            dialog.updateTotals();
            assertEquals("--", totalValue.getText());

            quantityField.setText("2");
            dialog.updateTotals();
            assertEquals("$201.00", totalValue.getText());

            quantityField.setText("0");
            dialog.updateTotals();
            assertEquals("--", totalValue.getText());
        });
    }

    @Test
    void updateTotals_showsPlaceholder_whenControllerReturnsNoEstimate() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            ExchangeController controller = new ExchangeController(
                    new Exchange("OSE", List.of(stock)),
                    new Player("Alice", new BigDecimal("1000.00"))) {
                @Override
                public java.util.Optional<BigDecimal> calculateBuyTotal(String symbol, BigDecimal quantity) {
                    return Optional.empty();
                }
            };
            BuyStockDialog dialog = new BuyStockDialog(controller, stock, null);
            dialog.buildContent();

            dialog.quantityField.setText("2");
            dialog.updateTotals();

            assertEquals("--", dialog.totalValue.getText());
        });
    }

    @Test
    void parseQuantity_handlesBlankAndValidValues() throws Exception {
        Method parseQuantity = TradeStockDialog.class.getDeclaredMethod("parseQuantity", String.class);
        parseQuantity.setAccessible(true);

        assertEquals(null, parseQuantity.invoke(null, ""));
        assertEquals(new BigDecimal("3.25"), parseQuantity.invoke(null, "3.25"));
    }

    @Test
    void canShow_returnsTrue_whenControllerAndStockArePresent() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
        BuyStockDialog dialog = new BuyStockDialog(newController(stock), stock, null);
        assertTrue(dialog.canShow());
    }

    @Test
    void dialogTitle_includesStockSymbol() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
        BuyStockDialog dialog = new BuyStockDialog(newController(stock), stock, null);
        assertEquals("Buy EQNR", dialog.dialogTitle());
    }

    @Test
    void confirmButtonText_returnsExpectedLabel() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
        BuyStockDialog dialog = new BuyStockDialog(newController(stock), stock, null);
        assertEquals("Confirm Purchase", dialog.confirmButtonText());
    }

    @Test
    void handleConfirm_withEmptyQuantity_showsValidationError() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            BuyStockDialog dialog = new BuyStockDialog(newController(stock), stock, null);
            VBox content = (VBox) dialog.buildContent();

            dialog.quantityField.setText("");
            findButtonByText(content, "Confirm Purchase").fire();

            assertTrue(dialog.errorLabel.isVisible());
            assertFalse(dialog.errorLabel.getText().isEmpty());
        });
    }

    @Test
    void handleConfirm_withZeroQuantity_showsValidationError() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            BuyStockDialog dialog = new BuyStockDialog(newController(stock), stock, null);
            VBox content = (VBox) dialog.buildContent();

            dialog.quantityField.setText("0");
            findButtonByText(content, "Confirm Purchase").fire();

            assertTrue(dialog.errorLabel.isVisible());
        });
    }

    @Test
    void handleConfirm_withInsufficientFunds_showsValidationError() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Exchange exchange = new Exchange("OSE", List.of(stock));
            Player poorPlayer = new Player("Bob", new BigDecimal("1.00"));
            ExchangeController controller = new ExchangeController(exchange, poorPlayer);
            BuyStockDialog dialog = new BuyStockDialog(controller, stock, null);
            VBox content = (VBox) dialog.buildContent();

            dialog.quantityField.setText("1");
            findButtonByText(content, "Confirm Purchase").fire();

            assertTrue(dialog.errorLabel.isVisible());
            assertTrue(dialog.errorLabel.getText().contains("funds"));
        });
    }

    private static Button findButtonByText(Node node, String text) {
        if (node instanceof Button b && text.equals(b.getText())) {
            return b;
        }
        if (node instanceof Pane p) {
            for (Node child : p.getChildren()) {
                Button found = findButtonByText(child, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static ExchangeController newController(Stock stock) {
        Exchange exchange = new Exchange("OSE", List.of(stock));
        Player player = new Player("Alice", new BigDecimal("1000.00"));
        return new ExchangeController(exchange, player);
    }

    private static ExchangeController newController() {
        return newController(new Stock("EQNR", "Equinor", new BigDecimal("100.00")));
    }

}
