package edu.ntnu.idi.idatt2003.millions.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameSaveSummaryTest {

    @Test
    void getters_returnConstructedValues() {
        GameSaveSummary s = new GameSaveSummary(1L, "Label", "OSE", 5, Instant.parse("2026-05-26T10:15:30Z"));
        assertEquals(1L, s.id());
        assertEquals("Label", s.label());
        assertEquals("OSE", s.exchangeName());
        assertEquals(5, s.week());
        assertEquals(Instant.parse("2026-05-26T10:15:30Z"), s.createdAt());
    }
}
