package edu.ntnu.idi.idatt2003.millions.controller;

import edu.ntnu.idi.idatt2003.millions.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfileControllerTest {

    private Exchange exchange;
    private Player player;
    private ExchangeController exchangeController;
    private ProfileController profileController;

    @BeforeEach
    void setUp() {
        Stock s1 = new Stock("X", "X Co", new BigDecimal("10.00"));
        Stock s2 = new Stock("Y", "Y Co", new BigDecimal("20.00"));
        exchange = new Exchange("E", List.of(s1, s2));
        player = new Player("P", new BigDecimal("1000.00"));
        exchangeController = new ExchangeController(exchange, player);
        profileController = new ProfileController(exchangeController);
    }

    @Test
    void totals_returnCorrectSums() {
        // add one purchase and two sales to archive
        Share pshare = new Share(exchange.getStocks().get(0), new BigDecimal("3"), new BigDecimal("9.00"));
        Purchase p = new Purchase(pshare, 1);
        player.getTransactionArchive().add(p);

        Share s1 = new Share(exchange.getStocks().get(0), new BigDecimal("2"), new BigDecimal("5.00"));
        Sale sale1 = new Sale(s1, 2);
        player.getTransactionArchive().add(sale1);

        Share s2 = new Share(exchange.getStocks().get(1), new BigDecimal("1"), new BigDecimal("1.00"));
        Sale sale2 = new Sale(s2, 3);
        player.getTransactionArchive().add(sale2);

        assertEquals(new BigDecimal("3"), profileController.getTotalPurchased());
        assertEquals(new BigDecimal("3"), profileController.getTotalSold());
    }

    @Test
    void getNetWorthHistory_returnsHistoryFromController() {
        assertNotNull(profileController.getNetWorthHistory());
        assertFalse(profileController.getNetWorthHistory().isEmpty());
    }

    @Test
    void topWins_and_topLosses_returnCorrectEntries() {
        Stock s = exchange.getStocks().get(0);
        // create two sales with different profits for symbol X
        Share sA = new Share(s, new BigDecimal("1"), new BigDecimal("5.00"));
        Sale saleA = new Sale(sA, 1); // profit = (10 - commission) - costBasis
        player.getTransactionArchive().add(saleA);

        Share sB = new Share(s, new BigDecimal("2"), new BigDecimal("9.50"));
        Sale saleB = new Sale(sB, 2);
        player.getTransactionArchive().add(saleB);

        List<ProfileController.OutcomeEntry> wins = profileController.getTopWins(5);
        // symbol X should appear if any positive profit exists
        assertTrue(wins.stream().anyMatch(e -> e.symbol().equals("X")));

        List<ProfileController.OutcomeEntry> losses = profileController.getTopLosses(5);
        // may be empty if no negative outcomes
        assertNotNull(losses);
    }
}
