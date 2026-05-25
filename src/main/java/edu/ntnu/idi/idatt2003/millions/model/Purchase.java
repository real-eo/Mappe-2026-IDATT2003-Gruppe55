package edu.ntnu.idi.idatt2003.millions.model;

import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.InsufficientFundsException;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.TransactionAlreadyCommittedException;
import edu.ntnu.idi.idatt2003.millions.model.calculator.PurchaseCalculator;

import java.math.BigDecimal;

/**
 * A transaction that purchases shares on behalf of a player.
 *
 * <p>On commit the total cost is deducted from the player's funds, the share
 * is added to their portfolio, and the transaction is archived.</p>
 */
public class Purchase extends Transaction {

    /**
     * Constructs a Purchase transaction.
     *
     * @param share      the share to buy
     * @param week       the week number
     */
    public Purchase(Share share, int week) {
        super(share, week, new PurchaseCalculator(share));
    }

    /**
     * Commits the purchase.
     *
     * @param player the player buying the shares
     * @throws TransactionAlreadyCommittedException if this transaction has already been committed
     * @throws InsufficientFundsException           if the player does not have enough funds
     */
 @Override
    public void commit(Player player) throws MillionsException {
        if (isCommitted()) {
            throw new TransactionAlreadyCommittedException(
                    "Purchase transaction has already been committed");
        }
        BigDecimal total = getCalculator().getTotal();
        if (player.getMoney().compareTo(total) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds: required " + total + " but player has " + player.getMoney());
        }
        player.withdrawMoney(total);
        player.getPortfolio().add(getShare());
        player.getTransactionArchive().add(this);
        markCommitted();
    }
}

