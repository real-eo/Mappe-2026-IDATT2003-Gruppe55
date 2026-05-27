package edu.ntnu.idi.idatt2003.millions.controller;

import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.GameRepository;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.SaveGameStorage;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.SqliteGameRepository;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.GameState;
import edu.ntnu.idi.idatt2003.millions.model.NetWorthSnapshot;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Purchase;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.Transaction;
import edu.ntnu.idi.idatt2003.millions.model.calculator.PurchaseCalculator;
import edu.ntnu.idi.idatt2003.millions.model.calculator.SaleCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Controller that mediates between the view and the Exchange / Player model.
 */
public class ExchangeController {

    private final Exchange exchange;
    private final Player player;
    private final List<NetWorthSnapshot> netWorthHistory = new ArrayList<>();

    /**
     * Constructs an ExchangeController seeded with existing net worth history (e.g. from a load).
     *
     * @param exchange        the exchange to operate on
     * @param player          the active player
     * @param savedHistory    previously recorded snapshots; empty list starts fresh
     */
    public ExchangeController(Exchange exchange, Player player, List<NetWorthSnapshot> savedHistory) {
        this.exchange = exchange;
        this.player = player;
        if (savedHistory != null && !savedHistory.isEmpty()) {
            netWorthHistory.addAll(savedHistory);
        } else {
            netWorthHistory.add(new NetWorthSnapshot(exchange.getWeek(), player.getNetWorth()));
        }
    }

    /**
     * Constructs an ExchangeController for a new game.
     *
     * @param exchange the exchange to operate on
     * @param player   the active player
     */
    public ExchangeController(Exchange exchange, Player player) {
        this(exchange, player, List.of());
    }

    /**
     * Executes a buy order for the current player.
     *
     * @param symbol   the stock symbol
     * @param quantity the number of shares
     * @throws MillionsException if the purchase fails
     */
    public void buy(String symbol, BigDecimal quantity) throws MillionsException {
        exchange.buy(player, symbol, quantity);
    }

    /**
     * Executes a sell order for the current player.
     *
     * @param share    the share to sell
     * @param quantity the number of shares
     * @throws MillionsException if the sale fails
     */
    public void sell(Share share, BigDecimal quantity) throws MillionsException {
        exchange.sell(player, share, quantity);
    }

    /**
     * Advances the simulation by one week.
     */
    public void advance() {
        exchange.advance();
        netWorthHistory.add(new NetWorthSnapshot(exchange.getWeek(), player.getNetWorth()));
    }

    /**
     * Returns the net worth history recorded since this controller was created.
     *
     * @return immutable list of net worth snapshots ordered by week
     */
    public List<NetWorthSnapshot> getNetWorthHistory() {
        return List.copyOf(netWorthHistory);
    }

    /**
     * Searches for stocks by keyword.
     *
     * @param keyword the search term
     * @return list of matching stocks
     */
    public List<Stock> findStocks(String keyword) {
        return exchange.findStocks(keyword);
    }

    /**
     * Returns stocks with the largest positive price change this week.
     *
     * @param limit maximum number of results
     * @return gainers sorted descending by price change
     */
    public List<Stock> getGainers(int limit) {
        return exchange.getGainers(limit);
    }

    /**
     * Returns stocks with the largest negative price change this week.
     *
     * @param limit maximum number of results
     * @return losers sorted ascending by price change
     */
    public List<Stock> getLosers(int limit) {
        return exchange.getLosers(limit);
    }

    /**
     * Calculates the total cost (including fees and tax) for a prospective purchase.
     *
     * @param symbol   the stock symbol
     * @param quantity number of shares
     * @return total purchase cost, or empty if the stock is not found
     */
    public Optional<BigDecimal> calculateBuyTotal(String symbol, BigDecimal quantity) {
        if (!exchange.hasStock(symbol)) {
            return Optional.empty();
        }
        Stock stock = exchange.getStocks().stream()
                .filter(s -> s.getSymbol().equals(symbol))
                .findFirst()
                .orElse(null);
        if (stock == null) {
            return Optional.empty();
        }
        Share share = new Share(stock, quantity, stock.getSalesPrice());
        return Optional.of(new PurchaseCalculator(share).getTotal());
    }

    /**
     * Calculates the total proceeds (after fees and tax) for a prospective sale.
     *
     * @param stock    the stock to sell
     * @param quantity number of shares to sell
     * @return total sale proceeds, or empty if the player does not own this stock
     */
    public Optional<BigDecimal> calculateSellTotal(Stock stock, BigDecimal quantity) {
        Optional<Share> owned = player.getPortfolio().findByStock(stock);
        if (owned.isEmpty()) {
            return Optional.empty();
        }
        Share saleShare = new Share(stock, quantity, owned.get().getPurchasePrice());
        return Optional.of(new SaleCalculator(saleShare).getTotal());
    }

    /**
     * Returns the share the player owns for a given stock.
     *
     * @param stock the stock to look up
     * @return the owned share, or empty if the player does not own it
     */
    public Optional<Share> getOwnedShare(Stock stock) {
        return player.getPortfolio().findByStock(stock);
    }

    /**
     * Returns the active player.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the exchange.
     *
     * @return the exchange
     */
    public Exchange getExchange() {
        return exchange;
    }

    /**
     * Returns the shares currently held in the player's portfolio.
     *
     * @return list of shares
     */
    public List<Share> getPortfolioShares() {
        return player.getPortfolio().getShares();
    }

    /**
     * Returns all transactions in the player's history, sorted by week descending.
     *
     * @return sorted list of transactions
     */
    public List<Transaction> getSortedTransactionHistory() {
        return player.getTransactionArchive().getTransactions().stream()
                .sorted(Comparator.comparingInt(Transaction::getWeek).reversed())
                .toList();
    }

    /**
     * Returns the total number of transactions in the player's history.
     *
     * @return transaction count
     */
    public int getTransactionCount() {
        return player.getTransactionArchive().getTransactions().size();
    }

    /**
     * Returns the current market value of a portfolio share (price × quantity).
     *
     * @param share the share to value
     * @return total value
     */
    public BigDecimal getPortfolioItemValue(Share share) {
        return share.getStock().getSalesPrice().multiply(share.getQuantity());
    }

    /**
     * Returns the effective unit sale price for a transaction.
     * For purchases this is the purchase price; for sales it is gross ÷ quantity.
     *
     * @param transaction the transaction
     * @param qty         the quantity sold/bought
     * @return unit price
     */
    public BigDecimal getUnitSalePrice(Transaction transaction, BigDecimal qty) {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (transaction instanceof Purchase) {
            return transaction.getShare().getPurchasePrice();
        }
        return transaction.getCalculator().getGross().divide(qty, 2, RoundingMode.HALF_UP);
    }

    /**
     * Searches for stocks by keyword and optional price bounds.
     * Null bounds mean no limit. Inverted bounds are normalised internally.
     *
     * @param keyword  the search term
     * @param minPrice minimum price inclusive, or null
     * @param maxPrice maximum price inclusive, or null
     * @return matching stocks
     */
    public List<Stock> findStocks(String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
        BigDecimal lo = minPrice;
        BigDecimal hi = maxPrice;
        if (lo != null && hi != null && lo.compareTo(hi) > 0) {
            BigDecimal tmp = lo;
            lo = hi;
            hi = tmp;
        }
        final BigDecimal finalLo = lo;
        final BigDecimal finalHi = hi;
        List<Stock> base = exchange.findStocks(keyword);
        if (finalLo == null && finalHi == null) {
            return base;
        }
        return base.stream()
                .filter(s -> {
                    BigDecimal price = s.getSalesPrice();
                    if (finalLo != null && price.compareTo(finalLo) < 0) {
                        return false;
                    }
                    if (finalHi != null && price.compareTo(finalHi) > 0) {
                        return false;
                    }
                    return true;
                })
                .toList();
    }

    /**
     * Returns up to {@code limit} gainers followed by up to {@code limit} losers.
     *
     * @param limit maximum entries per side
     * @return combined market movers list
     */
    public List<Stock> getMarketMovers(int limit) {
        List<Stock> result = new ArrayList<>();
        result.addAll(exchange.getGainers(limit));
        result.addAll(exchange.getLosers(limit));
        return result;
    }

    /**
     * Saves the current game state asynchronously.
     *
     * @param onSuccess called with the save ID on success
     * @param onFailure called with an error message on failure
     */
    public void saveGame(Consumer<Long> onSuccess, Consumer<String> onFailure) {
        Thread worker = new Thread(() -> {
            try {
                Path databasePath = SaveGameStorage.resolveDefaultDatabasePath();
                GameRepository repository = new SqliteGameRepository(databasePath);
                repository.initialize();
                long saveId = repository.save(new GameState(exchange, player, getNetWorthHistory()));
                if (onSuccess != null) {
                    javafx.application.Platform.runLater(() -> onSuccess.accept(saveId));
                }
            } catch (Exception e) {
                String message = e.getMessage() == null ? "Unknown error" : e.getMessage();
                if (onFailure != null) {
                    javafx.application.Platform.runLater(() -> onFailure.accept(message));
                }
            }
        }, "save-game-task");
        worker.setDaemon(true);
        worker.start();
    }
}

