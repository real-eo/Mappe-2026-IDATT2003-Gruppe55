package edu.ntnu.idi.idatt2003.millions.view.page;

import edu.ntnu.idi.idatt2003.millions.controller.LoadGameController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.GameSaveSummary;
import edu.ntnu.idi.idatt2003.millions.model.GameState;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Page that lists saved games and allows loading them.
 */
public class LoadGamePage {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault());

    private final LoadGameController loadController;
    private final Consumer<GameState> onLoad;
    private final Runnable onBack;
    private VBox listContainer;

    /**
     * Constructs the load game page.
     *
     * @param loadController controller handling persistence operations
     * @param onLoad         callback for loading a selected game
     * @param onBack         callback for returning to the start page
     */
    public LoadGamePage(LoadGameController loadController, Consumer<GameState> onLoad, Runnable onBack) {
        this.loadController = loadController;
        this.onLoad = onLoad;
        this.onBack = onBack;
    }

    /**
     * Creates the load game page root node.
     *
     * @return load page root
     */
    public Parent createRoot() {
        StackPane root = new StackPane();
        root.getStyleClass().add("start-root");
        root.setPadding(new Insets(40));

        StackPane card = new StackPane();
        card.getStyleClass().add("start-card");
        card.setPrefSize(620, 520);
        card.setMaxSize(620, 520);

        VBox content = new VBox(16);
        content.setAlignment(Pos.TOP_LEFT);
        content.setFillWidth(true);
        content.setPadding(new Insets(20));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Load Game");
        title.getStyleClass().add("load-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("secondary-action");
        if (onBack == null) {
            backButton.setDisable(true);
        } else {
            backButton.setOnAction(event -> onBack.run());
        }

        header.getChildren().addAll(title, spacer, backButton);

        Label subtitle = new Label("Choose a saved game to continue.");
        subtitle.getStyleClass().add("subtitle-text");

        listContainer = new VBox(10);
        listContainer.getStyleClass().add("load-list");
        listContainer.setFillWidth(true);
        listContainer.getChildren().add(createPlaceholder("Loading saves..."));

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.getStyleClass().add("load-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        content.getChildren().addAll(header, subtitle, scrollPane);
        card.getChildren().add(content);
        root.getChildren().add(card);

        loadSaves();
        return root;
    }

    private void loadSaves() {
        loadController.loadSaves(
            this::populateSaves,
            error -> {
                showAlert(Alert.AlertType.ERROR, "Load Game", "Failed to list saves", error);
                populateSaves(List.of());
            }
        );
    }

    private void populateSaves(List<GameSaveSummary> saves) {
        listContainer.getChildren().clear();
        if (saves == null || saves.isEmpty()) {
            listContainer.getChildren().add(createPlaceholder("No saved games found."));
            return;
        }

        for (GameSaveSummary save : saves) {
            listContainer.getChildren().add(createSaveRow(save));
        }
    }

    private HBox createSaveRow(GameSaveSummary save) {
        HBox row = new HBox(12);
        row.getStyleClass().add("load-item");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox details = new VBox(4);
        Label name = new Label(save.label());
        name.getStyleClass().add("load-name");
            Label meta = new Label("Exchange: " + save.exchangeName() + " - Week " + save.week());
        meta.getStyleClass().add("load-meta");
        Label timestamp = new Label("Saved: " + formatTimestamp(save.createdAt()));
        timestamp.getStyleClass().add("load-meta");
        details.getChildren().addAll(name, meta, timestamp);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button loadButton = new Button("Load");
        loadButton.getStyleClass().add("primary-action");
        if (onLoad == null) {
            loadButton.setDisable(true);
        } else {
            loadButton.setOnAction(event -> loadSave(save.id()));
        }

        row.getChildren().addAll(details, spacer, loadButton);
        return row;
    }

    private Label createPlaceholder(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("load-empty");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private void loadSave(long saveId) {
        loadController.loadSave(
            saveId,
            state -> {
                if (onLoad != null) {
                    onLoad.accept(state);
                }
            },
            error -> showAlert(Alert.AlertType.ERROR, "Load Game", "Failed to load save", error)
        );
    }

    private String formatTimestamp(Instant instant) {
        if (instant == null) {
            return "Unknown";
        }
        return TIMESTAMP_FORMAT.format(instant);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
