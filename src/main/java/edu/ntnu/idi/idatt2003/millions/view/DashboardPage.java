package edu.ntnu.idi.idatt2003.millions.view;

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;

/**
 * Static dashboard layout based on the Figma desktop design.
 */
public class DashboardPage {

    private static final double ICON_BASE_SIZE = 24.0;

    private static final String LOGO_PATH = "M3 16 L9 10 L13 14 L20 7 L21 8 L13 16 L9 12 L4 17 Z";
    private static final String CLOCK_PATH = "M12 4 A8 8 0 1 0 12 20 A8 8 0 1 0 12 4 M12 8 V12 L15 14";
    private static final String SEARCH_PATH = "M11 4 A7 7 0 1 0 11 18 A7 7 0 1 0 11 4 M16 16 L20 20";
    private static final String STAR_PATH = "M12 3 L14.8 8.6 L21 9.2 L16 13.2 L17.4 19.2 L12 16 L6.6 19.2 L8 13.2 L3 9.2 L9.2 8.6 Z";
    private static final String ARROW_UP_PATH = "M12 5 L18 11 H14 V19 H10 V11 H6 Z";
    private static final String ARROW_DOWN_PATH = "M12 19 L6 13 H10 V5 H14 V13 H18 Z";
    private static final String ARROW_RIGHT_PATH = "M5 12 H17 M13 8 L17 12 L13 16";
    private static final String PIE_PATH = "M12 3 A9 9 0 1 0 12 21 A9 9 0 1 0 12 3 M12 3 V12 H21 A9 9 0 0 0 12 3";

    private record StockInfo(String symbol, String name, String price, String change, boolean positive) {
    }

    public Parent createRoot() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("dashboard-root");
        root.setTop(createHeader());
        root.setCenter(createBody());
        return root;
    }

    private HBox createHeader() {
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

    private HBox createHeaderLeft() {
        HBox left = new HBox(14);
        left.getStyleClass().add("header-left");
        left.setAlignment(Pos.CENTER_LEFT);

        VBox brand = new VBox(2);
        HBox brandRow = new HBox(6);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        SVGPath logo = createIcon(LOGO_PATH, "icon-fill-accent", 16);
        Label title = new Label("Millions");
        title.getStyleClass().add("brand-title");
        brandRow.getChildren().addAll(logo, title);

        Label user = new Label("elias");
        user.getStyleClass().add("brand-subtitle");
        brand.getChildren().addAll(brandRow, user);

        Region divider = new Region();
        divider.getStyleClass().add("header-divider");

        HBox weekInfo = new HBox(4);
        weekInfo.setAlignment(Pos.CENTER_LEFT);
        SVGPath clock = createIcon(CLOCK_PATH, "icon-stroke-muted", 14);
        Label weekLabel = new Label("Week 1");
        weekLabel.getStyleClass().add("header-week");
        weekInfo.getChildren().addAll(clock, weekLabel);

        left.getChildren().addAll(brand, divider, weekInfo);
        return left;
    }

    private HBox createHeaderRight() {
        HBox right = new HBox(18);
        right.getStyleClass().add("header-right");
        right.setAlignment(Pos.CENTER_RIGHT);

        VBox cash = createStatBlock("Cash", "$10,000.00", false);
        VBox netWorth = createStatBlock("Net Worth", "$10,000.00", true);

        VBox status = new VBox(2);
        status.setAlignment(Pos.CENTER_RIGHT);
        Label statusLabel = new Label("Status");
        statusLabel.getStyleClass().add("stat-label");
        Label badge = new Label("Novice");
        badge.getStyleClass().add("status-badge");
        status.getChildren().addAll(statusLabel, badge);

        Button nextWeek = new Button("Next Week");
        nextWeek.getStyleClass().add("primary-action");
        nextWeek.setContentDisplay(ContentDisplay.LEFT);
        nextWeek.setGraphicTextGap(6);
        nextWeek.setGraphic(createIcon(ARROW_RIGHT_PATH, "icon-stroke-inverse", 14));

        right.getChildren().addAll(cash, netWorth, status, nextWeek);
        return right;
    }

    private VBox createStatBlock(String labelText, String value, boolean accent) {
        VBox block = new VBox(2);
        block.setAlignment(Pos.CENTER_RIGHT);
        block.getStyleClass().add("stat-block");

        Label label = new Label(labelText);
        label.getStyleClass().add("stat-label");

        Label amount = new Label(value);
        amount.getStyleClass().add("stat-value");
        if (accent) {
            amount.getStyleClass().add("stat-value-accent");
        }

        block.getChildren().addAll(label, amount);
        return block;
    }

    private HBox createBody() {
        HBox body = new HBox();
        body.getStyleClass().add("dashboard-body");

        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();

        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        body.getChildren().addAll(leftPanel, rightPanel);
        return body;
    }

    private VBox createLeftPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel-left");
        panel.setPadding(new Insets(14));

        HBox tabs = createTabBar(List.of("All Stocks", "Watchlist (0)", "Market Movers"), 0);
        HBox search = createSearchField();
        ScrollPane stockList = createStockList();
        VBox.setVgrow(stockList, Priority.ALWAYS);

        panel.getChildren().addAll(tabs, search, stockList);
        return panel;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("panel-right");
        panel.setPadding(new Insets(14));

        HBox tabs = createTabBar(List.of("Portfolio", "History (0)"), 0);
        StackPane content = new StackPane();
        content.getStyleClass().add("portfolio-panel");

        VBox emptyState = new VBox(10);
        emptyState.getStyleClass().add("empty-state");
        emptyState.setAlignment(Pos.CENTER);

        SVGPath emptyIcon = createIcon(PIE_PATH, "icon-stroke-muted", 48);
        Label emptyTitle = new Label("No Holdings");
        emptyTitle.getStyleClass().add("empty-title");
        Label emptySubtitle = new Label("Your portfolio is empty. Start buying stocks to build\nyour wealth.");
        emptySubtitle.getStyleClass().add("empty-subtitle");
        emptySubtitle.setTextAlignment(TextAlignment.CENTER);

        emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptySubtitle);
        content.getChildren().add(emptyState);
        VBox.setVgrow(content, Priority.ALWAYS);

        panel.getChildren().addAll(tabs, content);
        return panel;
    }

    private HBox createTabBar(List<String> labels, int activeIndex) {
        HBox tabs = new HBox(6);
        tabs.getStyleClass().add("dashboard-tabs");
        tabs.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < labels.size(); i++) {
            Label tab = new Label(labels.get(i));
            tab.getStyleClass().add("dashboard-tab");
            if (i == activeIndex) {
                tab.getStyleClass().add("dashboard-tab-active");
            }
            tabs.getChildren().add(tab);
        }

        return tabs;
    }

    private HBox createSearchField() {
        HBox search = new HBox(8);
        search.getStyleClass().add("search-container");
        search.setAlignment(Pos.CENTER_LEFT);

        SVGPath icon = createIcon(SEARCH_PATH, "icon-stroke-muted", 14);
        TextField field = new TextField();
        field.getStyleClass().add("search-field");
        field.setPromptText("Search stocks by symbol or name...");

        search.getChildren().addAll(icon, field);
        return search;
    }

    private ScrollPane createStockList() {
        VBox cards = new VBox(10);
        cards.getStyleClass().add("stock-list");
        cards.setFillWidth(true);

        List<StockInfo> stocks = List.of(
            new StockInfo("AAPL", "Apple Inc.", "$178.25", "+3.63%", true),
            new StockInfo("GOOGL", "Alphabet Inc.", "$142.65", "-0.94%", false),
            new StockInfo("MSFT", "Microsoft Corporation", "$380.50", "+0.66%", true),
            new StockInfo("TSLA", "Tesla Inc.", "$242.84", "+2.48%", true)
        );

        for (StockInfo stock : stocks) {
            cards.getChildren().add(createStockCard(stock));
        }

        ScrollPane scrollPane = new ScrollPane(cards);
        scrollPane.getStyleClass().add("stock-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    private VBox createStockCard(StockInfo stock) {
        VBox card = new VBox(16);
        card.getStyleClass().add("stock-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(2);
        HBox symbolRow = new HBox(6);
        symbolRow.setAlignment(Pos.CENTER_LEFT);

        Label symbol = new Label(stock.symbol());
        symbol.getStyleClass().add("stock-symbol");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        SVGPath star = createIcon(STAR_PATH, "icon-outline", 14);

        symbolRow.getChildren().addAll(symbol, spacer, star);

        Label company = new Label(stock.name());
        company.getStyleClass().add("stock-name");

        left.getChildren().addAll(symbolRow, company);
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = new VBox(2);
        right.setAlignment(Pos.CENTER_RIGHT);
        Label price = new Label(stock.price());
        price.getStyleClass().add("stock-price");

        HBox changeRow = new HBox(4);
        changeRow.setAlignment(Pos.CENTER_RIGHT);
        String changeClass = stock.positive() ? "change-positive" : "change-negative";
        SVGPath changeIcon = createIcon(stock.positive() ? ARROW_UP_PATH : ARROW_DOWN_PATH,
            stock.positive() ? "icon-positive" : "icon-negative", 10);
        Label change = new Label(stock.change());
        change.getStyleClass().addAll("stock-change", changeClass);

        changeRow.getChildren().addAll(changeIcon, change);
        right.getChildren().addAll(price, changeRow);

        header.getChildren().addAll(left, right);

        HBox actions = new HBox(8);
        actions.getStyleClass().add("stock-actions");

        Button buy = new Button("Buy");
        buy.getStyleClass().add("buy-button");
        Button sell = new Button("Sell");
        sell.getStyleClass().add("sell-button");
        sell.setDisable(true);

        buy.setMaxWidth(Double.MAX_VALUE);
        sell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(buy, Priority.ALWAYS);
        HBox.setHgrow(sell, Priority.ALWAYS);

        actions.getChildren().addAll(buy, sell);

        card.getChildren().addAll(header, actions);
        return card;
    }

    private SVGPath createIcon(String path, String styleClass, double size) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.getStyleClass().add(styleClass);
        double scale = size / ICON_BASE_SIZE;
        icon.setScaleX(scale);
        icon.setScaleY(scale);
        return icon;
    }
}
