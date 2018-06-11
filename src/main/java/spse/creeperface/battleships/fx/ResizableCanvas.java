package spse.creeperface.battleships.fx;

import javafx.beans.binding.ObjectBinding;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

/**
 * @author CreeperFace
 */
public class ResizableCanvas extends Canvas {

    private double ratio;

    public ResizableCanvas(Image image) {
        this(image.getWidth(), image.getHeight());
    }

    public ResizableCanvas(Canvas canvas) {
        this(canvas.getWidth(), canvas.getHeight());
    }

    public ResizableCanvas(double width, double height) {
        super(width, height);
        this.ratio = width / height;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    public Image toImage() {
        return snapshot(new SnapshotParameters(), new WritableImage((int) getWidth(), (int) getHeight()));
    }

    public void bindTo(Pane parent) {
        this.heightProperty().bind(new ObjectBinding<Number>() {
            @Override
            protected Number computeValue() {
                double parentWidth = parent.getWidth();

                return parentWidth * (1 / ratio);
            }
        });

        this.widthProperty().bind(new ObjectBinding<Number>() {
            @Override
            protected Number computeValue() {
                double parentHeight = parent.getHeight();

                return parentHeight * ratio;
            }
        });
    }
}
