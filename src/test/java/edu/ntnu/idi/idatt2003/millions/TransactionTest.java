package edu.ntnu.idi.idatt2003.millions;

import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.InsufficientFundsException;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.ShareNotOwnedException;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.TransactionAlreadyCommittedException;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.Purchase;
import edu.ntnu.idi.idatt2003.millions.model.Sale;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Purchase} and {@link Sale} commit logic.
 */
class TransactionTest {

    private Player player;
    private Stock stock;

    @BeforeEach
    void setUp() {
        player = new Player("Alice", new BigDecimal("10000"));
        stock = new Stock("EQNR", "Equinor", new BigDecimal("100.00"));
    }

    // --- Purchase tests ---

    @Test
    void purchase_commit_deductsMoney_and_addsShareToPortfolio() throws Exception {
        Share share = new Share(stock, new BigDecimal("10"), stock.getSalesPrice());
        Purchase purchase = new Purchase(share, 1);

        purchase.commit(player);

        // total = 1000 + 5 (0.5% commission) = 1005
        assertEquals(new BigDecimal("8995.00"), player.getMoney());
        assertTrue(player.getPortfolio().contains(share));
        assertEquals(1, player.getTransactionArchive().getTransactions().size());
        assertTrue(purchase.isCommitted());
    }

    @Test
    void purchase_commit_throwsInsufficientFunds_whenPlayerHasNoMoney() {
        Player poorPlayer = new Player("Bob", new BigDecimal("1.00"));
        Share share = new Share(stock, new BigDecimal("100"), stock.getSalesPrice());
        Purchase purchase = new Purchase(share, 1);

        assertThrows(InsufficientFundsException.class, () -> purchase.commit(poorPlayer));
    }

    @Test
    void purchase_commit_throwsAlreadyCommitted_onSecondCommit() throws Exception {
        Share share = new Share(stock, new BigDecimal("1"), stock.getSalesPrice());
        Purchase purchase = new Purchase(share, 1);
        purchase.commit(player);

        assertThrows(TransactionAlreadyCommittedException.class, () -> purchase.commit(player));
    }

    // --- Sale tests ---

    @Test
    void sale_commit_addsMoney_and_removesShareFromPortfolio() throws Exception {
        // First buy the share
        Share share = new Share(stock, new BigDecimal("10"), stock.getSalesPrice());
        Purchase purchase = new Purchase(share, 1);
        purchase.commit(player);

        // Now sell at same price (no profit -> no tax)
        Sale sale = new Sale(share, 1);
        sale.commit(player);

        assertFalse(player.getPortfolio().contains(share));
        assertTrue(sale.isCommitted());
    }

    @Test
    void sale_commit_throwsShareNotOwned_whenNotInPortfolio() {
        Share share = new Share(stock, new BigDecimal("5"), stock.getSalesPrice());
        Sale sale = new Sale(share, 1);

        assertThrows(ShareNotOwnedException.class, () -> sale.commit(player));
    }

    @Test
    void sale_commit_throwsAlreadyCommitted_onSecondCommit() throws Exception {
        Share share = new Share(stock, new BigDecimal("1"), stock.getSalesPrice());
        Purchase purchase = new Purchase(share, 1);
        purchase.commit(player);

        Sale sale = new Sale(share, 1);
        sale.commit(player);

        assertThrows(TransactionAlreadyCommittedException.class, () -> sale.commit(player));
    }
}

