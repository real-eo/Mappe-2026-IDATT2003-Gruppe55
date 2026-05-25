package edu.ntnu.idi.idatt2003.millions.view;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

/**
 * Start page UI for the Millions stock trading game.
 */
public class StartPage {

    private static final String TROPHY_PATH = "M2 1 H12 V4 C12 6 10.8 7.5 9 8 V10 H11 V12 H3 V10 H5 V8 C3.2 7.5 2 6 2 4 Z";
    private static final String PLAY_PATH = "M3 2 L12 7 L3 12 Z";

    public StackPane createRoot(BiConsumer<String, BigDecimal> onStart) {
        StackPane root = new StackPane();
        root.getStyleClass().add("start-root");
        root.setPadding(new Insets(40));

        StackPane card = new StackPane();
        card.getStyleClass().add("start-card");
        card.setPrefSize(500, 440);
        card.setMaxSize(500, 440);

        VBox content = new VBox(16);
        content.setAlignment(Pos.TOP_LEFT);
        content.setFillWidth(true);
        content.setPadding(new Insets(20));

        VBox header = new VBox(6);
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        SVGPath trophyIcon = createIcon(TROPHY_PATH, "icon-accent");
        trophyIcon.setScaleX(1.1);
        trophyIcon.setScaleY(1.1);
        Label title = new Label("Millions - Stock Trading Game");
        title.getStyleClass().add("title-text");
        titleRow.getChildren().addAll(trophyIcon, title);

        Label subtitle = new Label("Start your journey to financial success. Enter your details to begin trading.");
        subtitle.getStyleClass().add("subtitle-text");
        header.getChildren().addAll(titleRow, subtitle);

        VBox form = new VBox(10);

        Label nameLabel = new Label("Player Name");
        nameLabel.getStyleClass().add("field-label");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");
        HBox nameInput = createInputField(null, nameField);

        Label capitalLabel = new Label("Starting Capital");
        capitalLabel.getStyleClass().add("field-label");
        TextField capitalField = new TextField();
        capitalField.setPromptText("10000");
        UnaryOperator<TextFormatter.Change> digitsOnly = change -> {
            String nextText = change.getControlNewText();
            return nextText.matches("\\d*") ? change : null;
        };
        capitalField.setTextFormatter(new TextFormatter<>(digitsOnly));
        Label currencyIcon = new Label("$");
        currencyIcon.getStyleClass().add("icon-label");
        HBox capitalInput = createInputField(currencyIcon, capitalField);

        VBox infoPanel = new VBox(6);
        infoPanel.getStyleClass().add("info-panel");

        HBox infoHeader = new HBox(6);
        infoHeader.setAlignment(Pos.CENTER_LEFT);
        Label infoIcon = new Label("i");
        infoIcon.getStyleClass().add("icon-label");
        Label infoTitle = new Label("How to Play");
        infoTitle.getStyleClass().add("info-title");
        infoHeader.getChildren().addAll(infoIcon, infoTitle);

        VBox infoList = new VBox(3);
        infoList.getChildren().addAll(
            createInfoItem("- Buy and sell stocks to grow your net worth"),
            createInfoItem("- Track your portfolio and transaction history"),
            createInfoItem("- Advance weeks to see price changes"),
            createInfoItem("- Reach Investor or Speculator status")
        );

        infoPanel.getChildren().addAll(infoHeader, infoList);
        form.getChildren().addAll(nameLabel, nameInput, capitalLabel, capitalInput, infoPanel);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("primary-action");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setAlignment(Pos.CENTER);
        startButton.setContentDisplay(ContentDisplay.LEFT);
        startButton.setGraphicTextGap(8);
        startButton.setGraphic(createIcon(PLAY_PATH, "icon-inverse"));
        startButton.setDefaultButton(true);

        if (onStart == null) {
            startButton.setDisable(true);
        } else {
            Runnable startAction = () -> handleStart(nameField, capitalField, onStart);
            startButton.setOnAction(event -> startAction.run());
            nameField.setOnAction(event -> startAction.run());
            capitalField.setOnAction(event -> startAction.run());
        }

        content.getChildren().addAll(header, form, spacer, startButton);

        Button closeButton = new Button("x");
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnAction(event -> {
            if (root.getScene() != null && root.getScene().getWindow() != null) {
                root.getScene().getWindow().hide();
            }
        });

        card.getChildren().addAll(content, closeButton);
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(10, 10, 0, 0));

        root.getChildren().add(card);
        return root;
    }

    private void handleStart(TextField nameField, TextField capitalField,
                             BiConsumer<String, BigDecimal> onStart) {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) {
            showValidationError("Please enter your name.");
            return;
        }

        String capitalText = capitalField.getText() == null ? "" : capitalField.getText().trim();
        if (capitalText.isEmpty()) {
            showValidationError("Please enter a starting capital.");
            return;
        }

        BigDecimal startingMoney;
        try {
            startingMoney = new BigDecimal(capitalText);
        } catch (NumberFormatException exception) {
            showValidationError("Starting capital must be a valid number.");
            return;
        }

        if (startingMoney.compareTo(BigDecimal.ZERO) <= 0) {
            showValidationError("Starting capital must be greater than zero.");
            return;
        }

        onStart.accept(name, startingMoney);
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Start Game");
        alert.setHeaderText("Cannot start the game");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static HBox createInputField(Node leadingIcon, TextField textField) {
        HBox input = new HBox(6);
        input.getStyleClass().add("input-field");
        input.setAlignment(Pos.CENTER_LEFT);
        if (leadingIcon != null) {
            input.getChildren().add(leadingIcon);
        }
        textField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textField, Priority.ALWAYS);
        input.getChildren().add(textField);
        return input;
    }

    private static Label createInfoItem(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("info-item");
        return label;
    }

    private static SVGPath createIcon(String path, String styleClass) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.getStyleClass().add(styleClass);
        return icon;
    }

}
