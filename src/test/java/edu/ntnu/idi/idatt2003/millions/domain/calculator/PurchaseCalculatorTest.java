package edu.ntnu.idi.idatt2003.millions.domain.calculator;

import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PurchaseCalculatorTest {

    @Test
    void gross_commission_tax_and_total_areCalculatedCorrectly() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
        Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));

        PurchaseCalculator calculator = new PurchaseCalculator(share);

        assertEquals(new BigDecimal("1000.00"), calculator.getGross());
        assertEquals(new BigDecimal("5.00"), calculator.getCommission());
        assertEquals(BigDecimal.ZERO, calculator.getTax());
        assertEquals(new BigDecimal("1005.00"), calculator.getTotal());
    }

    @Test
    void rounds_gross_and_commission_to_two_decimals() {
        Stock stock = new Stock("DEC", "Decimals", new BigDecimal("0.1234"));
        Share share = new Share(stock, new BigDecimal("10000"), new BigDecimal("0.1234"));

        PurchaseCalculator calculator = new PurchaseCalculator(share);

        assertEquals(new BigDecimal("1234.00"), calculator.getGross());
        assertEquals(new BigDecimal("6.17"), calculator.getCommission());
    }
}
