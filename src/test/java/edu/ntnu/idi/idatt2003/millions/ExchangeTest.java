package edu.ntnu.idi.idatt2003.millions;

import edu.ntnu.idi.idatt2003.millions.exception.InsufficientFundsException;
import edu.ntnu.idi.idatt2003.millions.exception.ShareNotOwnedException;
import edu.ntnu.idi.idatt2003.millions.exception.StockNotFoundException;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Exchange}.
 */
class ExchangeTest {

    private Exchange exchange;
    private Player player;

    @BeforeEach
    void setUp() {
        exchange = new Exchange("Test Exchange", new Random(42));
        exchange.addStock(new Stock("EQNR", "Equinor ASA", new BigDecimal("100.00")));
        exchange.addStock(new Stock("DNB", "DNB Bank", new BigDecimal("200.00")));
        player = new Player("Alice", new BigDecimal("50000"));
    }

    @Test
    void buy_reducesPlayerBalance_and_addsShareToPortfolio() throws Exception {
        exchange.buy(player, "EQNR", 10);
        // gross=1000, commission=5, total=1005
        assertEquals(new BigDecimal("48995.00"), player.getMoney());
        assertEquals(1, player.getPortfolio().getShares().size());
    }

    @Test
    void buy_throwsStockNotFound_forUnknownSymbol() {
        assertThrows(StockNotFoundException.class, () -> exchange.buy(player, "UNKNOWN", 1));
    }

    @Test
    void buy_throwsInsufficientFunds_whenPlayerHasNoMoney() {
        Player poorPlayer = new Player("Bob", new BigDecimal("1.00"));
        assertThrows(InsufficientFundsException.class,
                () -> exchange.buy(poorPlayer, "EQNR", 100));
    }

    @Test
    void sell_increasesPlayerBalance_and_removesShareFromPortfolio() throws Exception {
        exchange.buy(player, "EQNR", 10);
        exchange.sell(player, "EQNR", 10);
        assertTrue(player.getPortfolio().getShares().isEmpty());
    }

    @Test
    void sell_throwsShareNotOwned_whenPlayerDoesNotOwnStock() {
        assertThrows(ShareNotOwnedException.class,
                () -> exchange.sell(player, "EQNR", 10));
    }

    @Test
    void advance_incrementsWeek() {
        assertEquals(1, exchange.getWeek());
        exchange.advance();
        assertEquals(2, exchange.getWeek());
    }

    @Test
    void advance_updatesPriceHistory() {
        Stock stock = exchange.findStocks("EQNR").get(0);
        int pricesBefore = stock.getPrices().size();
        exchange.advance();
        assertEquals(pricesBefore + 1, stock.getPrices().size());
    }

    @Test
    void findStocks_bySymbol() {
        List<Stock> results = exchange.findStocks("EQNR");
        assertEquals(1, results.size());
        assertEquals("EQNR", results.get(0).getSymbol());
    }

    @Test
    void findStocks_byCompanyName_caseInsensitive() {
        List<Stock> results = exchange.findStocks("equinor");
        assertEquals(1, results.size());
    }

    @Test
    void findStocks_emptyKeyword_returnsAll() {
        List<Stock> results = exchange.findStocks("");
        assertEquals(2, results.size());
    }

    @Test
    void getStock_throwsStockNotFound_forUnknownSymbol() {
        assertThrows(StockNotFoundException.class, () -> exchange.getStock("MISSING"));
    }
}
