package edu.ntnu.idi.idatt2003.millions.view.dialog;

import java.math.BigDecimal;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

final class StockPriceChart extends Pane {

    private static final double PAD_LEFT = 58;
    private static final double PAD_RIGHT = 12;
    private static final double PAD_TOP = 12;
    private static final double PAD_BOTTOM = 26;

    private static final Color BG   = Color.web("#1a1f2e");
    private static final Color GRID = Color.web("#1e293b");
    private static final Color MUTED = Color.web("#64748b");

    private final Canvas canvas = new Canvas();
    private List<BigDecimal> prices = List.of();

    StockPriceChart() {
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener(e -> draw());
        canvas.heightProperty().addListener(e -> draw());
        getChildren().add(canvas);
    }

    void setPrices(List<BigDecimal> prices) {
        this.prices = prices == null ? List.of() : prices;
        draw();
    }

    private void draw() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        gc.setFill(BG);
        gc.fillRoundRect(0, 0, w, h, 9, 9);

        if (prices.size() < 2) {
            gc.setFill(MUTED);
            gc.setFont(Font.font("Inter", 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Advance weeks to see price history", w / 2, h / 2 + 4);
            return;
        }

        double chartW = w - PAD_LEFT - PAD_RIGHT;
        double chartH = h - PAD_TOP - PAD_BOTTOM;

        double minVal = prices.stream().mapToDouble(BigDecimal::doubleValue).min().orElse(0);
        double maxVal = prices.stream().mapToDouble(BigDecimal::doubleValue).max().orElse(1);
        double span = maxVal - minVal;
        if (span == 0) span = Math.max(1, maxVal * 0.1);
        double margin = span * 0.12;
        double yMin = minVal - margin;
        double yMax = maxVal + margin;
        double yRange = yMax - yMin;

        boolean up = prices.get(prices.size() - 1).compareTo(prices.get(0)) >= 0;
        Color line = up ? Color.web("#10b981") : Color.web("#ef4444");

        drawGrid(gc, chartW, chartH, yMax, yRange);
        drawXLabels(gc, chartW, chartH);
        drawFill(gc, chartW, chartH, yMin, yRange, line);
        drawLine(gc, chartW, chartH, yMin, yRange, line);
    }

    private void drawGrid(GraphicsContext gc, double chartW, double chartH, double yMax, double yRange) {
        int gridLines = 3;
        gc.setStroke(GRID);
        gc.setLineWidth(0.67);
        gc.setFont(Font.font("Consolas", 9));
        gc.setTextAlign(TextAlignment.RIGHT);

        for (int i = 0; i <= gridLines; i++) {
            double y = PAD_TOP + (chartH / gridLines) * i;
            gc.strokeLine(PAD_LEFT, y, PAD_LEFT + chartW, y);
            double val = yMax - (yRange / gridLines) * i;
            gc.setFill(MUTED);
            gc.fillText(formatPrice(val), PAD_LEFT - 5, y + 4);
        }
    }

    private void drawXLabels(GraphicsContext gc, double chartW, double chartH) {
        int n = prices.size();
        int tickCount = Math.min(n, 6);
        gc.setFont(Font.font("Inter", 9));
        gc.setFill(MUTED);
        gc.setTextAlign(TextAlignment.CENTER);

        for (int i = 0; i < tickCount; i++) {
            int idx = (n - 1) * i / Math.max(1, tickCount - 1);
            double x = PAD_LEFT + ((double) idx / Math.max(1, n - 1)) * chartW;
            gc.fillText("W" + (idx + 1), x, PAD_TOP + chartH + 18);
        }
    }

    private void drawFill(GraphicsContext gc, double chartW, double chartH,
                          double yMin, double yRange, Color lineColor) {
        int n = prices.size();
        double bottom = PAD_TOP + chartH;
        gc.setFill(new LinearGradient(0, PAD_TOP, 0, bottom, false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 0.20)),
            new Stop(1, Color.color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 0.0))));

        gc.beginPath();
        double x0 = PAD_LEFT;
        double y0 = PAD_TOP + (1.0 - (prices.get(0).doubleValue() - yMin) / yRange) * chartH;
        gc.moveTo(x0, bottom);
        gc.lineTo(x0, y0);
        for (int i = 1; i < n; i++) {
            double x = PAD_LEFT + ((double) i / (n - 1)) * chartW;
            double y = PAD_TOP + (1.0 - (prices.get(i).doubleValue() - yMin) / yRange) * chartH;
            gc.lineTo(x, y);
        }
        gc.lineTo(PAD_LEFT + chartW, bottom);
        gc.closePath();
        gc.fill();
    }

    private void drawLine(GraphicsContext gc, double chartW, double chartH,
                          double yMin, double yRange, Color lineColor) {
        int n = prices.size();
        gc.setStroke(lineColor);
        gc.setLineWidth(1.8);
        gc.beginPath();
        for (int i = 0; i < n; i++) {
            double x = PAD_LEFT + ((double) i / (n - 1)) * chartW;
            double y = PAD_TOP + (1.0 - (prices.get(i).doubleValue() - yMin) / yRange) * chartH;
            if (i == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.stroke();

        if (n <= 20) {
            gc.setFill(lineColor);
            for (int i = 0; i < n; i++) {
                double x = PAD_LEFT + ((double) i / (n - 1)) * chartW;
                double y = PAD_TOP + (1.0 - (prices.get(i).doubleValue() - yMin) / yRange) * chartH;
                gc.fillOval(x - 2.5, y - 2.5, 5, 5);
            }
        }
    }

    private static String formatPrice(double value) {
        if (Math.abs(value) >= 1_000) {
            return String.format("$%.1fK", value / 1_000);
        }
        return String.format("$%.2f", value);
    }
}
