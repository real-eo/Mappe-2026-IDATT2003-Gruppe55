package edu.ntnu.idi.idatt2003.millions.view.dashboard;

import javafx.scene.control.Button;

final class FilterButton extends Button {

    private static final String FILTER_PATH = "M4 5 H20 L14 13 V19 L10 17 V13 Z";

    FilterButton() {
        getStyleClass().add("filter-button");
        setGraphic(DashboardIcons.createIcon(FILTER_PATH, "icon-stroke-muted", 14));
        setFocusTraversable(false);
    }
}
