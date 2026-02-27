package edu.ntnu.idi.idatt2003.millions.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores completed (committed) transactions and provides aggregate queries.
 */
public class TransactionArchive {

    private final List<Transaction> transactions;

    /**
     * Constructs an empty TransactionArchive.
     */
    public TransactionArchive() {
        this.transactions = new ArrayList<>();
    }

    /**
     * Adds a committed transaction to the archive.
     *
     * @param transaction the transaction to archive (must not be null)
     * @throws IllegalArgumentException if transaction is null
     */
    public void add(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction must not be null");
        }
        transactions.add(transaction);
    }

    /**
     * Returns an unmodifiable view of all archived transactions.
     *
     * @return list of transactions, in the order they were committed
     */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     * Returns the number of distinct weeks in which transactions were committed.
     *
     * @return distinct week count
     */
    public long countDistinctWeeks() {
        return transactions.stream()
                .mapToInt(Transaction::getWeek)
                .distinct()
                .count();
    }
}
