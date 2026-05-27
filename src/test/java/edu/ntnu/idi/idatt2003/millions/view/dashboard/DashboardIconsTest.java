package edu.ntnu.idi.idatt2003.millions.view.dashboard;

import javafx.scene.shape.SVGPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardIconsTest {

    @Test
    void createIcon_setsPathAndStyleClass() {
        SVGPath icon = DashboardIcons.createIcon(DashboardIcons.SEARCH_PATH, "search-icon", 24.0);

        assertEquals(DashboardIcons.SEARCH_PATH, icon.getContent());
        assertTrue(icon.getStyleClass().contains("search-icon"));
    }

    @Test
    void createIcon_scalesRelativeToBaseSize() {
        SVGPath icon = DashboardIcons.createIcon(DashboardIcons.STAR_PATH, "star", 12.0);

        assertEquals(0.5, icon.getScaleX(), 0.0001);
        assertEquals(0.5, icon.getScaleY(), 0.0001);
    }

    @Test
    void createIcon_withZeroSize_setsZeroScale() {
        SVGPath icon = DashboardIcons.createIcon(DashboardIcons.NEUTRAL_PATH, "neutral", 0.0);

        assertEquals(0.0, icon.getScaleX(), 0.0001);
        assertEquals(0.0, icon.getScaleY(), 0.0001);
    }
}
