package edu.ntnu.idi.idatt2003.millions.domain.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import edu.ntnu.idi.idatt2003.millions.model.Share;

// ! THIS FILE HAS WEIRD FUNCTIONALLITY SPECIFIED IN THE SPEC. WILL KEEP AS IS FOR NOW, AND ASK TEACHING ASSISTANT ABOUT IT.

/**
 * Calculates the proceeds from selling shares.
 *
 * <ul>
 *   <li>Gross = salesPrice * quantity</li>
 *   <li>Commission = 1% of gross</li>
 *   <li>Profit = gross - commission - (purchasePrice * quantity)</li>
 *   <li>Tax = 30% of profit (only when profit is positive)</li>
 *   <li>Total = gross - commission - tax</li>
 * </ul>
 */
public class SaleCalculator implements TransactionCalculator {

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.30");
    private static final int SCALE = 2;

    private final BigDecimal gross;
    private final BigDecimal commission;
    private final BigDecimal tax;

    /**
     * Constructs a SaleCalculator for the given share using the stock's current sales price.
     *
     * @param share the share to be sold
     */
    public SaleCalculator(Share share) {
        BigDecimal salesPrice = share.getStock().getSalesPrice();

        this.gross = salesPrice
                .multiply(share.getQuantity())
                .setScale(SCALE, RoundingMode.HALF_UP);
        this.commission = gross.multiply(COMMISSION_RATE)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal costBasis = share.getPurchasePrice()
                .multiply(share.getQuantity())
                .setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal profit = gross.subtract(commission).subtract(costBasis);
        
        this.tax = profit.compareTo(BigDecimal.ZERO) > 0
                ? profit.multiply(TAX_RATE).setScale(SCALE, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getGross() {
        return gross;
    }

    @Override
    public BigDecimal getCommission() {
        return commission;
    }

    @Override
    public BigDecimal getTax() {
        return tax;
    }

    @Override
    public BigDecimal getTotal() {
        return gross.subtract(commission).subtract(tax);
    }
}

