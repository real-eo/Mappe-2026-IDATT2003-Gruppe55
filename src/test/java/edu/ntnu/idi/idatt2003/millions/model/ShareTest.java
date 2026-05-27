package edu.ntnu.idi.idatt2003.millions.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void getters_returnConstructedValues() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100"));
        Share share = new Share(stock, new BigDecimal("5"), new BigDecimal("80"));
        assertSame(stock, share.getStock());
        assertEquals(new BigDecimal("5"), share.getQuantity());
        assertEquals(new BigDecimal("80"), share.getPurchasePrice());
    }

    @Test
    void toString_containsQuantityAndSymbol() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100"));
        Share share = new Share(stock, new BigDecimal("3"), new BigDecimal("90"));
        String s = share.toString();
        assertTrue(s.contains("3"));
        assertTrue(s.contains("EQNR"));
    }
}
