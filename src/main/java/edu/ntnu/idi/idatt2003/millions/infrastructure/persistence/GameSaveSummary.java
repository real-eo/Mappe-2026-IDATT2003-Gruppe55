package edu.ntnu.idi.idatt2003.millions.infrastructure.persistence;

import java.time.Instant;

/**
 * Summary of a saved game entry.
 *
 * @param id the database identifier of the saved game
 * @param label the user-defined label for the save entry
 * @param exchangeName the name of the exchange in the saved game
 * @param week the current simulation week at save time
 * @param createdAt the timestamp when the save entry was created
 */
public record GameSaveSummary(long id,
                              String label,
                              String exchangeName,
                              int week,
                              Instant createdAt) {
}
