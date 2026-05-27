package edu.ntnu.idi.idatt2003.millions.view.dashboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility formatters used by dashboard UI components.
 */
public final class DashboardFormatters {

    private DashboardFormatters() {
    }

    /**
     * Formats a money value using the dashboard currency style.
     *
     * @param amount the monetary amount to format
     * @return formatted money string
     */
    public static String formatMoney(BigDecimal amount) {
        return formatPrice(amount);
    }

    /**
     * Formats a share price with two decimal places and a dollar prefix.
     *
     * @param price the price to format
     * @return formatted price string
     */
    public static String formatPrice(BigDecimal price) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return "$" + format.format(price);
    }

    /**
     * Formats a quantity with up to four decimal places.
     *
     * @param quantity the quantity to format
     * @return formatted quantity string
     */
    public static String formatQuantity(BigDecimal quantity) {
        DecimalFormat format = new DecimalFormat("#,##0.####", DecimalFormatSymbols.getInstance(Locale.US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(quantity);
    }

    /**
     * Formats a percent value with sign and two decimal places.
     *
     * @param percent the percent value to format
     * @return signed formatted percent string
     */
    public static String formatSignedPercent(BigDecimal percent) {
        DecimalFormat format = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        BigDecimal value = percent.abs().setScale(2, RoundingMode.HALF_UP);
        String formatted = format.format(value) + "%";
        if (percent.signum() > 0) {
            return "+" + formatted;
        }
        if (percent.signum() < 0) {
            return "-" + formatted;
        }
        return formatted;
    }
}
