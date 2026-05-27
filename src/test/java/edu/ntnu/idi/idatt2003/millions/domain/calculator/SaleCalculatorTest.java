package edu.ntnu.idi.idatt2003.millions.domain.calculator;

import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaleCalculatorTest {

    @Test
    void profitable_sale_applies_commission_and_tax() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("200.00"));
        Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));

        SaleCalculator calculator = new SaleCalculator(share);

        assertEquals(new BigDecimal("2000.00"), calculator.getGross());
        assertEquals(new BigDecimal("20.00"), calculator.getCommission());
        assertEquals(new BigDecimal("294.00"), calculator.getTax());
        assertEquals(new BigDecimal("1686.00"), calculator.getTotal());
    }

    @Test
    void non_profitable_sale_has_zero_tax() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
        Share share = new Share(stock, new BigDecimal("5"), new BigDecimal("200.00"));

        SaleCalculator calculator = new SaleCalculator(share);

        assertEquals(BigDecimal.ZERO, calculator.getTax());
        assertEquals(new BigDecimal("495.00"), calculator.getTotal());
    }
}
