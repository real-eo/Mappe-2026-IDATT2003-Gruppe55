package edu.ntnu.idi.idatt2003.millions.view.page;

import edu.ntnu.idi.idatt2003.millions.model.NetWorthSnapshot;
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

/**
 * Canvas-based chart component for rendering weekly net worth history.
 */
public class NetWorthChart extends Pane {

    private static final double PAD_LEFT = 72;
    private static final double PAD_RIGHT = 16;
    private static final double PAD_TOP = 16;
    private static final double PAD_BOTTOM = 36;

    private static final Color BG = Color.web("#111827");
    private static final Color GRID = Color.web("#1e293b");
    private static final Color LINE = Color.web("#3b82f6");
    private static final Color MUTED = Color.web("#94a3b8");

    private final Canvas canvas = new Canvas();
    private List<NetWorthSnapshot> data = List.of();

    /**
     * Creates an empty chart and binds it to this pane's size.
     */
    public NetWorthChart() {
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener(e -> draw());
        canvas.heightProperty().addListener(e -> draw());
        getChildren().add(canvas);
    }

    /**
     * Sets chart data and redraws the visualization.
     *
     * @param snapshots weekly net worth snapshots to plot
     */
    public void setData(List<NetWorthSnapshot> snapshots) {
        data = snapshots == null ? List.of() : snapshots;
        draw();
    }

    private void draw() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        gc.setFill(BG);
        gc.fillRoundRect(0, 0, w, h, 11, 11);

        if (data.size() < 2) {
            gc.setFill(MUTED);
            gc.setFont(Font.font("Inter", 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Advance weeks to see net worth history", w / 2, h / 2 + 4);
            return;
        }

        double chartW = w - PAD_LEFT - PAD_RIGHT;
        double chartH = h - PAD_TOP - PAD_BOTTOM;

        double minVal = data.stream().mapToDouble(s -> s.netWorth().doubleValue()).min().orElse(0);
        double maxVal = data.stream().mapToDouble(s -> s.netWorth().doubleValue()).max().orElse(1);

        double span = maxVal - minVal;
        if (span == 0) span = Math.max(1, maxVal * 0.1);
        double margin = span * 0.12;
        double yMin = minVal - margin;
        double yMax = maxVal + margin;
        double yRange = yMax - yMin;

        int wMin = data.get(0).week();
        int wMax = data.get(data.size() - 1).week();
        double xRange = Math.max(1, wMax - wMin);

        drawGrid(gc, chartW, chartH, yMin, yMax, yRange);
        drawXLabels(gc, chartW, chartH, xRange, wMin, wMax);
        drawFill(gc, chartW, chartH, yMin, yRange, xRange, wMin);
        drawLine(gc, chartW, chartH, yMin, yRange, xRange, wMin);
    }

    private void drawGrid(GraphicsContext gc, double chartW, double chartH,
                          double yMin, double yMax, double yRange) {
        int gridLines = 4;
        gc.setStroke(GRID);
        gc.setLineWidth(0.67);
        gc.setFont(Font.font("Consolas", 10));
        gc.setTextAlign(TextAlignment.RIGHT);

        for (int i = 0; i <= gridLines; i++) {
            double y = PAD_TOP + (chartH / gridLines) * i;
            gc.strokeLine(PAD_LEFT, y, PAD_LEFT + chartW, y);

            double val = yMax - (yRange / gridLines) * i;
            gc.setFill(MUTED);
            gc.fillText(formatAmount(val), PAD_LEFT - 6, y + 4);
        }
    }

    private void drawXLabels(GraphicsContext gc, double chartW, double chartH,
                              double xRange, int wMin, int wMax) {
        int tickCount = Math.min(data.size(), 7);
        gc.setStroke(GRID);
        gc.setLineWidth(0.67);
        gc.setFont(Font.font("Inter", 10));
        gc.setFill(MUTED);
        gc.setTextAlign(TextAlignment.CENTER);

        for (int i = 0; i < tickCount; i++) {
            int idx = (data.size() - 1) * i / Math.max(1, tickCount - 1);
            int week = data.get(idx).week();
            double x = PAD_LEFT + ((week - wMin) / xRange) * chartW;
            gc.fillText("W" + week, x, PAD_TOP + chartH + 22);
        }
    }

    private void drawFill(GraphicsContext gc, double chartW, double chartH,
                          double yMin, double yRange, double xRange, int wMin) {
        double bottom = PAD_TOP + chartH;
        gc.setFill(new LinearGradient(
            0, PAD_TOP, 0, bottom, false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#3b82f6", 0.22)),
            new Stop(1, Color.web("#3b82f6", 0.0))
        ));
        gc.beginPath();
        NetWorthSnapshot first = data.get(0);
        double x0 = PAD_LEFT + ((first.week() - wMin) / xRange) * chartW;
        double y0 = PAD_TOP + (1.0 - (first.netWorth().doubleValue() - yMin) / yRange) * chartH;
        gc.moveTo(x0, bottom);
        gc.lineTo(x0, y0);
        for (int i = 1; i < data.size(); i++) {
            NetWorthSnapshot s = data.get(i);
            double x = PAD_LEFT + ((s.week() - wMin) / xRange) * chartW;
            double y = PAD_TOP + (1.0 - (s.netWorth().doubleValue() - yMin) / yRange) * chartH;
            gc.lineTo(x, y);
        }
        NetWorthSnapshot last = data.get(data.size() - 1);
        gc.lineTo(PAD_LEFT + ((last.week() - wMin) / xRange) * chartW, bottom);
        gc.closePath();
        gc.fill();
    }

    private void drawLine(GraphicsContext gc, double chartW, double chartH,
                          double yMin, double yRange, double xRange, int wMin) {
        gc.setStroke(LINE);
        gc.setLineWidth(2.0);
        gc.beginPath();
        for (int i = 0; i < data.size(); i++) {
            NetWorthSnapshot s = data.get(i);
            double x = PAD_LEFT + ((s.week() - wMin) / xRange) * chartW;
            double y = PAD_TOP + (1.0 - (s.netWorth().doubleValue() - yMin) / yRange) * chartH;
            if (i == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.stroke();

        if (data.size() <= 30) {
            gc.setFill(LINE);
            for (NetWorthSnapshot s : data) {
                double x = PAD_LEFT + ((s.week() - wMin) / xRange) * chartW;
                double y = PAD_TOP + (1.0 - (s.netWorth().doubleValue() - yMin) / yRange) * chartH;
                gc.fillOval(x - 3, y - 3, 6, 6);
            }
        }
    }

    private String formatAmount(double value) {
        if (Math.abs(value) >= 1_000_000) {
            return String.format("$%.1fM", value / 1_000_000);
        }
        if (Math.abs(value) >= 1_000) {
            return String.format("$%.1fK", value / 1_000);
        }
        return String.format("$%.0f", value);
    }
}
