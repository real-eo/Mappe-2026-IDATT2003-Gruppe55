package edu.ntnu.idi.idatt2003.millions.view.dashboard;

import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.shape.SVGPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterButtonTest {

    @Test
    void constructor_setsExpectedStyleGraphicAndFocusBehavior() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            FilterButton button = new FilterButton();

            assertTrue(button.getStyleClass().contains("filter-button"));
            assertFalse(button.isFocusTraversable());
            assertNotNull(button.getGraphic());
            assertTrue(button.getGraphic() instanceof SVGPath);
        });
    }
}
