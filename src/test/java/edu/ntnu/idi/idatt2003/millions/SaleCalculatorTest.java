package edu.ntnu.idi.idatt2003.millions;

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
     * gross       = 200 * 10  = 2000
     * commission  = 1% * 2000 = 20
     * costBasis   = 100 * 10  = 1000
     * profit      = 2000 - 20 - 1000 = 980
     * tax         = 30% * 980 = 294
     * total       = 2000 - 20 - 294 = 1686
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

    /**
     * Scenario: bought at 200, now selling at 100 (loss â€“ no tax).
     * gross       = 100 * 5  = 500
     * commission  = 1% * 500 = 5
     * costBasis   = 200 * 5  = 1000
     * profit      = 500 - 5 - 1000 = -505  (negative -> no tax)
     * total       = 500 - 5 - 0 = 495
     */
    @Test
    void loss_sale_hasNoTax() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
        Share share = new Share(stock, new BigDecimal("5"), new BigDecimal("200.00"));
        SaleCalculator calc = new SaleCalculator(share);

        assertEquals(BigDecimal.ZERO, calc.getTax());
        assertEquals(new BigDecimal("495.00"), calc.getTotal());
    }
}

