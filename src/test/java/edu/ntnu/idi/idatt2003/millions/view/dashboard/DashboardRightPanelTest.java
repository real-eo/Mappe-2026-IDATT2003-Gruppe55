package edu.ntnu.idi.idatt2003.millions.view.dashboard;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Purchase;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DashboardRightPanelTest {

    @Test
    void createPanel_withNullController_showsEmptyState() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            DashboardRightPanel panel = new DashboardRightPanel(null);
            VBox root = panel.createPanel();

            assertNotNull(root);
            assertNotNull(findLabelWithText(root, "No Holdings"));
            assertNotNull(findLabelWithText(root, "History (0)"));
        });
    }

    @Test
    void refresh_withData_displaysPortfolioSymbolAndHistoryCount() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Exchange exchange = new Exchange("OSE", List.of(stock));
            Player player = new Player("Alice", new BigDecimal("1000.00"));
            ExchangeController controller = new ExchangeController(exchange, player);

            Share share = new Share(stock, new BigDecimal("2"), new BigDecimal("90.00"));
            player.getPortfolio().add(share);
            player.getTransactionArchive().add(new Purchase(share, 1));

            DashboardRightPanel panel = new DashboardRightPanel(controller);
            VBox root = panel.createPanel();
            panel.refresh();

            assertNotNull(findLabelWithText(root, "EQNR"));
            assertNotNull(findLabelWithText(root, "History (1)"));
        });
    }

    private static Label findLabelWithText(Node node, String text) {
        if (node instanceof Label label && text.equals(label.getText())) {
            return label;
        }
        if (node instanceof ScrollPane scrollPane) {
            Node content = scrollPane.getContent();
            if (content != null) {
                Label found = findLabelWithText(content, text);
                if (found != null) {
                    return found;
                }
            }
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Label found = findLabelWithText(child, text);
                if (found != null) {
                    return found;
                }
            }
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
}
