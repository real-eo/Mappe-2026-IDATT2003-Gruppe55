package edu.ntnu.idi.idatt2003.millions.infrastructure.exception;

/**
 * Thrown when an attempt is made to commit a transaction that has already been committed.
 */
public class TransactionAlreadyCommittedException extends MillionsException {

    /**
     * Constructs a new TransactionAlreadyCommittedException with the given message.
     *
     * @param message the detail message
     */
    public TransactionAlreadyCommittedException(String message) {
        super(message);
    }
}

