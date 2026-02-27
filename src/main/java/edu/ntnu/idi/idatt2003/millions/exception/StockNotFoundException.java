package edu.ntnu.idi.idatt2003.millions.exception;

/**
 * Thrown when a stock symbol cannot be found on the exchange.
 */
public class StockNotFoundException extends MillionsException {

    /**
     * Constructs a new StockNotFoundException with the given message.
     *
     * @param message the detail message
     */
    public StockNotFoundException(String message) {
        super(message);
    }
}
