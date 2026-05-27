package edu.ntnu.idi.idatt2003.millions.domain.model;

import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.InvalidQuantityException;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.StockNotFoundException;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExchangeTest {

    private Exchange exchange;
    private Player player;

    @BeforeEach
    void setUp() {
        List<Stock> stocks = List.of(
                new Stock("EQNR", "Equinor", new BigDecimal("100.00")),
                new Stock("DNB", "DNB", new BigDecimal("200.00"))
        );
        exchange = new Exchange("Domain Exchange", stocks, new Random(42));
        player = new Player("Alice", new BigDecimal("5000.00"));
    }

    @Test
    void constructor_throws_when_name_is_blank() {
        List<Stock> stocks = List.of(new Stock("A", "A", new BigDecimal("1.00")));
        assertThrows(IllegalArgumentException.class, () -> new Exchange(" ", stocks));
    }

    @Test
    void constructor_throws_when_stocks_is_null() {
        assertThrows(IllegalArgumentException.class, () -> new Exchange("E", null));
    }

    @Test
    void getStock_and_hasStock_work_for_existing_symbol() throws Exception {
        assertTrue(exchange.hasStock("EQNR"));
        assertEquals("EQNR", exchange.getStock("EQNR").getSymbol());
    }

    @Test
    void getStock_throws_for_missing_symbol() {
        assertThrows(StockNotFoundException.class, () -> exchange.getStock("MISSING"));
    }

    @Test
    void buy_throws_for_non_positive_quantity() {
        assertThrows(InvalidQuantityException.class, () -> exchange.buy(player, "EQNR", BigDecimal.ZERO));
        assertThrows(InvalidQuantityException.class, () -> exchange.buy(player, "EQNR", new BigDecimal("-1")));
    }

    @Test
    void sell_throws_for_non_positive_quantity() {
        Share share = new Share(new Stock("X", "X", new BigDecimal("10.00")), new BigDecimal("1"), new BigDecimal("10.00"));
        assertThrows(InvalidQuantityException.class, () -> exchange.sell(player, share, BigDecimal.ZERO));
    }

    @Test
    void findStocks_filters_by_symbol_or_company_case_insensitive() {
        assertEquals(1, exchange.findStocks("eqnr").size());
        assertEquals(1, exchange.findStocks("dnb").size());
        assertEquals(2, exchange.findStocks("").size());
    }

    @Test
    void findStocks_returnsAll_whenKeywordIsNull() {
        assertEquals(2, exchange.findStocks(null).size());
    }

    @Test
    void gainers_and_losers_return_empty_for_non_positive_limit() {
        assertTrue(exchange.getGainers(0).isEmpty());
        assertTrue(exchange.getLosers(-1).isEmpty());
    }

    @Test
    void addStock_addsNewStockToExchange() {
        Stock newStock = new Stock("NHY", "Norsk Hydro", new BigDecimal("60.00"));
        exchange.addStock(newStock);
        assertTrue(exchange.hasStock("NHY"));
    }

    @Test
    void addStock_throwsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> exchange.addStock(null));
    }

    @Test
    void constructor_ignoresNullStocksInInputList() {
        Stock eqnr = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));

        List<Stock> input = new ArrayList<>();
        input.add(eqnr);
        input.add(null);

        Exchange built = new Exchange("OSE", input, new Random(0));

        assertTrue(built.hasStock("EQNR"));
        assertEquals(1, built.findStocks(null).size());
    }

    @Test
    void sell_wholeShare_overload_sellsEntireQuantity() throws Exception {
        exchange.buy(player, "EQNR", new BigDecimal("2"));
        Share share = exchange.getStock("EQNR")
                .equals(player.getPortfolio().getShares().get(0).getStock())
                ? player.getPortfolio().getShares().get(0)
                : player.getPortfolio().getShares().get(0);
        exchange.sell(player, share);
        assertTrue(player.getPortfolio().getShares().isEmpty());
    }

    @Test
    void advance_increments_week_and_updates_price_history() throws Exception {
        Stock eqnr = exchange.getStock("EQNR");
        int sizeBefore = eqnr.getPrices().size();

        exchange.advance();

        assertEquals(2, exchange.getWeek());
        assertEquals(sizeBefore + 1, eqnr.getPrices().size());
        assertFalse(eqnr.getSalesPrice().compareTo(BigDecimal.ZERO) <= 0);
    }
}
