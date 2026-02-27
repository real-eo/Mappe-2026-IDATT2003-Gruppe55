package edu.ntnu.idi.idatt2003.millions;

import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.TransactionArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
        exchange = new Exchange("Test Exchange", new Random(0));
        exchange.addStock(new Stock("EQNR", "Equinor ASA", new BigDecimal("100.00")));
        exchange.addStock(new Stock("DNB", "DNB Bank", new BigDecimal("200.00")));
        player = new Player("Alice", new BigDecimal("1000000"));
    }

    @Test
    void countDistinctWeeks_isZero_whenNoTransactions() {
        TransactionArchive archive = new TransactionArchive();
        assertEquals(0, archive.countDistinctWeeks());
    }

    @Test
    void countDistinctWeeks_countsMultipleTransactionsInSameWeek_asOne() throws Exception {
        exchange.buy(player, "EQNR", 10); // week 1
        exchange.buy(player, "DNB", 5);   // week 1
        assertEquals(1, player.getTransactionArchive().countDistinctWeeks());
    }

    @Test
    void countDistinctWeeks_countsDifferentWeeks_separately() throws Exception {
        exchange.buy(player, "EQNR", 10); // week 1
        exchange.advance();               // → week 2
        exchange.buy(player, "DNB", 5);   // week 2
        assertEquals(2, player.getTransactionArchive().countDistinctWeeks());
    }

    @Test
    void archive_storesTransactionsInOrder() throws Exception {
        exchange.buy(player, "EQNR", 10);
        exchange.sell(player, "EQNR", 10);
        assertEquals(2, player.getTransactionArchive().getTransactions().size());
    }
}
