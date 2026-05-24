package edu.ntnu.idi.idatt2003.millions.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Entry screen styled to match the Figma concept.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        root.getStyleClass().add("app-root");

        Region overlay = new Region();
        overlay.getStyleClass().add("overlay");
        overlay.prefWidthProperty().bind(root.widthProperty());
        overlay.prefHeightProperty().bind(root.heightProperty());

        VBox dialog = buildDialog(stage);
        StackPane.setAlignment(dialog, Pos.CENTER);

        root.getChildren().addAll(overlay, dialog);

        Scene scene = new Scene(root, 910, 593);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/styles/main.css"))
                .toExternalForm());

        stage.setTitle("Millions");
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildDialog(Stage stage) {
        Label titleIcon = new Label("M");
        titleIcon.getStyleClass().add("title-icon");

        Label title = new Label("Millions - Stock Trading Game");
        title.getStyleClass().add("dialog-title");

        Button closeButton = new Button("x");
        closeButton.getStyleClass().add("close-button");
        closeButton.setFocusTraversable(false);
        closeButton.setOnAction(event -> stage.close());

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        HBox titleRow = new HBox(8, titleIcon, title, titleSpacer, closeButton);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label(
                "Start your journey to financial success. Enter your details to begin trading.");
        subtitle.getStyleClass().add("dialog-subtitle");
        subtitle.setWrapText(true);

        VBox nameSection = buildNameSection();
        VBox capitalSection = buildCapitalSection();
        VBox infoPanel = buildInfoPanel();

        VBox form = new VBox(12, nameSection, capitalSection, infoPanel);
        form.setFillWidth(true);

        Label playIcon = new Label("> ");
        playIcon.getStyleClass().add("button-icon");

        Button startButton = new Button("Start Game", playIcon);
        startButton.getStyleClass().add("primary-button");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setPrefHeight(35);
        startButton.setContentDisplay(ContentDisplay.LEFT);
        startButton.setGraphicTextGap(8);
        startButton.setAlignment(Pos.CENTER);

        VBox dialog = new VBox(14, titleRow, subtitle, form, startButton);
        dialog.getStyleClass().add("dialog-card");
        dialog.setPrefWidth(500);
        dialog.setPrefHeight(440);
        dialog.setPadding(new Insets(20));
        dialog.setFillWidth(true);

        return dialog;
    }

    private VBox buildNameSection() {
        Label nameLabel = new Label("Player Name");
        nameLabel.getStyleClass().add("field-label");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");

        HBox nameContainer = new HBox(nameField);
        nameContainer.getStyleClass().add("input-container");
        HBox.setHgrow(nameField, Priority.ALWAYS);

        return new VBox(6, nameLabel, nameContainer);
    }

    private VBox buildCapitalSection() {
        Label capitalLabel = new Label("Starting Capital");
        capitalLabel.getStyleClass().add("field-label");

        Label currencyIcon = new Label("$");
        currencyIcon.getStyleClass().add("input-icon");

        TextField capitalField = new TextField("10000");

        HBox capitalContainer = new HBox(6, currencyIcon, capitalField);
        capitalContainer.getStyleClass().add("input-container");
        HBox.setHgrow(capitalField, Priority.ALWAYS);

        return new VBox(6, capitalLabel, capitalContainer);
    }

    private VBox buildInfoPanel() {
        Label infoIcon = new Label("i");
        infoIcon.getStyleClass().add("info-icon");

        Label infoTitle = new Label("How to Play");
        infoTitle.getStyleClass().add("info-title");

        HBox header = new HBox(6, infoIcon, infoTitle);
        header.setAlignment(Pos.CENTER_LEFT);

        Label itemOne = new Label("- Buy and sell stocks to grow your net worth");
        Label itemTwo = new Label("- Track your portfolio and transaction history");
        Label itemThree = new Label("- Advance weeks to see price changes");
        Label itemFour = new Label("- Reach Investor or Speculator status");

        itemOne.getStyleClass().add("info-item");
        itemTwo.getStyleClass().add("info-item");
        itemThree.getStyleClass().add("info-item");
        itemFour.getStyleClass().add("info-item");

        VBox list = new VBox(4, itemOne, itemTwo, itemThree, itemFour);
        VBox panel = new VBox(6, header, list);
        panel.getStyleClass().add("info-panel");

        return panel;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
