package edu.ntnu.idi.idatt2003.millions.infrastructure.exception;

/**
 * Thrown when a player attempts to sell a share they do not own.
 */
public class ShareNotOwnedException extends MillionsException {

    /**
     * Constructs a new ShareNotOwnedException with the given message.
     *
     * @param message the detail message
     */
    public ShareNotOwnedException(String message) {
        super(message);
    }
}

