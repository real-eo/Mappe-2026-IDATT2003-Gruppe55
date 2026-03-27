package edu.ntnu.idi.idatt2003.millions.infrastructure.exception;

/**
 * Base exception for all Millions application errors.
 */
public class MillionsException extends Exception {

    /**
     * Constructs a new MillionsException with the given message.
     *
     * @param message the detail message
     */
    public MillionsException(String message) {
        super(message);
    }
}

