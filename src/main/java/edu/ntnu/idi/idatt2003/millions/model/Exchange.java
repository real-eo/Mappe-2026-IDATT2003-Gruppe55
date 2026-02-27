package edu.ntnu.idi.idatt2003.millions.model;

import edu.ntnu.idi.idatt2003.millions.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.exception.ShareNotOwnedException;
import edu.ntnu.idi.idatt2003.millions.exception.StockNotFoundException;
import edu.ntnu.idi.idatt2003.millions.model.calculator.PurchaseCalculator;
import edu.ntnu.idi.idatt2003.millions.model.calculator.SaleCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Represents a stock exchange where players can buy and sell shares.
 *
 * <p>The exchange manages a set of stocks by symbol, tracks the current week,
 * and advances the simulation by updating stock prices each week.</p>
 */
public class Exchange {

    /** Maximum fractional change applied to a stock price each week (±5 %). */
    private static final BigDecimal MAX_CHANGE = new BigDecimal("0.05");
    private static final int SCALE = 4;

    private final String name;
    private final Map<String, Stock> stocks;
    private final Random random;
    private int week;

    /**
     * Constructs an Exchange with a custom {@link Random} instance (useful for deterministic tests).
     *
     * @param name   the exchange name
     * @param random the random number generator used for price updates
     * @throws IllegalArgumentException if name is blank
     */
    public Exchange(String name, Random random) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Exchange name must not be blank");
        }
        this.name = name;
        this.random = random;
        this.stocks = new HashMap<>();
        this.week = 1;
    }

    /**
     * Constructs an Exchange with a default {@link Random} instance.
     *
     * @param name the exchange name
     */
    public Exchange(String name) {
        this(name, new Random());
    }

    /**
     * Adds a stock to the exchange.
     *
     * @param stock the stock to add (symbol must be unique)
     * @throws IllegalArgumentException if stock is null or already listed
     */
    public void addStock(Stock stock) {
        if (stock == null) {
            throw new IllegalArgumentException("Stock must not be null");
        }
        stocks.put(stock.getSymbol(), stock);
    }

    /**
     * Returns the stock with the given symbol.
     *
     * @param symbol the ticker symbol
     * @return the stock
     * @throws StockNotFoundException if no stock with that symbol is listed
     */
    public Stock getStock(String symbol) throws StockNotFoundException {
        Stock stock = stocks.get(symbol);
        if (stock == null) {
            throw new StockNotFoundException("Stock not found: " + symbol);
        }
        return stock;
    }

    /**
     * Searches for stocks whose symbol or company name contains the given keyword
     * (case-insensitive).
     *
     * @param keyword the search term
     * @return list of matching stocks
     */
    public List<Stock> findStocks(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.copyOf(stocks.values());
        }
        String lower = keyword.toLowerCase();
        return stocks.values().stream()
                .filter(s -> s.getSymbol().toLowerCase().contains(lower)
                        || s.getCompanyName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /**
     * Creates and commits a {@link Purchase} transaction for the given player.
     *
     * @param player   the buying player
     * @param symbol   the stock symbol to buy
     * @param quantity the number of shares to purchase
     * @throws StockNotFoundException if the symbol is not listed
     * @throws MillionsException      if the purchase cannot be committed
     */
    public void buy(Player player, String symbol, int quantity) throws MillionsException {
        if (quantity <= 0) {
            throw new edu.ntnu.idi.idatt2003.millions.exception.InvalidQuantityException(
                    "Quantity must be positive");
        }
        Stock stock = getStock(symbol);
        Share share = new Share(stock, quantity, stock.getSalesPrice());
        Purchase purchase = new Purchase(share, week, new PurchaseCalculator(share));
        purchase.commit(player);
    }

    /**
     * Creates and commits a {@link Sale} transaction for the given player.
     *
     * @param player   the selling player
     * @param symbol   the stock symbol to sell
     * @param quantity the number of shares to sell
     * @throws StockNotFoundException if the symbol is not listed
     * @throws ShareNotOwnedException if the player does not own the stock
     * @throws MillionsException      if the sale cannot be committed
     */
    public void sell(Player player, String symbol, int quantity) throws MillionsException {
        if (quantity <= 0) {
            throw new edu.ntnu.idi.idatt2003.millions.exception.InvalidQuantityException(
                    "Quantity must be positive");
        }
        Stock stock = getStock(symbol);
        Share share = player.getPortfolio().findByStock(stock)
                .orElseThrow(() -> new ShareNotOwnedException(
                        "Player does not own stock: " + symbol));
        Sale sale = new Sale(share, week, new SaleCalculator(share));
        sale.commit(player);
    }

    /**
     * Advances the simulation by one week, incrementing the week counter and updating
     * each stock's price by a random small change (±{@value} % at most).
     */
    public void advance() {
        week++;
        for (Stock stock : stocks.values()) {
            BigDecimal current = stock.getSalesPrice();
            // Random change in range [-MAX_CHANGE, +MAX_CHANGE]
            double changeRate = (random.nextDouble() * 2 - 1) * MAX_CHANGE.doubleValue();
            BigDecimal factor = BigDecimal.ONE.add(
                    new BigDecimal(changeRate).setScale(SCALE, RoundingMode.HALF_UP));
            BigDecimal newPrice = current.multiply(factor).setScale(SCALE, RoundingMode.HALF_UP);
            if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                newPrice = new BigDecimal("0.01");
            }
            stock.addPrice(newPrice);
        }
    }

    /**
     * Returns the name of the exchange.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the current week number.
     *
     * @return the week
     */
    public int getWeek() {
        return week;
    }
}
