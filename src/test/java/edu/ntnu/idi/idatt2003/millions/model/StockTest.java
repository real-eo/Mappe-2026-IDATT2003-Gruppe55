package edu.ntnu.idi.idatt2003.millions.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.ntnu.idi.idatt2003.millions.model.Stock;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {

    private Stock stock;

    @BeforeEach
    void setUp() {
        stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
    }

    @Test
    void getHistoricalPrices_returnsAllRegisteredPricesInOrder() {
        stock.addPrice(new BigDecimal("110.50"));
        stock.addPrice(new BigDecimal("95.25"));

        List<BigDecimal> prices = stock.getHistoricalPrices();

        assertEquals(List.of(
                new BigDecimal("100.00"),
                new BigDecimal("110.50"),
                new BigDecimal("95.25")
        ), prices);
    }

    @Test
    void getHistoricalPrices_returnsUnmodifiableList() {
        List<BigDecimal> prices = stock.getHistoricalPrices();

        assertThrows(UnsupportedOperationException.class,
                () -> prices.add(new BigDecimal("123.45")));
    }

    @Test
    void getHighestPrice_returnsHighestRegisteredPrice() {
        stock.addPrice(new BigDecimal("120.00"));
        stock.addPrice(new BigDecimal("98.70"));
        stock.addPrice(new BigDecimal("119.99"));

        assertEquals(new BigDecimal("120.00"), stock.getHighestPrice());
    }

    @Test
    void getLowestPrice_returnsLowestRegisteredPrice() {
        stock.addPrice(new BigDecimal("120.00"));
        stock.addPrice(new BigDecimal("98.70"));
        stock.addPrice(new BigDecimal("119.99"));

        assertEquals(new BigDecimal("98.70"), stock.getLowestPrice());
    }

    @Test
    void getLatestPriceChange_returnsZeroWhenOnlyOnePriceExists() {
        assertEquals(BigDecimal.ZERO, stock.getLatestPriceChange());
    }

    @Test
    void getLatestPriceChange_returnsDifferenceBetweenLatestAndPrevious() {
        stock.addPrice(new BigDecimal("104.75"));
        stock.addPrice(new BigDecimal("99.25"));

        assertEquals(new BigDecimal("-5.50"), stock.getLatestPriceChange());
    }

    @Test
    void getLatestPriceChangePercent_calculatesPercentChange() {
        stock.addPrice(new BigDecimal("110.00"));
        // from 100 to 110 -> +10%
        assertEquals(new BigDecimal("10.000000"), stock.getLatestPriceChangePercent());
    }

    @Test
    void constructor_throwsOnBlankSymbol() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stock("", "Equinor", new BigDecimal("100")));
        assertThrows(IllegalArgumentException.class,
                () -> new Stock(null, "Equinor", new BigDecimal("100")));
    }

    @Test
    void constructor_throwsOnBlankCompanyName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stock("EQNR", "", new BigDecimal("100")));
        assertThrows(IllegalArgumentException.class,
                () -> new Stock("EQNR", null, new BigDecimal("100")));
    }

    @Test
    void constructor_throwsOnNonPositivePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stock("EQNR", "Equinor", BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> new Stock("EQNR", "Equinor", null));
    }

    @Test
    void addPrice_throwsOnNullOrNonPositive() {
        assertThrows(IllegalArgumentException.class, () -> stock.addPrice(null));
        assertThrows(IllegalArgumentException.class, () -> stock.addPrice(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> stock.addPrice(new BigDecimal("-1")));
    }

    @Test
    void getters_returnConstructedValues() {
        assertEquals("EQNR", stock.getSymbol());
        assertEquals("Equinor", stock.getCompanyName());
        assertEquals(new BigDecimal("100.00"), stock.getSalesPrice());
    }

    @Test
    void getPrices_delegatesToGetHistoricalPrices() {
        assertEquals(stock.getHistoricalPrices(), stock.getPrices());
    }

    @Test
    void getLatestPriceChangePercent_returnsZero_whenOnlyOnePriceExists() {
        assertEquals(BigDecimal.ZERO, stock.getLatestPriceChangePercent());
    }

    @Test
    void toString_containsSymbolCompanyAndPrice() {
        String text = stock.toString();
        assertTrue(text.contains("EQNR"));
        assertTrue(text.contains("Equinor"));
        assertTrue(text.contains("100.00"));
    }
}
