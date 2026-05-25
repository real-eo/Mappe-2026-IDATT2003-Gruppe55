package edu.ntnu.idi.idatt2003.millions.view;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import java.math.BigDecimal;
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

final class DashboardPortfolioPanel {

    private final ExchangeController controller;

    private VBox portfolioList;
    private VBox portfolioEmptyState;
    private ScrollPane portfolioScroll;

    DashboardPortfolioPanel(ExchangeController controller) {
        this.controller = controller;
    }

    VBox createPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("panel-right");
        panel.setPadding(new Insets(14));

        HBox tabs = createTabBar(List.of("Portfolio", "History (0)"), 0);
        StackPane portfolioContent = new StackPane();
        portfolioContent.getStyleClass().add("portfolio-panel");

        portfolioScroll = createPortfolioList();
        portfolioEmptyState = createPortfolioEmptyState();

        portfolioContent.getChildren().addAll(portfolioScroll, portfolioEmptyState);
        StackPane.setAlignment(portfolioScroll, Pos.TOP_LEFT);
        StackPane.setAlignment(portfolioEmptyState, Pos.CENTER);
        VBox.setVgrow(portfolioContent, Priority.ALWAYS);
        refresh();

        panel.getChildren().addAll(tabs, portfolioContent);
        return panel;
    }

    void refresh() {
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
        Label meta = new Label(DashboardFormatters.formatQuantity(quantity) + " shares @ "
            + DashboardFormatters.formatPrice(stock.getSalesPrice()));
        meta.getStyleClass().add("portfolio-meta");
        right.getChildren().addAll(value, meta);

        header.getChildren().addAll(left, right);
        card.getChildren().add(header);
        return card;
    }
}
