package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.io.StockCsvLoader;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Window;

final class DashboardStockPanel {

    private static final String STOCK_RESOURCE = "data/sp500.csv";
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private enum StockTab {
        ALL,
        WATCHLIST,
        MOVERS
    }

    private enum ChangeKind {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }

    private record StockInfo(Stock stock, String price, String change, ChangeKind changeKind) {
    }

    private record StockChange(BigDecimal percent, ChangeKind kind) {
    }

    private final ExchangeController controller;
    private final Runnable onTradeComplete;
    private final Set<String> watchlistSymbols = new HashSet<>();

    private StockTab activeStockTab = StockTab.ALL;
    private TextField searchField;
    private VBox stockListContainer;
    private Label allStocksTab;
    private Label watchlistTab;
    private Label marketMoversTab;

    DashboardStockPanel(ExchangeController controller, Runnable onTradeComplete) {
        this.controller = controller;
        this.onTradeComplete = onTradeComplete;
    }

    VBox createPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel-left");
        panel.setPadding(new Insets(14));

        HBox tabs = createStockTabBar();
        HBox search = createSearchField();
        ScrollPane stockList = createStockList();
        VBox.setVgrow(stockList, Priority.ALWAYS);

        panel.getChildren().addAll(tabs, search, stockList);
        return panel;
    }

    void refresh() {
        updateStockTabs();
        refreshStockList();
    }

    private HBox createStockTabBar() {
        HBox tabs = new HBox(6);
        tabs.getStyleClass().add("dashboard-tabs");
        tabs.setAlignment(Pos.CENTER_LEFT);

        allStocksTab = createTabLabel("All Stocks", StockTab.ALL);
        watchlistTab = createTabLabel(buildWatchlistLabel(), StockTab.WATCHLIST);
        marketMoversTab = createTabLabel("Market Movers", StockTab.MOVERS);

        tabs.getChildren().addAll(allStocksTab, watchlistTab, marketMoversTab);
        updateStockTabs();
        return tabs;
    }

    private Label createTabLabel(String text, StockTab tab) {
        Label label = new Label(text);
        label.getStyleClass().add("dashboard-tab");
        label.setOnMouseClicked(event -> setActiveStockTab(tab));
        return label;
    }

    private void setActiveStockTab(StockTab tab) {
        activeStockTab = tab;
        updateStockTabs();
        refreshStockList();
    }

    private void updateStockTabs() {
        if (allStocksTab == null || watchlistTab == null || marketMoversTab == null) {
            return;
        }

        watchlistTab.setText(buildWatchlistLabel());
        updateTabStyle(allStocksTab, activeStockTab == StockTab.ALL);
        updateTabStyle(watchlistTab, activeStockTab == StockTab.WATCHLIST);
        updateTabStyle(marketMoversTab, activeStockTab == StockTab.MOVERS);
    }

    private void updateTabStyle(Label tab, boolean active) {
        if (active) {
            if (!tab.getStyleClass().contains("dashboard-tab-active")) {
                tab.getStyleClass().add("dashboard-tab-active");
            }
            return;
        }
        tab.getStyleClass().remove("dashboard-tab-active");
    }

    private String buildWatchlistLabel() {
        return "Watchlist (" + watchlistSymbols.size() + ")";
    }

    private HBox createSearchField() {
        HBox search = new HBox(8);
        search.getStyleClass().add("search-container");
        search.setAlignment(Pos.CENTER_LEFT);

        SVGPath icon = DashboardIcons.createIcon(DashboardIcons.SEARCH_PATH, "icon-stroke-muted", 14);
        searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Search stocks by symbol or name...");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> refreshStockList());
        searchField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        search.setOnMouseClicked(event -> searchField.requestFocus());

        search.getChildren().addAll(icon, searchField);
        return search;
    }

    private ScrollPane createStockList() {
        stockListContainer = new VBox(10);
        stockListContainer.getStyleClass().add("stock-list");
        stockListContainer.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(stockListContainer);
        scrollPane.getStyleClass().add("stock-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        refreshStockList();
        return scrollPane;
    }

    private VBox createStockCard(StockInfo stockInfo) {
        VBox card = new VBox(16);
        card.getStyleClass().add("stock-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(2);
        HBox symbolRow = new HBox(6);
        symbolRow.setAlignment(Pos.CENTER_LEFT);

        Stock stock = stockInfo.stock();
        Label symbol = new Label(stock.getSymbol());
        symbol.getStyleClass().add("stock-symbol");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        SVGPath star = DashboardIcons.createIcon(DashboardIcons.STAR_PATH, "icon-outline", 14);
        star.getStyleClass().add("star-toggle");
        boolean isFavorite = watchlistSymbols.contains(stock.getSymbol());
        updateStarStyle(star, isFavorite);
        star.setOnMouseClicked(event -> {
            toggleWatchlist(stock.getSymbol());
            updateStarStyle(star, watchlistSymbols.contains(stock.getSymbol()));
        });

        symbolRow.getChildren().addAll(symbol, spacer, star);

        Label company = new Label(stock.getCompanyName());
        company.getStyleClass().add("stock-name");

        left.getChildren().addAll(symbolRow, company);
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = new VBox(2);
        right.setAlignment(Pos.CENTER_RIGHT);
        Label price = new Label(stockInfo.price());
        price.getStyleClass().add("stock-price");

        HBox changeRow = new HBox(4);
        changeRow.setAlignment(Pos.CENTER_RIGHT);
        String changeClass;
        String iconClass;
        String iconPath;
        switch (stockInfo.changeKind()) {
            case POSITIVE -> {
                changeClass = "change-positive";
                iconClass = "icon-positive";
                iconPath = DashboardIcons.ARROW_UP_PATH;
            }
            case NEGATIVE -> {
                changeClass = "change-negative";
                iconClass = "icon-negative";
                iconPath = DashboardIcons.ARROW_DOWN_PATH;
            }
            default -> {
                changeClass = "change-neutral";
                iconClass = "icon-neutral";
                iconPath = DashboardIcons.NEUTRAL_PATH;
            }
        }
        SVGPath changeIcon = DashboardIcons.createIcon(iconPath, iconClass, 10);
        Label change = new Label(stockInfo.change());
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

        if (controller == null) {
            buy.setDisable(true);
            sell.setDisable(true);
        } else {
            buy.setOnAction(event -> openBuyDialog(buy, stock));
            sell.setOnAction(event -> openSellDialog(sell, stock));
        }

        buy.setMaxWidth(Double.MAX_VALUE);
        sell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(buy, Priority.ALWAYS);
        HBox.setHgrow(sell, Priority.ALWAYS);

        actions.getChildren().addAll(buy, sell);

        card.getChildren().addAll(header, actions);
        return card;
    }

    private VBox createEmptyStockCard() {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("stock-card", "stock-card-empty");
        card.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(resolveEmptyStockTitle());
        title.getStyleClass().add("stock-empty-title");
        Label subtitle = new Label(resolveEmptyStockSubtitle());
        subtitle.getStyleClass().add("stock-empty-subtitle");

        card.getChildren().addAll(title, subtitle);
        return card;
    }

    private String resolveEmptyStockTitle() {
        if (activeStockTab == StockTab.WATCHLIST) {
            return "No watchlist found";
        }
        return "No stocks loaded";
    }

    private String resolveEmptyStockSubtitle() {
        if (activeStockTab == StockTab.WATCHLIST) {
            return "Click the star icon to favorite and add stocks to the watchlist.";
        }
        return "Check that data/sp500.csv is available.";
    }

    private List<StockInfo> loadStockInfos(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();
        if (controller != null) {
            List<Stock> stocks = controller.findStocks(normalized);
            return stocks.stream()
                .map(this::toStockInfo)
                .toList();
        }

        StockCsvLoader loader = new StockCsvLoader();
        try {
            List<Stock> stocks = loader.loadFromResource(STOCK_RESOURCE);
            if (normalized.isBlank()) {
                return stocks.stream()
                    .map(this::toStockInfo)
                    .toList();
            }

            String lower = normalized.toLowerCase(Locale.US);
            return stocks.stream()
                .filter(stock -> stock.getSymbol().toLowerCase(Locale.US).contains(lower)
                    || stock.getCompanyName().toLowerCase(Locale.US).contains(lower))
                .map(this::toStockInfo)
                .toList();
        } catch (IOException exception) {
            System.err.println("Failed to load stock CSV: " + exception.getMessage());
            return List.of();
        }
    }

    private StockInfo toStockInfo(Stock stock) {
        StockChange change = calculateChange(stock);
        return new StockInfo(
            stock,
            DashboardFormatters.formatPrice(stock.getSalesPrice()),
            formatPercent(change),
            change.kind()
        );
    }

    private StockChange calculateChange(Stock stock) {
        List<BigDecimal> prices = stock.getHistoricalPrices();
        if (prices.size() < 2) {
            return new StockChange(BigDecimal.ZERO, ChangeKind.NEUTRAL);
        }

        BigDecimal latest = prices.get(prices.size() - 1);
        BigDecimal previous = prices.get(prices.size() - 2);
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return new StockChange(BigDecimal.ZERO, ChangeKind.NEUTRAL);
        }

        BigDecimal delta = latest.subtract(previous);
        BigDecimal percent = delta.divide(previous, 6, RoundingMode.HALF_UP)
            .multiply(ONE_HUNDRED);
        ChangeKind kind = percent.signum() > 0
            ? ChangeKind.POSITIVE
            : (percent.signum() < 0 ? ChangeKind.NEGATIVE : ChangeKind.NEUTRAL);
        return new StockChange(percent, kind);
    }

    private String formatPercent(StockChange change) {
        DecimalFormat format = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        BigDecimal value = change.percent().abs().setScale(2, RoundingMode.HALF_UP);
        String formatted = format.format(value) + "%";
        if (change.kind() == ChangeKind.POSITIVE) {
            return "+" + formatted;
        }
        if (change.kind() == ChangeKind.NEGATIVE) {
            return "-" + formatted;
        }
        return formatted;
    }

    private void refreshStockList() {
        if (stockListContainer == null) {
            return;
        }

        stockListContainer.getChildren().clear();
        String keyword = searchField == null ? "" : searchField.getText();
        String searchTerm = activeStockTab == StockTab.MOVERS ? "" : keyword;
        List<StockInfo> stocks = loadStockInfos(searchTerm);
        if (activeStockTab == StockTab.WATCHLIST) {
            stocks = stocks.stream()
                .filter(stock -> watchlistSymbols.contains(stock.stock().getSymbol()))
                .toList();
        } else if (activeStockTab == StockTab.MOVERS) {
            stocks = filterMarketMovers(stocks);
        }
        if (stocks.isEmpty()) {
            stockListContainer.getChildren().add(createEmptyStockCard());
            return;
        }

        for (StockInfo stock : stocks) {
            stockListContainer.getChildren().add(createStockCard(stock));
        }
    }

    private void openBuyDialog(Button source, Stock stock) {
        Window owner = resolveOwnerWindow(source);
        BuyStockDialog dialog = new BuyStockDialog(controller, stock, onTradeComplete);
        dialog.show(owner);
    }

    private void openSellDialog(Button source, Stock stock) {
        Window owner = resolveOwnerWindow(source);
        SellStockDialog dialog = new SellStockDialog(controller, stock, onTradeComplete);
        dialog.show(owner);
    }

    private List<StockInfo> filterMarketMovers(List<StockInfo> stocks) {
        List<StockInfo> gainers = stocks.stream()
            .filter(info -> info.stock().getLatestPriceChange().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing((StockInfo info) -> info.stock().getLatestPriceChange())
                .reversed())
            .limit(5)
            .toList();

        List<StockInfo> losers = stocks.stream()
            .filter(info -> info.stock().getLatestPriceChange().compareTo(BigDecimal.ZERO) < 0)
            .sorted(Comparator.comparing(info -> info.stock().getLatestPriceChange()))
            .limit(5)
            .toList();

        List<StockInfo> movers = new ArrayList<>(gainers.size() + losers.size());
        movers.addAll(gainers);
        movers.addAll(losers);
        return movers;
    }

    private void toggleWatchlist(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return;
        }

        if (watchlistSymbols.contains(symbol)) {
            watchlistSymbols.remove(symbol);
        } else {
            watchlistSymbols.add(symbol);
        }

        updateStockTabs();
        if (activeStockTab == StockTab.WATCHLIST) {
            refreshStockList();
        }
    }

    private void updateStarStyle(SVGPath star, boolean favorite) {
        star.getStyleClass().remove("icon-star-active");
        if (favorite) {
            star.getStyleClass().add("icon-star-active");
        }
    }

    private Window resolveOwnerWindow(Button source) {
        if (source.getScene() == null) {
            return null;
        }
        return source.getScene().getWindow();
    }
}
