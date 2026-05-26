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
}
