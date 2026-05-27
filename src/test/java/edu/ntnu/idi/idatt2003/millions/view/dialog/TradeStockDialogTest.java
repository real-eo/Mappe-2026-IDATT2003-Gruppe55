package edu.ntnu.idi.idatt2003.millions.view.dialog;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeStockDialogTest {

    @Test
    void canShow_returnsFalse_whenControllerOrStockIsMissing() {
        DummyTradeStockDialog noController = new DummyTradeStockDialog(null,
                new Stock("EQNR", "Equinor", new BigDecimal("100.00")));
        DummyTradeStockDialog noStock = new DummyTradeStockDialog(newController(), null);

        assertTrue(!noController.canShow());
        assertTrue(!noStock.canShow());
    }

    @Test
    void buildContent_initializesFields_andWiresConfirmAction() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            DummyTradeStockDialog dialog = new DummyTradeStockDialog(newController(),
                    new Stock("EQNR", "Equinor", new BigDecimal("100.00")));

            VBox content = (VBox) dialog.buildContent();
            assertNotNull(content);
            assertNotNull(dialog.quantityField);
            assertNotNull(dialog.totalValue);
            assertTrue(dialog.updateCalls.get() >= 1);

            int before = dialog.updateCalls.get();
            dialog.quantityField.setText("2");
            assertTrue(dialog.updateCalls.get() > before);

            Button confirm = findButtonWithText(content, "Confirm Trade");
            assertNotNull(confirm);
            confirm.fire();
            assertEquals(1, dialog.confirmCalls.get());
        });
    }

    @Test
    void parseQuantity_returnsNullForInvalidOrBlankValues() {
        assertNull(TradeStockDialog.parseQuantity(null));
        assertNull(TradeStockDialog.parseQuantity(""));
        assertNull(TradeStockDialog.parseQuantity("   "));
        assertNull(TradeStockDialog.parseQuantity("abc"));
        assertEquals(new BigDecimal("3.5"), TradeStockDialog.parseQuantity("3.5"));
    }

    @Test
    void numericFilter_acceptsValidShapes_andRejectsInvalidShapes() {
        UnaryOperator<TextFormatter.Change> filter = TradeStockDialog.numericFilter();

        FxTestUtils.runOnFxThreadAndWait(() -> {
            TextField field = new TextField();
            TextFormatter<String> formatter = new TextFormatter<>(change -> {
                TextFormatter.Change accepted = filter.apply(change);
                if (accepted == null) {
                    return null;
                }
                return accepted;
            });
            field.setTextFormatter(formatter);

            field.setText("12.3456");
            assertEquals("12.3456", field.getText());

            field.setText("12.34567");
            assertEquals("12.3456", field.getText());

            field.setText("abc");
            assertEquals("12.3456", field.getText());
        });
    }

    @Test
    void formatPrice_formatsAsUsd() {
        assertEquals("$1,234.50", TradeStockDialog.formatPrice(new BigDecimal("1234.5")));
    }

    private static ExchangeController newController() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
        Exchange exchange = new Exchange("OSE", List.of(stock));
        Player player = new Player("Alice", new BigDecimal("1000.00"));
        return new ExchangeController(exchange, player);
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

    private static final class DummyTradeStockDialog extends TradeStockDialog {
        private final AtomicInteger updateCalls = new AtomicInteger();
        private final AtomicInteger confirmCalls = new AtomicInteger();

        private DummyTradeStockDialog(ExchangeController controller, Stock stock) {
            super(controller, stock, null);
        }

        @Override
        protected String dialogTitle() {
            return "Trade";
        }

        @Override
        protected String totalRowLabel() {
            return "Total";
        }

        @Override
        protected String confirmButtonText() {
            return "Confirm Trade";
        }

        @Override
        protected void handleConfirm() {
            confirmCalls.incrementAndGet();
        }

        @Override
        protected void updateTotals() {
            updateCalls.incrementAndGet();
        }
    }
}
