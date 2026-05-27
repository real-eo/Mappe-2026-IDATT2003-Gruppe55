package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.GameState;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

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

                Method launchDashboard = Main.class.getDeclaredMethod(
                        "launchDashboard", Stage.class, String.class, BigDecimal.class, Path.class);
                launchDashboard.setAccessible(true);
                launchDashboard.invoke(main, stage, "Alice", new BigDecimal("10000.00"), null);

                Parent root = stage.getScene().getRoot();
                assertNotNull(root);
                assertTrue(stage.getScene().getStylesheets().stream().anyMatch(s -> s.contains("dashboard.css")));
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Test
    void launchDashboard_withCustomCsvPath_buildsDashboardScene() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            try {
                Main main = new Main();
                Stage stage = new Stage();
                stage.setScene(new Scene(new javafx.scene.layout.StackPane(), 800, 600));

                Path tempCsv = Files.createTempFile("test-stocks-", ".csv");
                Files.writeString(tempCsv, "TST,Test Corp,99.00\n", StandardCharsets.UTF_8);

                Method launchDashboard = Main.class.getDeclaredMethod(
                        "launchDashboard", Stage.class, String.class, BigDecimal.class, Path.class);
                launchDashboard.setAccessible(true);
                launchDashboard.invoke(main, stage, "Alice", new BigDecimal("10000.00"), tempCsv);

                assertNotNull(stage.getScene().getRoot());
                assertTrue(stage.getScene().getStylesheets().stream().anyMatch(s -> s.contains("dashboard.css")));
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Test
    void resolveExchangeName_returnsSnP500_forNullPath() throws Exception {
        Method resolveExchangeName = Main.class.getDeclaredMethod("resolveExchangeName", Path.class);
        resolveExchangeName.setAccessible(true);

        String result = (String) resolveExchangeName.invoke(null, (Object) null);
        assertEquals("S&P 500", result);
    }

    @Test
    void resolveExchangeName_stripsExtension_forCsvFile() throws Exception {
        Method resolveExchangeName = Main.class.getDeclaredMethod("resolveExchangeName", Path.class);
        resolveExchangeName.setAccessible(true);

        String result = (String) resolveExchangeName.invoke(null, Path.of("mystocks.csv"));
        assertEquals("mystocks", result);
    }

    @Test
    void resolveExchangeName_stripsExtension_caseInsensitive() throws Exception {
        Method resolveExchangeName = Main.class.getDeclaredMethod("resolveExchangeName", Path.class);
        resolveExchangeName.setAccessible(true);

        String result = (String) resolveExchangeName.invoke(null, Path.of("data.CSV"));
        assertEquals("data", result);
    }

    @Test
    void resolveExchangeName_returnsFullName_forNonCsvFile() throws Exception {
        Method resolveExchangeName = Main.class.getDeclaredMethod("resolveExchangeName", Path.class);
        resolveExchangeName.setAccessible(true);

        String result = (String) resolveExchangeName.invoke(null, Path.of("stocks.txt"));
        assertEquals("stocks.txt", result);
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

    @Test
    void showStartPage_and_showLoadPage_clearActiveController() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            try {
                Main main = new Main();
                Stage stage = new Stage();
                stage.setScene(new Scene(new javafx.scene.layout.StackPane(), 800, 600));

                Field activeControllerField = Main.class.getDeclaredField("activeController");
                activeControllerField.setAccessible(true);
                activeControllerField.set(main, new RecordingExchangeController());

                Method showStartPage = Main.class.getDeclaredMethod("showStartPage", Stage.class);
                showStartPage.setAccessible(true);
                showStartPage.invoke(main, stage);
                assertTrue(activeControllerField.get(main) == null);

                activeControllerField.set(main, new RecordingExchangeController());
                Method showLoadPage = Main.class.getDeclaredMethod("showLoadPage", Stage.class);
                showLoadPage.setAccessible(true);
                showLoadPage.invoke(main, stage);
                assertTrue(activeControllerField.get(main) == null);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Test
    void showDashboard_setsActiveController_andAppliesDashboardStylesheet() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            try {
                Main main = new Main();
                Stage stage = new Stage();
                stage.setScene(new Scene(new javafx.scene.layout.StackPane(), 800, 600));
                ExchangeController controller = new RecordingExchangeController();

                Method showDashboard = Main.class.getDeclaredMethod("showDashboard", Stage.class, ExchangeController.class);
                showDashboard.setAccessible(true);
                showDashboard.invoke(main, stage, controller);

                Field activeControllerField = Main.class.getDeclaredField("activeController");
                activeControllerField.setAccessible(true);

                assertSame(controller, activeControllerField.get(main));
                assertTrue(stage.getScene().getStylesheets().stream().anyMatch(s -> s.contains("dashboard.css")));
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Test
    void configureAutoSave_consumesCloseEvent_onlyWhenControllerPresent() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            try {
                Main main = new Main();
                Stage stage = new Stage();

                Method configureAutoSave = Main.class.getDeclaredMethod("configureAutoSave", Stage.class);
                configureAutoSave.setAccessible(true);
                configureAutoSave.invoke(main, stage);

                Field activeControllerField = Main.class.getDeclaredField("activeController");
                activeControllerField.setAccessible(true);

                WindowEvent withoutController = new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST);
                stage.getOnCloseRequest().handle(withoutController);
                assertFalse(withoutController.isConsumed());

                RecordingExchangeController recording = new RecordingExchangeController();
                activeControllerField.set(main, recording);
                WindowEvent withController = new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST);
                stage.getOnCloseRequest().handle(withController);

                assertTrue(withController.isConsumed());
                assertTrue(recording.saveInvoked.get());
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Test
    void autoSaveAndExit_handlesNullController_andInvokesSaveWhenPresent() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            try {
                Main main = new Main();
                Method autoSaveAndExit = Main.class.getDeclaredMethod("autoSaveAndExit");
                autoSaveAndExit.setAccessible(true);

                Field activeControllerField = Main.class.getDeclaredField("activeController");
                activeControllerField.setAccessible(true);

                RecordingExchangeController recording = new RecordingExchangeController();
                activeControllerField.set(main, recording);
                autoSaveAndExit.invoke(main);
                assertTrue(recording.saveInvoked.get());
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    private static final class RecordingExchangeController extends ExchangeController {
        private final AtomicBoolean saveInvoked = new AtomicBoolean(false);

        private RecordingExchangeController() {
            super(
                new Exchange("OSE", List.of(new Stock("EQNR", "Equinor", new BigDecimal("100.00")))),
                new Player("Recorder", new BigDecimal("1000.00"))
            );
        }

        @Override
        public void saveGame(java.util.function.Consumer<Long> onSuccess, java.util.function.Consumer<String> onFailure) {
            saveInvoked.set(true);
        }
    }
}
