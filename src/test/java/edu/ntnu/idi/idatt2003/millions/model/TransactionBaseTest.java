package edu.ntnu.idi.idatt2003.millions.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionBaseTest {

    // Create a small concrete subclass for testing constructor validation
    static class DummyTransaction extends Transaction {
        protected DummyTransaction(Share share, int week, edu.ntnu.idi.idatt2003.millions.model.calculator.TransactionCalculator calculator) {
            super(share, week, calculator);
        }

        @Override
        public void commit(Player player) {
            // no-op
        }
    }

    // simple calculator implementation for tests
    static class SimpleCalc implements edu.ntnu.idi.idatt2003.millions.model.calculator.TransactionCalculator {
        @Override public java.math.BigDecimal getGross() { return java.math.BigDecimal.ZERO; }
        @Override public java.math.BigDecimal getCommission() { return java.math.BigDecimal.ZERO; }
        @Override public java.math.BigDecimal getTax() { return java.math.BigDecimal.ZERO; }
        @Override public java.math.BigDecimal getTotal() { return java.math.BigDecimal.ZERO; }
    }

    @Test
    void constructor_throwsOnNullShare() {
        var calc = new SimpleCalc();
        assertThrows(IllegalArgumentException.class, () -> new DummyTransaction(null, 1, calc));
    }

    @Test
    void constructor_throwsOnNullCalculator() {
        Share share = new Share(new Stock("EQNR","Equinor", new BigDecimal("100")), new BigDecimal("1"), new BigDecimal("100"));
        assertThrows(IllegalArgumentException.class, () -> new DummyTransaction(share, 1, null));
    }

    @Test
    void constructor_throwsOnNonPositiveWeek() {
        Share share = new Share(new Stock("EQNR","Equinor", new BigDecimal("100")), new BigDecimal("1"), new BigDecimal("100"));
        var calc = new SimpleCalc();
        assertThrows(IllegalArgumentException.class, () -> new DummyTransaction(share, 0, calc));
    }
}
