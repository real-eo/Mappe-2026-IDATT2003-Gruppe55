package edu.ntnu.idi.idatt2003.millions.view.dashboard;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.view.dialog.BuyStockDialog;
import edu.ntnu.idi.idatt2003.millions.view.dialog.SellStockDialog;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Window;

/**
 * Left-side dashboard panel with stock list, search, filter, and tabs.
 */
final class DashboardLeftPanel {

    private static final double FILTER_POPUP_WIDTH = 240.0;
    private static final int MARKET_MOVERS_LIMIT = 5;

    private enum StockTab { ALL, WATCHLIST, MOVERS }

    private enum ChangeKind { POSITIVE, NEGATIVE, NEUTRAL }

    private enum SortOption {
        ALPHA_ASC("Alphabetical (A-Z)", Comparator.comparing(
                (StockInfo i) -> i.stock().getSymbol(), String.CASE_INSENSITIVE_ORDER)),
        ALPHA_DESC("Alphabetical (Z-A)", Comparator.comparing(
                (StockInfo i) -> i.stock().getSymbol(), String.CASE_INSENSITIVE_ORDER).reversed()),
        PRICE_ASC("Price: Low to High", Comparator.comparing(i -> i.stock().getSalesPrice())),
        PRICE_DESC("Price: High to Low",
                Comparator.comparing((StockInfo i) -> i.stock().getSalesPrice()).reversed()),
        CHANGE_ASC("Change: Low to High", Comparator.comparing(StockInfo::changePercent)),
        CHANGE_DESC("Change: High to Low", Comparator.comparing(StockInfo::changePercent).reversed());

        private final String label;
        private final Comparator<StockInfo> comparator;

        SortOption(String label, Comparator<StockInfo> comparator) {
            this.label = label;
            this.comparator = comparator;
        }

        Comparator<StockInfo> comparator() { return comparator; }

        @Override
        public String toString() { return label; }
    }

    private record StockInfo(Stock stock, String price, String change,
                             ChangeKind changeKind, BigDecimal changePercent) {}

    private final ExchangeController controller;
    private final Runnable onRefresh;
    private final Set<String> watchlist = new HashSet<>();
    private StockTab activeTab = StockTab.ALL;
    private SortOption activeSort = SortOption.ALPHA_ASC;

    private VBox stockListContainer;
    private TextField searchField;
    private TextField minPriceField;
    private TextField maxPriceField;
    private ComboBox<SortOption> sortDropdown;
    private Popup filterPopup;
    private Label allTab;
    private Label watchlistTab;
    private Label moversTab;

    DashboardLeftPanel(ExchangeController controller, Runnable onRefresh) {
        this.controller = controller;
        this.onRefresh = onRefresh;
    }

    /**
     * Builds and returns the left panel node.
     *
     * @return the panel root
     */
    VBox createPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel-left");
        panel.setPadding(new Insets(14));

        ScrollPane stockList = buildStockList();
        VBox.setVgrow(stockList, Priority.ALWAYS);
        panel.getChildren().addAll(buildTabBar(), buildSearchRow(), stockList);
        return panel;
    }

    /**
     * Refreshes the stock list display.
     */
    void refresh() {
        refreshStockList();
    }

    // ── Tab bar ──────────────────────────────────────────────────────────────

    private HBox buildTabBar() {
        allTab = tabLabel("All Stocks", StockTab.ALL);
        watchlistTab = tabLabel(watchlistLabel(), StockTab.WATCHLIST);
        moversTab = tabLabel("Market Movers", StockTab.MOVERS);
        updateTabStyles();

        HBox bar = new HBox(6);
        bar.getStyleClass().add("dashboard-tabs");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getChildren().addAll(allTab, watchlistTab, moversTab);
        return bar;
    }

    private Label tabLabel(String text, StockTab tab) {
        Label label = new Label(text);
        label.getStyleClass().add("dashboard-tab");
        label.setOnMouseClicked(e -> selectTab(tab));
        return label;
    }

    private void selectTab(StockTab tab) {
        activeTab = tab;
        updateTabStyles();
        refreshStockList();
    }

    private void updateTabStyles() {
        if (allTab == null) {
            return;
        }
        watchlistTab.setText(watchlistLabel());
        setTabActive(allTab, activeTab == StockTab.ALL);
        setTabActive(watchlistTab, activeTab == StockTab.WATCHLIST);
        setTabActive(moversTab, activeTab == StockTab.MOVERS);
    }

    private String watchlistLabel() {
        return "Watchlist (" + watchlist.size() + ")";
    }

    // ── Search + filter ───────────────────────────────────────────────────────

    private HBox buildSearchRow() {
        searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Search stocks by symbol or name...");
        searchField.textProperty().addListener((obs, o, n) -> refreshStockList());
        searchField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
            }
        });
        searchField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        filterPopup = buildFilterPopup();
        FilterButton filterButton = new FilterButton();
        filterButton.setOnAction(e -> toggleFilterPopup(filterButton));

        SVGPath icon = DashboardIcons.createIcon(DashboardIcons.SEARCH_PATH, "icon-stroke-muted", 14);
        HBox row = new HBox(8);
        row.getStyleClass().add("search-container");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setOnMouseClicked(e -> searchField.requestFocus());
        row.getChildren().addAll(icon, searchField, filterButton);
        return row;
    }

    private Popup buildFilterPopup() {
        sortDropdown = new ComboBox<>(FXCollections.observableArrayList(SortOption.values()));
        sortDropdown.getStyleClass().add("filter-dropdown");
        sortDropdown.setMaxWidth(Double.MAX_VALUE);
        sortDropdown.setValue(activeSort);
        sortDropdown.valueProperty().addListener((obs, o, n) -> {
            if (n != null) {
                activeSort = n;
                refreshStockList();
            }
        });

        minPriceField = numberField("Min");
        maxPriceField = numberField("Max");
        HBox.setHgrow(minPriceField, Priority.ALWAYS);
        HBox.setHgrow(maxPriceField, Priority.ALWAYS);

        HBox rangeRow = new HBox(8);
        rangeRow.getStyleClass().add("filter-range");
        rangeRow.getChildren().addAll(minPriceField, maxPriceField);

        Label sortLabel = new Label("Sort by");
        sortLabel.getStyleClass().add("filter-label");
        Label rangeLabel = new Label("Price range");
        rangeLabel.getStyleClass().add("filter-label");

        VBox content = new VBox(10);
        content.getStyleClass().add("filter-popup");
        content.setPrefWidth(FILTER_POPUP_WIDTH);
        content.getChildren().addAll(sortLabel, sortDropdown, rangeLabel, rangeRow);

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.getContent().add(content);
        return popup;
    }

    private TextField numberField(String prompt) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String t = change.getControlNewText();
            return (t.isBlank() || t.matches("\\d*(\\.\\d{0,2})?")) ? change : null;
        };
        TextField field = new TextField();
        field.getStyleClass().add("filter-input");
        field.setPromptText(prompt);
        field.setTextFormatter(new TextFormatter<>(filter));
        field.textProperty().addListener((obs, o, n) -> refreshStockList());
        return field;
    }

    private void toggleFilterPopup(Button anchor) {
        if (filterPopup == null || anchor == null) {
            return;
        }
        if (filterPopup.isShowing()) {
            filterPopup.hide();
            return;
        }
        Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        if (bounds == null) {
            return;
        }
        filterPopup.show(anchor, bounds.getMaxX() - FILTER_POPUP_WIDTH, bounds.getMaxY() + 6);
    }

    // ── Stock list ────────────────────────────────────────────────────────────

    private ScrollPane buildStockList() {
        stockListContainer = new VBox(10);
        stockListContainer.getStyleClass().add("stock-list");
        stockListContainer.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(stockListContainer);
        scroll.getStyleClass().add("stock-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        refreshStockList();
        return scroll;
    }

    private void refreshStockList() {
        if (stockListContainer == null) {
            return;
        }
        List<StockInfo> stocks;
        if (activeTab == StockTab.MOVERS) {
            stocks = loadMarketMovers();
        } else {
            String keyword = searchField == null ? "" : searchField.getText();
            BigDecimal minPrice = parsePrice(minPriceField);
            BigDecimal maxPrice = parsePrice(maxPriceField);
            stocks = loadStockInfos(keyword, minPrice, maxPrice);
            if (activeTab == StockTab.WATCHLIST) {
                stocks = stocks.stream().filter(s -> watchlist.contains(s.stock().getSymbol())).toList();
            }
        }
        stocks = stocks.stream().sorted(activeSort.comparator()).toList();

        stockListContainer.getChildren().clear();
        if (stocks.isEmpty()) {
            stockListContainer.getChildren().add(buildEmptyCard());
        } else {
            stocks.forEach(s -> stockListContainer.getChildren().add(buildStockCard(s)));
        }
    }

    private List<StockInfo> loadStockInfos(String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
        if (controller == null) {
            return List.of();
        }
        String normalized = keyword == null ? "" : keyword.trim();
        return controller.findStocks(normalized, minPrice, maxPrice).stream()
                .map(this::toStockInfo).toList();
    }

    private List<StockInfo> loadMarketMovers() {
        if (controller == null) {
            return List.of();
        }
        return controller.getMarketMovers(MARKET_MOVERS_LIMIT).stream()
                .map(this::toStockInfo).toList();
    }

    private StockInfo toStockInfo(Stock stock) {
        BigDecimal pct = stock.getLatestPriceChangePercent();
        int sign = pct.signum();
        ChangeKind kind = sign > 0 ? ChangeKind.POSITIVE : sign < 0 ? ChangeKind.NEGATIVE : ChangeKind.NEUTRAL;
        return new StockInfo(stock, DashboardFormatters.formatPrice(stock.getSalesPrice()),
                DashboardFormatters.formatSignedPercent(pct), kind, pct);
    }

    // ── Cards ─────────────────────────────────────────────────────────────────

    private VBox buildStockCard(StockInfo info) {
        Stock stock = info.stock();

        SVGPath star = DashboardIcons.createIcon(DashboardIcons.STAR_PATH, "icon-outline", 14);
        star.getStyleClass().add("star-toggle");
        updateStarStyle(star, watchlist.contains(stock.getSymbol()));
        star.setOnMouseClicked(e -> {
            toggleWatchlist(stock.getSymbol());
            updateStarStyle(star, watchlist.contains(stock.getSymbol()));
        });

        HBox symbolRow = new HBox(6);
        symbolRow.setAlignment(Pos.CENTER_LEFT);
        Label symbolLabel = new Label(stock.getSymbol());
        symbolLabel.getStyleClass().add("stock-symbol");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        symbolRow.getChildren().addAll(symbolLabel, spacer, star);

        VBox left = new VBox(2);
        left.getChildren().addAll(symbolRow, label(stock.getCompanyName(), "stock-name"));
        HBox.setHgrow(left, Priority.ALWAYS);

        record ChangeStyle(String css, String iconCss, String iconPath) {}
        ChangeStyle cs = switch (info.changeKind()) {
            case POSITIVE -> new ChangeStyle("change-positive", "icon-positive", DashboardIcons.ARROW_UP_PATH);
            case NEGATIVE -> new ChangeStyle("change-negative", "icon-negative", DashboardIcons.ARROW_DOWN_PATH);
            default -> new ChangeStyle("change-neutral", "icon-neutral", DashboardIcons.NEUTRAL_PATH);
        };

        Label changeLabel = new Label(info.change());
        changeLabel.getStyleClass().addAll("stock-change", cs.css());
        HBox changeRow = new HBox(4);
        changeRow.setAlignment(Pos.CENTER_RIGHT);
        changeRow.getChildren().addAll(DashboardIcons.createIcon(cs.iconPath(), cs.iconCss(), 10), changeLabel);

        VBox right = new VBox(2);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.getChildren().addAll(label(info.price(), "stock-price"), changeRow);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(left, right);

        Button buy = actionButton("Buy", "buy-button", stock, true);
        Button sell = actionButton("Sell", "sell-button", stock, false);
        buy.setMaxWidth(Double.MAX_VALUE);
        sell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(buy, Priority.ALWAYS);
        HBox.setHgrow(sell, Priority.ALWAYS);

        HBox actions = new HBox(8);
        actions.getStyleClass().add("stock-actions");
        actions.getChildren().addAll(buy, sell);

        VBox card = new VBox(16);
        card.getStyleClass().add("stock-card");
        card.getChildren().addAll(header, buildHighLowRow(stock), actions);
        return card;
    }

    private HBox buildHighLowRow(Stock stock) {
        Label hLabel = new Label("H");
        hLabel.getStyleClass().add("stock-hl-label");
        Label hValue = new Label(DashboardFormatters.formatPrice(stock.getHighestPrice()));
        hValue.getStyleClass().addAll("stock-hl-value", "stock-hl-high");

        Label sep = new Label("·");
        sep.getStyleClass().add("stock-hl-sep");

        Label lLabel = new Label("L");
        lLabel.getStyleClass().add("stock-hl-label");
        Label lValue = new Label(DashboardFormatters.formatPrice(stock.getLowestPrice()));
        lValue.getStyleClass().addAll("stock-hl-value", "stock-hl-low");

        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(hLabel, hValue, sep, lLabel, lValue);
        return row;
    }

    private Button actionButton(String text, String styleClass, Stock stock, boolean isBuy) {
        Button btn = new Button(text);
        btn.getStyleClass().add(styleClass);
        if (controller == null) {
            btn.setDisable(true);
        } else if (isBuy) {
            btn.setOnAction(e -> openBuyDialog(btn, stock));
        } else {
            btn.setOnAction(e -> openSellDialog(btn, stock));
        }
        return btn;
    }

    private VBox buildEmptyCard() {
        String title = activeTab == StockTab.WATCHLIST ? "No watchlist found" : "No stocks loaded";
        String subtitle = activeTab == StockTab.WATCHLIST
                ? "Click the star icon to favorite and add stocks to the watchlist."
                : "Check that data/sp500.csv is available.";

        VBox card = new VBox(6);
        card.getStyleClass().addAll("stock-card", "stock-card-empty");
        card.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().addAll(label(title, "stock-empty-title"), label(subtitle, "stock-empty-subtitle"));
        return card;
    }

    // ── Filter / sort ─────────────────────────────────────────────────────────

    private static BigDecimal parsePrice(TextField field) {
        if (field == null) {
            return null;
        }
        String text = field.getText();
        if (text == null || text.isBlank() || text.equals(".")) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ── Watchlist ─────────────────────────────────────────────────────────────

    private void toggleWatchlist(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return;
        }
        if (!watchlist.remove(symbol)) {
            watchlist.add(symbol);
        }
        updateTabStyles();
        if (activeTab == StockTab.WATCHLIST) {
            refreshStockList();
        }
    }

    private static void updateStarStyle(SVGPath star, boolean active) {
        star.getStyleClass().remove("icon-star-active");
        if (active) {
            star.getStyleClass().add("icon-star-active");
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private void openBuyDialog(Button source, Stock stock) {
        new BuyStockDialog(controller, stock, onRefresh).show(ownerWindow(source));
    }

    private void openSellDialog(Button source, Stock stock) {
        new SellStockDialog(controller, stock, onRefresh).show(ownerWindow(source));
    }

    private static Window ownerWindow(Button source) {
        return source.getScene() == null ? null : source.getScene().getWindow();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void setTabActive(Label tab, boolean active) {
        tab.getStyleClass().remove("dashboard-tab-active");
        if (active) {
            tab.getStyleClass().add("dashboard-tab-active");
        }
    }

    private static Label label(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        return l;
    }
}
