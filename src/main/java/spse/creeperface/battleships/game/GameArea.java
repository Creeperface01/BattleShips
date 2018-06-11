package spse.creeperface.battleships.game;

import com.flowpowered.math.vector.Vector2i;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.GridPane;
import lombok.Getter;

/**
 * @author CreeperFace
 */
public class GameArea {

    @Getter
    private final GridPane pane;

    private final Canvas[] LOOKUP = new Canvas[256 * 256];

    public GameArea(GridPane pane) {
        this.pane = pane;

        mapEntries();
    }

    Canvas getNode(Vector2i pos) {
        if (pos.getY() < 0 || pos.getY() > 255 || pos.getX() < 0 || pos.getX() > 255) {
            return null;
        }

        return LOOKUP[Game.positionHash(pos)];
    }

    void setNode(Vector2i pos, Canvas node) {
        if (pos.getY() < 0 || pos.getY() > 255 || pos.getX() < 0 || pos.getX() > 255) {
            return;
        }

        LOOKUP[Game.positionHash(pos)] = node;
        pane.add(node, pos.getX(), pos.getY());
    }

    private void mapEntries() {
        this.pane.getChildren().forEach(node -> {
            LOOKUP[Game.positionHash(new Vector2i(GridPane.getColumnIndex(node), GridPane.getRowIndex(node)))] = (Canvas) node;
        });
    }
}
