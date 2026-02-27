package edu.ntnu.idi.idatt2003.millions.model;

import java.math.BigDecimal;

/**
 * Represents a holding of a certain number of shares of one stock, bought at a specific price.
 */
public class Share {

    private final Stock stock;
    private final int quantity;
    private final BigDecimal purchasePrice;

    /**
     * Constructs a Share.
     *
     * @param stock         the underlying stock
     * @param quantity      the number of shares (must be positive)
     * @param purchasePrice the price per share at time of purchase (must be positive)
     * @throws IllegalArgumentException if any argument is invalid
     */
    public Share(Stock stock, int quantity, BigDecimal purchasePrice) {
        if (stock == null) {
            throw new IllegalArgumentException("Stock must not be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Purchase price must be positive");
        }
        this.stock = stock;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
    }

    /**
     * Returns the underlying stock.
     *
     * @return the stock
     */
    public Stock getStock() {
        return stock;
    }

    /**
     * Returns the number of shares held.
     *
     * @return quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the price per share at the time of purchase.
     *
     * @return purchase price
     */
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    @Override
    public String toString() {
        return quantity + " × " + stock.getSymbol() + " @ " + purchasePrice;
    }
}
