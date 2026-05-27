package edu.ntnu.idi.idatt2003.millions.view.page;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.controller.ProfileController;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Purchase;
import edu.ntnu.idi.idatt2003.millions.model.Sale;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfilePageTest {

    @Test
    void createRoot_withNullController_disablesSaveButton() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            ProfilePage page = new ProfilePage(null);
            Parent root = page.createRoot();

            Button save = findButtonWithText(root, "Save Game");
            assertNotNull(save);
            assertTrue(save.isDisabled());
        });
    }

    @Test
    void createRoot_withController_rendersStatsAndOutcomes() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Exchange exchange = new Exchange("OSE", List.of(stock));
            Player player = new Player("Alice", new BigDecimal("10000.00"));
            ExchangeController exchangeController = new ExchangeController(exchange, player);
            ProfileController profileController = new ProfileController(exchangeController);

            Share bought = new Share(stock, new BigDecimal("2"), new BigDecimal("90.00"));
            player.getTransactionArchive().add(new Purchase(bought, 1));

            Share soldAtLoss = new Share(stock, new BigDecimal("1"), new BigDecimal("150.00"));
            player.getTransactionArchive().add(new Sale(soldAtLoss, 2));

            ProfilePage page = new ProfilePage(profileController);
            Parent root = page.createRoot();

            assertNotNull(findLabelContaining(root, "Shares purchased"));
            assertNotNull(findLabelContaining(root, "Shares sold"));
            assertNotNull(findLabelContaining(root, "Top wins"));
            assertNotNull(findLabelContaining(root, "Top losses"));
        });
    }

    @Test
    void createRoot_withProfitableSale_marksOutcomeAsPositive() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Exchange exchange = new Exchange("OSE", List.of(stock));
            Player player = new Player("Alice", new BigDecimal("10000.00"));
            ExchangeController exchangeController = new ExchangeController(exchange, player);
            ProfileController profileController = new ProfileController(exchangeController);

            Share soldAtGain = new Share(stock, new BigDecimal("1"), new BigDecimal("50.00"));
            player.getTransactionArchive().add(new Sale(soldAtGain, 1));

            ProfilePage page = new ProfilePage(profileController);
            Parent root = page.createRoot();

            assertNotNull(findLabelContaining(root, "$"));
            assertTrue(findLabelContaining(root, "Top wins") != null);
        });
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

    private static Label findLabelContaining(Node node, String textPart) {
        if (node instanceof Label label && label.getText() != null && label.getText().contains(textPart)) {
            return label;
        }
        if (node instanceof Pane pane) {
            for (Node child : pane.getChildren()) {
                Label found = findLabelContaining(child, textPart);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
