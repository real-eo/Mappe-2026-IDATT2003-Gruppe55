package edu.ntnu.idi.idatt2003.millions.view.page;

import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void startButton_invokesOnStart_withNullPath_whenDefaultSourceSelected() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StartPage page = new StartPage();
            AtomicReference<String> receivedName = new AtomicReference<>();
            AtomicReference<BigDecimal> receivedCapital = new AtomicReference<>();
            AtomicReference<Path> receivedPath = new AtomicReference<>(Path.of("sentinel"));

            StartPage.OnStartGame onStart = (name, capital, csvPath) -> {
                receivedName.set(name);
                receivedCapital.set(capital);
                receivedPath.set(csvPath);
            };

            StackPane root = page.createRoot(onStart, () -> {});
            findTextFieldByPrompt(root, "Enter your name").setText("Alice");
            findTextFieldByPrompt(root, "10000").setText("15000");
            findButtonWithText(root, "Start Game").fire();

            assertEquals("Alice", receivedName.get());
            assertEquals(new BigDecimal("15000"), receivedCapital.get());
            assertNull(receivedPath.get());
        });
    }

    @Test
    void loadButton_invokesOnLoadCallback() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StartPage page = new StartPage();
            AtomicBoolean loaded = new AtomicBoolean(false);

            StackPane root = page.createRoot((name, capital, csvPath) -> {}, () -> loaded.set(true));
            findButtonWithText(root, "Load Game").fire();

            assertTrue(loaded.get());
        });
    }

    @Test
    void defaultRadio_isSelectedOnCreation() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StackPane root = new StartPage().createRoot((n, c, p) -> {}, () -> {});

            RadioButton defaultRadio = findRadioButtonWithText(root, "S&P 500 (default)");
            RadioButton customRadio = findRadioButtonWithText(root, "Custom CSV");

            assertNotNull(defaultRadio);
            assertNotNull(customRadio);
            assertTrue(defaultRadio.isSelected());
            assertFalse(customRadio.isSelected());
        });
    }

    @Test
    void csvFileRow_notVisibleByDefault() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StackPane root = new StartPage().createRoot((n, c, p) -> {}, () -> {});

            Button browseButton = findButtonWithText(root, "Browse...");
            assertNotNull(browseButton);
            assertFalse(browseButton.getParent().isVisible());
            assertFalse(browseButton.getParent().isManaged());
        });
    }

    @Test
    void csvFileRow_becomesVisible_whenCustomCsvSelected() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StackPane root = new StartPage().createRoot((n, c, p) -> {}, () -> {});

            RadioButton customRadio = findRadioButtonWithText(root, "Custom CSV");
            Button browseButton = findButtonWithText(root, "Browse...");

            customRadio.fire();

            assertTrue(browseButton.getParent().isVisible());
            assertTrue(browseButton.getParent().isManaged());
        });
    }

    @Test
    void csvFileRow_hidesAgain_whenSwitchingBackToDefault() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StackPane root = new StartPage().createRoot((n, c, p) -> {}, () -> {});

            RadioButton defaultRadio = findRadioButtonWithText(root, "S&P 500 (default)");
            RadioButton customRadio = findRadioButtonWithText(root, "Custom CSV");
            Button browseButton = findButtonWithText(root, "Browse...");

            customRadio.fire();
            assertTrue(browseButton.getParent().isVisible());

            defaultRadio.fire();
            assertFalse(browseButton.getParent().isVisible());
            assertFalse(browseButton.getParent().isManaged());
        });
    }

    @Test
    void startButton_showsError_whenCustomCsvSelectedButNoFileChosen() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StackPane root = new StartPage().createRoot((n, c, p) -> {}, () -> {});

            findTextFieldByPrompt(root, "Enter your name").setText("Bob");
            findTextFieldByPrompt(root, "10000").setText("5000");
            findRadioButtonWithText(root, "Custom CSV").fire();
            findButtonWithText(root, "Start Game").fire();

            Label errorLabel = findLabelByStyleClass(root, "form-error-label");
            assertNotNull(errorLabel);
            assertTrue(errorLabel.isVisible());
            assertFalse(errorLabel.getText().isBlank());
        });
    }

    @Test
    void startButton_showsError_whenNameIsEmpty() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StackPane root = new StartPage().createRoot((n, c, p) -> {}, () -> {});

            findTextFieldByPrompt(root, "10000").setText("5000");
            findButtonWithText(root, "Start Game").fire();

            Label errorLabel = findLabelByStyleClass(root, "form-error-label");
            assertNotNull(errorLabel);
            assertTrue(errorLabel.isVisible());
        });
    }

    @Test
    void startButton_showsError_whenCapitalIsEmpty() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StackPane root = new StartPage().createRoot((n, c, p) -> {}, () -> {});

            findTextFieldByPrompt(root, "Enter your name").setText("Alice");
            findButtonWithText(root, "Start Game").fire();

            Label errorLabel = findLabelByStyleClass(root, "form-error-label");
            assertNotNull(errorLabel);
            assertTrue(errorLabel.isVisible());
        });
    }

    @Test
    void startButton_showsError_whenCapitalIsZero() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StackPane root = new StartPage().createRoot((n, c, p) -> {}, () -> {});

            findTextFieldByPrompt(root, "Enter your name").setText("Alice");
            findTextFieldByPrompt(root, "10000").setText("0");
            findButtonWithText(root, "Start Game").fire();

            Label errorLabel = findLabelByStyleClass(root, "form-error-label");
            assertNotNull(errorLabel);
            assertTrue(errorLabel.isVisible());
        });
    }

    @Test
    void errorLabel_clearsWhenNameIsEdited() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StackPane root = new StartPage().createRoot((n, c, p) -> {}, () -> {});

            findButtonWithText(root, "Start Game").fire();
            Label errorLabel = findLabelByStyleClass(root, "form-error-label");
            assertTrue(errorLabel.isVisible());

            findTextFieldByPrompt(root, "Enter your name").setText("Bob");
            assertFalse(errorLabel.isVisible());
        });
    }

    @Test
    void errorLabel_clearsWhenCapitalIsEdited() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StackPane root = new StartPage().createRoot((n, c, p) -> {}, () -> {});

            findButtonWithText(root, "Start Game").fire();
            Label errorLabel = findLabelByStyleClass(root, "form-error-label");
            assertTrue(errorLabel.isVisible());

            findTextFieldByPrompt(root, "10000").setText("1000");
            assertFalse(errorLabel.isVisible());
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

    private static RadioButton findRadioButtonWithText(Node node, String text) {
        if (node instanceof RadioButton radio && text.equals(radio.getText())) {
            return radio;
        }
        if (node instanceof Pane pane) {
            for (Node child : pane.getChildren()) {
                RadioButton found = findRadioButtonWithText(child, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static Label findLabelByStyleClass(Node node, String styleClass) {
        if (node instanceof Label label && label.getStyleClass().contains(styleClass)) {
            return label;
        }
        if (node instanceof Pane pane) {
            for (Node child : pane.getChildren()) {
                Label found = findLabelByStyleClass(child, styleClass);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
