package edu.ntnu.idi.idatt2003.millions.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.ntnu.idi.idatt2003.millions.model.Exchange;
import edu.ntnu.idi.idatt2003.millions.model.Player;
import edu.ntnu.idi.idatt2003.millions.model.PlayerStatus;
import edu.ntnu.idi.idatt2003.millions.model.Purchase;
import edu.ntnu.idi.idatt2003.millions.model.Share;
import edu.ntnu.idi.idatt2003.millions.model.Stock;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerStatusTest {

    private Player player;
    private Stock stock;
    private Exchange exchange;

    @BeforeEach
    void setUp() {
        player = new Player("Alice", new BigDecimal("10000"));
        stock = new Stock("EQNR", "Equinor", new BigDecimal("100"));
        exchange = new Exchange("OSE", List.of(stock), new Random(0));
    }

    @Test
    void getStatus_isNovice_forNewPlayer() {
        assertEquals(PlayerStatus.NOVICE, player.getStatus());
    }

    @Test
    void getStatus_isNovice_whenGainExistsButTradingWeeksAreTooLow() throws Exception {
        stock.addPrice(new BigDecimal("1000"));
        exchange.buy(player, "EQNR", new BigDecimal("1"));

        assertEquals(PlayerStatus.NOVICE, player.getStatus());
    }

    @Test
    void getStatus_isInvestor_whenAtLeastTenWeeksAndTwentyPercentGrowth() throws Exception {
        for (int week = 0; week < 10; week++) {
            exchange.buy(player, "EQNR", new BigDecimal("1"));
            exchange.advance();
        }

        stock.addPrice(new BigDecimal("600"));

        assertEquals(PlayerStatus.INVESTOR, player.getStatus());
    }

    @Test
    void getStatus_isSpeculator_whenAtLeastTwentyWeeksAndDoubleNetWorth() throws Exception {
        for (int week = 0; week < 20; week++) {
            exchange.buy(player, "EQNR", new BigDecimal("1"));
            exchange.advance();
        }

        stock.addPrice(new BigDecimal("2000"));

        assertEquals(PlayerStatus.SPECULATOR, player.getStatus());
    }

    @Test
    void getStatus_isNovice_whenNineWeeksWithHighGrowth() {
        for (int week = 1; week <= 9; week++) {
            player.getTransactionArchive().add(
                    new Purchase(new Share(stock, BigDecimal.ONE, BigDecimal.ONE), week));
        }
        player.addMoney(new BigDecimal("5000"));

        assertEquals(PlayerStatus.NOVICE, player.getStatus());
    }

    @Test
    void getStatus_isInvestor_whenTwentyWeeksButGrowthBelowSpeculatorThreshold() {
        for (int week = 1; week <= 20; week++) {
            player.getTransactionArchive().add(
                    new Purchase(new Share(stock, BigDecimal.ONE, BigDecimal.ONE), week));
        }
        player.addMoney(new BigDecimal("5000"));
        // net worth = 15000: >= 10000*1.20=12000 (INVESTOR) but < 10000*2.00=20000 (SPECULATOR)

        assertEquals(PlayerStatus.INVESTOR, player.getStatus());
    }
}
