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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        });
    }

    @Test
    void parseQuantity_handlesBlankAndValidValues() throws Exception {
        Method parseQuantity = TradeStockDialog.class.getDeclaredMethod("parseQuantity", String.class);
        parseQuantity.setAccessible(true);

        assertEquals(null, parseQuantity.invoke(null, ""));
        assertEquals(new BigDecimal("3.25"), parseQuantity.invoke(null, "3.25"));
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
