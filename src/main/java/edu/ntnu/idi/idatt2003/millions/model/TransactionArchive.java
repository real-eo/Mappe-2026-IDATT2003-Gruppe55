package edu.ntnu.idi.idatt2003.millions.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public boolean add(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction must not be null");
        }
        return transactions.add(transaction);
    }

    /**
     * Checks if the archive is empty.
     *
     * @return true if no transactions are archived
     */
    public boolean isEmpty() {
        return transactions.isEmpty();
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
     * Returns all purchase transactions for a given week.
     *
     * @param week the week number
     * @return list of purchases in that week
     */
    public List<Purchase> getPurchases(int week) {
        return transactions.stream()
                .filter(t -> t.getWeek() == week && t instanceof Purchase)
                .map(t -> (Purchase) t)
                .collect(Collectors.toList());
    }

    /**
     * Returns all sale transactions for a given week.
     *
     * @param week the week number
     * @return list of sales in that week
     */
    public List<Sale> getSales(int week) {
        return transactions.stream()
                .filter(t -> t.getWeek() == week && t instanceof Sale)
                .map(t -> (Sale) t)
                .collect(Collectors.toList());
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

