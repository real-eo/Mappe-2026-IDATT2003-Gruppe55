package edu.ntnu.idi.idatt2003.millions.view.dashboard;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardFormattersTest {

    @Test
    void formatPrice_formatsCurrencyWithDollar() {
        assertEquals("$1,234.56", DashboardFormatters.formatPrice(new BigDecimal("1234.56")));
    }

    @Test
    void formatQuantity_formatsDecimals() {
        assertEquals("1,234.5678", DashboardFormatters.formatQuantity(new BigDecimal("1234.5678")));
    }

    @Test
    void formatSignedPercent_formatsPositiveNegativeAndZero() {
        assertEquals("+10.00%", DashboardFormatters.formatSignedPercent(new BigDecimal("10")));
        assertEquals("-5.50%", DashboardFormatters.formatSignedPercent(new BigDecimal("-5.5")));
        assertEquals("0.00%", DashboardFormatters.formatSignedPercent(BigDecimal.ZERO));
    }

    @Test
    void formatMoney_delegatesToFormatPrice() {
        assertEquals(DashboardFormatters.formatPrice(new BigDecimal("500.00")),
                DashboardFormatters.formatMoney(new BigDecimal("500.00")));
    }

    @Test
    void formatPrice_handlesVeryLargeNumbers() {
        assertEquals("$9,999,999,999.99",
                DashboardFormatters.formatPrice(new BigDecimal("9999999999.99")));
    }

    @Test
    void formatQuantity_handlesVerySmallDecimals() {
        assertEquals("0.0001", DashboardFormatters.formatQuantity(new BigDecimal("0.0001")));
    }

    @Test
    void formatPrice_withZero_returnsFormattedZero() {
        assertEquals("$0.00", DashboardFormatters.formatPrice(BigDecimal.ZERO));
    }

    @Test
    void formatQuantity_withIntegerValue_omitsDecimalPart() {
        assertEquals("100", DashboardFormatters.formatQuantity(new BigDecimal("100")));
    }

    @Test
    void formatQuantity_withMoreThanFourDecimals_roundsToFourPlaces() {
        assertEquals("1.1235", DashboardFormatters.formatQuantity(new BigDecimal("1.12345")));
    }
}
