package edu.ntnu.idi.idatt2003.millions.infrastructure.persistence;

import java.time.Instant;

/**
 * Summary of a saved game entry.
 */
public record GameSaveSummary(long id,
                              String label,
                              String exchangeName,
                              int week,
                              Instant createdAt) {
}
