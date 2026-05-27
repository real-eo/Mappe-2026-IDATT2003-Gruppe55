package edu.ntnu.idi.idatt2003.millions.view.page;

import edu.ntnu.idi.idatt2003.millions.controller.LoadGameController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.GameSaveSummary;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.GameState;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadGamePageTest {

    @Test
    void createRoot_showsSaveRows_fromControllerCallback() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            LoadGameController stubController = new LoadGameController() {
                @Override
                public void loadSaves(java.util.function.Consumer<List<GameSaveSummary>> onSuccess,
                                      java.util.function.Consumer<String> onError) {
                    onSuccess.accept(List.of(
                            new GameSaveSummary(1L, "Save One", "OSE", 4, Instant.parse("2026-05-27T12:00:00Z")),
                            new GameSaveSummary(2L, "Save Two", "OSE", 5, Instant.parse("2026-05-27T13:00:00Z"))
                    ));
                }
            };

            LoadGamePage page = new LoadGamePage(stubController, state -> { }, () -> { });
            Parent root = page.createRoot();

            assertNotNull(root);
            assertNotNull(findLabelWithText(root, "Save One"));
            assertNotNull(findLabelWithText(root, "Save Two"));
        });
    }

    @Test
    void backButton_disabled_whenOnBackIsNull() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            LoadGameController stubController = new LoadGameController() {
                @Override
                public void loadSaves(java.util.function.Consumer<List<GameSaveSummary>> onSuccess,
                                      java.util.function.Consumer<String> onError) {
                    onSuccess.accept(List.of());
                }
            };

            LoadGamePage page = new LoadGamePage(stubController, state -> { }, null);
            Parent root = page.createRoot();

            Button back = findButtonWithText(root, "Back");
            assertNotNull(back);
            assertTrue(back.isDisabled());
        });
    }

    @Test
    void loadButton_triggersLoadSaveCallback() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            AtomicLong loadedId = new AtomicLong(-1);
            AtomicBoolean onLoadCalled = new AtomicBoolean(false);

            LoadGameController stubController = new LoadGameController() {
                @Override
                public void loadSaves(java.util.function.Consumer<List<GameSaveSummary>> onSuccess,
                                      java.util.function.Consumer<String> onError) {
                    onSuccess.accept(List.of(
                            new GameSaveSummary(42L, "Load Me", "OSE", 8, Instant.now())
                    ));
                }

                @Override
                public void loadSave(long id,
                                     java.util.function.Consumer<GameState> onSuccess,
                                     java.util.function.Consumer<String> onError) {
                    loadedId.set(id);
                    Exchange exchange = new Exchange("OSE", List.of(
                            new Stock("EQNR", "Equinor", new java.math.BigDecimal("100.00"))));
                    Player player = new Player("Alice", new java.math.BigDecimal("1000.00"));
                    onSuccess.accept(new GameState(exchange, player));
                }
            };

            LoadGamePage page = new LoadGamePage(stubController, state -> onLoadCalled.set(true), () -> { });
            Parent root = page.createRoot();

            Button load = findButtonWithText(root, "Load");
            assertNotNull(load);
            load.fire();

            assertTrue(loadedId.get() == 42L);
            assertTrue(onLoadCalled.get());
        });
    }

    private static Button findButtonWithText(Node node, String text) {
        if (node instanceof Button button && text.equals(button.getText())) {
            return button;
        }
        if (node instanceof ScrollPane scrollPane) {
            Node content = scrollPane.getContent();
            if (content != null) {
                Button found = findButtonWithText(content, text);
                if (found != null) {
                    return found;
                }
            }
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Button found = findButtonWithText(child, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
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
        return null;
    }
}
