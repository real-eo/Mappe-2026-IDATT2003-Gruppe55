package edu.ntnu.idi.idatt2003.millions.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionBaseTest {

    static class DummyTransaction extends Transaction {
        protected DummyTransaction(Share share, int week, edu.ntnu.idi.idatt2003.millions.model.calculator.TransactionCalculator calculator) {
            super(share, week, calculator);
        }

        @Override
        public void commit(Player player) {
            // no-op
        }
    }

    static class CommittingTransaction extends Transaction {
        protected CommittingTransaction(Share share, int week, edu.ntnu.idi.idatt2003.millions.model.calculator.TransactionCalculator calculator) {
            super(share, week, calculator);
        }

        @Override
        public void commit(Player player) {
            markCommitted();
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

    @Test
    void getters_returnConstructedValues() {
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100"));
        Share share = new Share(stock, new BigDecimal("2"), new BigDecimal("100"));
        var calc = new SimpleCalc();
        DummyTransaction tx = new DummyTransaction(share, 7, calc);

        assertSame(share, tx.getShare());
        assertEquals(7, tx.getWeek());
        assertSame(calc, tx.getCalculator());
        assertFalse(tx.isCommitted());
    }

    @Test
    void markCommitted_setsIsCommittedToTrue() {
        Share share = new Share(new Stock("A", "A Corp", new BigDecimal("10")), new BigDecimal("1"), new BigDecimal("10"));
        CommittingTransaction tx = new CommittingTransaction(share, 1, new SimpleCalc());
        assertFalse(tx.isCommitted());
        tx.commit(null);
        assertTrue(tx.isCommitted());
    }
}
