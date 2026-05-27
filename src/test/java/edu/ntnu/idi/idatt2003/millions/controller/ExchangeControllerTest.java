package edu.ntnu.idi.idatt2003.millions.controller;

import edu.ntnu.idi.idatt2003.millions.model.*;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeControllerTest {

    private Exchange exchange;
    private Player player;
    private ExchangeController controller;

    @BeforeEach
    void setUp() {
        Stock a = new Stock("A", "A Corp", new BigDecimal("100.00"));
        Stock b = new Stock("B", "B Ltd", new BigDecimal("50.00"));
        exchange = new Exchange("Test", List.of(a, b));
        player = new Player("Tester", new BigDecimal("10000.00"));
        controller = new ExchangeController(exchange, player);
    }

    @Test
    void calculateBuyTotal_returnsEmpty_whenStockMissing() {
        Optional<BigDecimal> result = controller.calculateBuyTotal("MISSING", new BigDecimal("1"));
        assertTrue(result.isEmpty());
    }

    @Test
    void calculateBuyTotal_returnsTotal_whenStockPresent() {
        Optional<BigDecimal> result = controller.calculateBuyTotal("A", new BigDecimal("2"));
        assertTrue(result.isPresent());
        // gross = 100 * 2 = 200, commission 0.5% -> 1.00, total = 201.00
        assertEquals(new BigDecimal("201.00"), result.get());
    }

    @Test
    void calculateSellTotal_empty_whenPlayerDoesNotOwnStock() {
        Optional<BigDecimal> result = controller.calculateSellTotal(exchange.getStocks().get(0), new BigDecimal("1"));
        assertTrue(result.isEmpty());
    }

    @Test
    void calculateSellTotal_returnsTotal_whenPlayerOwnsStock() {
        Stock s = exchange.getStocks().get(0);
        Share owned = new Share(s, new BigDecimal("4"), new BigDecimal("10.00"));
        player.getPortfolio().add(owned);
        Optional<BigDecimal> result = controller.calculateSellTotal(s, new BigDecimal("2"));
        assertTrue(result.isPresent());
        // gross = 200, commission = 2.00, costBasis = 20.00, profit = 178.00, tax = 53.40, total = 144.60
        assertEquals(new BigDecimal("144.60"), result.get());
    }

    @Test
    void getUnitSalePrice_returnsZero_forNullOrZeroQty() {
        Share share = new Share(exchange.getStocks().get(0), new BigDecimal("1"), new BigDecimal("100.00"));
        Purchase purchase = new Purchase(share, 1);
        assertEquals(BigDecimal.ZERO, controller.getUnitSalePrice(purchase, null));
        assertEquals(BigDecimal.ZERO, controller.getUnitSalePrice(purchase, BigDecimal.ZERO));
    }

    @Test
    void getUnitSalePrice_forPurchase_returnsPurchasePrice() {
        Share share = new Share(exchange.getStocks().get(0), new BigDecimal("3"), new BigDecimal("12.34"));
        Purchase purchase = new Purchase(share, 1);
        assertEquals(new BigDecimal("12.34"), controller.getUnitSalePrice(purchase, new BigDecimal("3")));
    }

    @Test
    void getUnitSalePrice_forSale_usesGrossDividedByQty() {
        Stock s = exchange.getStocks().get(0);
        // set purchase price lower so sale has non-zero tax maybe
        Share owned = new Share(s, new BigDecimal("5"), new BigDecimal("10.00"));
        // simulate owned share in portfolio
        player.getPortfolio().add(owned);

        Share toSell = new Share(s, new BigDecimal("2"), owned.getPurchasePrice());
        Sale sale = new Sale(toSell, 1);
        BigDecimal unit = controller.getUnitSalePrice(sale, new BigDecimal("2"));
        assertNotNull(unit);
        // Should equal gross/qty (salesPrice * qty / qty) -> salesPrice
        assertEquals(s.getSalesPrice().setScale(2, java.math.RoundingMode.HALF_UP), unit);
    }

    @Test
    void findStocks_withBounds_normalisesInvertedBounds() {
        // provide inverted bounds: min > max
        List<Stock> results = controller.findStocks("", new BigDecimal("60.00"), new BigDecimal("10.00"));
        // only stock with price between 10 and 60 is B (50)
        assertEquals(1, results.size());
        assertEquals("B", results.get(0).getSymbol());
    }

    @Test
    void getMarketMovers_combinesGainersAndLosers() {
        Stock up = new Stock("UP", "Up", new BigDecimal("10.00"));
        up.addPrice(new BigDecimal("20.00"));
        Stock down = new Stock("DOWN", "Down", new BigDecimal("10.00"));
        down.addPrice(new BigDecimal("5.00"));
        Exchange e = new Exchange("E", List.of(up, down));
        ExchangeController c = new ExchangeController(e, player);
        List<Stock> movers = c.getMarketMovers(1);
        // should contain one gainer and one loser
        assertEquals(2, movers.size());
        assertTrue(movers.stream().anyMatch(s -> s.getSymbol().equals("UP")));
        assertTrue(movers.stream().anyMatch(s -> s.getSymbol().equals("DOWN")));
    }

    @Test
    void transactionHistory_and_count_reflect_committed_transactions() throws Exception {
        // perform two purchases in different weeks
        controller.buy("A", new BigDecimal("1")); // week 1
        exchange.advance(); // week 2
        controller.buy("B", new BigDecimal("2")); // week 2

        List<Transaction> history = controller.getSortedTransactionHistory();
        assertEquals(2, history.size());
        // sorted by week descending -> first entry should be week 2
        assertEquals(2, history.get(0).getWeek());
        assertEquals(2, controller.getTransactionCount());
    }

    @Test
    void portfolioValue_and_ownedShare_lookup() throws Exception {
        controller.buy("A", new BigDecimal("3"));
        Stock s = exchange.getStocks().get(0);
        var owned = controller.getOwnedShare(s);
        assertTrue(owned.isPresent());
        BigDecimal expectedValue = s.getSalesPrice().multiply(new BigDecimal("3"));
        assertEquals(0, controller.getPortfolioItemValue(owned.get()).compareTo(expectedValue));
    }

    @Test
    void advance_appendsNetWorthSnapshot() {
        int before = controller.getNetWorthHistory().size();
        controller.advance();
        assertEquals(before + 1, controller.getNetWorthHistory().size());
        assertEquals(exchange.getWeek(), controller.getNetWorthHistory().getLast().week());
    }

    @Test
    void constructor_withSavedHistory_usesProvidedHistory() {
        var snapshot = new edu.ntnu.idi.idatt2003.millions.model.NetWorthSnapshot(1, new BigDecimal("9999"));
        ExchangeController c = new ExchangeController(exchange, player, List.of(snapshot));
        assertEquals(1, c.getNetWorthHistory().size());
        assertEquals(new BigDecimal("9999"), c.getNetWorthHistory().get(0).netWorth());
    }

    @Test
    void constructor_withNullSavedHistory_startsWithCurrentSnapshot() {
        ExchangeController c = new ExchangeController(exchange, player, null);

        assertEquals(1, c.getNetWorthHistory().size());
        assertEquals(exchange.getWeek(), c.getNetWorthHistory().get(0).week());
        assertEquals(player.getNetWorth(), c.getNetWorthHistory().get(0).netWorth());
    }

    @Test
    void findStocks_withNoPriceBounds_returnsBaseSearchResult() {
        List<Stock> fromBase = controller.findStocks("A");
        List<Stock> withNullBounds = controller.findStocks("A", null, null);

        assertEquals(fromBase, withNullBounds);
    }

    @Test
    void getExchange_and_getPlayer_returnProvidedInstances() {
        assertSame(exchange, controller.getExchange());
        assertSame(player, controller.getPlayer());
    }

    @Test
    void sell_delegatesToExchange() throws Exception {
        controller.buy("A", new BigDecimal("2"));
        Stock s = exchange.getStocks().stream().filter(st -> st.getSymbol().equals("A")).findFirst().orElseThrow();
        Share share = controller.getOwnedShare(s).orElseThrow();
        controller.sell(share, share.getQuantity());
        assertTrue(controller.getPortfolioShares().isEmpty());
    }

    @Test
    void saveGame_withNullCallbacks_doesNotThrow() {
        FxTestUtils.initToolkit();
        assertDoesNotThrow(() -> controller.saveGame(null, null));
    }

    @Test
    void saveGame_acceptsCallbacks_withoutThrowing() {
        FxTestUtils.initToolkit();

        assertDoesNotThrow(() -> controller.saveGame(id -> { }, error -> { }));
    }
}
