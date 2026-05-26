package edu.ntnu.idi.idatt2003.millions.view.dashboard;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.model.Purchase;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.Transaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;

/**
 * Right-side dashboard panel showing the portfolio and transaction history.
 */
final class DashboardRightPanel {

    private enum Tab { PORTFOLIO, HISTORY }

    private final ExchangeController controller;
    private Tab activeTab = Tab.PORTFOLIO;

    private VBox portfolioList;
    private VBox portfolioEmptyState;
    private ScrollPane portfolioScroll;
    private VBox historyList;
    private VBox historyEmptyState;
    private ScrollPane historyScroll;
    private Label portfolioTabLabel;
    private Label historyTabLabel;
    private boolean portfolioEmpty;
    private boolean historyEmpty;

    DashboardRightPanel(ExchangeController controller) {
        this.controller = controller;
    }

    /**
     * Builds and returns the right panel node.
     *
     * @return the panel root
     */
    VBox createPanel() {
        portfolioList = createList("portfolio-list");
        portfolioEmptyState = createEmptyState(DashboardIcons.PIE_PATH,
                "No Holdings", "Your portfolio is empty. Start buying stocks to build\nyour wealth.");
        portfolioScroll = wrapInScroll(portfolioList, "portfolio-scroll");

        historyList = createList("history-list");
        historyEmptyState = createEmptyState(DashboardIcons.CLOCK_PATH,
                "No History", "Buy or sell stocks to see your activity here.");
        historyScroll = wrapInScroll(historyList, "history-scroll");

        StackPane content = new StackPane();
        content.getStyleClass().add("portfolio-panel");
        content.getChildren().addAll(portfolioScroll, portfolioEmptyState, historyScroll, historyEmptyState);
        StackPane.setAlignment(portfolioScroll, Pos.TOP_LEFT);
        StackPane.setAlignment(portfolioEmptyState, Pos.CENTER);
        StackPane.setAlignment(historyScroll, Pos.TOP_LEFT);
        StackPane.setAlignment(historyEmptyState, Pos.CENTER);
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox panel = new VBox(12);
        panel.getStyleClass().add("panel-right");
        panel.setPadding(new Insets(14));
        panel.getChildren().addAll(buildTabBar(), content);

        refresh();
        return panel;
    }

    /**
     * Refreshes portfolio and history contents from the controller.
     */
    void refresh() {
        refreshPortfolio();
        refreshHistory();
        updateTabLabels();
        updateVisibility();
    }

    private HBox buildTabBar() {
        portfolioTabLabel = tabLabel("Portfolio", Tab.PORTFOLIO);
        historyTabLabel = tabLabel(historyLabel(), Tab.HISTORY);
        setTabActive(portfolioTabLabel, true);

        HBox tabs = new HBox(6);
        tabs.getStyleClass().add("dashboard-tabs");
        tabs.setAlignment(Pos.CENTER_LEFT);
        tabs.getChildren().addAll(portfolioTabLabel, historyTabLabel);
        return tabs;
    }

    private Label tabLabel(String text, Tab tab) {
        Label label = new Label(text);
        label.getStyleClass().add("dashboard-tab");
        label.setOnMouseClicked(e -> selectTab(tab));
        return label;
    }

    private void selectTab(Tab tab) {
        activeTab = tab;
        updateTabLabels();
        updateVisibility();
    }

    private void updateTabLabels() {
        if (portfolioTabLabel == null || historyTabLabel == null) {
            return;
        }
        historyTabLabel.setText(historyLabel());
        setTabActive(portfolioTabLabel, activeTab == Tab.PORTFOLIO);
        setTabActive(historyTabLabel, activeTab == Tab.HISTORY);
    }

    private String historyLabel() {
        int count = controller == null ? 0
                : controller.getPlayer().getTransactionArchive().getTransactions().size();
        return "History (" + count + ")";
    }

    private void refreshPortfolio() {
        if (portfolioList == null) {
            return;
        }
        List<Share> shares = controller == null ? List.of()
                : controller.getPlayer().getPortfolio().getShares();
        portfolioList.getChildren().clear();
        shares.forEach(s -> portfolioList.getChildren().add(buildPortfolioCard(s)));
        portfolioEmpty = shares.isEmpty();
    }

    private void refreshHistory() {
        if (historyList == null) {
            return;
        }
        List<Transaction> txns = controller == null ? List.of()
                : controller.getPlayer().getTransactionArchive().getTransactions();
        historyList.getChildren().clear();
        txns.stream()
                .sorted(Comparator.comparingInt(Transaction::getWeek).reversed())
                .forEach(t -> historyList.getChildren().add(buildHistoryCard(t)));
        historyEmpty = txns.isEmpty();
    }

    private void updateVisibility() {
        setPanelVisible(portfolioScroll, portfolioEmptyState, activeTab == Tab.PORTFOLIO, portfolioEmpty);
        setPanelVisible(historyScroll, historyEmptyState, activeTab == Tab.HISTORY, historyEmpty);
    }

    private VBox buildPortfolioCard(Share share) {
        Stock stock = share.getStock();
        BigDecimal qty = share.getQuantity();
        BigDecimal total = stock.getSalesPrice().multiply(qty);

        VBox left = new VBox(2);
        left.getChildren().addAll(
                label(stock.getSymbol(), "portfolio-symbol"),
                label(stock.getCompanyName(), "portfolio-name"));
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = new VBox(2);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.getChildren().addAll(
                label(DashboardFormatters.formatPrice(total), "portfolio-value"),
                label(DashboardFormatters.formatQuantity(qty) + " shares @ "
                        + DashboardFormatters.formatPrice(stock.getSalesPrice()), "portfolio-meta"));

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(left, right);

        VBox card = new VBox(10);
        card.getStyleClass().add("portfolio-card");
        card.getChildren().add(header);
        return card;
    }

    private VBox buildHistoryCard(Transaction transaction) {
        Stock stock = transaction.getShare().getStock();
        BigDecimal qty = transaction.getShare().getQuantity();
        boolean isPurchase = transaction instanceof Purchase;

        Label typeLabel = label(isPurchase ? "Purchase" : "Sale", "history-type");
        typeLabel.getStyleClass().add(isPurchase ? "history-type-buy" : "history-type-sell");

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(typeLabel, label(stock.getSymbol(), "history-symbol"));

        VBox left = new VBox(4);
        left.setAlignment(Pos.CENTER_LEFT);
        left.getChildren().addAll(titleRow, label(stock.getCompanyName(), "history-company"));
        HBox.setHgrow(left, Priority.ALWAYS);

        BigDecimal unitPrice = resolveUnitPrice(transaction, qty);
        VBox right = new VBox(2);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.getChildren().addAll(
                label(DashboardFormatters.formatPrice(transaction.getCalculator().getTotal()), "history-value"),
                label("Week " + transaction.getWeek() + " | "
                        + DashboardFormatters.formatQuantity(qty) + " shares @ "
                        + DashboardFormatters.formatPrice(unitPrice), "history-meta"));

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(left, right);

        VBox card = new VBox(10);
        card.getStyleClass().add("history-card");
        card.getChildren().add(header);
        return card;
    }

    private static BigDecimal resolveUnitPrice(Transaction transaction, BigDecimal qty) {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (transaction instanceof Purchase) {
            return transaction.getShare().getPurchasePrice();
        }
        return transaction.getCalculator().getGross().divide(qty, 2, RoundingMode.HALF_UP);
    }

    private static VBox createEmptyState(String iconPath, String title, String subtitle) {
        VBox box = new VBox(10);
        box.getStyleClass().add("empty-state");
        box.setAlignment(Pos.CENTER);
        SVGPath icon = DashboardIcons.createIcon(iconPath, "icon-stroke-muted", 48);
        Label sub = label(subtitle, "empty-subtitle");
        sub.setTextAlignment(TextAlignment.CENTER);
        box.getChildren().addAll(icon, label(title, "empty-title"), sub);
        return box;
    }

    private static VBox createList(String styleClass) {
        VBox list = new VBox(10);
        list.getStyleClass().add(styleClass);
        list.setFillWidth(true);
        return list;
    }

    private static ScrollPane wrapInScroll(VBox list, String styleClass) {
        ScrollPane scroll = new ScrollPane(list);
        scroll.getStyleClass().add(styleClass);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scroll;
    }

    private static void setPanelVisible(ScrollPane scroll, VBox empty, boolean active, boolean isEmpty) {
        if (scroll == null || empty == null) {
            return;
        }
        scroll.setVisible(active && !isEmpty);
        scroll.setManaged(active && !isEmpty);
        empty.setVisible(active && isEmpty);
        empty.setManaged(active && isEmpty);
    }

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
