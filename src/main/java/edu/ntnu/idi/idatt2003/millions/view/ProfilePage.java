package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.GameRepository;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.SqliteGameRepository;
import edu.ntnu.idi.idatt2003.millions.model.GameState;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Profile page placeholder with a save action.
 */
public class ProfilePage {

    private final ExchangeController controller;

    /**
     * Constructs the profile page with its controller dependency.
     *
     * @param controller the exchange controller
     */
    public ProfilePage(ExchangeController controller) {
        this.controller = controller;
    }

    /**
     * Creates the profile page root node.
     *
     * @return profile page root
     */
    public Parent createRoot() {
        VBox page = new VBox(16);
        page.getStyleClass().add("profile-page");
        page.setAlignment(Pos.CENTER);
        page.setPadding(new Insets(32));

        Label title = new Label("Profile");
        title.getStyleClass().add("profile-title");

        Button saveButton = new Button("Save Game");
        saveButton.getStyleClass().add("primary-button");
        if (controller == null) {
            saveButton.setDisable(true);
        } else {
            saveButton.setOnAction(event -> saveGame());
        }

        page.getChildren().addAll(title, saveButton);
        return page;
    }

    private void saveGame() {
        if (controller == null) {
            return;
        }

        Task<Long> saveTask = new Task<>() {
            @Override
            protected Long call() throws Exception {
                Path databasePath = resolveSavePath();
                GameRepository repository = new SqliteGameRepository(databasePath);
                repository.initialize();
                return repository.save(new GameState(controller.getExchange(), controller.getPlayer()));
            }
        };

        saveTask.setOnSucceeded(event -> {
            Long saveId = saveTask.getValue();
            showAlert(Alert.AlertType.INFORMATION,
                "Save Game",
                "Game saved successfully",
                "Save ID: " + saveId);
        });

        saveTask.setOnFailed(event -> {
            Throwable error = saveTask.getException();
            String message = error == null ? "Unknown error" : error.getMessage();
            showAlert(Alert.AlertType.ERROR,
                "Save Game",
                "Failed to save game",
                message);
        });

        Thread worker = new Thread(saveTask, "save-game-task");
        worker.setDaemon(true);
        worker.start();
    }

    private Path resolveSavePath() throws IOException {
        Path path = Path.of("saves", "millions.db");
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        return path;
    }

    private void showAlert(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
