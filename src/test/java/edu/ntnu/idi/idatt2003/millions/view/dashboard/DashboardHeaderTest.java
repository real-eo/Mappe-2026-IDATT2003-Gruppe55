package edu.ntnu.idi.idatt2003.millions.view.dashboard;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardHeaderTest {

    @Test
    void createHeader_withNullController_usesDefaultTexts() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            DashboardHeader header = new DashboardHeader(null, null, null);
            HBox root = header.createHeader();

            assertNotNull(root);
            assertTrue(findLabelWithText(root, "Player") != null);
            assertTrue(findLabelWithText(root, "Week 1") != null);
            assertTrue(findLabelWithText(root, "$10,000.00") != null);
        });
    }

    @Test
    void refresh_withController_updatesDisplayedValues() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Exchange exchange = new Exchange("OSE", List.of(stock));
            Player player = new Player("Alice", new BigDecimal("1000.00"));
            ExchangeController controller = new ExchangeController(exchange, player);

            DashboardHeader header = new DashboardHeader(controller, null, null);
            HBox root = header.createHeader();
            controller.advance();
            header.refresh();

            assertTrue(findLabelWithText(root, "Alice") != null);
            assertTrue(findLabelWithText(root, "Week 2") != null);
        });
    }

    @Test
    void updateProfileToggleLabel_changesButtonText() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            DashboardHeader header = new DashboardHeader(null, null, null);
            HBox root = header.createHeader();

            header.updateProfileToggleLabel(true);
            Button homeButton = findButtonWithText(root, "Home");
            assertNotNull(homeButton);

            header.updateProfileToggleLabel(false);
            Button profileButton = findButtonWithText(root, "Profile");
            assertNotNull(profileButton);
        });
    }

    @Test
    void nextWeekButton_advancesControllerAndRefreshesHeader() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Exchange exchange = new Exchange("OSE", List.of(stock));
            Player player = new Player("Alice", new BigDecimal("1000.00"));
            ExchangeController controller = new ExchangeController(exchange, player);

            DashboardHeader header = new DashboardHeader(controller, null, null);
            HBox root = header.createHeader();
            Button nextWeek = findButtonWithText(root, "Next Week");

            assertNotNull(nextWeek);
            nextWeek.fire();

            assertTrue(findLabelWithText(root, "Week 2") != null);
        });
    }

    @Test
    void profileToggleButton_invokesCallbackWhenPresent() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            AtomicBoolean toggled = new AtomicBoolean(false);
            DashboardHeader header = new DashboardHeader(null, () -> toggled.set(true), null);
            HBox root = header.createHeader();
            Button profile = findButtonWithText(root, "Profile");

            assertNotNull(profile);
            profile.fire();

            assertTrue(toggled.get());
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
