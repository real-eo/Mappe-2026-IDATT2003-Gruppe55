package edu.ntnu.idi.idatt2003.millions.view.dialog;

import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.canvas.Canvas;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StockPriceChartTest {

    @Test
    void constructor_createsCanvasChild() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StockPriceChart chart = new StockPriceChart();
            assertEquals(1, chart.getChildren().size());
            assertNotNull(chart.getChildren().get(0));
            assertEquals(Canvas.class, chart.getChildren().get(0).getClass());
        });
    }

    @Test
    void setPrices_acceptsNullShortAndLongLists_withoutThrowing() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StockPriceChart chart = new StockPriceChart();
            chart.resize(600, 240);
            chart.setPrices(null);
            chart.setPrices(List.of(new BigDecimal("100.00")));
            chart.setPrices(List.of(new BigDecimal("100.00"), new BigDecimal("110.00"), new BigDecimal("120.00")));
        });
    }

    @Test
    void setPrices_withAllEqualPrices_doesNotThrow() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StockPriceChart chart = new StockPriceChart();
            chart.resize(600, 240);
            chart.setPrices(List.of(
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                new BigDecimal("100.00")
            ));
        });
    }

    @Test
    void setPrices_withMoreThan20Prices_doesNotThrow() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            StockPriceChart chart = new StockPriceChart();
            chart.resize(600, 240);
            java.util.List<BigDecimal> prices = new java.util.ArrayList<>();
            for (int i = 0; i < 21; i++) {
                prices.add(new BigDecimal(100 + i));
            }
            chart.setPrices(prices);
        });
    }

    @Test
    void formatPrice_formatsThousandsAndDecimals() throws Exception {
        Method formatPrice = StockPriceChart.class.getDeclaredMethod("formatPrice", double.class);
        formatPrice.setAccessible(true);

        assertEquals(String.format(Locale.getDefault(), "$%.2f", 12.34), formatPrice.invoke(null, 12.34));
        assertEquals(String.format(Locale.getDefault(), "$%.1fK", 1.5), formatPrice.invoke(null, 1500.0));
    }
}
