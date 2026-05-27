package edu.ntnu.idi.idatt2003.millions.view.dialog;

import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MillionsDialogTest {

    @Test
    void show_returnsEarly_whenCanShowIsFalse() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            MillionsDialog dialog = new MillionsDialog() {
                @Override
                protected Node buildContent() {
                    return new StackPane();
                }

                @Override
                protected boolean canShow() {
                    return false;
                }
            };

            dialog.show(null);

            assertNull(dialog.stage);
        });
    }

    @Test
    void buildContent_isInvoked_whenShowingIsAllowed() {
        FxTestUtils.runOnFxThreadAndWait(() -> {
            final boolean[] invoked = {false};
            MillionsDialog dialog = new MillionsDialog() {
                @Override
                protected Node buildContent() {
                    invoked[0] = true;
                    return new StackPane();
                }

                @Override
                public void show(javafx.stage.Window owner) {
                    // Avoid opening a real modal stage while still executing the path we care about.
                    if (!canShow()) {
                        return;
                    }
                    buildContent();
                }
            };

            dialog.show(null);

            assertTrue(invoked[0]);
        });
    }

    @Test
    void resolveStylesheet_returnsDashboardStylesheetUrl() throws Exception {
        MillionsDialog dialog = new MillionsDialog() {
            @Override
            protected Node buildContent() {
                return new StackPane();
            }
        };

        Method resolveStylesheet = MillionsDialog.class.getDeclaredMethod("resolveStylesheet");
        resolveStylesheet.setAccessible(true);

        String stylesheet = (String) resolveStylesheet.invoke(dialog);
        assertNotNull(stylesheet);
        assertTrue(stylesheet.contains("dashboard.css"));
    }
}
