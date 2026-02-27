package edu.ntnu.idi.idatt2003.millions.exception;

/**
 * Thrown when a transaction quantity is invalid (e.g. zero or negative).
 */
public class InvalidQuantityException extends MillionsException {

    /**
     * Constructs a new InvalidQuantityException with the given message.
     *
     * @param message the detail message
     */
    public InvalidQuantityException(String message) {
        super(message);
    }
}
