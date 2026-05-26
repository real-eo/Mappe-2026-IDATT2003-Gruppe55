package edu.ntnu.idi.idatt2003.millions.view.page;

import edu.ntnu.idi.idatt2003.millions.controller.ProfileController;
import edu.ntnu.idi.idatt2003.millions.controller.ProfileController.OutcomeEntry;
import edu.ntnu.idi.idatt2003.millions.view.dashboard.DashboardFormatters;
import java.math.BigDecimal;
import java.util.List;
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
 * Profile page showing player statistics and a save action.
 */
public class ProfilePage {

    private final ProfileController profileController;
    private static final int MAX_OUTCOMES = 3;

    /**
     * Constructs the profile page.
     *
     * @param profileController the controller providing profile data
     */
    public ProfilePage(ProfileController profileController) {
        this.profileController = profileController;
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
        if (profileController == null) {
            saveButton.setDisable(true);
        } else {
            saveButton.setOnAction(event -> handleSave());
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

        BigDecimal totalPurchased = profileController == null
                ? BigDecimal.ZERO : profileController.getTotalPurchased();
        BigDecimal totalSold = profileController == null
                ? BigDecimal.ZERO : profileController.getTotalSold();
        List<OutcomeEntry> wins = profileController == null
                ? List.of() : profileController.getTopWins(MAX_OUTCOMES);
        List<OutcomeEntry> losses = profileController == null
                ? List.of() : profileController.getTopLosses(MAX_OUTCOMES);

        VBox totals = new VBox(8);
        totals.setAlignment(Pos.CENTER_LEFT);
        totals.getChildren().addAll(
                createStatRow("Shares purchased", totalPurchased),
                createStatRow("Shares sold", totalSold));

        VBox winsColumn = createOutcomeColumn("Top wins", wins, "no wins");
        VBox lossesColumn = createOutcomeColumn("Top losses", losses, "no losers");

        columns.getChildren().addAll(totals, winsColumn, lossesColumn);
        stats.getChildren().add(columns);
        return stats;
    }

    private VBox createOutcomeColumn(String titleText, List<OutcomeEntry> outcomes, String emptyLabel) {
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

        for (OutcomeEntry outcome : outcomes) {
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

    private void handleSave() {
        profileController.saveGame(
            saveId -> showAlert(Alert.AlertType.INFORMATION,
                "Save Game", "Game saved successfully", "Save ID: " + saveId),
            errorMessage -> showAlert(Alert.AlertType.ERROR,
                "Save Game", "Failed to save game", errorMessage));
    }

    private String formatSignedMoney(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return "-" + DashboardFormatters.formatMoney(amount.abs());
        }
        return DashboardFormatters.formatMoney(amount);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
