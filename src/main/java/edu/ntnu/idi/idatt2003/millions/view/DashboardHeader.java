package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.PlayerStatus;
import java.math.BigDecimal;
import java.util.Locale;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

final class DashboardHeader {

    private static final BigDecimal DEFAULT_DASHBOARD_AMOUNT = new BigDecimal("10000.00");

    private final ExchangeController controller;
    private final Runnable onToggleProfile;
    private final Runnable onAdvanceWeek;

    private Label userLabel;
    private Label weekLabel;
    private Label cashValue;
    private Label netWorthValue;
    private Label statusBadge;
    private Button profileToggleButton;

    DashboardHeader(ExchangeController controller, Runnable onToggleProfile, Runnable onAdvanceWeek) {
        this.controller = controller;
        this.onToggleProfile = onToggleProfile;
        this.onAdvanceWeek = onAdvanceWeek;
    }

    HBox createHeader() {
        HBox header = new HBox(18);
        header.getStyleClass().add("dashboard-header");
        header.setAlignment(Pos.CENTER_LEFT);

        HBox headerLeft = createHeaderLeft();
        HBox headerRight = createHeaderRight();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(headerLeft, spacer, headerRight);
        return header;
    }

    void refresh() {
        if (userLabel == null || weekLabel == null || cashValue == null || netWorthValue == null || statusBadge == null) {
            return;
        }

        userLabel.setText(resolvePlayerName());
        weekLabel.setText(resolveWeekText());
        cashValue.setText(DashboardFormatters.formatMoney(resolveCash()));
        netWorthValue.setText(DashboardFormatters.formatMoney(resolveNetWorth()));
        statusBadge.setText(resolveStatusText());
    }

    void updateProfileToggleLabel(boolean showingProfile) {
        if (profileToggleButton == null) {
            return;
        }
        profileToggleButton.setText(resolveProfileToggleLabel(showingProfile));
    }

    private HBox createHeaderLeft() {
        HBox left = new HBox(14);
        left.getStyleClass().add("header-left");
        left.setAlignment(Pos.CENTER_LEFT);

        VBox brand = new VBox(2);
        HBox brandRow = new HBox(6);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        SVGPath logo = DashboardIcons.createIcon(DashboardIcons.LOGO_PATH, "icon-fill-accent", 16);
        Label title = new Label("Millions");
        title.getStyleClass().add("brand-title");
        brandRow.getChildren().addAll(logo, title);

        userLabel = new Label(resolvePlayerName());
        userLabel.getStyleClass().add("brand-subtitle");
        brand.getChildren().addAll(brandRow, userLabel);

        Region divider = new Region();
        divider.getStyleClass().add("header-divider");

        HBox weekInfo = new HBox(4);
        weekInfo.setAlignment(Pos.CENTER_LEFT);
        SVGPath clock = DashboardIcons.createIcon(DashboardIcons.CLOCK_PATH, "icon-stroke-muted", 14);
        weekLabel = new Label(resolveWeekText());
        weekLabel.getStyleClass().add("header-week");
        weekInfo.getChildren().addAll(clock, weekLabel);

        left.getChildren().addAll(brand, divider, weekInfo);
        return left;
    }

    private HBox createHeaderRight() {
        HBox right = new HBox(18);
        right.getStyleClass().add("header-right");
        right.setAlignment(Pos.CENTER_RIGHT);

        cashValue = new Label(DashboardFormatters.formatMoney(resolveCash()));
        VBox cash = createStatBlock("Cash", cashValue, false);

        netWorthValue = new Label(DashboardFormatters.formatMoney(resolveNetWorth()));
        VBox netWorth = createStatBlock("Net Worth", netWorthValue, true);

        VBox status = new VBox(2);
        status.setAlignment(Pos.CENTER_RIGHT);
        Label statusLabel = new Label("Status");
        statusLabel.getStyleClass().add("stat-label");
        statusBadge = new Label(resolveStatusText());
        statusBadge.getStyleClass().add("status-badge");
        status.getChildren().addAll(statusLabel, statusBadge);

        profileToggleButton = new Button(resolveProfileToggleLabel(false));
        profileToggleButton.getStyleClass().add("secondary-button");
        profileToggleButton.setOnAction(event -> {
            if (onToggleProfile != null) {
                onToggleProfile.run();
            }
        });

        Button nextWeek = new Button("Next Week");
        nextWeek.getStyleClass().add("primary-action");
        nextWeek.setContentDisplay(ContentDisplay.LEFT);
        nextWeek.setGraphicTextGap(6);
        nextWeek.setGraphic(DashboardIcons.createIcon(DashboardIcons.ARROW_RIGHT_PATH, "icon-stroke-inverse", 14));
        if (controller == null) {
            nextWeek.setDisable(true);
        } else {
            nextWeek.setOnAction(event -> {
                if (onAdvanceWeek != null) {
                    onAdvanceWeek.run();
                } else {
                    controller.advance();
                    refresh();
                }
            });
        }

        right.getChildren().addAll(cash, netWorth, status, profileToggleButton, nextWeek);
        return right;
    }

    private VBox createStatBlock(String labelText, Label amount, boolean accent) {
        VBox block = new VBox(2);
        block.setAlignment(Pos.CENTER_RIGHT);
        block.getStyleClass().add("stat-block");

        Label label = new Label(labelText);
        label.getStyleClass().add("stat-label");

        amount.getStyleClass().add("stat-value");
        if (accent) {
            amount.getStyleClass().add("stat-value-accent");
        }

        block.getChildren().addAll(label, amount);
        return block;
    }

    private String resolvePlayerName() {
        if (controller == null) {
            return "Player";
        }
        return controller.getPlayer().getName();
    }

    private String resolveWeekText() {
        if (controller == null) {
            return "Week 1";
        }
        Exchange exchange = controller.getExchange();
        return "Week " + exchange.getWeek();
    }

    private BigDecimal resolveCash() {
        if (controller == null) {
            return DEFAULT_DASHBOARD_AMOUNT;
        }
        Player player = controller.getPlayer();
        return player.getMoney();
    }

    private BigDecimal resolveNetWorth() {
        if (controller == null) {
            return DEFAULT_DASHBOARD_AMOUNT;
        }
        Player player = controller.getPlayer();
        return player.getNetWorth();
    }

    private String resolveStatusText() {
        if (controller == null) {
            return "Novice";
        }
        return formatStatus(controller.getPlayer().getStatus());
    }

    private String formatStatus(PlayerStatus status) {
        String name = status.name().toLowerCase(Locale.US);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private String resolveProfileToggleLabel(boolean showingProfile) {
        return showingProfile ? "Home" : "Profile";
    }
}
