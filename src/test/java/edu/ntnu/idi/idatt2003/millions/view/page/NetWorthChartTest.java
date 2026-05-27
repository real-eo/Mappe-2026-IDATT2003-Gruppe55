package edu.ntnu.idi.idatt2003.millions.view.page;

import edu.ntnu.idi.idatt2003.millions.model.NetWorthSnapshot;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NetWorthChartTest {

    @Test
    void constructor_createsCanvasChild() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            NetWorthChart chart = new NetWorthChart();
            assertEquals(1, chart.getChildren().size());
            assertNotNull(chart.getChildren().get(0));
        });
    }

    @Test
    void setData_acceptsNullAndShortLists_withoutThrowing() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            NetWorthChart chart = new NetWorthChart();
            chart.resize(600, 240);

            chart.setData(null);
            chart.setData(List.of(new NetWorthSnapshot(1, new BigDecimal("1000"))));
            chart.setData(List.of(
                    new NetWorthSnapshot(1, new BigDecimal("1000")),
                    new NetWorthSnapshot(2, new BigDecimal("1200"))));
        });
    }

    @Test
    void formatAmount_formatsPlainThousandsAndMillions() throws Exception {
        NetWorthChart chart = new NetWorthChart();
        Method formatAmount = NetWorthChart.class.getDeclaredMethod("formatAmount", double.class);
        formatAmount.setAccessible(true);

        assertEquals("$999", formatAmount.invoke(chart, 999.0));
        assertEquals(String.format("$%.1fK", 1.5), formatAmount.invoke(chart, 1500.0));
        assertEquals(String.format("$%.1fM", 2.5), formatAmount.invoke(chart, 2_500_000.0));
        assertEquals(String.format("$%.1fK", -1.5), formatAmount.invoke(chart, -1500.0));
    }
}
