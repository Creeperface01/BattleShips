package spse.creeperface.battleships.util;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * @author CreeperFace
 */
public class FloatingAlert {

    private final Pane pane;
    private final Label label;

    public FloatingAlert(Pane pane) {
        this.pane = pane;

        Label alertLabel = new Label();
        alertLabel.setVisible(false);
        pane.getChildren().add(alertLabel);

        alertLabel.toFront();

        this.label = alertLabel;
    }

    public void error(String message) {

    }

    public void alert(String message) {

    }

    public void info(String message) {

    }
}
