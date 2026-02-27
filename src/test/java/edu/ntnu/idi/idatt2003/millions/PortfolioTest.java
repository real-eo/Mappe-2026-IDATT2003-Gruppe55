package edu.ntnu.idi.idatt2003.millions;

import edu.ntnu.idi.idatt2003.millions.model.Portfolio;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        share = new Share(stock, 10, new BigDecimal("280"));
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
}
