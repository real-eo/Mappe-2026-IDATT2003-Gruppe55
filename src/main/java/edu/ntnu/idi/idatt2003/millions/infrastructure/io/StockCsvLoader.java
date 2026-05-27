package edu.ntnu.idi.idatt2003.millions.infrastructure.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import edu.ntnu.idi.idatt2003.millions.model.Stock;

/**
 * Loads stocks from CSV data on the format: symbol,name,price.
 *
 * <p>Lines that are empty or start with '#' are ignored.</p>
 */
public class StockCsvLoader {

    /**
     * Creates a CSV stock loader.
     */
    public StockCsvLoader() {
    }

    /**
     * Loads stocks from a CSV file on disk.
     *
     * @param path path to CSV file
     * @return list of parsed stocks
     * @throws IOException if reading fails
     */
    public List<Stock> loadFromPath(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return parse(reader);
        }
    }

    /**
     * Loads stocks from a classpath resource.
     *
     * @param resourcePath path inside resources, e.g. data/sp500.csv
     * @return list of parsed stocks
     * @throws IOException if reading fails
     */
    public List<Stock> loadFromResource(String resourcePath) throws IOException {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("Resource path must not be blank");
        }

        InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return parse(reader);
        }
    }

    private List<Stock> parse(BufferedReader reader) throws IOException {
        List<Stock> stocks = new ArrayList<>();
        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            String trimmed = line.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            stocks.add(parseStockLine(trimmed, lineNumber));
        }

        return List.copyOf(stocks);
    }

    private Stock parseStockLine(String line, int lineNumber) {
        int firstComma = line.indexOf(',');
        int lastComma = line.lastIndexOf(',');

        if (firstComma < 0 || lastComma <= firstComma) {
            throw new IllegalArgumentException("Invalid stock line at " + lineNumber + ": " + line);
        }

        String symbol = line.substring(0, firstComma).trim();
        String name = line.substring(firstComma + 1, lastComma).trim();
        String priceText = line.substring(lastComma + 1).trim();

        if (symbol.isEmpty() || name.isEmpty() || priceText.isEmpty()) {
            throw new IllegalArgumentException("Invalid stock line at " + lineNumber + ": " + line);
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceText);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid price at line " + lineNumber + ": " + priceText,
                    exception
            );
        }

        return new Stock(symbol, name, price);
    }
}

