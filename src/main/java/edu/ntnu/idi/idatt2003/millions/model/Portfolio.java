package edu.ntnu.idi.idatt2003.millions.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages a player's collection of share holdings.
 */
public class Portfolio {

    private final List<Share> shares;

    /**
     * Constructs an empty Portfolio.
     */
    public Portfolio() {
        this.shares = new ArrayList<>();
    }

    /**
     * Adds a share to the portfolio.
     *
     * @param share the share to add (must not be null)
     * @throws IllegalArgumentException if share is null
     */
    public void add(Share share) {
        if (share == null) {
            throw new IllegalArgumentException("Share must not be null");
        }
        shares.add(share);
    }

    /**
     * Removes the share associated with the same stock from the portfolio.
     *
     * @param share the share whose stock is used for lookup
     * @return {@code true} if a matching share was found and removed, {@code false} otherwise
     */
    public boolean remove(Share share) {
        if (share == null) {
            return false;
        }
        return shares.removeIf(s -> s.getStock().getSymbol().equals(share.getStock().getSymbol()));
    }

    /**
     * Checks whether the portfolio contains a share with the same stock symbol.
     *
     * @param share the share to check
     * @return {@code true} if the portfolio holds the stock
     */
    public boolean contains(Share share) {
        if (share == null) {
            return false;
        }
        return shares.stream()
                .anyMatch(s -> s.getStock().getSymbol().equals(share.getStock().getSymbol()));
    }

    /**
     * Finds the first share in the portfolio whose stock matches the given stock.
     *
     * @param stock the stock to search for
     * @return an Optional containing the matching share, or empty if not found
     */
    public Optional<Share> findByStock(Stock stock) {
        if (stock == null) {
            return Optional.empty();
        }
        return shares.stream()
                .filter(s -> s.getStock().getSymbol().equals(stock.getSymbol()))
                .findFirst();
    }

    /**
     * Returns an unmodifiable view of all shares in the portfolio.
     *
     * @return list of shares
     */
    public List<Share> getShares() {
        return Collections.unmodifiableList(shares);
    }
}
