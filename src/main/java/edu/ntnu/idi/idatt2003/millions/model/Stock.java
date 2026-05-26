package edu.ntnu.idi.idatt2003.millions.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

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
     * Returns all historical prices that have been registered for this stock.
     *
     * @return list of prices, oldest first
     */
    public List<BigDecimal> getHistoricalPrices() {
        return Collections.unmodifiableList(prices);
    }

    /**
     * Returns the highest registered price for this stock.
     *
     * @return highest price
     */
    public BigDecimal getHighestPrice() {
        return prices.stream()
                .max(BigDecimal::compareTo)
                .orElseThrow();
    }

    /**
     * Returns the lowest registered price for this stock.
     *
     * @return lowest price
     */
    public BigDecimal getLowestPrice() {
        return prices.stream()
                .min(BigDecimal::compareTo)
                .orElseThrow();
    }

    /**
     * Returns the latest price change: latest price minus previous price.
     * If only one price has been registered, the change is interpreted as zero.
     *
     * @return latest price change
     */
    public BigDecimal getLatestPriceChange() {
        if (prices.size() < 2) {
            return BigDecimal.ZERO;
        }

        BigDecimal latest = prices.get(prices.size() - 1);
        BigDecimal previous = prices.get(prices.size() - 2);
        return latest.subtract(previous);
    }

    /**
     * Returns the latest percent change: (latest - previous) / previous * 100.
     * If only one price has been registered or the previous price is zero, the change is zero.
     *
     * @return latest percent change
     */
    public BigDecimal getLatestPriceChangePercent() {
        if (prices.size() < 2) {
            return BigDecimal.ZERO;
        }

        BigDecimal latest = prices.get(prices.size() - 1);
        BigDecimal previous = prices.get(prices.size() - 2);
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal delta = latest.subtract(previous);
        return delta.divide(previous, 6, RoundingMode.HALF_UP)
            .multiply(ONE_HUNDRED);
    }

    /**
     * Returns all historical prices.
     *
     * @return list of prices, oldest first
     */
    public List<BigDecimal> getPrices() {
        return getHistoricalPrices();
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

