package edu.ntnu.idi.idatt2003.millions.model;

import java.math.BigDecimal;

/**
 * Represents a player participating in the Millions stock market game.
 *
 * <p>A player has a name, a starting capital, a current cash balance, a portfolio
 * of share holdings, and an archive of past transactions.</p>
 */
public class Player {

    private static final long INVESTOR_MIN_WEEKS = 10;
    private static final long SPECULATOR_MIN_WEEKS = 20;
    private static final BigDecimal INVESTOR_MIN_FACTOR = new BigDecimal("1.20");
    private static final BigDecimal SPECULATOR_MIN_FACTOR = new BigDecimal("2.00");

    private final String name;
    private final BigDecimal startingMoney;
    private BigDecimal money;
    private final Portfolio portfolio;
    private final TransactionArchive transactionArchive;

    /**
     * Constructs a Player with the given name and starting capital.
     *
     * @param name          the player's name (must not be blank)
     * @param startingMoney the initial cash balance (must be positive)
     * @throws IllegalArgumentException if name is blank or startingMoney is non-positive
     */
    public Player(String name, BigDecimal startingMoney) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name must not be blank");
        }
        if (startingMoney == null || startingMoney.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Starting money must be positive");
        }
        this.name = name;
        this.startingMoney = startingMoney;
        this.money = startingMoney;
        this.portfolio = new Portfolio();
        this.transactionArchive = new TransactionArchive();
    }

    /**
     * Returns the player's name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the amount of money the player started with.
     *
     * @return starting money
     */
    public BigDecimal getStartingMoney() {
        return startingMoney;
    }

    /**
     * Returns the player's current cash balance.
     *
     * @return current money
     */
    public BigDecimal getMoney() {
        return money;
    }

    /**
     * Subtracts the given amount from the player's cash balance.
     *
     * @param amount the amount to subtract (must be positive)
     * @throws IllegalArgumentException if amount is null or non-positive
     */
    public void withdrawMoney(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.money = this.money.subtract(amount);
    }

    /**
     * Adds the given amount to the player's cash balance.
     *
     * @param amount the amount to add (must be positive)
     * @throws IllegalArgumentException if amount is null or non-positive
     */
    public void addMoney(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.money = this.money.add(amount);
    }

    /**
     * Returns the player's share portfolio.
     *
     * @return the portfolio
     */
    public Portfolio getPortfolio() {
        return portfolio;
    }

    /**
     * Returns the player's transaction archive.
     *
     * @return the transaction archive
     */
    public TransactionArchive getTransactionArchive() {
        return transactionArchive;
    }

    /**
     * Returns the player's current net worth: cash balance plus portfolio market value.
     *
     * @return current net worth
     */
    public BigDecimal getNetWorth() {
        BigDecimal portfolioValue = portfolio.getShares().stream()
                .map(share -> share.getStock().getSalesPrice().multiply(share.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return money.add(portfolioValue);
    }

    /**
     * Returns the player's progression status based on trading history and net worth growth.
     *
     * <ul>
     *   <li>NOVICE: default level</li>
     *   <li>INVESTOR: at least 10 distinct trading weeks and at least 20% net worth growth</li>
     *   <li>SPECULATOR: at least 20 distinct trading weeks and at least 100% net worth growth</li>
     * </ul>
     *
     * @return the player's current status
     */
    public PlayerStatus getStatus() {
        long tradingWeeks = transactionArchive.countDistinctWeeks();
        BigDecimal netWorth = getNetWorth();

        if (tradingWeeks >= SPECULATOR_MIN_WEEKS
                && netWorth.compareTo(startingMoney.multiply(SPECULATOR_MIN_FACTOR)) >= 0) {
            return PlayerStatus.SPECULATOR;
        }

        if (tradingWeeks >= INVESTOR_MIN_WEEKS
                && netWorth.compareTo(startingMoney.multiply(INVESTOR_MIN_FACTOR)) >= 0) {
            return PlayerStatus.INVESTOR;
        }

        return PlayerStatus.NOVICE;
    }

    @Override
    public String toString() {
        return name + " [" + money + "]";
    }
}

