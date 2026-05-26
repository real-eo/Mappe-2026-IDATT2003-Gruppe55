package edu.ntnu.idi.idatt2003.millions.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void constructor_throwsOnNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Player(null, new BigDecimal("1000")));
    }

    @Test
    void constructor_throwsOnBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Player("  ", new BigDecimal("1000")));
    }

    @Test
    void constructor_throwsOnNullStartingMoney() {
        assertThrows(IllegalArgumentException.class,
                () -> new Player("Alice", null));
    }

    @Test
    void constructor_throwsOnNonPositiveStartingMoney() {
        assertThrows(IllegalArgumentException.class,
                () -> new Player("Alice", BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> new Player("Alice", new BigDecimal("-100")));
    }

    @Test
    void constructor_setsInitialState() {
        Player player = new Player("Bob", new BigDecimal("5000"));
        assertEquals("Bob", player.getName());
        assertEquals(new BigDecimal("5000"), player.getStartingMoney());
        assertEquals(new BigDecimal("5000"), player.getMoney());
        assertNotNull(player.getPortfolio());
        assertNotNull(player.getTransactionArchive());
    }

    @Test
    void addMoney_throwsOnNull() {
        Player player = new Player("Alice", new BigDecimal("1000"));
        assertThrows(IllegalArgumentException.class, () -> player.addMoney(null));
    }

    @Test
    void addMoney_throwsOnNonPositive() {
        Player player = new Player("Alice", new BigDecimal("1000"));
        assertThrows(IllegalArgumentException.class, () -> player.addMoney(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> player.addMoney(new BigDecimal("-50")));
    }

    @Test
    void addMoney_increasesBalance() {
        Player player = new Player("Alice", new BigDecimal("1000"));
        player.addMoney(new BigDecimal("500"));
        assertEquals(new BigDecimal("1500"), player.getMoney());
    }

    @Test
    void withdrawMoney_throwsOnNull() {
        Player player = new Player("Alice", new BigDecimal("1000"));
        assertThrows(IllegalArgumentException.class, () -> player.withdrawMoney(null));
    }

    @Test
    void withdrawMoney_throwsOnNonPositive() {
        Player player = new Player("Alice", new BigDecimal("1000"));
        assertThrows(IllegalArgumentException.class, () -> player.withdrawMoney(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> player.withdrawMoney(new BigDecimal("-10")));
    }

    @Test
    void withdrawMoney_decreasesBalance() {
        Player player = new Player("Alice", new BigDecimal("1000"));
        player.withdrawMoney(new BigDecimal("300"));
        assertEquals(new BigDecimal("700"), player.getMoney());
    }

    @Test
    void getNetWorth_withEmptyPortfolio_equalsMoney() {
        Player player = new Player("Alice", new BigDecimal("5000"));
        assertEquals(new BigDecimal("5000"), player.getNetWorth());
    }

    @Test
    void getNetWorth_includesPortfolioValue() {
        Player player = new Player("Alice", new BigDecimal("5000"));
        Stock stock = new Stock("X", "X Corp", new BigDecimal("100"));
        Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("100"));
        player.getPortfolio().add(share);
        player.withdrawMoney(new BigDecimal("1000"));
        assertEquals(new BigDecimal("5000"), player.getNetWorth());
    }

    @Test
    void toString_containsNameAndMoney() {
        Player player = new Player("Alice", new BigDecimal("1000"));
        String s = player.toString();
        assertTrue(s.contains("Alice"));
        assertTrue(s.contains("1000"));
    }
}
