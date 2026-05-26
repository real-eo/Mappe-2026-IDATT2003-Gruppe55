package edu.ntnu.idi.idatt2003.millions.view.dashboard;

import edu.ntnu.idi.idatt2003.millions.controller.ExchangeController;
import edu.ntnu.idi.idatt2003.millions.controller.ProfileController;
import edu.ntnu.idi.idatt2003.millions.view.page.ProfilePage;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Root dashboard layout. Composes the header, stock panel, and portfolio panel.
 */
public class DashboardPage {

    private final ExchangeController controller;
    private BorderPane root;
    private Parent dashboardBody;
    private boolean showingProfile;
    private DashboardHeader header;
    private DashboardLeftPanel leftPanel;
    private DashboardRightPanel rightPanel;

    public DashboardPage() {
        this(null);
    }

    public DashboardPage(ExchangeController controller) {
        this.controller = controller;
    }

    /**
     * Builds and returns the dashboard root node.
     *
     * @return the root parent
     */
    public Parent createRoot() {
        root = new BorderPane();
        root.getStyleClass().add("dashboard-root");

        header = new DashboardHeader(controller, this::toggleProfileView, this::advanceWeekAndRefresh);
        root.setTop(header.createHeader());

        dashboardBody = createBody();
        root.setCenter(dashboardBody);
        return root;
    }

    private Parent createBody() {
        leftPanel = new DashboardLeftPanel(controller, this::refreshAll);
        rightPanel = new DashboardRightPanel(controller);

        VBox left = leftPanel.createPanel();
        VBox right = rightPanel.createPanel();
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox body = new HBox();
        body.getStyleClass().add("dashboard-body");
        body.getChildren().addAll(left, right);
        return body;
    }

    private void advanceWeekAndRefresh() {
        if (controller != null) {
            controller.advance();
        }
        refreshAll();
    }

    private void refreshAll() {
        header.refresh();
        leftPanel.refresh();
        rightPanel.refresh();
    }

    private void toggleProfileView() {
        showingProfile = !showingProfile;
        if (showingProfile) {
            ProfileController profileController = controller == null
                    ? null : new ProfileController(controller);
            root.setCenter(new ProfilePage(profileController).createRoot());
        } else {
            root.setCenter(dashboardBody);
        }
        header.updateProfileToggleLabel(showingProfile);
    }
}
