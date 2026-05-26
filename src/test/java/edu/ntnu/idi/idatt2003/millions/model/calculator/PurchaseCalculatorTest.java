package edu.ntnu.idi.idatt2003.millions.model.calculator;

import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.calculator.PurchaseCalculator;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link PurchaseCalculator}.
 */
class PurchaseCalculatorTest {

    @Test
    void gross_isPrice_times_quantity() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
        Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
        PurchaseCalculator calc = new PurchaseCalculator(share);
        assertEquals(new BigDecimal("1000.00"), calc.getGross());
    }

    @Test
    void commission_isHalfPercent_ofGross() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
        Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
        PurchaseCalculator calc = new PurchaseCalculator(share);
        // 0.5% of 1000 = 5.00
        assertEquals(new BigDecimal("5.00"), calc.getCommission());
    }

    @Test
    void tax_isAlwaysZero() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("200.00"));
        Share share = new Share(stock, new BigDecimal("5"), new BigDecimal("200.00"));
        PurchaseCalculator calc = new PurchaseCalculator(share);
        assertEquals(BigDecimal.ZERO, calc.getTax());
    }

    @Test
    void total_isGross_plus_commission() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
        Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
        PurchaseCalculator calc = new PurchaseCalculator(share);
        // 1000 + 5 = 1005
        assertEquals(new BigDecimal("1005.00"), calc.getTotal());
    }

    @Test
    void largeQuantity_roundsCorrectly() {
        Stock stock = new Stock("BIG", "Big Co", new BigDecimal("0.1234"));
        Share share = new Share(stock, new BigDecimal("10000"), new BigDecimal("0.1234"));
        PurchaseCalculator calc = new PurchaseCalculator(share);
        // coarse check that gross scales
        assertEquals(new BigDecimal("1234.00"), calc.getGross());
    }
}
