package edu.ntnu.idi.idatt2003.millions.view.page;

import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StartPageTest {

    @Test
    void createRoot_disablesButtons_whenCallbacksAreNull() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StartPage page = new StartPage();
            StackPane root = page.createRoot(null, null);

            Button start = findButtonWithText(root, "Start Game");
            Button load = findButtonWithText(root, "Load Game");

            assertNotNull(start);
            assertNotNull(load);
            assertTrue(start.isDisabled());
            assertTrue(load.isDisabled());
        });
    }

    @Test
    void startButton_invokesOnStart_forValidInputs() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StartPage page = new StartPage();
            AtomicReference<String> receivedName = new AtomicReference<>();
            AtomicReference<BigDecimal> receivedCapital = new AtomicReference<>();

            BiConsumer<String, BigDecimal> onStart = (name, capital) -> {
                receivedName.set(name);
                receivedCapital.set(capital);
            };

            StackPane root = page.createRoot(onStart, () -> {
            });
            TextField nameField = findTextFieldByPrompt(root, "Enter your name");
            TextField capitalField = findTextFieldByPrompt(root, "10000");
            Button start = findButtonWithText(root, "Start Game");

            assertNotNull(nameField);
            assertNotNull(capitalField);
            assertNotNull(start);

            nameField.setText("Alice");
            capitalField.setText("15000");
            start.fire();

            assertEquals("Alice", receivedName.get());
            assertEquals(new BigDecimal("15000"), receivedCapital.get());
        });
    }

    @Test
    void loadButton_invokesOnLoadCallback() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StartPage page = new StartPage();
            AtomicBoolean loaded = new AtomicBoolean(false);

            StackPane root = page.createRoot((name, capital) -> { }, () -> loaded.set(true));
            Button load = findButtonWithText(root, "Load Game");

            assertNotNull(load);
            load.fire();

            assertTrue(loaded.get());
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

    private static TextField findTextFieldByPrompt(Node node, String promptText) {
        if (node instanceof TextField field && promptText.equals(field.getPromptText())) {
            return field;
        }
        if (node instanceof Pane pane) {
            for (Node child : pane.getChildren()) {
                TextField found = findTextFieldByPrompt(child, promptText);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
