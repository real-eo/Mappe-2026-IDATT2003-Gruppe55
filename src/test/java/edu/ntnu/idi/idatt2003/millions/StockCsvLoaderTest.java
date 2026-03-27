package edu.ntnu.idi.idatt2003.millions;

import edu.ntnu.idi.idatt2003.millions.model.Stock;
import edu.ntnu.idi.idatt2003.millions.model.io.StockCsvLoader;
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
}
