package edu.ntnu.idi.idatt2003.millions.view;

import javafx.scene.shape.SVGPath;

final class DashboardIcons {

    static final double ICON_BASE_SIZE = 24.0;

    static final String LOGO_PATH = "M3 16 L9 10 L13 14 L20 7 L21 8 L13 16 L9 12 L4 17 Z";
    static final String CLOCK_PATH = "M12 4 A8 8 0 1 0 12 20 A8 8 0 1 0 12 4 M12 8 V12 L15 14";
    static final String SEARCH_PATH = "M11 4 A7 7 0 1 0 11 18 A7 7 0 1 0 11 4 M16 16 L20 20";
    static final String STAR_PATH = "M12 3 L14.8 8.6 L21 9.2 L16 13.2 L17.4 19.2 L12 16 L6.6 19.2 L8 13.2 L3 9.2 L9.2 8.6 Z";
    static final String ARROW_UP_PATH = "M12 5 L18 11 H14 V19 H10 V11 H6 Z";
    static final String ARROW_DOWN_PATH = "M12 19 L6 13 H10 V5 H14 V13 H18 Z";
    static final String ARROW_RIGHT_PATH = "M5 12 H17 M13 8 L17 12 L13 16";
    static final String NEUTRAL_PATH = "M5 12 H19";
    static final String PIE_PATH = "M12 3 A9 9 0 1 0 12 21 A9 9 0 1 0 12 3 M12 3 V12 H21 A9 9 0 0 0 12 3";

    private DashboardIcons() {
    }

    static SVGPath createIcon(String path, String styleClass, double size) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.getStyleClass().add(styleClass);
        double scale = size / ICON_BASE_SIZE;
        icon.setScaleX(scale);
        icon.setScaleY(scale);
        return icon;
    }
}
