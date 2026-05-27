package edu.ntnu.idi.idatt2003.millions.view.page;

import edu.ntnu.idi.idatt2003.millions.infrastructure.io.StockCsvLoader;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.function.UnaryOperator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;

/**
 * Start page UI for the Millions stock trading game.
 */
public class StartPage {

    /**
     * Callback invoked when the player starts a new game.
     *
     * @param name        the player's name
     * @param capital     the starting capital
     * @param csvPath     path to a custom stock CSV file, or {@code null} to use the default S&P 500
     */
    @FunctionalInterface
    public interface OnStartGame {
        void accept(String name, BigDecimal capital, Path csvPath);
    }

    private static final String TROPHY_PATH = "M2 1 H12 V4 C12 6 10.8 7.5 9 8 V10 H11 V12 H3 V10 H5 V8 C3.2 7.5 2 6 2 4 Z";
    private static final String PLAY_PATH = "M3 2 L12 7 L3 12 Z";

    private Label errorLabel;
    private Path selectedCsvPath;

    /**
     * Creates the start page component.
     */
    public StartPage() {
    }

    /**
     * Builds the start page root node.
     *
     * @param onStart callback triggered when the user starts a new game
     * @param onLoad  callback triggered when the user chooses to load a game
     * @return configured root pane for the start page
     */
    public StackPane createRoot(OnStartGame onStart, Runnable onLoad) {
        StackPane root = new StackPane();
        root.getStyleClass().add("start-root");
        root.setPadding(new Insets(40));

        StackPane card = new StackPane();
        card.getStyleClass().add("start-card");
        card.setPrefSize(500, 490);
        card.setMaxWidth(500);

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

        Label csvLabel = new Label("Stock Data");
        csvLabel.getStyleClass().add("field-label");

        ToggleGroup stockSource = new ToggleGroup();
        RadioButton defaultRadio = new RadioButton("S&P 500 (default)");
        defaultRadio.setToggleGroup(stockSource);
        defaultRadio.setSelected(true);
        defaultRadio.getStyleClass().add("csv-radio");
        RadioButton customRadio = new RadioButton("Custom CSV");
        customRadio.setToggleGroup(stockSource);
        customRadio.getStyleClass().add("csv-radio");

        HBox radioRow = new HBox(16, defaultRadio, customRadio);
        radioRow.setAlignment(Pos.CENTER_LEFT);

        Label csvFileLabel = new Label("No file chosen");
        csvFileLabel.getStyleClass().add("info-item");

        Button chooseFileButton = new Button("Browse...");
        chooseFileButton.getStyleClass().add("secondary-action");
        chooseFileButton.setPadding(new Insets(4, 10, 4, 10));

        HBox csvFileRow = new HBox(8, chooseFileButton, csvFileLabel);
        csvFileRow.setAlignment(Pos.CENTER_LEFT);
        csvFileRow.setVisible(false);
        csvFileRow.setManaged(false);

        customRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            csvFileRow.setVisible(isSelected);
            csvFileRow.setManaged(isSelected);
            if (!isSelected) {
                selectedCsvPath = null;
                csvFileLabel.setText("No file chosen");
            }
        });

        chooseFileButton.setOnAction(event -> pickCsvFile(chooseFileButton, csvFileLabel));

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
        form.getChildren().addAll(
            nameLabel, nameInput,
            capitalLabel, capitalInput,
            csvLabel, radioRow, csvFileRow,
            infoPanel
        );

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
            Runnable startAction = () -> handleStart(nameField, capitalField, customRadio, onStart);
            startButton.setOnAction(event -> startAction.run());
            nameField.setOnAction(event -> startAction.run());
            capitalField.setOnAction(event -> startAction.run());
        }

        Button loadButton = new Button("Load Game");
        loadButton.getStyleClass().add("secondary-action");
        loadButton.setMaxWidth(Double.MAX_VALUE);
        if (onLoad == null) {
            loadButton.setDisable(true);
        } else {
            loadButton.setOnAction(event -> onLoad.run());
        }

        VBox actions = new VBox(10);
        actions.getChildren().addAll(startButton, loadButton);

        errorLabel = new Label();
        errorLabel.getStyleClass().add("form-error-label");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        nameField.textProperty().addListener((obs, o, n) -> clearError());
        capitalField.textProperty().addListener((obs, o, n) -> clearError());

        content.getChildren().addAll(header, form, spacer, actions, errorLabel);

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

    private void pickCsvFile(Button browseButton, Label csvFileLabel) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Stock CSV File");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV files", "*.csv")
        );
        File file = chooser.showOpenDialog(browseButton.getScene().getWindow());
        if (file == null) {
            return;
        }
        Path path = file.toPath();
        try {
            var stocks = new StockCsvLoader().loadFromPath(path);
            if (stocks.isEmpty()) {
                showValidationError("The selected CSV file contains no valid stock entries.");
                return;
            }
            selectedCsvPath = path;
            csvFileLabel.setText(file.getName() + " (" + stocks.size() + " stocks)");
            clearError();
        } catch (IOException | IllegalArgumentException ex) {
            showValidationError("Invalid CSV file: " + ex.getMessage());
            selectedCsvPath = null;
            csvFileLabel.setText("No file chosen");
        }
    }

    private void handleStart(TextField nameField, TextField capitalField,
                             RadioButton customRadio, OnStartGame onStart) {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) {
            showValidationError("Enter your name to get started.");
            return;
        }

        String capitalText = capitalField.getText() == null ? "" : capitalField.getText().trim();
        if (capitalText.isEmpty()) {
            showValidationError("Enter a starting capital to get started.");
            return;
        }

        BigDecimal startingMoney;
        try {
            startingMoney = new BigDecimal(capitalText);
        } catch (NumberFormatException exception) {
            showValidationError("Please enter a valid amount for your starting capital.");
            return;
        }

        if (startingMoney.compareTo(BigDecimal.ZERO) <= 0) {
            showValidationError("Starting capital must be greater than $0.");
            return;
        }

        if (customRadio.isSelected() && selectedCsvPath == null) {
            showValidationError("Select a CSV file or switch back to the default S&P 500.");
            return;
        }

        onStart.accept(name, startingMoney, customRadio.isSelected() ? selectedCsvPath : null);
    }

    private void showValidationError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
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
