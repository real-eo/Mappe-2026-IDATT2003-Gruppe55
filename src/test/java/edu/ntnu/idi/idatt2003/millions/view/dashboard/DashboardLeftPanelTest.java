package edu.ntnu.idi.idatt2003.millions.view.dashboard;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardLeftPanelTest {

    @Test
    void createPanel_withNullController_showsEmptyMessage() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            DashboardLeftPanel panel = new DashboardLeftPanel(null, null);
            VBox root = panel.createPanel();

            assertNotNull(root);
            assertNotNull(findLabelWithText(root, "No stocks loaded"));
        });
    }

    @Test
    void parsePrice_handlesNullBlankDotAndValidInput() throws Exception {
        Method parsePrice = DashboardLeftPanel.class.getDeclaredMethod("parsePrice", TextField.class);
        parsePrice.setAccessible(true);

        FxTestUtils.runOnFxThreadAndWait(() -> {
            try {
                assertNull(parsePrice.invoke(null, new Object[]{null}));

                TextField blank = new TextField("");
                assertNull(parsePrice.invoke(null, blank));

                TextField dot = new TextField(".");
                assertNull(parsePrice.invoke(null, dot));

                TextField invalid = new TextField("abc");
                assertNull(parsePrice.invoke(null, invalid));

                TextField valid = new TextField("123.45");
                assertEquals(new BigDecimal("123.45"), parsePrice.invoke(null, valid));
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Test
    void toggleWatchlist_addsAndRemovesSymbol() throws Exception {
        DashboardLeftPanel panel = new DashboardLeftPanel(null, null);

        Method toggleWatchlist = DashboardLeftPanel.class.getDeclaredMethod("toggleWatchlist", String.class);
        toggleWatchlist.setAccessible(true);

        Field watchlistField = DashboardLeftPanel.class.getDeclaredField("watchlist");
        watchlistField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<String> watchlist = (Set<String>) watchlistField.get(panel);

        toggleWatchlist.invoke(panel, "EQNR");
        assertTrue(watchlist.contains("EQNR"));

        toggleWatchlist.invoke(panel, "EQNR");
        assertTrue(!watchlist.contains("EQNR"));

        toggleWatchlist.invoke(panel, "");
        toggleWatchlist.invoke(panel, new Object[]{null});
        assertEquals(0, watchlist.size());
    }

    @Test
    void updateStarStyle_togglesActiveClass() throws Exception {
        Method updateStarStyle = DashboardLeftPanel.class.getDeclaredMethod("updateStarStyle", SVGPath.class, boolean.class);
        updateStarStyle.setAccessible(true);

        SVGPath star = new SVGPath();
        updateStarStyle.invoke(null, star, true);
        assertTrue(star.getStyleClass().contains("icon-star-active"));

        updateStarStyle.invoke(null, star, false);
        assertTrue(!star.getStyleClass().contains("icon-star-active"));
    }

    @Test
    void toStockInfo_classifiesChangeKind() throws Exception {
        Stock positive = new Stock("UP", "Up Corp", new BigDecimal("100.00"));
        positive.addPrice(new BigDecimal("110.00"));

        Stock negative = new Stock("DN", "Down Corp", new BigDecimal("100.00"));
        negative.addPrice(new BigDecimal("90.00"));

        Stock neutral = new Stock("FL", "Flat Corp", new BigDecimal("100.00"));

        DashboardLeftPanel panel = new DashboardLeftPanel(null, null);
        Method toStockInfo = DashboardLeftPanel.class.getDeclaredMethod("toStockInfo", Stock.class);
        toStockInfo.setAccessible(true);

        Object positiveInfo = toStockInfo.invoke(panel, positive);
        Object negativeInfo = toStockInfo.invoke(panel, negative);
        Object neutralInfo = toStockInfo.invoke(panel, neutral);

        Method changeKind = positiveInfo.getClass().getDeclaredMethod("changeKind");
        changeKind.setAccessible(true);

        assertEquals("POSITIVE", changeKind.invoke(positiveInfo).toString());
        assertEquals("NEGATIVE", changeKind.invoke(negativeInfo).toString());
        assertEquals("NEUTRAL", changeKind.invoke(neutralInfo).toString());
    }

    @Test
    void loadStockInfos_returnsFilteredStocks_whenControllerPresent() throws Exception {
        Stock a = new Stock("AAA", "A Corp", new BigDecimal("100.00"));
        Stock b = new Stock("BBB", "B Corp", new BigDecimal("50.00"));
        Exchange exchange = new Exchange("E", List.of(a, b));
        Player player = new Player("Alice", new BigDecimal("1000.00"));
        ExchangeController controller = new ExchangeController(exchange, player);

        DashboardLeftPanel panel = new DashboardLeftPanel(controller, null);

        Method loadStockInfos = DashboardLeftPanel.class.getDeclaredMethod(
                "loadStockInfos", String.class, BigDecimal.class, BigDecimal.class);
        loadStockInfos.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Object> infos = (List<Object>) loadStockInfos.invoke(panel, "A", null, null);

        assertEquals(1, infos.size());
    }

    @Test
    void selectTab_marketMoversWithNullController_keepsEmptyStateRenderable() throws Exception {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            try {
                DashboardLeftPanel panel = new DashboardLeftPanel(null, null);
                VBox root = panel.createPanel();

                Class<?> tabClass = Class.forName(DashboardLeftPanel.class.getName() + "$StockTab");
                @SuppressWarnings("unchecked")
                Enum<?> movers = Enum.valueOf((Class<Enum>) tabClass, "MOVERS");

                Method selectTab = DashboardLeftPanel.class.getDeclaredMethod("selectTab", tabClass);
                selectTab.setAccessible(true);
                selectTab.invoke(panel, movers);

                assertNotNull(findLabelWithText(root, "No stocks loaded"));
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
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
