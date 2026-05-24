package edu.ntnu.idi.idatt2003.millions.view;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Entry screen styled to match the Figma concept.
 */
public class MainApp extends Application {

    private static final double BASE_WIDTH = 910;
    private static final double BASE_HEIGHT = 593;
    private static final double DIALOG_WIDTH = 500;
    private static final double DIALOG_HEIGHT = 440;
    private static final double DIALOG_PADDING = 20.75;
    private static final double SECTION_GAP = 20.6;
    private static final double FIELD_GAP = 6.9;

    private static final String TROPHY_PATH_1 = "M5.25 7.875H3.9375C3.35734 7.875 2.80094 7.64453 2.3907 7.2343C1.98047 6.82406 1.75 6.26766 1.75 5.6875C1.75 5.10734 1.98047 4.55094 2.3907 4.1407C2.80094 3.73047 3.35734 3.5 3.9375 3.5H5.25";
    private static final String TROPHY_PATH_2 = "M15.75 7.875H17.0625C17.6427 7.875 18.1991 7.64453 18.6093 7.2343C19.0195 6.82406 19.25 6.26766 19.25 5.6875C19.25 5.10734 19.0195 4.55094 18.6093 4.1407C18.1991 3.73047 17.6427 3.5 17.0625 3.5H15.75";
    private static final String TROPHY_PATH_3 = "M3.5 19.25H17.5";
    private static final String TROPHY_PATH_4 = "M8.75 12.8275V14.875C8.75 15.3562 8.33875 15.7325 7.90125 15.9337C6.86875 16.4062 6.125 17.71 6.125 19.25";
    private static final String TROPHY_PATH_5 = "M12.25 12.8275V14.875C12.25 15.3562 12.6613 15.7325 13.0988 15.9337C14.1313 16.4062 14.875 17.71 14.875 19.25";
    private static final String TROPHY_PATH_6 = "M15.75 1.75H5.25V7.875C5.25 9.26739 5.80312 10.6027 6.78769 11.5873C7.77226 12.5719 9.10761 13.125 10.5 13.125C11.8924 13.125 13.2277 12.5719 14.2123 11.5873C15.1969 10.6027 15.75 9.26739 15.75 7.875V1.75Z";

    private static final String DOLLAR_PATH_1 = "M7 1.16667V12.8333";
    private static final String DOLLAR_PATH_2 = "M9.91667 2.91667H5.54167C5.00018 2.91667 4.48088 3.13177 4.09799 3.51466C3.7151 3.89754 3.5 4.41685 3.5 4.95833C3.5 5.49982 3.7151 6.01912 4.09799 6.40201C4.48088 6.7849 5.00018 7 5.54167 7H8.45833C8.99982 7 9.51912 7.2151 9.90201 7.59799C10.2849 7.98088 10.5 8.50018 10.5 9.04167C10.5 9.58315 10.2849 10.1025 9.90201 10.4853C9.51912 10.8682 8.99982 11.0833 8.45833 11.0833H3.5";

    private static final String INFO_PATH_1 = "M7 12.8333C10.2217 12.8333 12.8333 10.2217 12.8333 7C12.8333 3.77834 10.2217 1.16667 7 1.16667C3.77834 1.16667 1.16667 3.77834 1.16667 7C1.16667 10.2217 3.77834 12.8333 7 12.8333Z";
    private static final String INFO_PATH_2 = "M7 4.66667V7";
    private static final String INFO_PATH_3 = "M7 9.33333H7.00583";

    private static final String PLAY_PATH = "M3.5 1.75L11.6667 7L3.5 12.25V1.75Z";
    private static final String CLOSE_PATH_1 = "M7.58333 0.583333L0.583333 7.58333";
    private static final String CLOSE_PATH_2 = "M0.583333 0.583333L7.58333 7.58333";

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        root.getStyleClass().add("app-root");

        VBox dialog = buildDialog(stage);
        StackPane.setAlignment(dialog, Pos.CENTER);

        DoubleBinding scale = Bindings.createDoubleBinding(
            () -> Math.min(
                root.getWidth() / BASE_WIDTH,
                root.getHeight() / BASE_HEIGHT),
            root.widthProperty(),
            root.heightProperty());
        dialog.scaleXProperty().bind(scale);
        dialog.scaleYProperty().bind(scale);

        root.getChildren().add(dialog);

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, visualBounds.getWidth(), visualBounds.getHeight());
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/styles/main.css"))
                .toExternalForm());

        stage.setTitle("Millions");
        stage.setX(visualBounds.getMinX());
        stage.setY(visualBounds.getMinY());
        stage.setWidth(visualBounds.getWidth());
        stage.setHeight(visualBounds.getHeight());
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildDialog(Stage stage) {
        Node titleIcon = createSvgIcon(21, 21, Color.web("#3b82f6"), 1.75,
            TROPHY_PATH_1,
            TROPHY_PATH_2,
            TROPHY_PATH_3,
            TROPHY_PATH_4,
            TROPHY_PATH_5,
            TROPHY_PATH_6);

        Label title = new Label("Millions - Stock Trading Game");
        title.getStyleClass().add("dialog-title");
        Node closeIcon = createSvgIcon(8.16667, 14, Color.web("#e2e8f0"), 1.16667,
            CLOSE_PATH_1,
            CLOSE_PATH_2);

        Button closeButton = new Button();
        closeButton.getStyleClass().add("close-button");
        closeButton.setGraphic(closeIcon);
        closeButton.setOpacity(0.7);
        closeButton.setMinSize(14, 14);
        closeButton.setPrefSize(14, 14);
        closeButton.setMaxSize(14, 14);
        closeButton.setFocusTraversable(false);
        closeButton.setOnAction(event -> stage.close());

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        HBox titleRow = new HBox(7, titleIcon, title, titleSpacer, closeButton);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label(
            "Start your journey to financial success. Enter your details to begin trading.");
        subtitle.getStyleClass().add("dialog-subtitle");
        subtitle.setWrapText(true);

        VBox header = new VBox(6.75, titleRow, subtitle);

        VBox nameSection = buildNameSection();
        VBox capitalSection = buildCapitalSection();
        VBox infoPanel = buildInfoPanel();

        VBox form = new VBox(SECTION_GAP, nameSection, capitalSection, infoPanel);
        form.setFillWidth(true);

        Node playIcon = createSvgIcon(14, 14, Color.WHITE, 1.16667, PLAY_PATH);

        Button startButton = new Button("Start Game", playIcon);
        startButton.getStyleClass().add("primary-button");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setPrefHeight(35);
        startButton.setContentDisplay(ContentDisplay.LEFT);
        startButton.setGraphicTextGap(8);
        startButton.setAlignment(Pos.CENTER);

        VBox dialog = new VBox(13.9, header, form, startButton);
        dialog.getStyleClass().add("dialog-card");
        dialog.setPrefWidth(DIALOG_WIDTH);
        dialog.setPrefHeight(DIALOG_HEIGHT);
        dialog.setPadding(new Insets(DIALOG_PADDING));
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

        nameContainer.setPrefHeight(32);

        VBox section = new VBox(FIELD_GAP, nameLabel, nameContainer);
        section.setFillWidth(true);

        return section;
    }

    private VBox buildCapitalSection() {
        Label capitalLabel = new Label("Starting Capital");
        capitalLabel.getStyleClass().add("field-label");

        Node currencyIcon = createSvgIcon(14, 14, Color.web("#94a3b8"), 1.16667,
                DOLLAR_PATH_1,
                DOLLAR_PATH_2);

        TextField capitalField = new TextField("10000");

        HBox capitalContainer = new HBox(6, currencyIcon, capitalField);
        capitalContainer.getStyleClass().add("input-container");
        HBox.setHgrow(capitalField, Priority.ALWAYS);

        capitalContainer.setPrefHeight(32);

        VBox section = new VBox(FIELD_GAP, capitalLabel, capitalContainer);
        section.setFillWidth(true);

        return section;
    }

    private VBox buildInfoPanel() {
        Node infoIcon = createSvgIcon(14, 14, Color.web("#94a3b8"), 1.16667,
            INFO_PATH_1,
            INFO_PATH_2,
            INFO_PATH_3);

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

        VBox list = new VBox(3.5, itemOne, itemTwo, itemThree, itemFour);
        VBox panel = new VBox(3, header, list);
        panel.getStyleClass().add("info-panel");

        return panel;
    }

    private StackPane createSvgIcon(double viewBoxSize,
                                   double targetSize,
                                   Color stroke,
                                   double strokeWidth,
                                   String... paths) {
        Group group = new Group();
        for (String path : paths) {
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(path);
            svgPath.setFill(Color.TRANSPARENT);
            svgPath.setStroke(stroke);
            svgPath.setStrokeWidth(strokeWidth);
            svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
            svgPath.setStrokeLineJoin(StrokeLineJoin.ROUND);
            group.getChildren().add(svgPath);
        }

        double scale = targetSize / viewBoxSize;
        group.setScaleX(scale);
        group.setScaleY(scale);

        StackPane container = new StackPane(group);
        container.setMinSize(targetSize, targetSize);
        container.setPrefSize(targetSize, targetSize);
        container.setMaxSize(targetSize, targetSize);
        return container;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
