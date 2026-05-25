package edu.ntnu.idi.idatt2003.millions.model;

import java.util.Objects;

/**
 * Bundles the current exchange and player state for persistence or transfer.
 */
public class GameState {

    private final Exchange exchange;
    private final Player player;

    /**
     * Constructs a GameState snapshot.
     *
     * @param exchange the exchange state
     * @param player   the player state
     */
    public GameState(Exchange exchange, Player player) {
        this.exchange = Objects.requireNonNull(exchange, "exchange must not be null");
        this.player = Objects.requireNonNull(player, "player must not be null");
    }

    /**
     * Returns the exchange state.
     *
     * @return the exchange
     */
    public Exchange getExchange() {
        return exchange;
    }

    /**
     * Returns the player state.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }
}
