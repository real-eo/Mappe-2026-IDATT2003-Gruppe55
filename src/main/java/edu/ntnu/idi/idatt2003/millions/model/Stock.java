package edu.ntnu.idi.idatt2003.millions.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a publicly traded stock on the exchange.
 *
 * <p>A stock tracks its full price history. The current sales price is always
 * the most recently added entry.</p>
 */
public class Stock {

    private final String symbol;
    private final String companyName;
    private final List<BigDecimal> prices;

    /**
     * Constructs a Stock with an initial price.
     *
     * @param symbol      the ticker symbol (e.g. "AAPL")
     * @param companyName the full company name
     * @param initialPrice the starting price
     * @throws IllegalArgumentException if symbol or companyName is blank, or initialPrice is non-positive
     */
    public Stock(String symbol, String companyName, BigDecimal initialPrice) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol must not be blank");
        }
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name must not be blank");
        }
        if (initialPrice == null || initialPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Initial price must be positive");
        }
        
        this.symbol = symbol;
        this.companyName = companyName;
        this.prices = new ArrayList<>();
        this.prices.add(initialPrice);
    }

    /**
     * Returns the ticker symbol.
     *
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the company name.
     *
     * @return the company name
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Returns the current sales price (the latest price in the history).
     *
     * @return the current sales price
     */
    public BigDecimal getSalesPrice() {
        return prices.get(prices.size() - 1);
    }

    /**
     * Returns an unmodifiable view of all historical prices.
     *
     * @return list of prices, oldest first
     */
    public List<BigDecimal> getPrices() {
        return Collections.unmodifiableList(prices);
    }

    /**
     * Appends a new price to the price history.
     *
     * @param price the new price (must be positive)
     * @throws IllegalArgumentException if price is null or non-positive
     */
    public void addPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        prices.add(price);
    }

    @Override
    public String toString() {
        return symbol + " (" + companyName + ") @ " + getSalesPrice();
    }
}
