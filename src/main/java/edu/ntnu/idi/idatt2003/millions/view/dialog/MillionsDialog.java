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

public abstract class MillionsDialog {

    private static final String STYLESHEET_PATH = "/styles/dashboard.css";

    protected Stage stage;

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

    protected abstract Node buildContent();

    protected void close() {
        stage.close();
    }

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
