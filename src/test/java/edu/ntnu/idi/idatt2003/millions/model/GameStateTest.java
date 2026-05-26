package edu.ntnu.idi.idatt2003.millions.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    void constructor_throws_whenExchangeIsNull() {
        Player player = new Player("P", new BigDecimal("100.00"));
        assertThrows(NullPointerException.class, () -> new GameState(null, player));
    }

    @Test
    void constructor_throws_whenPlayerIsNull() {
        Exchange exchange = new Exchange("E", List.of(new Stock("A", "A", new BigDecimal("1.00"))));
        assertThrows(NullPointerException.class, () -> new GameState(exchange, null));
    }

    @Test
    void getters_returnProvidedReferences() {
        Exchange exchange = new Exchange("E", List.of(new Stock("A", "A", new BigDecimal("1.00"))));
        Player player = new Player("P", new BigDecimal("100.00"));
        GameState state = new GameState(exchange, player);

        assertSame(exchange, state.getExchange());
        assertSame(player, state.getPlayer());
    }

    @Test
    void getNetWorthHistory_isEmptyByDefault() {
        Exchange exchange = new Exchange("E", List.of(new Stock("A", "A", new BigDecimal("1.00"))));
        Player player = new Player("P", new BigDecimal("100.00"));
        GameState state = new GameState(exchange, player);
        assertTrue(state.getNetWorthHistory().isEmpty());
    }

    @Test
    void getNetWorthHistory_returnsProvidedHistory() {
        Exchange exchange = new Exchange("E", List.of(new Stock("A", "A", new BigDecimal("1.00"))));
        Player player = new Player("P", new BigDecimal("100.00"));
        var snapshot = new NetWorthSnapshot(1, new BigDecimal("100.00"));
        GameState state = new GameState(exchange, player, List.of(snapshot));
        assertEquals(1, state.getNetWorthHistory().size());
        assertEquals(1, state.getNetWorthHistory().get(0).week());
        assertEquals(new BigDecimal("100.00"), state.getNetWorthHistory().get(0).netWorth());
    }

    @Test
    void getNetWorthHistory_treatsNullHistoryAsEmpty() {
        Exchange exchange = new Exchange("E", List.of(new Stock("A", "A", new BigDecimal("1.00"))));
        Player player = new Player("P", new BigDecimal("100.00"));
        GameState state = new GameState(exchange, player, null);
        assertTrue(state.getNetWorthHistory().isEmpty());
    }
}
