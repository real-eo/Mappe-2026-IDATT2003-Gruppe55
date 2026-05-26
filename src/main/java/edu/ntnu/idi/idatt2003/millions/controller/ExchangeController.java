package edu.ntnu.idi.idatt2003.millions.controller;

import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.calculator.PurchaseCalculator;
import edu.ntnu.idi.idatt2003.millions.model.calculator.SaleCalculator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller that mediates between the view and the Exchange / Player model.
 */
public class ExchangeController {

    private final Exchange exchange;
    private final Player player;

    /**
     * Constructs an ExchangeController.
     *
     * @param exchange the exchange to operate on
     * @param player   the active player
     */
    public ExchangeController(Exchange exchange, Player player) {
        this.exchange = exchange;
        this.player = player;
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
}

