package edu.ntnu.idi.idatt2003.millions.infrastructure.exception;

/**
 * Thrown when a player does not have enough funds to complete a purchase.
 */
public class InsufficientFundsException extends MillionsException {

    /**
     * Constructs a new InsufficientFundsException with the given message.
     *
     * @param message the detail message
     */
    public InsufficientFundsException(String message) {
        super(message);
    }
}

