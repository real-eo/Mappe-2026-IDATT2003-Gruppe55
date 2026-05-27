package edu.ntnu.idi.idatt2003.millions.view.dialog;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Base class for modal dialogs used in the Millions UI.
 */
public abstract class MillionsDialog {

    private static final String STYLESHEET_PATH = "/styles/dashboard.css";

    /**
     * Backing stage for this dialog instance.
     */
    protected Stage stage;

    /**
     * Creates a dialog instance.
     */
    protected MillionsDialog() {
    }

    /**
     * Shows the dialog as a modal window.
     *
     * @param owner the owner window, or null to show as standalone
     */
    public void show(Window owner) {
        if (!canShow()) {
            return;
        }

        stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }

        StackPane root = buildShell();
        Scene scene;
        if (owner != null) {
            scene = new Scene(root, owner.getWidth(), owner.getHeight());
            stage.setX(owner.getX());
            stage.setY(owner.getY());
        } else {
            scene = new Scene(root);
        }
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(resolveStylesheet());

        stage.setScene(scene);
        stage.setResizable(false);
        if (owner == null) {
            stage.sizeToScene();
            stage.centerOnScreen();
        }
        stage.showAndWait();
    }

    /**
     * Builds the dialog's content node.
     *
     * @return root content node for the dialog card
     */
    protected abstract Node buildContent();

    /**
     * Closes the dialog window.
     */
    protected void close() {
        stage.close();
    }

    /**
     * Indicates whether the dialog can be shown in its current state.
     *
     * @return true if dialog can be shown
     */
    protected boolean canShow() {
        return true;
    }

    private StackPane buildShell() {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("trade-overlay");
        overlay.setPadding(new Insets(40));

        StackPane card = new StackPane();
        card.getStyleClass().add("trade-dialog-card");
        card.setMaxWidth(520);
        card.setPrefWidth(520);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setPrefHeight(Region.USE_COMPUTED_SIZE);
        card.setMinHeight(Region.USE_PREF_SIZE);
        card.setOnMouseClicked(event -> event.consume());

        card.getChildren().add(buildContent());

        overlay.setOnMouseClicked(event -> close());
        overlay.getChildren().add(card);
        return overlay;
    }

    private String resolveStylesheet() {
        var url = getClass().getResource(STYLESHEET_PATH);
        if (url == null) {
            throw new IllegalStateException("Missing stylesheet: " + STYLESHEET_PATH);
        }
        return url.toExternalForm();
    }
}
