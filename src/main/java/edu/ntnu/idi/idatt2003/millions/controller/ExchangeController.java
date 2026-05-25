package edu.ntnu.idi.idatt2003.millions.controller;

import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;

import java.math.BigDecimal;

import java.util.List;

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

