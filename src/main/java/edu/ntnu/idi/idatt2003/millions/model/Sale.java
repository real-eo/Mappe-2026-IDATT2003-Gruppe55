package edu.ntnu.idi.idatt2003.millions.model;

import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.MillionsException;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.ShareNotOwnedException;
import edu.ntnu.idi.idatt2003.millions.infrastructure.exception.TransactionAlreadyCommittedException;
import edu.ntnu.idi.idatt2003.millions.model.calculator.SaleCalculator;

import java.math.BigDecimal;

/**
 * A transaction that sells shares on behalf of a player.
 *
 * <p>On commit the net proceeds are added to the player's funds, the share
 * is removed from their portfolio, and the transaction is archived.</p>
 */
public class Sale extends Transaction {

    /**
     * Constructs a Sale transaction.
     *
     * @param share      the share to sell
     * @param week       the week number
     * @param calculator the proceeds calculator for this sale
     */
    public Sale(Share share, int week) {
        super(share, week, new SaleCalculator(share));
    }

    /**
     * Commits the sale.
     *
     * @param player the player selling the shares
     * @throws TransactionAlreadyCommittedException if this transaction has already been committed
     * @throws ShareNotOwnedException               if the player does not own the share being sold
     */
    @Override
    public void commit(Player player) throws MillionsException {
        if (isCommitted()) {
            throw new TransactionAlreadyCommittedException(
                    "Sale transaction has already been committed");
        }
        if (!player.getPortfolio().contains(getShare())) {
            throw new ShareNotOwnedException(
                    "Player does not own stock: " + getShare().getStock().getSymbol());
        }
        BigDecimal total = getCalculator().getTotal();
        player.addMoney(total);
        player.getPortfolio().remove(getShare());
        player.getTransactionArchive().add(this);
        markCommitted();
    }
}

