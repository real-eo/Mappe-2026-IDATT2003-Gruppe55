package edu.ntnu.idi.idatt2003.millions.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TransactionArchiveTest {

    private Exchange exchange;
    private Player player;

    @BeforeEach
    void setUp() {
        Stock EQNR = new Stock("EQNR", "Equinor ASA", new BigDecimal("100.00"));
        Stock DNB = new Stock("DNB", "DNB Bank", new BigDecimal("200.00"));

        exchange = new Exchange("Test Exchange", List.of(EQNR, DNB), new Random(0));
        player = new Player("Alice", new BigDecimal("1000000"));
    }

    @Test
    void add_null_throws() {
        TransactionArchive archive = new TransactionArchive();
        assertThrows(IllegalArgumentException.class, () -> archive.add(null));
    }

    @Test
    void add_and_query_purchases_and_sales() {
        TransactionArchive archive = new TransactionArchive();

        Stock s1 = new Stock("AAA", "A Corp", new BigDecimal("10.00"));
        Stock s2 = new Stock("BBB", "B Corp", new BigDecimal("20.00"));

        Share share1 = new Share(s1, new BigDecimal("5"), s1.getSalesPrice());
        Share share2 = new Share(s2, new BigDecimal("3"), s2.getSalesPrice());

        Purchase p1 = new Purchase(share1, 1);
        Sale s = new Sale(share2, 2);
        Purchase p2 = new Purchase(share2, 2);

        assertTrue(archive.isEmpty());

        archive.add(p1);
        archive.add(s);
        archive.add(p2);

        assertFalse(archive.isEmpty());

        List<Purchase> week1Purchases = archive.getPurchases(1);
        assertEquals(1, week1Purchases.size());

        List<Sale> week2Sales = archive.getSales(2);
        assertEquals(1, week2Sales.size());

        assertEquals(2, archive.countDistinctWeeks());

        // total purchased quantity = p1(5) + p2(3) = 8
        assertEquals(new BigDecimal("8"), archive.getTotalPurchasedQuantity());

        // total sold quantity = s(3)
        assertEquals(new BigDecimal("3"), archive.getTotalSoldQuantity());
    }

    @Test
    void getTransactions_returns_unmodifiable_list() {
        TransactionArchive archive = new TransactionArchive();
        Stock s = new Stock("X", "X Corp", new BigDecimal("1.00"));
        Purchase p = new Purchase(new Share(s, new BigDecimal("1"), s.getSalesPrice()), 1);
        archive.add(p);
        List<Transaction> list = archive.getTransactions();
        assertThrows(UnsupportedOperationException.class, () -> list.add(p));
    }

    @Test
    void countDistinctWeeks_isZero_whenNoTransactions() {
        TransactionArchive archive = new TransactionArchive();
        assertEquals(0, archive.countDistinctWeeks());
    }

    @Test
    void countDistinctWeeks_countsMultipleTransactionsInSameWeek_asOne() throws Exception {
        exchange.buy(player, "EQNR", new BigDecimal("10")); // week 1
        exchange.buy(player, "DNB", new BigDecimal("5"));   // week 1
        assertEquals(1, player.getTransactionArchive().countDistinctWeeks());
    }

    @Test
    void countDistinctWeeks_countsDifferentWeeks_separately() throws Exception {
        exchange.buy(player, "EQNR", new BigDecimal("10")); // week 1
        exchange.advance();               // -> week 2
        exchange.buy(player, "DNB", new BigDecimal("5"));   // week 2
        assertEquals(2, player.getTransactionArchive().countDistinctWeeks());
    }

    @Test
    void archive_storesTransactionsInOrder() throws Exception {
        exchange.buy(player, "EQNR", new BigDecimal("10"));
        var share = player.getPortfolio().findByStock(exchange.getStock("EQNR")).orElseThrow();
        exchange.sell(player, share);
        assertEquals(2, player.getTransactionArchive().getTransactions().size());

        // First transaction should be a Purchase committed in week 1
        assertEquals(1, player.getTransactionArchive().getTransactions().get(0).getWeek());
    }

    @Test
    void getPurchases_and_getSales_returnEmpty_whenNoMatches() {
        TransactionArchive archive = new TransactionArchive();
        assertTrue(archive.getPurchases(99).isEmpty());
        assertTrue(archive.getSales(99).isEmpty());
    }

    @Test
    void totalQuantities_areZero_whenArchiveEmpty() {
        TransactionArchive archive = new TransactionArchive();
        assertEquals(BigDecimal.ZERO, archive.getTotalPurchasedQuantity());
        assertEquals(BigDecimal.ZERO, archive.getTotalSoldQuantity());
    }
    
    @Test
    void getPurchases_ignoresSalesFromSameWeek() {
        TransactionArchive archive = new TransactionArchive();
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100"));

        archive.add(new Sale(new Share(stock, new BigDecimal("2"), new BigDecimal("100")), 3));

        assertTrue(archive.getPurchases(3).isEmpty());
    }

    @Test
    void getSales_ignoresPurchasesFromSameWeek() {
        TransactionArchive archive = new TransactionArchive();
        Stock stock = new Stock("EQNR", "Equinor", new BigDecimal("100"));

        archive.add(new Purchase(new Share(stock, new BigDecimal("2"), new BigDecimal("100")), 3));

        assertTrue(archive.getSales(3).isEmpty());
    }
}
