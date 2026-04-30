package edu.ntnu.idi.idatt2003.millions.domain.calculator;

import java.math.BigDecimal;

/**
 * Strategy interface for computing transaction costs and totals.
 */
public interface TransactionCalculator {

    /**
        * Returns the gross amount of the transaction (price * quantity).
     *
     * @return gross amount
     */
    BigDecimal getGross();

    /**
     * Returns the brokerage commission for the transaction.
     *
     * @return commission amount
     */
    BigDecimal getCommission();

    /**
     * Returns the tax component for the transaction.
     *
     * @return tax amount
     */
    BigDecimal getTax();

    /**
     * Returns the net total amount to be paid or received by the player.
     *
     * @return total amount
     */
    BigDecimal getTotal();
}

