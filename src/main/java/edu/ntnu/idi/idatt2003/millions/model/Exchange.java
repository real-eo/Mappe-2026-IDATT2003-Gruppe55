package edu.ntnu.idi.idatt2003.millions.model;

import edu.ntnu.idi.idatt2003.millions.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.exception.ShareNotOwnedException;
import edu.ntnu.idi.idatt2003.millions.exception.StockNotFoundException;

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
     * @param stocks the list of stocks available on this exchange
     * @param random the random number generator used for price updates
     * @throws IllegalArgumentException if name is blank or stocks is null
     */
    public Exchange(String name, List<Stock> stocks, Random random) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Exchange name must not be blank");
        }
        if (stocks == null) {
            throw new IllegalArgumentException("Stocks list must not be null");
        }

        this.name = name;
        this.random = random;
        this.stocks = new HashMap<>();
        this.week = 1;
        
        for (Stock stock : stocks) {
            if (stock != null) {
                this.stocks.put(stock.getSymbol(), stock);
            }
        }
    }

    /**
     * Constructs an Exchange with a name, list of stocks, and a default {@link Random} instance.
     *
     * @param name   the exchange name
     * @param stocks the list of stocks available on this exchange
     * @throws IllegalArgumentException if name is blank or stocks is null
     */
    public Exchange(String name, List<Stock> stocks) {
        this(name, stocks, new Random());
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
     * Checks if a stock with the given symbol is listed on this exchange.
     *
     * @param symbol the ticker symbol
     * @return true if the stock exists
     */
    public boolean hasStock(String symbol) {
        return stocks.containsKey(symbol);
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
    public Purchase buy(Player player, String symbol, BigDecimal quantity) throws MillionsException {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new edu.ntnu.idi.idatt2003.millions.exception.InvalidQuantityException(
                    "Quantity must be positive");
        }

        Stock stock = getStock(symbol);
        Share share = new Share(stock, quantity, stock.getSalesPrice());
        Purchase purchase = new Purchase(share, week);
        purchase.commit(player);
        
        return purchase;
    }

    // ! It seems really stupid to pass the share object here instead of just the symbol as we also pass the player.
    // ! However, the spec explicitly states that the method signature should be `sell(Player player, Share share)`,
    // ! so we will follow the spec as given. In a real application, we would likely want to refactor this to take 
    // ! the symbol and quantity instead like the `buy` method, and look up the share from the player's portfolio.
    /**
     * Creates and commits a {@link Sale} transaction for the given player.
     *
     * @param player   the selling player
     * @param share    the share to sell
     * @param quantity the number of shares to sell
     * @throws StockNotFoundException if the symbol is not listed
     * @throws ShareNotOwnedException if the player does not own the stock
     * @throws MillionsException      if the sale cannot be committed
     */
    public Sale sell(Player player, Share share, BigDecimal quantity) throws MillionsException {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new edu.ntnu.idi.idatt2003.millions.exception.InvalidQuantityException(
                    "Quantity must be positive");
        }

        Sale sale = new Sale(share, week);
        sale.commit(player);

        return sale;
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
}
