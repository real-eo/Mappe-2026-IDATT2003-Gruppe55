package edu.ntnu.idi.idatt2003.millions.model;

import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.InsufficientFundsException;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.ShareNotOwnedException;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.StockNotFoundException;
import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Share;
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
        List<Stock> stocks = List.of(
                new Stock("EQNR", "Equinor ASA", new BigDecimal("100.00")),
                new Stock("DNB", "DNB Bank", new BigDecimal("200.00"))
        );
        exchange = new Exchange("Test Exchange", stocks, new Random(42));
        player = new Player("Alice", new BigDecimal("50000"));
    }

    @Test
    void buy_reducesPlayerBalance_and_addsShareToPortfolio() throws Exception {
        exchange.buy(player, "EQNR", new BigDecimal("10"));
        // gross=1000, commission=5, total=1005
        assertEquals(new BigDecimal("48995.00"), player.getMoney());
        assertEquals(1, player.getPortfolio().getShares().size());
    }

    @Test
    void buy_throwsStockNotFound_forUnknownSymbol() {
        assertThrows(StockNotFoundException.class, () -> exchange.buy(player, "UNKNOWN", new BigDecimal("1")));
    }

    @Test
    void buy_throwsInsufficientFunds_whenPlayerHasNoMoney() {
        Player poorPlayer = new Player("Bob", new BigDecimal("1.00"));
        assertThrows(InsufficientFundsException.class,
                () -> exchange.buy(poorPlayer, "EQNR", new BigDecimal("100")));
    }

    @Test
    void sell_increasesPlayerBalance_and_removesShareFromPortfolio() throws Exception {
        exchange.buy(player, "EQNR", new BigDecimal("10"));
        // Get the share from portfolio to sell it
        var share = player.getPortfolio().findByStock(exchange.getStock("EQNR")).orElseThrow();
        exchange.sell(player, share, new BigDecimal("10"));
        assertTrue(player.getPortfolio().getShares().isEmpty());

        // Verify player's money increased after sale (purchase -> sale proceeds)
        assertTrue(player.getMoney().compareTo(new BigDecimal("48995.00")) > 0);
    }

    @Test
    void sell_throwsShareNotOwned_whenPlayerDoesNotOwnStock() throws StockNotFoundException {
        Stock stock = exchange.getStock("EQNR");
        var share = new Share(stock, new BigDecimal("5"), stock.getSalesPrice());
        assertThrows(ShareNotOwnedException.class,
                () -> exchange.sell(player, share, new BigDecimal("10")));
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

    @Test
    void getGainers_returnsTopPositiveChanges_withLimit() {
        Stock winner = new Stock("WIN", "Winner Corp", new BigDecimal("100.00"));
        winner.addPrice(new BigDecimal("125.00")); // +25

        Stock runnerUp = new Stock("UP", "Up Corp", new BigDecimal("100.00"));
        runnerUp.addPrice(new BigDecimal("110.00")); // +10

        Stock loser = new Stock("DOWN", "Down Corp", new BigDecimal("100.00"));
        loser.addPrice(new BigDecimal("90.00")); // -10

        Stock unchanged = new Stock("FLAT", "Flat Corp", new BigDecimal("100.00")); // 0

        Exchange statsExchange = new Exchange("Stats", List.of(winner, runnerUp, loser, unchanged));

        List<Stock> gainers = statsExchange.getGainers(2);

        assertEquals(2, gainers.size());
        assertEquals("WIN", gainers.get(0).getSymbol());
        assertEquals("UP", gainers.get(1).getSymbol());
    }

    @Test
    void getLosers_returnsMostNegativeChanges_withLimit() {
        Stock worst = new Stock("WORST", "Worst Corp", new BigDecimal("100.00"));
        worst.addPrice(new BigDecimal("70.00")); // -30

        Stock secondWorst = new Stock("BAD", "Bad Corp", new BigDecimal("100.00"));
        secondWorst.addPrice(new BigDecimal("92.00")); // -8

        Stock gainer = new Stock("GOOD", "Good Corp", new BigDecimal("100.00"));
        gainer.addPrice(new BigDecimal("130.00")); // +30

        Exchange statsExchange = new Exchange("Stats", List.of(worst, secondWorst, gainer));

        List<Stock> losers = statsExchange.getLosers(2);

        assertEquals(2, losers.size());
        assertEquals("WORST", losers.get(0).getSymbol());
        assertEquals("BAD", losers.get(1).getSymbol());
    }

    @Test
    void gainersAndLosers_returnEmpty_whenLimitIsNonPositive() {
        assertTrue(exchange.getGainers(0).isEmpty());
        assertTrue(exchange.getLosers(0).isEmpty());
        assertTrue(exchange.getGainers(-1).isEmpty());
        assertTrue(exchange.getLosers(-1).isEmpty());
    }
}
