package edu.ntnu.idi.idatt2003.millions.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetWorthSnapshotTest {

    @Test
    void record_accessors_returnConstructedValues() {
        NetWorthSnapshot snapshot = new NetWorthSnapshot(3, new BigDecimal("12345.67"));
        assertEquals(3, snapshot.week());
        assertEquals(new BigDecimal("12345.67"), snapshot.netWorth());
    }
}
