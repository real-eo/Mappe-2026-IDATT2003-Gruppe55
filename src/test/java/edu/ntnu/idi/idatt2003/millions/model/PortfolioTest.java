package edu.ntnu.idi.idatt2003.millions.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.ntnu.idi.idatt2003.millions.model.Portfolio;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Portfolio}.
 */
class PortfolioTest {

    private Portfolio portfolio;
    private Stock stock;
    private Share share;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio();
        stock = new Stock("EQNR", "Equinor ASA", new BigDecimal("280"));
        share = new Share(stock, new BigDecimal("10"), new BigDecimal("280"));
    }

    @Test
    void addShare_portfolioContainsShare() {
        portfolio.add(share);
        assertTrue(portfolio.contains(share));
    }

    @Test
    void removeShare_portfolioNoLongerContainsShare() {
        portfolio.add(share);
        portfolio.remove(share);
        assertFalse(portfolio.contains(share));
    }

    @Test
    void containsReturnsFalse_whenShareNotAdded() {
        assertFalse(portfolio.contains(share));
    }

    @Test
    void findByStock_returnsShare_whenPresent() {
        portfolio.add(share);
        assertTrue(portfolio.findByStock(stock).isPresent());
    }

    @Test
    void findByStock_returnsEmpty_whenAbsent() {
        assertTrue(portfolio.findByStock(stock).isEmpty());
    }

    @Test
    void getShares_isUnmodifiable() {
        portfolio.add(share);
        assertThrows(UnsupportedOperationException.class,
                () -> portfolio.getShares().add(share));
    }

    @Test
    void addNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> portfolio.add(null));
    }

    @Test
    void removeNull_returnsFalse() {
        assertFalse(portfolio.remove(null));
    }
    @Test
    void containsReturnsFalse_whenShareIsNull() {
        assertFalse(portfolio.contains(null));
    }

    @Test
    void findByStock_returnsEmpty_whenStockIsNull() {
        assertTrue(portfolio.findByStock(null).isEmpty());
    }

    @Test
    void remove_returnsFalse_whenShareNotPresent() {
        assertFalse(portfolio.remove(share));
    }

    @Test
    void remove_matchesBySymbol_notByInstance() {
        portfolio.add(share);
        Share sameSymbolDifferentInstance =
                new Share(new Stock("EQNR", "Equinor ASA", new BigDecimal("300")),
                        new BigDecimal("1"), new BigDecimal("300"));

        assertTrue(portfolio.remove(sameSymbolDifferentInstance));
        assertTrue(portfolio.getShares().isEmpty());
    }
}
