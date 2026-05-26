package edu.ntnu.idi.idatt2003.millions.infrastructure.persistence;

import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.GameState;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SqliteGameRepositoryTest {

    @Test
    void save_list_and_load_roundtrip_preservesCoreState() throws Exception {
        Path db = Files.createTempFile("millions-test-", ".db");
        try {
            SqliteGameRepository repository = new SqliteGameRepository(db);
            repository.initialize();

            Stock stock = new Stock("EQ", "Equity", new BigDecimal("100.00"));
            stock.addPrice(new BigDecimal("110.00"));

            Exchange exchange = new Exchange("E", List.of(stock));
            Player player = new Player("Alice", new BigDecimal("1000.00"));
            player.withdrawMoney(new BigDecimal("100.00"));
            player.getPortfolio().add(new Share(stock, new BigDecimal("2"), new BigDecimal("95.00")));

            long id = repository.save(new GameState(exchange, player));
            assertTrue(id > 0);

            List<GameSaveSummary> saves = repository.listSaves();
            assertFalse(saves.isEmpty());
            assertEquals(id, saves.get(0).id());

            Optional<GameState> loadedOpt = repository.load(id);
            assertTrue(loadedOpt.isPresent());

            GameState loaded = loadedOpt.get();
            assertEquals("E", loaded.getExchange().getName());
            assertEquals(exchange.getWeek(), loaded.getExchange().getWeek());
            assertEquals("Alice", loaded.getPlayer().getName());
            assertEquals(player.getMoney(), loaded.getPlayer().getMoney());
            assertEquals(1, loaded.getPlayer().getPortfolio().getShares().size());
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void load_returnsEmpty_whenSaveIdMissing() throws Exception {
        Path db = Files.createTempFile("millions-test-", ".db");
        try {
            SqliteGameRepository repository = new SqliteGameRepository(db);
            repository.initialize();
            Optional<GameState> loaded = repository.load(999_999L);
            assertTrue(loaded.isEmpty());
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void load_throws_whenUnsupportedTransactionType_presentInDatabase() throws Exception {
        Path db = Files.createTempFile("millions-test-", ".db");
        String jdbcUrl = "jdbc:sqlite:" + db.toAbsolutePath();
        try {
            SqliteGameRepository repository = new SqliteGameRepository(db);
            repository.initialize();

            // Seed minimal valid save + player + stock + stock price + invalid transaction type.
            try (Connection c = DriverManager.getConnection(jdbcUrl);
                 Statement s = c.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON");
                s.execute("INSERT INTO game_save (id, label, exchange_name, week, created_at) VALUES (1, 'L', 'EX', 1, '2026-01-01T00:00:00Z')");
                s.execute("INSERT INTO player (save_id, name, starting_money, money) VALUES (1, 'P', '100.00', '100.00')");
                s.execute("INSERT INTO stock (save_id, symbol, company_name) VALUES (1, 'ABC', 'ABC Inc')");
                s.execute("INSERT INTO stock_price (save_id, symbol, position, price) VALUES (1, 'ABC', 0, '10.00')");
                s.execute("INSERT INTO transaction_log (save_id, type, symbol, quantity, purchase_price, week) VALUES (1, 'MYSTERY', 'ABC', '1', '9.00', 1)");
            }

            Exception ex = assertThrows(Exception.class, () -> repository.load(1));
            assertTrue(ex.getMessage().contains("Unsupported transaction type"));
        } finally {
            Files.deleteIfExists(db);
        }
    }
}
