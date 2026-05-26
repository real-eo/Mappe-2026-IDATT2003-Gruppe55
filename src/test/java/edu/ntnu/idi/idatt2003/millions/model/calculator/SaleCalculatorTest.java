package edu.ntnu.idi.idatt2003.millions.model.calculator;

import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.calculator.SaleCalculator;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link SaleCalculator}.
 */
class SaleCalculatorTest {

    /**
     * Scenario: bought at 100, now selling at 200 (profitable).
     */
    @Test
    void profitable_sale_calculatesCorrectly() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("200.00"));
        Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
        SaleCalculator calc = new SaleCalculator(share);

        assertEquals(new BigDecimal("2000.00"), calc.getGross());
        assertEquals(new BigDecimal("20.00"), calc.getCommission());
        assertEquals(new BigDecimal("294.00"), calc.getTax());
        assertEquals(new BigDecimal("1686.00"), calc.getTotal());
    }

    @Test
    void loss_sale_hasNoTax() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
        Share share = new Share(stock, new BigDecimal("5"), new BigDecimal("200.00"));
        SaleCalculator calc = new SaleCalculator(share);

        assertEquals(BigDecimal.ZERO, calc.getTax());
        assertEquals(new BigDecimal("495.00"), calc.getTotal());
    }

    @Test
    void breakeven_hasNoTax_and_commissionApplied() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
        Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
        SaleCalculator calc = new SaleCalculator(share);

        // selling at same price -> profit = -commission -> no tax
        assertEquals(BigDecimal.ZERO, calc.getTax());
        assertEquals(new BigDecimal("990.00"), calc.getTotal());
    }
}
