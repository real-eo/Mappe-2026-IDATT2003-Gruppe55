package edu.ntnu.idi.idatt2003.millions.controller;

import edu.ntnu.idi.idatt2003.millions.model.NetWorthSnapshot;
import edu.ntnu.idi.idatt2003.millions.model.Sale;
import edu.ntnu.idi.idatt2003.millions.model.Transaction;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Controller for profile-page operations: player statistics and game persistence.
 */
public class ProfileController {

    private final ExchangeController exchangeController;

    /**
     * Constructs a ProfileController backed by the given ExchangeController.
     *
     * @param exchangeController the active exchange controller
     */
    public ProfileController(ExchangeController exchangeController) {
        this.exchangeController = exchangeController;
    }

    /**
     * Returns the total number of shares purchased across all transactions.
     *
     * @return total purchased quantity
     */
    public BigDecimal getTotalPurchased() {
        return exchangeController.getPlayer()
                .getTransactionArchive()
                .getTotalPurchasedQuantity();
    }

    /**
     * Returns the total number of shares sold across all transactions.
     *
     * @return total sold quantity
     */
    public BigDecimal getTotalSold() {
        return exchangeController.getPlayer()
                .getTransactionArchive()
                .getTotalSoldQuantity();
    }

    /**
     * Returns the top-N stocks by net profit from completed sales.
     *
     * @param limit maximum number of entries
     * @return list of outcome entries sorted by profit descending
     */
    public List<OutcomeEntry> getTopWins(int limit) {
        return resolveSaleOutcomes(true, limit);
    }

    /**
     * Returns the top-N stocks by net loss from completed sales.
     *
     * @param limit maximum number of entries
     * @return list of outcome entries sorted by loss ascending
     */
    public List<OutcomeEntry> getTopLosses(int limit) {
        return resolveSaleOutcomes(false, limit);
    }

    /**
     * Returns the net worth history for this session.
     *
     * @return list of net worth snapshots ordered by week
     */
    public List<NetWorthSnapshot> getNetWorthHistory() {
        return exchangeController.getNetWorthHistory();
    }

    /**
     * Saves the current game state asynchronously.
     *
     * @param onSuccess called with the save ID on success
     * @param onFailure called with an error message on failure
     */
    public void saveGame(Consumer<Long> onSuccess, Consumer<String> onFailure) {
        exchangeController.saveGame(onSuccess, onFailure);
    }

    private List<OutcomeEntry> resolveSaleOutcomes(boolean wins, int limit) {
        Map<String, BigDecimal> outcomes = new HashMap<>();
        for (Transaction transaction : exchangeController.getPlayer()
                .getTransactionArchive().getTransactions()) {
            if (transaction instanceof Sale sale) {
                BigDecimal quantity = sale.getShare().getQuantity();
                BigDecimal costBasis = sale.getShare().getPurchasePrice().multiply(quantity);
                BigDecimal netProfit = sale.getCalculator().getTotal().subtract(costBasis);
                String symbol = sale.getShare().getStock().getSymbol();
                outcomes.merge(symbol, netProfit, BigDecimal::add);
            }
        }

        List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(outcomes.entrySet());
        Comparator<Map.Entry<String, BigDecimal>> comparator = Map.Entry.comparingByValue();
        if (wins) {
            comparator = comparator.reversed();
        }

        return entries.stream()
                .filter(entry -> wins
                        ? entry.getValue().compareTo(BigDecimal.ZERO) > 0
                        : entry.getValue().compareTo(BigDecimal.ZERO) < 0)
                .sorted(comparator)
                .limit(limit)
                .map(entry -> new OutcomeEntry(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * Represents the net profit or loss for a single stock symbol.
     *
     * @param symbol    the stock ticker symbol
     * @param netProfit positive for profit, negative for loss
     */
    public record OutcomeEntry(String symbol, BigDecimal netProfit) {
    }
}
