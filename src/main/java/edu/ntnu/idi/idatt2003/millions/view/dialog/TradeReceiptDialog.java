package edu.ntnu.idi.idatt2003.millions.view.dialog;

import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.calculator.TransactionCalculator;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Dialog showing a completed trade receipt with fee and tax breakdown.
 */
public class TradeReceiptDialog extends MillionsDialog {

    private final boolean isBuy;
    private final Stock stock;
    private final BigDecimal quantity;
    private final BigDecimal pricePerShare;
    private final TransactionCalculator calculator;

    /**
     * Creates a trade receipt dialog.
     *
     * @param isBuy true when representing a buy transaction, false for a sale
     * @param stock stock involved in the trade
     * @param quantity traded share quantity
     * @param pricePerShare execution price per share
     * @param calculator calculator containing computed totals, fees, and tax
     */
    public TradeReceiptDialog(boolean isBuy, Stock stock, BigDecimal quantity,
        BigDecimal pricePerShare, TransactionCalculator calculator) {
        this.isBuy = isBuy;
        this.stock = stock;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.calculator = calculator;
    }

    @Override
    protected Node buildContent() {
        VBox content = new VBox(12);
        content.setPadding(new Insets(18));
        content.setFillWidth(true);

        VBox header = new VBox(4);
        Label title = new Label(isBuy ? "Purchase Complete" : "Sale Complete");
        title.getStyleClass().addAll("trade-title", "receipt-success-title");
        Label subtitle = new Label(stock.getCompanyName() + "  ·  " + stock.getSymbol());
        subtitle.getStyleClass().add("trade-subtitle");
        header.getChildren().addAll(title, subtitle);

        VBox breakdown = new VBox(8);
        breakdown.getStyleClass().add("trade-price-panel");

        breakdown.getChildren().addAll(
            receiptRow(isBuy ? "Shares Purchased" : "Shares Sold", formatQuantity(quantity)),
            receiptRow("Price per Share", formatPrice(pricePerShare))
        );

        Separator sep1 = new Separator();
        sep1.getStyleClass().add("receipt-separator");
        breakdown.getChildren().add(sep1);
        VBox.setMargin(sep1, new Insets(2, 0, 2, 0));

        breakdown.getChildren().add(receiptRow("Gross Value", formatPrice(calculator.getGross())));
        breakdown.getChildren().add(receiptRow(
            isBuy ? "Commission (0.5%)" : "Commission (1%)",
            (isBuy ? "+" : "-") + formatPrice(calculator.getCommission())
        ));
        if (!isBuy && calculator.getTax().compareTo(BigDecimal.ZERO) > 0) {
            breakdown.getChildren().add(
                receiptRow("Capital Gains Tax (30%)", "-" + formatPrice(calculator.getTax()))
            );
        }

        Separator sep2 = new Separator();
        sep2.getStyleClass().add("receipt-separator");
        breakdown.getChildren().add(sep2);
        VBox.setMargin(sep2, new Insets(2, 0, 2, 0));

        HBox totalRow = new HBox(10);
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label totalLabel = new Label(isBuy ? "Total Paid" : "Total Received");
        totalLabel.getStyleClass().add("trade-field-label");
        Label totalValue = new Label(formatPrice(calculator.getTotal()));
        totalValue.getStyleClass().addAll("trade-price-value", "receipt-total-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        totalRow.getChildren().addAll(totalLabel, spacer, totalValue);
        breakdown.getChildren().add(totalRow);

        HBox actions = new HBox();
        actions.getStyleClass().add("trade-actions");
        Button done = new Button("Done");
        done.getStyleClass().add("primary-button");
        done.setDefaultButton(true);
        done.setOnAction(e -> close());
        actions.getChildren().add(done);

        content.getChildren().addAll(header, breakdown, actions);
        return content;
    }

    private HBox receiptRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        l.getStyleClass().add("trade-price-label");
        Label v = new Label(value);
        v.getStyleClass().add("trade-price-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(l, spacer, v);
        return row;
    }

    private static String formatPrice(BigDecimal price) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "$" + format.format(price);
    }

    private static String formatQuantity(BigDecimal qty) {
        DecimalFormat format = new DecimalFormat("#,##0.####", DecimalFormatSymbols.getInstance(Locale.US));
        return format.format(qty);
    }
}
