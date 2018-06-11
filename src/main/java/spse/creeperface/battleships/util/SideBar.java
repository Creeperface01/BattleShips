package spse.creeperface.battleships.util;

/**
 * @author CreeperFace
 */

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SideBar extends VBox {

    private final Animation hide;
    private final Animation show;

    public SideBar(String id, Node... nodes) {
        setId(id);
        getChildren().addAll(nodes);
        setVisible(false);

        hide = new Transition() {

            private double startWidth;

            {
                setCycleDuration(Duration.millis(250));
            }

            @Override
            public void play() {
                startWidth = SideBar.this.getWidth();
                super.play();
            }

            protected void interpolate(double frac) {
                final double curWidth = startWidth * (1d - frac);
                setTranslateX(-startWidth + curWidth);
            }
        };


        hide.onFinishedProperty().set(e -> setVisible(false));

        show = new Transition() {

            private double startWidth;

            {
                setCycleDuration(Duration.millis(250));
            }

            @Override
            public void play() {
                startWidth = SideBar.this.getWidth();
                super.play();
            }

            protected void interpolate(double frac) {
                final double curWidth = startWidth * frac;
                setTranslateX(-startWidth + curWidth);
            }
        };

        this.setOnMouseEntered(e -> show());
        this.setOnMouseExited(e -> hide());
    }

    public void show() {
        if (show.getStatus() == Animation.Status.STOPPED && hide.getStatus() == Animation.Status.STOPPED) {
            if (!isVisible()) {
                setVisible(true);
                show.play();
            }
        }
    }

    public void hide() {
        if (hide.getStatus() != Animation.Status.STOPPED) {
            return;
        }

        if (show.getStatus() != Animation.Status.STOPPED) {
            show.stop();
        }

        if (isVisible()) {
            hide.play();
        }
    }
}
