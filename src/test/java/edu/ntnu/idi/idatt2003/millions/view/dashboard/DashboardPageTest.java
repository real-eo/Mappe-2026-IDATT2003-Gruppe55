package edu.ntnu.idi.idatt2003.millions.view.dashboard;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardPageTest {

    @Test
    void createRoot_withoutController_buildsRootLayout() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            DashboardPage page = new DashboardPage();
            Parent root = page.createRoot();

            assertNotNull(root);
            assertTrue(root instanceof BorderPane);
            BorderPane pane = (BorderPane) root;
            assertNotNull(pane.getTop());
            assertNotNull(pane.getCenter());
        });
    }

    @Test
    void toggleProfileView_swapsCenterContent() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
            Exchange exchange = new Exchange("OSE", List.of(stock));
            Player player = new Player("Alice", new BigDecimal("1000.00"));
            ExchangeController controller = new ExchangeController(exchange, player);

            DashboardPage page = new DashboardPage(controller);
            BorderPane root = (BorderPane) page.createRoot();
            Parent initialCenter = (Parent) root.getCenter();

            try {
                Method toggle = DashboardPage.class.getDeclaredMethod("toggleProfileView");
                toggle.setAccessible(true);
                toggle.invoke(page);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }

            assertNotNull(root.getCenter());
            assertTrue(root.getCenter() != initialCenter);
        });
    }
}
