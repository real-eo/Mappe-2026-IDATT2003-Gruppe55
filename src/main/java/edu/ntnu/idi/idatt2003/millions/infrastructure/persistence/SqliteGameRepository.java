package edu.ntnu.idi.idatt2003.millions.infrastructure.persistence;

import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.GameState;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Purchase;
import edu.ntnu.idi.idatt2003.millions.model.Sale;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.Transaction;
import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.GameSaveSummary;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * SQLite implementation of the game repository.
 */
public class SqliteGameRepository implements GameRepository {

    private static final String CREATE_GAME_SAVE_TABLE = """
            CREATE TABLE IF NOT EXISTS game_save (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                label TEXT NOT NULL,
                exchange_name TEXT NOT NULL,
                week INTEGER NOT NULL,
                created_at TEXT NOT NULL
            )
            """;

    private static final String CREATE_PLAYER_TABLE = """
            CREATE TABLE IF NOT EXISTS player (
                save_id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                starting_money TEXT NOT NULL,
                money TEXT NOT NULL,
                FOREIGN KEY(save_id) REFERENCES game_save(id) ON DELETE CASCADE
            )
            """;

    private static final String CREATE_STOCK_TABLE = """
            CREATE TABLE IF NOT EXISTS stock (
                save_id INTEGER NOT NULL,
                symbol TEXT NOT NULL,
                company_name TEXT NOT NULL,
                PRIMARY KEY (save_id, symbol),
                FOREIGN KEY(save_id) REFERENCES game_save(id) ON DELETE CASCADE
            )
            """;

    private static final String CREATE_STOCK_PRICE_TABLE = """
            CREATE TABLE IF NOT EXISTS stock_price (
                save_id INTEGER NOT NULL,
                symbol TEXT NOT NULL,
                position INTEGER NOT NULL,
                price TEXT NOT NULL,
                PRIMARY KEY (save_id, symbol, position),
                FOREIGN KEY(save_id, symbol) REFERENCES stock(save_id, symbol) ON DELETE CASCADE
            )
            """;

    private static final String CREATE_PORTFOLIO_SHARE_TABLE = """
            CREATE TABLE IF NOT EXISTS portfolio_share (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                save_id INTEGER NOT NULL,
                symbol TEXT NOT NULL,
                quantity TEXT NOT NULL,
                purchase_price TEXT NOT NULL,
                FOREIGN KEY(save_id, symbol) REFERENCES stock(save_id, symbol) ON DELETE CASCADE
            )
            """;

    private static final String CREATE_TRANSACTION_TABLE = """
            CREATE TABLE IF NOT EXISTS transaction_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                save_id INTEGER NOT NULL,
                type TEXT NOT NULL,
                symbol TEXT NOT NULL,
                quantity TEXT NOT NULL,
                purchase_price TEXT NOT NULL,
                week INTEGER NOT NULL,
                FOREIGN KEY(save_id, symbol) REFERENCES stock(save_id, symbol) ON DELETE CASCADE
            )
            """;

    private final String jdbcUrl;

    /**
     * Constructs a repository backed by the provided database path.
     *
     * @param databasePath location of the SQLite database file
     */
    public SqliteGameRepository(Path databasePath) {
        Objects.requireNonNull(databasePath, "databasePath must not be null");
        this.jdbcUrl = "jdbc:sqlite:" + databasePath.toAbsolutePath();
    }

    @Override
    public void initialize() throws SQLException {
        try (Connection connection = openConnection()) {
            ensureSchema(connection);
        }
    }

    @Override
    public long save(GameState state) throws SQLException {
        Objects.requireNonNull(state, "state must not be null");
        Player player = state.getPlayer();
        Exchange exchange = state.getExchange();
        List<Stock> stocks = exchange.getStocks();

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            ensureSchema(connection);

            long saveId = insertSave(connection, player.getName(), exchange.getName(), exchange.getWeek());
            insertPlayer(connection, saveId, player);
            insertStocks(connection, saveId, stocks);
            insertStockPrices(connection, saveId, stocks);
            insertPortfolioShares(connection, saveId, player.getPortfolio().getShares());
            insertTransactions(connection, saveId, player.getTransactionArchive().getTransactions());

            connection.commit();
            return saveId;
        }
    }

    @Override
    public List<GameSaveSummary> listSaves() throws SQLException {
        try (Connection connection = openConnection()) {
            ensureSchema(connection);

            List<GameSaveSummary> saves = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, label, exchange_name, week, created_at"
                            + " FROM game_save ORDER BY created_at DESC")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        long id = resultSet.getLong("id");
                        String label = resultSet.getString("label");
                        String exchangeName = resultSet.getString("exchange_name");
                        int week = resultSet.getInt("week");
                        String createdAt = resultSet.getString("created_at");
                        Instant timestamp = parseInstant(createdAt);
                        saves.add(new GameSaveSummary(id, label, exchangeName, week, timestamp));
                    }
                }
            }
            return saves;
        }
    }

    @Override
    public Optional<GameState> load(long saveId) throws SQLException {
        try (Connection connection = openConnection()) {
            ensureSchema(connection);

            SaveRecord record = loadSaveRecord(connection, saveId);
            if (record == null) {
                return Optional.empty();
            }

            Map<String, Stock> stocks = loadStocks(connection, saveId);
            Exchange exchange = new Exchange(record.exchangeName(),
                    new ArrayList<>(stocks.values()),
                    new Random(),
                    record.week());

            Player player = loadPlayer(connection, saveId);
            populatePortfolio(connection, saveId, player, stocks);
            populateTransactions(connection, saveId, player, stocks);

            return Optional.of(new GameState(exchange, player));
        }
    }

    private Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    private void ensureSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_GAME_SAVE_TABLE);
            statement.execute(CREATE_PLAYER_TABLE);
            statement.execute(CREATE_STOCK_TABLE);
            statement.execute(CREATE_STOCK_PRICE_TABLE);
            statement.execute(CREATE_PORTFOLIO_SHARE_TABLE);
            statement.execute(CREATE_TRANSACTION_TABLE);
        }
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return Instant.EPOCH;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            return Instant.EPOCH;
        }
    }

    private long insertSave(Connection connection, String label, String exchangeName, int week)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO game_save (label, exchange_name, week, created_at) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, label);
            statement.setString(2, exchangeName);
            statement.setInt(3, week);
            statement.setString(4, Instant.now().toString());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to store game save metadata");
    }

    private void insertPlayer(Connection connection, long saveId, Player player) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO player (save_id, name, starting_money, money) VALUES (?, ?, ?, ?)")) {
            statement.setLong(1, saveId);
            statement.setString(2, player.getName());
            statement.setString(3, player.getStartingMoney().toPlainString());
            statement.setString(4, player.getMoney().toPlainString());
            statement.executeUpdate();
        }
    }

    private void insertStocks(Connection connection, long saveId, List<Stock> stocks) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO stock (save_id, symbol, company_name) VALUES (?, ?, ?)")) {
            for (Stock stock : stocks) {
                statement.setLong(1, saveId);
                statement.setString(2, stock.getSymbol());
                statement.setString(3, stock.getCompanyName());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertStockPrices(Connection connection, long saveId, List<Stock> stocks) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO stock_price (save_id, symbol, position, price) VALUES (?, ?, ?, ?)")) {
            for (Stock stock : stocks) {
                int position = 0;
                for (BigDecimal price : stock.getPrices()) {
                    statement.setLong(1, saveId);
                    statement.setString(2, stock.getSymbol());
                    statement.setInt(3, position++);
                    statement.setString(4, price.toPlainString());
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        }
    }

    private void insertPortfolioShares(Connection connection, long saveId, List<Share> shares) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO portfolio_share (save_id, symbol, quantity, purchase_price) VALUES (?, ?, ?, ?)")) {
            for (Share share : shares) {
                statement.setLong(1, saveId);
                statement.setString(2, share.getStock().getSymbol());
                statement.setString(3, share.getQuantity().toPlainString());
                statement.setString(4, share.getPurchasePrice().toPlainString());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertTransactions(Connection connection, long saveId, List<Transaction> transactions)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO transaction_log (save_id, type, symbol, quantity, purchase_price, week)"
                        + " VALUES (?, ?, ?, ?, ?, ?)")) {
            for (Transaction transaction : transactions) {
                String type = resolveTransactionType(transaction);
                statement.setLong(1, saveId);
                statement.setString(2, type);
                statement.setString(3, transaction.getShare().getStock().getSymbol());
                statement.setString(4, transaction.getShare().getQuantity().toPlainString());
                statement.setString(5, transaction.getShare().getPurchasePrice().toPlainString());
                statement.setInt(6, transaction.getWeek());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private SaveRecord loadSaveRecord(Connection connection, long saveId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT exchange_name, week FROM game_save WHERE id = ?")) {
            statement.setLong(1, saveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new SaveRecord(resultSet.getString("exchange_name"),
                        resultSet.getInt("week"));
            }
        }
    }

    private Player loadPlayer(Connection connection, long saveId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT name, starting_money, money FROM player WHERE save_id = ?")) {
            statement.setLong(1, saveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Missing player for save " + saveId);
                }

                String name = resultSet.getString("name");
                BigDecimal startingMoney = new BigDecimal(resultSet.getString("starting_money"));
                BigDecimal money = new BigDecimal(resultSet.getString("money"));

                Player player = new Player(name, startingMoney);
                int comparison = money.compareTo(startingMoney);
                if (comparison > 0) {
                    player.addMoney(money.subtract(startingMoney));
                } else if (comparison < 0) {
                    player.withdrawMoney(startingMoney.subtract(money));
                }
                return player;
            }
        }
    }

    private Map<String, Stock> loadStocks(Connection connection, long saveId) throws SQLException {
        Map<String, String> companies = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT symbol, company_name FROM stock WHERE save_id = ?")) {
            statement.setLong(1, saveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    companies.put(resultSet.getString("symbol"),
                            resultSet.getString("company_name"));
                }
            }
        }

        Map<String, List<BigDecimal>> prices = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT symbol, position, price FROM stock_price WHERE save_id = ?"
                        + " ORDER BY symbol, position")) {
            statement.setLong(1, saveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String symbol = resultSet.getString("symbol");
                    BigDecimal price = new BigDecimal(resultSet.getString("price"));
                    prices.computeIfAbsent(symbol, key -> new ArrayList<>()).add(price);
                }
            }
        }

        Map<String, Stock> stocks = new HashMap<>();
        for (Map.Entry<String, String> entry : companies.entrySet()) {
            String symbol = entry.getKey();
            List<BigDecimal> priceList = prices.get(symbol);
            if (priceList == null || priceList.isEmpty()) {
                throw new SQLException("Missing prices for stock " + symbol);
            }
            Stock stock = new Stock(symbol, entry.getValue(), priceList.get(0));
            for (int i = 1; i < priceList.size(); i++) {
                stock.addPrice(priceList.get(i));
            }
            stocks.put(symbol, stock);
        }

        return stocks;
    }

    private void populatePortfolio(Connection connection,
                                   long saveId,
                                   Player player,
                                   Map<String, Stock> stocks) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT symbol, quantity, purchase_price FROM portfolio_share"
                        + " WHERE save_id = ? ORDER BY id")) {
            statement.setLong(1, saveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String symbol = resultSet.getString("symbol");
                    Stock stock = requireStock(stocks, symbol);
                    BigDecimal quantity = new BigDecimal(resultSet.getString("quantity"));
                    BigDecimal purchasePrice = new BigDecimal(resultSet.getString("purchase_price"));
                    player.getPortfolio().add(new Share(stock, quantity, purchasePrice));
                }
            }
        }
    }

    private void populateTransactions(Connection connection,
                                      long saveId,
                                      Player player,
                                      Map<String, Stock> stocks) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT type, symbol, quantity, purchase_price, week FROM transaction_log"
                        + " WHERE save_id = ? ORDER BY id")) {
            statement.setLong(1, saveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String type = resultSet.getString("type");
                    String symbol = resultSet.getString("symbol");
                    int week = resultSet.getInt("week");
                    BigDecimal quantity = new BigDecimal(resultSet.getString("quantity"));
                    BigDecimal purchasePrice = new BigDecimal(resultSet.getString("purchase_price"));
                    Stock stock = requireStock(stocks, symbol);
                    Share share = new Share(stock, quantity, purchasePrice);

                    Transaction transaction = createArchivedTransaction(type, share, week);
                    player.getTransactionArchive().add(transaction);
                }
            }
        }
    }

    private Stock requireStock(Map<String, Stock> stocks, String symbol) throws SQLException {
        Stock stock = stocks.get(symbol);
        if (stock == null) {
            throw new SQLException("Missing stock " + symbol + " in save data");
        }
        return stock;
    }

    private String resolveTransactionType(Transaction transaction) {
        if (transaction instanceof Purchase) {
            return "PURCHASE";
        }
        if (transaction instanceof Sale) {
            return "SALE";
        }
        return "UNKNOWN";
    }

    private Transaction createArchivedTransaction(String type, Share share, int week) throws SQLException {
        String normalized = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "PURCHASE" -> new ArchivedPurchase(share, week);
            case "SALE" -> new ArchivedSale(share, week);
            default -> throw new SQLException("Unsupported transaction type: " + type);
        };
    }

    private record SaveRecord(String exchangeName, int week) {
    }

    private static final class ArchivedPurchase extends Purchase {

        private ArchivedPurchase(Share share, int week) {
            super(share, week);
            markCommitted();
        }
    }

    private static final class ArchivedSale extends Sale {

        private ArchivedSale(Share share, int week) {
            super(share, week);
            markCommitted();
        }
    }
}
