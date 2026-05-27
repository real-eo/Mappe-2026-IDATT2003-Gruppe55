package edu.ntnu.idi.idatt2003.millions.infrastructure.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionsTest {

    @Test
    void millionsException_preservesMessage() {
        MillionsException exception = new MillionsException("base message");
        assertEquals("base message", exception.getMessage());
    }

    @Test
    void insufficientFundsException_isAMillionsException_andPreservesMessage() {
        InsufficientFundsException exception = new InsufficientFundsException("not enough money");
        assertTrue(exception instanceof MillionsException);
        assertEquals("not enough money", exception.getMessage());
    }

    @Test
    void invalidQuantityException_isAMillionsException_andPreservesMessage() {
        InvalidQuantityException exception = new InvalidQuantityException("bad quantity");
        assertTrue(exception instanceof MillionsException);
        assertEquals("bad quantity", exception.getMessage());
    }

    @Test
    void shareNotOwnedException_isAMillionsException_andPreservesMessage() {
        ShareNotOwnedException exception = new ShareNotOwnedException("share not owned");
        assertTrue(exception instanceof MillionsException);
        assertEquals("share not owned", exception.getMessage());
    }

    @Test
    void stockNotFoundException_isAMillionsException_andPreservesMessage() {
        StockNotFoundException exception = new StockNotFoundException("stock missing");
        assertTrue(exception instanceof MillionsException);
        assertEquals("stock missing", exception.getMessage());
    }

    @Test
    void transactionAlreadyCommittedException_isAMillionsException_andPreservesMessage() {
        TransactionAlreadyCommittedException exception =
                new TransactionAlreadyCommittedException("already committed");
        assertTrue(exception instanceof MillionsException);
        assertEquals("already committed", exception.getMessage());
    }
}
