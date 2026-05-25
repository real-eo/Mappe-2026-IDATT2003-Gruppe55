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
}
