package edu.ntnu.idi.idatt2003.millions.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

final class DashboardFormatters {

    private DashboardFormatters() {
    }

    static String formatMoney(BigDecimal amount) {
        return formatPrice(amount);
    }

    static String formatPrice(BigDecimal price) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return "$" + format.format(price);
    }

    static String formatQuantity(BigDecimal quantity) {
        DecimalFormat format = new DecimalFormat("#,##0.####", DecimalFormatSymbols.getInstance(Locale.US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(quantity);
    }

    static String formatSignedPercent(BigDecimal percent) {
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
