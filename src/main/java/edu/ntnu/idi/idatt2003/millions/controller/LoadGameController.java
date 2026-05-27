package edu.ntnu.idi.idatt2003.millions.controller;

import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.GameRepository;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.GameSaveSummary;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.SaveGameStorage;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.SqliteGameRepository;
import edu.ntnu.idi.idatt2003.millions.model.GameState;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Controller for loading saved games from persistent storage.
 */
public class LoadGameController {

    /**
     * Creates a controller for loading saved games.
     */
    public LoadGameController() {
    }

    /**
     * Lists all saved games asynchronously.
     *
     * @param onSuccess called with the list of save summaries on success
     * @param onError   called with an error message on failure
     */
    public void loadSaves(Consumer<List<GameSaveSummary>> onSuccess, Consumer<String> onError) {
        Thread worker = new Thread(() -> {
            try {
                Path databasePath = SaveGameStorage.resolveDefaultDatabasePath();
                GameRepository repository = new SqliteGameRepository(databasePath);
                repository.initialize();
                List<GameSaveSummary> saves = repository.listSaves();
                if (onSuccess != null) {
                    javafx.application.Platform.runLater(() -> onSuccess.accept(saves));
                }
            } catch (Exception e) {
                String message = e.getMessage() == null ? "Unknown error" : e.getMessage();
                if (onError != null) {
                    javafx.application.Platform.runLater(() -> onError.accept(message));
                }
            }
        }, "load-saves-task");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Loads a single saved game by ID asynchronously.
     *
     * @param id        the save ID to load
     * @param onSuccess called with the loaded game state on success
     * @param onError   called with an error message on failure
     */
    public void loadSave(long id, Consumer<GameState> onSuccess, Consumer<String> onError) {
        Thread worker = new Thread(() -> {
            try {
                Path databasePath = SaveGameStorage.resolveDefaultDatabasePath();
                GameRepository repository = new SqliteGameRepository(databasePath);
                repository.initialize();
                Optional<GameState> state = repository.load(id);
                GameState gameState = state.orElseThrow(
                        () -> new IllegalStateException("Save not found: " + id));
                if (onSuccess != null) {
                    javafx.application.Platform.runLater(() -> onSuccess.accept(gameState));
                }
            } catch (Exception e) {
                String message = e.getMessage() == null ? "Unknown error" : e.getMessage();
                if (onError != null) {
                    javafx.application.Platform.runLater(() -> onError.accept(message));
                }
            }
        }, "load-save-task");
        worker.setDaemon(true);
        worker.start();
    }
}
