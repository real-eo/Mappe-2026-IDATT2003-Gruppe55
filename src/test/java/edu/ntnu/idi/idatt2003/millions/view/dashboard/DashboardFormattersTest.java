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
}
