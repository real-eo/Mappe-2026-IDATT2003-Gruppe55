package edu.ntnu.idi.idatt2003.millions.model.calculator;

import edu.ntnu.idi.idatt2003.millions.model.Share;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates the cost of purchasing shares.
 *
 * <ul>
 *   <li>Gross = purchasePrice × quantity</li>
 *   <li>Commission = 0.5% of gross</li>
 *   <li>Tax = 0</li>
 *   <li>Total = gross + commission</li>
 * </ul>
 */
public class PurchaseCalculator implements TransactionCalculator {

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005");
    private static final int SCALE = 2;

    private final BigDecimal gross;
    private final BigDecimal commission;

    /**
     * Constructs a PurchaseCalculator for the given share.
     *
     * @param share the share to be purchased
     */
    public PurchaseCalculator(Share share) {
        this.gross = share.getPurchasePrice()
                .multiply(BigDecimal.valueOf(share.getQuantity()))
                .setScale(SCALE, RoundingMode.HALF_UP);
        this.commission = gross.multiply(COMMISSION_RATE)
                .setScale(SCALE, RoundingMode.HALF_UP);
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
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotal() {
        return gross.add(commission);
    }
}
