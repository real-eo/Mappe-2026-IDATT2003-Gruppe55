package edu.ntnu.idi.idatt2003.millions.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ShareTest {

    @Test
    void constructor_throwsOnNullStock() {
        assertThrows(IllegalArgumentException.class, () -> new Share(null, new BigDecimal("1"), new BigDecimal("10")));
    }

    @Test
    void constructor_throwsOnNonPositiveQuantity() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100"));
        assertThrows(IllegalArgumentException.class, () -> new Share(stock, BigDecimal.ZERO, new BigDecimal("10")));
    }

    @Test
    void constructor_throwsOnNonPositivePrice() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100"));
        assertThrows(IllegalArgumentException.class, () -> new Share(stock, new BigDecimal("1"), BigDecimal.ZERO));
    }
}
