package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.infrastructure.io.StockCsvLoader;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.PlayerStatus;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
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
import javafx.stage.Window;

/**
 * Static dashboard layout based on the Figma desktop design.
 */
public class DashboardPage {

    private static final double ICON_BASE_SIZE = 24.0;
    private static final BigDecimal DEFAULT_DASHBOARD_AMOUNT = new BigDecimal("10000.00");

    private static final String LOGO_PATH = "M3 16 L9 10 L13 14 L20 7 L21 8 L13 16 L9 12 L4 17 Z";
    private static final String CLOCK_PATH = "M12 4 A8 8 0 1 0 12 20 A8 8 0 1 0 12 4 M12 8 V12 L15 14";
    private static final String SEARCH_PATH = "M11 4 A7 7 0 1 0 11 18 A7 7 0 1 0 11 4 M16 16 L20 20";
    private static final String STAR_PATH = "M12 3 L14.8 8.6 L21 9.2 L16 13.2 L17.4 19.2 L12 16 L6.6 19.2 L8 13.2 L3 9.2 L9.2 8.6 Z";
    private static final String ARROW_UP_PATH = "M12 5 L18 11 H14 V19 H10 V11 H6 Z";
    private static final String ARROW_DOWN_PATH = "M12 19 L6 13 H10 V5 H14 V13 H18 Z";
    private static final String ARROW_RIGHT_PATH = "M5 12 H17 M13 8 L17 12 L13 16";
    private static final String NEUTRAL_PATH = "M5 12 H19";
    private static final String PIE_PATH = "M12 3 A9 9 0 1 0 12 21 A9 9 0 1 0 12 3 M12 3 V12 H21 A9 9 0 0 0 12 3";
    private static final String STOCK_RESOURCE = "data/sp500.csv";
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

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
    private Label userLabel;
    private Label weekLabel;
    private Label cashValue;
    private Label netWorthValue;
    private Label statusBadge;
    private VBox stockListContainer;
    private StackPane portfolioContent;
    private ScrollPane portfolioScroll;
    private VBox portfolioList;
    private VBox portfolioEmptyState;

    public DashboardPage() {
        this(null);
    }

    public DashboardPage(ExchangeController controller) {
        this.controller = controller;
    }

    public Parent createRoot() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("dashboard-root");
        root.setTop(createHeader());
        root.setCenter(createBody());
        refreshHeader();
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

        userLabel = new Label(resolvePlayerName());
        userLabel.getStyleClass().add("brand-subtitle");
        brand.getChildren().addAll(brandRow, userLabel);

        Region divider = new Region();
        divider.getStyleClass().add("header-divider");

        HBox weekInfo = new HBox(4);
        weekInfo.setAlignment(Pos.CENTER_LEFT);
        SVGPath clock = createIcon(CLOCK_PATH, "icon-stroke-muted", 14);
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

        cashValue = new Label(formatMoney(resolveCash()));
        VBox cash = createStatBlock("Cash", cashValue, false);

        netWorthValue = new Label(formatMoney(resolveNetWorth()));
        VBox netWorth = createStatBlock("Net Worth", netWorthValue, true);

        VBox status = new VBox(2);
        status.setAlignment(Pos.CENTER_RIGHT);
        Label statusLabel = new Label("Status");
        statusLabel.getStyleClass().add("stat-label");
        statusBadge = new Label(resolveStatusText());
        statusBadge.getStyleClass().add("status-badge");
        status.getChildren().addAll(statusLabel, statusBadge);

        Button nextWeek = new Button("Next Week");
        nextWeek.getStyleClass().add("primary-action");
        nextWeek.setContentDisplay(ContentDisplay.LEFT);
        nextWeek.setGraphicTextGap(6);
        nextWeek.setGraphic(createIcon(ARROW_RIGHT_PATH, "icon-stroke-inverse", 14));
        if (controller == null) {
            nextWeek.setDisable(true);
        } else {
            nextWeek.setOnAction(event -> {
                controller.advance();
                refreshHeader();
            });
        }

        right.getChildren().addAll(cash, netWorth, status, nextWeek);
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
        portfolioContent = new StackPane();
        portfolioContent.getStyleClass().add("portfolio-panel");

        portfolioScroll = createPortfolioList();
        portfolioEmptyState = createPortfolioEmptyState();

        portfolioContent.getChildren().addAll(portfolioScroll, portfolioEmptyState);
        StackPane.setAlignment(portfolioScroll, Pos.TOP_LEFT);
        StackPane.setAlignment(portfolioEmptyState, Pos.CENTER);
        VBox.setVgrow(portfolioContent, Priority.ALWAYS);
        refreshPortfolio();

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

        SVGPath emptyIcon = createIcon(PIE_PATH, "icon-stroke-muted", 48);
        Label emptyTitle = new Label("No Holdings");
        emptyTitle.getStyleClass().add("empty-title");
        Label emptySubtitle = new Label("Your portfolio is empty. Start buying stocks to build\nyour wealth.");
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
        SVGPath star = createIcon(STAR_PATH, "icon-outline", 14);

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
                iconPath = ARROW_UP_PATH;
            }
            case NEGATIVE -> {
                changeClass = "change-negative";
                iconClass = "icon-negative";
                iconPath = ARROW_DOWN_PATH;
            }
            default -> {
                changeClass = "change-neutral";
                iconClass = "icon-neutral";
                iconPath = NEUTRAL_PATH;
            }
        }
        SVGPath changeIcon = createIcon(iconPath, iconClass, 10);
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
        Label value = new Label(formatPrice(totalValue));
        value.getStyleClass().add("portfolio-value");
        Label meta = new Label(formatQuantity(quantity) + " shares @ " + formatPrice(stock.getSalesPrice()));
        meta.getStyleClass().add("portfolio-meta");
        right.getChildren().addAll(value, meta);

        header.getChildren().addAll(left, right);
        card.getChildren().add(header);
        return card;
    }

    private VBox createEmptyStockCard() {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("stock-card", "stock-card-empty");
        card.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("No stocks loaded");
        title.getStyleClass().add("stock-empty-title");
        Label subtitle = new Label("Check that data/sp500.csv is available.");
        subtitle.getStyleClass().add("stock-empty-subtitle");

        card.getChildren().addAll(title, subtitle);
        return card;
    }

    private List<StockInfo> loadStockInfos() {
        if (controller != null) {
            List<Stock> stocks = controller.findStocks("");
            return stocks.stream()
                .map(this::toStockInfo)
                .toList();
        }

        StockCsvLoader loader = new StockCsvLoader();
        try {
            List<Stock> stocks = loader.loadFromResource(STOCK_RESOURCE);
            return stocks.stream()
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
            formatPrice(stock.getSalesPrice()),
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

    private String formatPrice(BigDecimal price) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return "$" + format.format(price);
    }

    private String formatQuantity(BigDecimal quantity) {
        DecimalFormat format = new DecimalFormat("#,##0.####", DecimalFormatSymbols.getInstance(Locale.US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(quantity);
    }

    private String formatMoney(BigDecimal price) {
        return formatPrice(price);
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

    private SVGPath createIcon(String path, String styleClass, double size) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.getStyleClass().add(styleClass);
        double scale = size / ICON_BASE_SIZE;
        icon.setScaleX(scale);
        icon.setScaleY(scale);
        return icon;
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
        cashValue.setText(formatMoney(player.getMoney()));
        netWorthValue.setText(formatMoney(player.getNetWorth()));
        statusBadge.setText(formatStatus(player.getStatus()));
        refreshPortfolio();
        refreshStockList();
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

        boolean isEmpty = shares.isEmpty();
        portfolioEmptyState.setVisible(isEmpty);
        portfolioEmptyState.setManaged(isEmpty);
        portfolioScroll.setVisible(!isEmpty);
        portfolioScroll.setManaged(!isEmpty);
    }

    private void refreshStockList() {
        if (stockListContainer == null) {
            return;
        }

        stockListContainer.getChildren().clear();
        List<StockInfo> stocks = loadStockInfos();
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
        BuyStockDialog dialog = new BuyStockDialog(controller, stock, this::refreshHeader);
        dialog.show(owner);
    }

    private void openSellDialog(Button source, Stock stock) {
        Window owner = resolveOwnerWindow(source);
        SellStockDialog dialog = new SellStockDialog(controller, stock, this::refreshHeader);
        dialog.show(owner);
    }

    private Window resolveOwnerWindow(Button source) {
        if (source.getScene() == null) {
            return null;
        }
        return source.getScene().getWindow();
    }
}
