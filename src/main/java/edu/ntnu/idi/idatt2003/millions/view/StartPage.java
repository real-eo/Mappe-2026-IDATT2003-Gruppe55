package edu.ntnu.idi.idatt2003.millions.view;

import java.net.URL;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

/**
 * Start page UI for the Millions stock trading game.
 */
public class StartPage extends Application {

    private static final String TROPHY_PATH = "M2 1 H12 V4 C12 6 10.8 7.5 9 8 V10 H11 V12 H3 V10 H5 V8 C3.2 7.5 2 6 2 4 Z";
    private static final String PLAY_PATH = "M3 2 L12 7 L3 12 Z";

    @Override
    public void start(Stage stage) {
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

        content.getChildren().addAll(header, form, spacer, startButton);

        Button closeButton = new Button("x");
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnAction(event -> stage.close());

        card.getChildren().addAll(content, closeButton);
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(10, 10, 0, 0));

        root.getChildren().add(card);

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(resolveStylesheet("/styles/startpage.css"));
        stage.setTitle("Millions - Stock Trading Game");
        stage.setScene(scene);
        stage.show();
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

    private static String resolveStylesheet(String path) {
        URL url = StartPage.class.getResource(path);
        if (url == null) {
            throw new IllegalStateException("Missing stylesheet: " + path);
        }
        return url.toExternalForm();
    }
}
