package edu.ntnu.idi.idatt2003.millions.infrastructure.io;

import edu.ntnu.idi.idatt2003.millions.infrastructure.io.StockCsvLoader;
import edu.ntnu.idi.idatt2003.millions.model.Stock;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockCsvLoaderTest {

    @Test
    void loadFromPath_ignoresCommentsAndBlankLines() throws IOException {
        Path csv = Files.createTempFile("stocks-", ".csv");
        Files.writeString(csv, String.join("\n",
                "# comment",
                "",
                "AAPL,Apple Inc.,276.43",
                "GOOG,Alphabet Inc. (Class C),311.62"
        ), StandardCharsets.UTF_8);

        StockCsvLoader loader = new StockCsvLoader();
        List<Stock> stocks = loader.loadFromPath(csv);

        assertEquals(2, stocks.size());
        assertEquals("AAPL", stocks.get(0).getSymbol());
        assertEquals("GOOG", stocks.get(1).getSymbol());
    }

    @Test
    void loadFromPath_supportsCommaInName() throws IOException {
        Path csv = Files.createTempFile("stocks-name-comma-", ".csv");
        Files.writeString(csv,
                "BRK.B,Berkshire Hathaway, Inc.,501.05\n",
                StandardCharsets.UTF_8);

        StockCsvLoader loader = new StockCsvLoader();
        List<Stock> stocks = loader.loadFromPath(csv);

        assertEquals(1, stocks.size());
        assertEquals("Berkshire Hathaway, Inc.", stocks.get(0).getCompanyName());
    }

    @Test
    void loadFromPath_throwsOnInvalidLineFormat() throws IOException {
        Path csv = Files.createTempFile("stocks-invalid-", ".csv");
        Files.writeString(csv,
                "AAPL,Apple Inc.\n",
                StandardCharsets.UTF_8);

        StockCsvLoader loader = new StockCsvLoader();

        assertThrows(IllegalArgumentException.class, () -> loader.loadFromPath(csv));
    }

    @Test
    void loadFromResource_loadsSp500Csv() throws IOException {
        StockCsvLoader loader = new StockCsvLoader();
        List<Stock> stocks = loader.loadFromResource("data/sp500.csv");

        assertTrue(stocks.size() > 100);
    }

    @Test
    void loadFromPath_throwsOnNullPath() {
        StockCsvLoader loader = new StockCsvLoader();
        assertThrows(IllegalArgumentException.class, () -> loader.loadFromPath(null));
    }

    @Test
    void loadFromResource_throwsOnBlankPath() {
        StockCsvLoader loader = new StockCsvLoader();
        assertThrows(IllegalArgumentException.class, () -> loader.loadFromResource(""));
        assertThrows(IllegalArgumentException.class, () -> loader.loadFromResource(null));
    }

    @Test
    void loadFromResource_throwsOnMissingResource() {
        StockCsvLoader loader = new StockCsvLoader();
        assertThrows(IllegalArgumentException.class, () -> loader.loadFromResource("nonexistent.csv"));
    }

    @Test
    void loadFromPath_throwsOnInvalidPrice() throws IOException {
        Path csv = Files.createTempFile("stocks-bad-price-", ".csv");
        Files.writeString(csv, "AAPL,Apple Inc.,not-a-number\n", StandardCharsets.UTF_8);
        StockCsvLoader loader = new StockCsvLoader();
        assertThrows(IllegalArgumentException.class, () -> loader.loadFromPath(csv));
    }

    @Test
    void loadFromPath_ignoresTrailingBlankLines_withWindowsLineEndings() throws IOException {
        Path csv = Files.createTempFile("stocks-windows-eol-", ".csv");
        Files.writeString(csv,
                "AAPL,Apple Inc.,100.00\r\n\r\nMSFT,Microsoft,200.00\r\n\r\n",
                StandardCharsets.UTF_8);

        StockCsvLoader loader = new StockCsvLoader();
        List<Stock> stocks = loader.loadFromPath(csv);

        assertEquals(2, stocks.size());
        assertEquals("AAPL", stocks.get(0).getSymbol());
        assertEquals("MSFT", stocks.get(1).getSymbol());
    }

    @Test
    void loadFromPath_throwsOnMissingRequiredFields() throws IOException {
        Path csv = Files.createTempFile("stocks-missing-fields-", ".csv");
        Files.writeString(csv, "AAPL,,100.00\n", StandardCharsets.UTF_8);

        StockCsvLoader loader = new StockCsvLoader();
        assertThrows(IllegalArgumentException.class, () -> loader.loadFromPath(csv));
    }

    @Test
    void loadFromPath_returnsEmptyList_whenAllLinesAreComments() throws IOException {
        Path csv = Files.createTempFile("stocks-all-comments-", ".csv");
        Files.writeString(csv, "# header\n# another comment\n\n", StandardCharsets.UTF_8);

        List<Stock> stocks = new StockCsvLoader().loadFromPath(csv);

        assertTrue(stocks.isEmpty());
    }

    @Test
    void loadFromPath_trimsWhitespace_fromSymbolAndName() throws IOException {
        Path csv = Files.createTempFile("stocks-whitespace-", ".csv");
        Files.writeString(csv, "  AAPL  ,  Apple Inc.  ,  150.00  \n", StandardCharsets.UTF_8);

        List<Stock> stocks = new StockCsvLoader().loadFromPath(csv);

        assertEquals(1, stocks.size());
        assertEquals("AAPL", stocks.get(0).getSymbol());
        assertEquals("Apple Inc.", stocks.get(0).getCompanyName());
    }

    @Test
    void loadFromPath_throwsOnEmptyPrice() throws IOException {
        Path csv = Files.createTempFile("stocks-empty-price-", ".csv");
        Files.writeString(csv, "AAPL,Apple Inc.,\n", StandardCharsets.UTF_8);

        assertThrows(IllegalArgumentException.class,
                () -> new StockCsvLoader().loadFromPath(csv));
    }

    @Test
    void loadFromPath_throwsOnNonExistentFile() {
        Path nonExistent = Path.of("does-not-exist-12345.csv");
        assertThrows(IOException.class,
                () -> new StockCsvLoader().loadFromPath(nonExistent));
    }
}
