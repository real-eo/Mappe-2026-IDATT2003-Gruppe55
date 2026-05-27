package edu.ntnu.idi.idatt2003.millions.view.dialog;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SellStockDialogTest {

    @Test
    void canShow_returnsFalse_whenControllerOrStockMissing() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));

        SellStockDialog noController = new SellStockDialog(null, stock, null);
        SellStockDialog noStock = new SellStockDialog(newController(), null, null);

        assertTrue(!noController.canShow());
        assertTrue(!noStock.canShow());
    }

    @Test
    void buildContent_setsExpectedTexts_andUpdateTotalsHandlesOwnedAndMissingShares() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Exchange exchange = new Exchange("OSE", List.of(stock));
            Player player = new Player("Alice", new BigDecimal("1000.00"));
            ExchangeController controller = new ExchangeController(exchange, player);
            SellStockDialog dialog = new SellStockDialog(controller, stock, null);

            VBox content = (VBox) dialog.buildContent();
            assertNotNull(content);

            TextField quantityField = dialog.quantityField;
            Label totalValue = dialog.totalValue;

            quantityField.setText("1");
            dialog.updateTotals();
            assertEquals("--", totalValue.getText());

            Share share = new Share(stock, new BigDecimal("3"), new BigDecimal("90.00"));
            player.getPortfolio().add(share);
            quantityField.setText("2");
            dialog.updateTotals();
            assertTrue(totalValue.getText().startsWith("$"));

            quantityField.setText("0");
            dialog.updateTotals();
            assertEquals("--", totalValue.getText());
        });
    }

    @Test
    void updateTotals_showsPlaceholderWhenQuantityExceedsOwned() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Exchange exchange = new Exchange("OSE", List.of(stock));
            Player player = new Player("Alice", new BigDecimal("1000.00"));
            player.getPortfolio().add(new Share(stock, new BigDecimal("1"), new BigDecimal("90.00")));
            ExchangeController controller = new ExchangeController(exchange, player);
            SellStockDialog dialog = new SellStockDialog(controller, stock, null);
            dialog.buildContent();

            dialog.quantityField.setText("2");
            dialog.updateTotals();
            assertEquals("--", dialog.totalValue.getText());
        });
    }

    @Test
    void updateTotals_showsPlaceholder_whenControllerReturnsNoEstimate() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Exchange exchange = new Exchange("OSE", List.of(stock));
            Player player = new Player("Alice", new BigDecimal("1000.00"));
            player.getPortfolio().add(new Share(stock, new BigDecimal("1"), new BigDecimal("90.00")));
            ExchangeController controller = new ExchangeController(exchange, player) {
                @Override
                public Optional<BigDecimal> calculateSellTotal(Stock stock, BigDecimal quantity) {
                    return Optional.empty();
                }
            };
            SellStockDialog dialog = new SellStockDialog(controller, stock, null);
            dialog.buildContent();

            dialog.quantityField.setText("1");
            dialog.updateTotals();

            assertEquals("--", dialog.totalValue.getText());
        });
    }

    private static ExchangeController newController() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
        Exchange exchange = new Exchange("OSE", List.of(stock));
        Player player = new Player("Alice", new BigDecimal("1000.00"));
        return new ExchangeController(exchange, player);
    }

}
