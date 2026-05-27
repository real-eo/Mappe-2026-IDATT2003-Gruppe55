package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.GameState;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    void resolveStylesheet_returnsResourceUrl() throws Exception {
        Method resolveStylesheet = Main.class.getDeclaredMethod("resolveStylesheet", String.class);
        resolveStylesheet.setAccessible(true);

        String url = (String) resolveStylesheet.invoke(null, "/styles/main.css");
        assertNotNull(url);
        assertTrue(url.endsWith("main.css"));
    }

    @Test
    void resolveStylesheet_throwsWhenResourceMissing() throws Exception {
        Method resolveStylesheet = Main.class.getDeclaredMethod("resolveStylesheet", String.class);
        resolveStylesheet.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> resolveStylesheet.invoke(null, "/styles/does-not-exist.css"));
        assertNotNull(ex.getCause());
    }

    @Test
    void showStartPage_replacesSceneRoot_andAppliesStylesheet() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            try {
                Main main = new Main();
                Stage stage = new Stage();
                stage.setScene(new Scene(new javafx.scene.layout.StackPane(), 800, 600));

                Method showStartPage = Main.class.getDeclaredMethod("showStartPage", Stage.class);
                showStartPage.setAccessible(true);
                showStartPage.invoke(main, stage);

                assertNotNull(stage.getScene().getRoot());
                assertTrue(stage.getScene().getStylesheets().stream().anyMatch(s -> s.contains("startpage.css")));
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Test
    void launchDashboard_fromNameAndCapital_buildsDashboardScene() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            try {
                Main main = new Main();
                Stage stage = new Stage();
                stage.setScene(new Scene(new javafx.scene.layout.StackPane(), 800, 600));

                Method launchDashboard = Main.class.getDeclaredMethod("launchDashboard", Stage.class, String.class, BigDecimal.class);
                launchDashboard.setAccessible(true);
                launchDashboard.invoke(main, stage, "Alice", new BigDecimal("10000.00"));

                Parent root = stage.getScene().getRoot();
                assertNotNull(root);
                assertTrue(stage.getScene().getStylesheets().stream().anyMatch(s -> s.contains("dashboard.css")));
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Test
    void start_doesNotThrow_withRealStage() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            assertDoesNotThrow(() -> {
                Main main = new Main();
                Stage stage = new Stage();
                stage.setScene(new Scene(new javafx.scene.layout.StackPane(), 1024, 768));
                main.start(stage);
                assertNotNull(stage.getScene());
            });
        });
    }

    @Test
    void showLoadPage_replacesSceneRoot_andAppliesStylesheet() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            try {
                Main main = new Main();
                Stage stage = new Stage();
                stage.setScene(new Scene(new javafx.scene.layout.StackPane(), 800, 600));

                Method showLoadPage = Main.class.getDeclaredMethod("showLoadPage", Stage.class);
                showLoadPage.setAccessible(true);
                showLoadPage.invoke(main, stage);

                assertNotNull(stage.getScene().getRoot());
                assertTrue(stage.getScene().getStylesheets().stream().anyMatch(s -> s.contains("startpage.css")));
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Test
    void launchDashboard_fromGameState_buildsDashboardScene() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            try {
                Main main = new Main();
                Stage stage = new Stage();
                stage.setScene(new Scene(new javafx.scene.layout.StackPane(), 800, 600));

                Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
                Exchange exchange = new Exchange("OSE", List.of(stock));
                Player player = new Player("Alice", new BigDecimal("10000.00"));
                GameState state = new GameState(exchange, player);

                Method launchDashboard = Main.class.getDeclaredMethod("launchDashboard", Stage.class, GameState.class);
                launchDashboard.setAccessible(true);
                launchDashboard.invoke(main, stage, state);

                Parent root = stage.getScene().getRoot();
                assertNotNull(root);
                assertTrue(stage.getScene().getStylesheets().stream().anyMatch(s -> s.contains("dashboard.css")));
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }
}
