package edu.ntnu.idi.idatt2003.millions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.TransactionArchive;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TransactionArchive#countDistinctWeeks()}.
 */
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
    }
}

