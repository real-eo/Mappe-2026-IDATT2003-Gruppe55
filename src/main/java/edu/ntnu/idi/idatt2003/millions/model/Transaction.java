package edu.ntnu.idi.idatt2003.millions.model;

import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.model.calculator.TransactionCalculator;

/**
 * Abstract base class for all stock market transactions.
 *
 * <p>Each transaction is associated with a share, a week number, and a
 * {@link TransactionCalculator} that computes the financial details. A transaction
 * can only be committed once; subsequent commit attempts will throw an exception.</p>
 */
public abstract class Transaction {

    private final Share share;
    private final int week;
    private final TransactionCalculator calculator;
    private boolean committed;

    /**
     * Constructs a Transaction.
     *
     * @param share      the share involved in the transaction
     * @param week       the week number when the transaction takes place
     * @param calculator the calculator used to determine costs or proceeds
     * @throws IllegalArgumentException if share or calculator is null, or week is non-positive
     */
    protected Transaction(Share share, int week, TransactionCalculator calculator) {
        if (share == null) {
            throw new IllegalArgumentException("Share must not be null");
        }
        if (calculator == null) {
            throw new IllegalArgumentException("Calculator must not be null");
        }
        if (week <= 0) {
            throw new IllegalArgumentException("Week must be positive");
        }
        this.share = share;
        this.week = week;
        this.calculator = calculator;
        this.committed = false;
    }

    /**
     * Returns the share associated with this transaction.
     *
     * @return the share
     */
    public Share getShare() {
        return share;
    }

    /**
     * Returns the week number when this transaction takes place.
     *
     * @return the week number
     */
    public int getWeek() {
        return week;
    }

    /**
     * Returns the calculator used for this transaction.
     *
     * @return the transaction calculator
     */
    public TransactionCalculator getCalculator() {
        return calculator;
    }

    /**
     * Returns whether this transaction has been committed.
     *
     * @return {@code true} if committed
     */
    public boolean isCommitted() {
        return committed;
    }

    /**
     * Marks this transaction as committed. Called by subclasses upon successful commit.
     */
    protected void markCommitted() {
        this.committed = true;
    }

    /**
     * Commits this transaction, applying its effects to the given player.
     *
     * @param player the player executing the transaction
     * @throws MillionsException if the transaction cannot be committed
     */
    public abstract void commit(Player player) throws MillionsException;
}

