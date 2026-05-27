package edu.ntnu.idi.idatt2003.millions.model;

import java.math.BigDecimal;

/**
 * Immutable snapshot of player net worth at a specific week.
 *
 * @param week week number represented by this snapshot
 * @param netWorth net worth value for that week
 */
public record NetWorthSnapshot(int week, BigDecimal netWorth) {}
