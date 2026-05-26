package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.GameRepository;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.SaveGameStorage;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.SqliteGameRepository;
import edu.ntnu.idi.idatt2003.millions.model.GameState;
import edu.ntnu.idi.idatt2003.millions.model.Sale;
import edu.ntnu.idi.idatt2003.millions.model.Transaction;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Profile page placeholder with a save action.
 */
public class ProfilePage {

    private final ExchangeController controller;
    private static final int MAX_OUTCOMES = 3;

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

        VBox stats = createStatsCard();

        Button saveButton = new Button("Save Game");
        saveButton.getStyleClass().add("primary-button");
        if (controller == null) {
            saveButton.setDisable(true);
        } else {
            saveButton.setOnAction(event -> saveGame());
        }

        page.getChildren().addAll(title, stats, saveButton);
        return page;
    }

    private VBox createStatsCard() {
        VBox stats = new VBox(10);
        stats.getStyleClass().add("profile-stats");
        stats.setAlignment(Pos.CENTER_LEFT);

        HBox columns = new HBox(24);
        columns.getStyleClass().add("profile-stats-columns");
        columns.setAlignment(Pos.CENTER_LEFT);

        VBox totals = new VBox(8);
        totals.setAlignment(Pos.CENTER_LEFT);
        totals.getChildren().addAll(
                createStatRow("Shares purchased", resolveTotalPurchased()),
                createStatRow("Shares sold", resolveTotalSold()));

        VBox wins = createOutcomeColumn("Top wins", resolveTopWins(), "no wins");
        VBox losses = createOutcomeColumn("Top losses", resolveTopLosses(), "no losers");

        columns.getChildren().addAll(totals, wins, losses);
        stats.getChildren().add(columns);

        return stats;
    }

    private VBox createOutcomeColumn(String titleText, List<OutcomeRow> outcomes, String emptyLabel) {
        VBox column = new VBox(8);
        column.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(titleText);
        title.getStyleClass().add("profile-section-title");
        column.getChildren().add(title);

        if (outcomes.isEmpty()) {
            Label empty = new Label(emptyLabel);
            empty.getStyleClass().add("profile-list-empty");
            column.getChildren().add(empty);
            return column;
        }

        for (OutcomeRow outcome : outcomes) {
            HBox row = new HBox(8);
            row.getStyleClass().add("profile-list-item");
            row.setAlignment(Pos.CENTER_LEFT);

            Label symbol = new Label(outcome.symbol());
            symbol.getStyleClass().add("profile-list-symbol");

            Label value = new Label(formatSignedMoney(outcome.netProfit()));
            value.getStyleClass().add("profile-list-value");
            if (outcome.netProfit().compareTo(BigDecimal.ZERO) > 0) {
                value.getStyleClass().add("profile-list-value-positive");
            } else if (outcome.netProfit().compareTo(BigDecimal.ZERO) < 0) {
                value.getStyleClass().add("profile-list-value-negative");
            }

            row.getChildren().addAll(symbol, value);
            column.getChildren().add(row);
        }

        return column;
    }

    private HBox createStatRow(String labelText, BigDecimal value) {
        HBox row = new HBox(12);
        row.getStyleClass().add("profile-stat-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.getStyleClass().add("stat-label");

        Label valueLabel = new Label(DashboardFormatters.formatQuantity(value));
        valueLabel.getStyleClass().add("stat-value");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(label, spacer, valueLabel);
        return row;
    }

    private BigDecimal resolveTotalPurchased() {
        if (controller == null) {
            return BigDecimal.ZERO;
        }
        return controller.getPlayer().getTransactionArchive().getTotalPurchasedQuantity();
    }

    private BigDecimal resolveTotalSold() {
        if (controller == null) {
            return BigDecimal.ZERO;
        }
        return controller.getPlayer().getTransactionArchive().getTotalSoldQuantity();
    }

    private List<OutcomeRow> resolveTopWins() {
        return resolveSaleOutcomes(true);
    }

    private List<OutcomeRow> resolveTopLosses() {
        return resolveSaleOutcomes(false);
    }

    private List<OutcomeRow> resolveSaleOutcomes(boolean wins) {
        if (controller == null) {
            return List.of();
        }

        Map<String, BigDecimal> outcomes = new HashMap<>();
        for (Transaction transaction : controller.getPlayer().getTransactionArchive().getTransactions()) {
            if (transaction instanceof Sale sale) {
                BigDecimal quantity = sale.getShare().getQuantity();
                BigDecimal costBasis = sale.getShare().getPurchasePrice().multiply(quantity);
                BigDecimal netProfit = sale.getCalculator().getTotal().subtract(costBasis);
                String symbol = sale.getShare().getStock().getSymbol();
                outcomes.merge(symbol, netProfit, BigDecimal::add);
            }
        }

        List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(outcomes.entrySet());
        Comparator<Map.Entry<String, BigDecimal>> comparator = Map.Entry.comparingByValue();
        if (wins) {
            comparator = comparator.reversed();
        }

        return entries.stream()
                .filter(entry -> wins
                        ? entry.getValue().compareTo(BigDecimal.ZERO) > 0
                        : entry.getValue().compareTo(BigDecimal.ZERO) < 0)
                .sorted(comparator)
                .limit(MAX_OUTCOMES)
                .map(entry -> new OutcomeRow(entry.getKey(), entry.getValue()))
                .toList();
    }

    private record OutcomeRow(String symbol, BigDecimal netProfit) {
    }

    private String formatSignedMoney(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return "-" + DashboardFormatters.formatMoney(amount.abs());
        }
        return DashboardFormatters.formatMoney(amount);
    }

    private void saveGame() {
        if (controller == null) {
            return;
        }

        Task<Long> saveTask = new Task<>() {
            @Override
            protected Long call() throws Exception {
                Path databasePath = SaveGameStorage.resolveDefaultDatabasePath();
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

    private void showAlert(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
