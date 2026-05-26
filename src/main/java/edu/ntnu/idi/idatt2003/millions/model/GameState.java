package edu.ntnu.idi.idatt2003.millions.model;

import java.util.List;
import java.util.Objects;

/**
 * Bundles the current exchange and player state for persistence or transfer.
 */
public class GameState {

    private final Exchange exchange;
    private final Player player;
    private final List<NetWorthSnapshot> netWorthHistory;

    /**
     * Constructs a GameState snapshot with net worth history.
     *
     * @param exchange        the exchange state
     * @param player          the player state
     * @param netWorthHistory recorded net worth snapshots
     */
    public GameState(Exchange exchange, Player player, List<NetWorthSnapshot> netWorthHistory) {
        this.exchange = Objects.requireNonNull(exchange, "exchange must not be null");
        this.player = Objects.requireNonNull(player, "player must not be null");
        this.netWorthHistory = netWorthHistory == null ? List.of() : List.copyOf(netWorthHistory);
    }

    /**
     * Constructs a GameState snapshot without history (history treated as empty).
     *
     * @param exchange the exchange state
     * @param player   the player state
     */
    public GameState(Exchange exchange, Player player) {
        this(exchange, player, List.of());
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

    /**
     * Returns the net worth history recorded for this save.
     *
     * @return immutable list of snapshots ordered by week
     */
    public List<NetWorthSnapshot> getNetWorthHistory() {
        return netWorthHistory;
    }
}
