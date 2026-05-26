package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.io.StockCsvLoader;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.PlayerStatus;
import edu.ntnu.idi.idatt2003.millions.model.Purchase;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.Transaction;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.UnaryOperator;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Window;

/**
 * Static dashboard layout based on the Figma desktop design.
 */
public class DashboardPage {

    private static final BigDecimal DEFAULT_DASHBOARD_AMOUNT = new BigDecimal("10000.00");
    private static final String STOCK_RESOURCE = "data/sp500.csv";
    private static final double FILTER_POPUP_WIDTH = 240.0;

    private enum StockTab {
        ALL,
        WATCHLIST,
        MOVERS
    }

    private enum RightTab {
        PORTFOLIO,
        HISTORY
    }

    private enum ChangeKind {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }

    private enum SortOption {
        ALPHA_ASC("Alphabetical (A-Z)", Comparator.comparing(
            (StockInfo info) -> info.stock().getSymbol(), String.CASE_INSENSITIVE_ORDER)),
        ALPHA_DESC("Alphabetical (Z-A)", Comparator.comparing(
            (StockInfo info) -> info.stock().getSymbol(), String.CASE_INSENSITIVE_ORDER).reversed()),
        PRICE_ASC("Price: Low to High", Comparator.comparing(info -> info.stock().getSalesPrice())),
        PRICE_DESC("Price: High to Low",
            Comparator.comparing((StockInfo info) -> info.stock().getSalesPrice()).reversed()),
        CHANGE_ASC("Change: Low to High", Comparator.comparing(StockInfo::changePercent)),
        CHANGE_DESC("Change: High to Low", Comparator.comparing(StockInfo::changePercent).reversed());

        private final String label;
        private final Comparator<StockInfo> comparator;

        SortOption(String label, Comparator<StockInfo> comparator) {
            this.label = label;
            this.comparator = comparator;
        }

        Comparator<StockInfo> comparator() {
            return comparator;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private record StockInfo(Stock stock, String price, String change, ChangeKind changeKind,
                             BigDecimal changePercent) {
    }

    private record StockChange(BigDecimal percent, ChangeKind kind) {
    }

    private final ExchangeController controller;
    private final Set<String> watchlistSymbols = new HashSet<>();
    private StockTab activeStockTab = StockTab.ALL;
    private RightTab activeRightTab = RightTab.PORTFOLIO;
    private BorderPane root;
    private Parent dashboardBody;
    private Parent profileBody;
    private boolean showingProfile;
    private Button profileToggleButton;
    private Label userLabel;
    private Label weekLabel;
    private Label cashValue;
    private Label netWorthValue;
    private Label statusBadge;
    private TextField searchField;
    private TextField minPriceField;
    private TextField maxPriceField;
    private ComboBox<SortOption> sortDropdown;
    private Popup filterPopup;
    private SortOption activeSort = SortOption.ALPHA_ASC;
    private VBox stockListContainer;
    private StackPane portfolioContent;
    private ScrollPane portfolioScroll;
    private VBox portfolioList;
    private VBox portfolioEmptyState;
    private ScrollPane historyScroll;
    private VBox historyList;
    private VBox historyEmptyState;
    private Label portfolioTab;
    private Label historyTab;
    private boolean portfolioEmpty;
    private boolean historyEmpty;
    private Label allStocksTab;
    private Label watchlistTab;
    private Label marketMoversTab;

    public DashboardPage() {
        this(null);
    }

    public DashboardPage(ExchangeController controller) {
        this.controller = controller;
    }

    public Parent createRoot() {
        root = new BorderPane();
        root.getStyleClass().add("dashboard-root");
        root.setTop(createHeader());
        dashboardBody = createBody();
        profileBody = createProfileBody();
        root.setCenter(dashboardBody);
        refreshHeader();
        return root;
    }

    private Parent createProfileBody() {
        return new ProfilePage(controller).createRoot();
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

        profileToggleButton = new Button(resolveProfileToggleLabel());
        profileToggleButton.getStyleClass().add("secondary-button");
        profileToggleButton.setOnAction(event -> toggleProfileView());

        Button nextWeek = new Button("Next Week");
        nextWeek.getStyleClass().add("primary-action");
        nextWeek.setContentDisplay(ContentDisplay.LEFT);
        nextWeek.setGraphicTextGap(6);
        nextWeek.setGraphic(DashboardIcons.createIcon(DashboardIcons.ARROW_RIGHT_PATH, "icon-stroke-inverse", 14));
        if (controller == null) {
            nextWeek.setDisable(true);
        } else {
            nextWeek.setOnAction(event -> {
                controller.advance();
                refreshHeader();
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

        HBox tabs = createStockTabBar();
        HBox search = createSearchField();
        ScrollPane stockList = createStockList();
        VBox.setVgrow(stockList, Priority.ALWAYS);

        panel.getChildren().addAll(tabs, search, stockList);
        return panel;
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

    private VBox createRightPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("panel-right");
        panel.setPadding(new Insets(14));

        HBox tabs = createRightTabBar();
        portfolioContent = new StackPane();
        portfolioContent.getStyleClass().add("portfolio-panel");

        portfolioScroll = createPortfolioList();
        portfolioEmptyState = createPortfolioEmptyState();
        historyScroll = createHistoryList();
        historyEmptyState = createHistoryEmptyState();

        portfolioContent.getChildren().addAll(portfolioScroll, portfolioEmptyState, historyScroll, historyEmptyState);
        StackPane.setAlignment(portfolioScroll, Pos.TOP_LEFT);
        StackPane.setAlignment(portfolioEmptyState, Pos.CENTER);
        StackPane.setAlignment(historyScroll, Pos.TOP_LEFT);
        StackPane.setAlignment(historyEmptyState, Pos.CENTER);
        VBox.setVgrow(portfolioContent, Priority.ALWAYS);
        refreshPortfolio();
        refreshHistory();
        updateRightPanelVisibility();
        updateRightTabs();

        panel.getChildren().addAll(tabs, portfolioContent);
        return panel;
    }

    private ScrollPane createPortfolioList() {
        portfolioList = new VBox(10);
        portfolioList.getStyleClass().add("portfolio-list");
        portfolioList.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(portfolioList);
        scrollPane.getStyleClass().add("portfolio-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    private VBox createPortfolioEmptyState() {
        VBox emptyState = new VBox(10);
        emptyState.getStyleClass().add("empty-state");
        emptyState.setAlignment(Pos.CENTER);

        SVGPath emptyIcon = DashboardIcons.createIcon(DashboardIcons.PIE_PATH, "icon-stroke-muted", 48);
        Label emptyTitle = new Label("No Holdings");
        emptyTitle.getStyleClass().add("empty-title");
        Label emptySubtitle = new Label("Your portfolio is empty. Start buying stocks to build\nyour wealth.");
        emptySubtitle.getStyleClass().add("empty-subtitle");
        emptySubtitle.setTextAlignment(TextAlignment.CENTER);

        emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptySubtitle);
        return emptyState;
    }

    private ScrollPane createHistoryList() {
        historyList = new VBox(10);
        historyList.getStyleClass().add("history-list");
        historyList.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(historyList);
        scrollPane.getStyleClass().add("history-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scrollPane;
    }

    private VBox createHistoryEmptyState() {
        VBox emptyState = new VBox(10);
        emptyState.getStyleClass().add("empty-state");
        emptyState.setAlignment(Pos.CENTER);

        SVGPath emptyIcon = DashboardIcons.createIcon(DashboardIcons.CLOCK_PATH, "icon-stroke-muted", 48);
        Label emptyTitle = new Label("No History");
        emptyTitle.getStyleClass().add("empty-title");
        Label emptySubtitle = new Label("Buy or sell stocks to see your activity here.");
        emptySubtitle.getStyleClass().add("empty-subtitle");
        emptySubtitle.setTextAlignment(TextAlignment.CENTER);

        emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptySubtitle);
        return emptyState;
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

    private HBox createRightTabBar() {
        HBox tabs = new HBox(6);
        tabs.getStyleClass().add("dashboard-tabs");
        tabs.setAlignment(Pos.CENTER_LEFT);

        portfolioTab = new Label("Portfolio");
        portfolioTab.getStyleClass().add("dashboard-tab");
        portfolioTab.setOnMouseClicked(event -> setActiveRightTab(RightTab.PORTFOLIO));

        historyTab = new Label(buildHistoryLabel());
        historyTab.getStyleClass().add("dashboard-tab");
        historyTab.setOnMouseClicked(event -> setActiveRightTab(RightTab.HISTORY));

        tabs.getChildren().addAll(portfolioTab, historyTab);
        updateRightTabs();
        return tabs;
    }

    private void setActiveRightTab(RightTab tab) {
        activeRightTab = tab;
        updateRightTabs();
        updateRightPanelVisibility();
    }

    private void updateRightTabs() {
        if (portfolioTab == null || historyTab == null) {
            return;
        }
        historyTab.setText(buildHistoryLabel());
        updateTabStyle(portfolioTab, activeRightTab == RightTab.PORTFOLIO);
        updateTabStyle(historyTab, activeRightTab == RightTab.HISTORY);
    }

    private String buildHistoryLabel() {
        int total = controller == null
            ? 0
            : controller.getPlayer().getTransactionArchive().getTransactions().size();
        return "History (" + total + ")";
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
        searchField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
            }
        });
        searchField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        search.setOnMouseClicked(event -> searchField.requestFocus());

        filterPopup = createFilterPopup();
        FilterButton filterButton = new FilterButton();
        filterButton.setOnAction(event -> toggleFilterPopup(filterButton));

        search.getChildren().addAll(icon, searchField, filterButton);
        return search;
    }

    private Popup createFilterPopup() {
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);

        VBox content = new VBox(10);
        content.getStyleClass().add("filter-popup");
        content.setPrefWidth(FILTER_POPUP_WIDTH);

        Label sortLabel = new Label("Sort by");
        sortLabel.getStyleClass().add("filter-label");

        sortDropdown = new ComboBox<>(FXCollections.observableArrayList(SortOption.values()));
        sortDropdown.getStyleClass().add("filter-dropdown");
        sortDropdown.setMaxWidth(Double.MAX_VALUE);
        sortDropdown.setValue(activeSort);
        sortDropdown.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                activeSort = newValue;
                refreshStockList();
            }
        });

        Label rangeLabel = new Label("Price range");
        rangeLabel.getStyleClass().add("filter-label");

        HBox rangeRow = new HBox(8);
        rangeRow.getStyleClass().add("filter-range");
        minPriceField = createNumberField("Min");
        maxPriceField = createNumberField("Max");
        HBox.setHgrow(minPriceField, Priority.ALWAYS);
        HBox.setHgrow(maxPriceField, Priority.ALWAYS);
        rangeRow.getChildren().addAll(minPriceField, maxPriceField);

        content.getChildren().addAll(sortLabel, sortDropdown, rangeLabel, rangeRow);
        popup.getContent().add(content);
        return popup;
    }

    private TextField createNumberField(String prompt) {
        TextField field = new TextField();
        field.getStyleClass().add("filter-input");
        field.setPromptText(prompt);
        field.setTextFormatter(createNumericFormatter());
        field.textProperty().addListener((obs, oldValue, newValue) -> refreshStockList());
        return field;
    }

    private TextFormatter<String> createNumericFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (text.isBlank()) {
                return change;
            }
            if (text.matches("\\d*(\\.\\d{0,2})?")) {
                return change;
            }
            return null;
        };
        return new TextFormatter<>(filter);
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
        double x = bounds.getMaxX() - FILTER_POPUP_WIDTH;
        double y = bounds.getMaxY() + 6;
        filterPopup.show(anchor, x, y);
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

    private VBox createPortfolioCard(Share share) {
        VBox card = new VBox(10);
        card.getStyleClass().add("portfolio-card");

        Stock stock = share.getStock();
        BigDecimal quantity = share.getQuantity();
        BigDecimal totalValue = stock.getSalesPrice().multiply(quantity);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(2);
        Label symbol = new Label(stock.getSymbol());
        symbol.getStyleClass().add("portfolio-symbol");
        Label company = new Label(stock.getCompanyName());
        company.getStyleClass().add("portfolio-name");
        left.getChildren().addAll(symbol, company);
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = new VBox(2);
        right.setAlignment(Pos.CENTER_RIGHT);
        Label value = new Label(DashboardFormatters.formatPrice(totalValue));
        value.getStyleClass().add("portfolio-value");
        Label meta = new Label(DashboardFormatters.formatQuantity(quantity)
            + " shares @ " + DashboardFormatters.formatPrice(stock.getSalesPrice()));
        meta.getStyleClass().add("portfolio-meta");
        right.getChildren().addAll(value, meta);

        header.getChildren().addAll(left, right);
        card.getChildren().add(header);
        return card;
    }

    private VBox createHistoryCard(Transaction transaction) {
        VBox card = new VBox(10);
        card.getStyleClass().add("history-card");

        Stock stock = transaction.getShare().getStock();
        BigDecimal quantity = transaction.getShare().getQuantity();
        boolean isPurchase = transaction instanceof Purchase;
        String typeText = isPurchase ? "Purchase" : "Sale";

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(4);
        left.setAlignment(Pos.CENTER_LEFT);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label type = new Label(typeText);
        type.getStyleClass().add("history-type");
        type.getStyleClass().add(isPurchase ? "history-type-buy" : "history-type-sell");
        Label symbol = new Label(stock.getSymbol());
        symbol.getStyleClass().add("history-symbol");
        titleRow.getChildren().addAll(type, symbol);

        Label company = new Label(stock.getCompanyName());
        company.getStyleClass().add("history-company");
        left.getChildren().addAll(titleRow, company);
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = new VBox(2);
        right.setAlignment(Pos.CENTER_RIGHT);
        Label value = new Label(DashboardFormatters.formatPrice(transaction.getCalculator().getTotal()));
        value.getStyleClass().add("history-value");
        Label meta = new Label(buildHistoryMeta(transaction, quantity));
        meta.getStyleClass().add("history-meta");
        right.getChildren().addAll(value, meta);

        header.getChildren().addAll(left, right);
        card.getChildren().add(header);
        return card;
    }

    private String buildHistoryMeta(Transaction transaction, BigDecimal quantity) {
        BigDecimal price = resolveTransactionPrice(transaction, quantity);
        return "Week " + transaction.getWeek()
            + " | " + DashboardFormatters.formatQuantity(quantity)
            + " shares @ " + DashboardFormatters.formatPrice(price);
    }

    private BigDecimal resolveTransactionPrice(Transaction transaction, BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (transaction instanceof Purchase) {
            return transaction.getShare().getPurchasePrice();
        }
        BigDecimal gross = transaction.getCalculator().getGross();
        return gross.divide(quantity, 2, RoundingMode.HALF_UP);
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
            DashboardFormatters.formatSignedPercent(change.percent()),
            change.kind(),
            change.percent()
        );
    }

    private StockChange calculateChange(Stock stock) {
        BigDecimal percent = stock.getLatestPriceChangePercent();
        ChangeKind kind = resolveChangeKind(percent);
        return new StockChange(percent, kind);
    }

    private ChangeKind resolveChangeKind(BigDecimal percent) {
        int sign = percent.signum();
        if (sign > 0) {
            return ChangeKind.POSITIVE;
        }
        if (sign < 0) {
            return ChangeKind.NEGATIVE;
        }
        return ChangeKind.NEUTRAL;
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

    private void refreshHeader() {
        if (controller == null || userLabel == null) {
            return;
        }
        Player player = controller.getPlayer();
        Exchange exchange = controller.getExchange();

        userLabel.setText(player.getName());
        weekLabel.setText("Week " + exchange.getWeek());
        cashValue.setText(DashboardFormatters.formatMoney(player.getMoney()));
        netWorthValue.setText(DashboardFormatters.formatMoney(player.getNetWorth()));
        statusBadge.setText(formatStatus(player.getStatus()));
        refreshPortfolio();
        refreshHistory();
        updateRightTabs();
        refreshStockList();
    }

    private void toggleProfileView() {
        if (root == null || dashboardBody == null) {
            return;
        }
        showingProfile = !showingProfile;
        if (showingProfile) {
            profileBody = createProfileBody();
        }
        root.setCenter(showingProfile ? profileBody : dashboardBody);
        updateProfileToggleLabel();
    }

    private String resolveProfileToggleLabel() {
        return showingProfile ? "Home" : "Profile";
    }

    private void updateProfileToggleLabel() {
        if (profileToggleButton == null) {
            return;
        }
        profileToggleButton.setText(resolveProfileToggleLabel());
    }


    private void refreshPortfolio() {
        if (portfolioList == null || portfolioEmptyState == null || portfolioScroll == null) {
            return;
        }

        List<Share> shares = controller == null
            ? List.of()
            : controller.getPlayer().getPortfolio().getShares();

        portfolioList.getChildren().clear();
        for (Share share : shares) {
            portfolioList.getChildren().add(createPortfolioCard(share));
        }

        portfolioEmpty = shares.isEmpty();
        updateRightPanelVisibility();
    }

    private void refreshHistory() {
        if (historyList == null || historyEmptyState == null || historyScroll == null) {
            return;
        }

        List<Transaction> transactions = controller == null
            ? List.of()
            : controller.getPlayer().getTransactionArchive().getTransactions();

        historyList.getChildren().clear();
        transactions.stream()
            .sorted(Comparator.comparingInt(Transaction::getWeek).reversed())
            .forEach(transaction -> historyList.getChildren().add(createHistoryCard(transaction)));

        historyEmpty = transactions.isEmpty();
        updateRightPanelVisibility();
    }

    private void updateRightPanelVisibility() {
        updatePanelVisibility(portfolioScroll, portfolioEmptyState, activeRightTab == RightTab.PORTFOLIO,
            portfolioEmpty);
        updatePanelVisibility(historyScroll, historyEmptyState, activeRightTab == RightTab.HISTORY, historyEmpty);
    }

    private void updatePanelVisibility(ScrollPane scroll, VBox emptyState, boolean active, boolean empty) {
        if (scroll == null || emptyState == null) {
            return;
        }
        boolean showScroll = active && !empty;
        boolean showEmpty = active && empty;
        scroll.setVisible(showScroll);
        scroll.setManaged(showScroll);
        emptyState.setVisible(showEmpty);
        emptyState.setManaged(showEmpty);
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
        stocks = applyPriceFilter(stocks);
        stocks = applySort(stocks);
        if (stocks.isEmpty()) {
            stockListContainer.getChildren().add(createEmptyStockCard());
            return;
        }

        for (StockInfo stock : stocks) {
            stockListContainer.getChildren().add(createStockCard(stock));
        }
    }

    private List<StockInfo> applyPriceFilter(List<StockInfo> stocks) {
        PriceRange range = resolvePriceRange();
        if (range.min() == null && range.max() == null) {
            return stocks;
        }

        return stocks.stream()
            .filter(info -> withinRange(info.stock().getSalesPrice(), range.min(), range.max()))
            .toList();
    }

    private List<StockInfo> applySort(List<StockInfo> stocks) {
        if (activeSort == null) {
            return stocks;
        }
        return stocks.stream()
            .sorted(activeSort.comparator())
            .toList();
    }

    private boolean withinRange(BigDecimal price, BigDecimal min, BigDecimal max) {
        if (price == null) {
            return false;
        }
        if (min != null && price.compareTo(min) < 0) {
            return false;
        }
        if (max != null && price.compareTo(max) > 0) {
            return false;
        }
        return true;
    }

    private PriceRange resolvePriceRange() {
        BigDecimal min = parsePriceField(minPriceField);
        BigDecimal max = parsePriceField(maxPriceField);
        if (min != null && max != null && min.compareTo(max) > 0) {
            BigDecimal temp = min;
            min = max;
            max = temp;
        }
        return new PriceRange(min, max);
    }

    private BigDecimal parsePriceField(TextField field) {
        if (field == null) {
            return null;
        }
        String text = field.getText();
        if (text == null || text.isBlank() || text.equals(".")) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void openBuyDialog(Button source, Stock stock) {
        Window owner = resolveOwnerWindow(source);
        BuyStockDialog dialog = new BuyStockDialog(controller, stock, this::refreshHeader);
        dialog.show(owner);
    }

    private void openSellDialog(Button source, Stock stock) {
        Window owner = resolveOwnerWindow(source);
        SellStockDialog dialog = new SellStockDialog(controller, stock, this::refreshHeader);
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

    private record PriceRange(BigDecimal min, BigDecimal max) {
    }

    private Window resolveOwnerWindow(Button source) {
        if (source.getScene() == null) {
            return null;
        }
        return source.getScene().getWindow();
    }
}
