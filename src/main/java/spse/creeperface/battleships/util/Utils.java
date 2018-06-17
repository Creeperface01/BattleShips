package spse.creeperface.battleships.util;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2i;
import com.google.common.collect.Lists;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.experimental.UtilityClass;
import spse.creeperface.battleships.fx.ResizableCanvas;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.function.IntSupplier;

/**
 * @author CreeperFace
 */
@UtilityClass
public class Utils {

    private static final int ALPHABET_LENGTH = 'z' - 'a';
    private static final int START_CHAR = 'a';

    private static final List<String> botNames = Lists.newArrayList("Karel", "Robin", "Mikuláš", "František", "Evžen", "Horvác", "Jan", "Jakub", "Artur", "Dominik", "Milan", "Lukáš", "Jiří", "Matěj", "Matyáš");

    private static final Vector2i[] SIDES = new Vector2i[]{new Vector2i(1, 0), new Vector2i(0, 1), new Vector2i(-1, 0), new Vector2i(0, -1)};

    public Character checkNickname(String s) {
        for (char c : s.toCharArray()) {
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-')) {
                return c;
            }
        }

        return null;
    }

    public void showAlert(Alert.AlertType alertType, Window owner, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);

        alert.showAndWait();
    }

    public void switchScene(Stage stage, Scene scene) {
        double prevWidth = 100;
        double prevHeight = 100;

        Scene old = stage.getScene();
        if (old != null) {
            prevWidth = old.getWidth();
            prevHeight = old.getHeight();
        }

        if (old == null) {
            stage.setMaximized(true);
        } else {
            stage.setWidth(prevWidth);
            stage.setHeight(prevHeight);
        }

        stage.setScene(scene);
    }

    public void switchScene(Stage stage, Parent root) {
        double prevWidth = 100;
        double prevHeight = 100;

        Scene old = stage.getScene();
        if (old != null) {
            prevWidth = old.getWidth();
            prevHeight = old.getHeight();
        }

        Scene scene = new Scene(root, prevWidth, prevHeight);

        if (old == null) {
            stage.setMaximized(true);
        }

        stage.setScene(scene);
    }

    public void showTip(Control node, String message) {
        Tooltip tip = new Tooltip(message);
        node.setTooltip(tip);
    }

    public String getAlphabetNumber(int i) {
        return getAlphabetNumber(i, "");
    }

    private String getAlphabetNumber(int i, String prev) {
        prev += (char) (START_CHAR + (i % ALPHABET_LENGTH));

        if (i <= ALPHABET_LENGTH) {
            return prev;
        }

        return getAlphabetNumber(i / ALPHABET_LENGTH, prev);
    }

    public Vector2i[] getNeighbours(Vector2i vec) {
        Vector2i[] sides = new Vector2i[SIDES.length];

        for (int i = 0; i < sides.length; i++) {
            sides[i] = vec.add(SIDES[i]);
        }

        return sides;
    }

    public String boundsToString(Bounds bounds) {
        return "(minX: " + bounds.getMinX() + ", maxX: " + bounds.getMaxX() + ", minY: " + bounds.getMinY() + ", maxY: " + bounds.getMaxY() + ")";
    }

    public String pickRandomBotName(String exception) {
        int i = new Random().nextInt(botNames.size());
        String name = botNames.get(i);

        if (name.equalsIgnoreCase(exception)) {
            name = i >= (botNames.size() - 1) ? botNames.get(0) : botNames.get(i + 1);
        }

        return name;
    }

    public Image cloneImage(Image image) {
        WritableImage img = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter writer = img.getPixelWriter();
        PixelReader reader = image.getPixelReader();

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                writer.setColor(x, y, reader.getColor(x, y));
            }
        }

        return img;
    }

    public ResizableCanvas cloneCanvas(Canvas canvas) {
        WritableImage image = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        ResizableCanvas c = new ResizableCanvas(canvas.getWidth(), canvas.getHeight());

        canvas.snapshot(new SnapshotParameters(), image);
        c.getGraphicsContext2D().drawImage(image, 0, 0);

        return c;
    }

    public void addNumberFilder(TextField field, int minVal, IntSupplier maxVal) {
        field.addEventFilter(KeyEvent.KEY_TYPED, e -> checkNumber(e, field, maxVal.getAsInt()));
        field.focusedProperty().addListener((v, oldVal, newVal) -> checkRange(field, minVal, maxVal.getAsInt()));
    }

    public void addNumberFilder(TextField field, IntSupplier minVal, int maxVal) {
        field.addEventFilter(KeyEvent.KEY_TYPED, e -> checkNumber(e, field, maxVal));
        field.focusedProperty().addListener((v, oldVal, newVal) -> checkRange(field, minVal.getAsInt(), maxVal));
    }

    public void addNumberFilder(TextField field, IntSupplier minVal, IntSupplier maxVal) {
        field.addEventFilter(KeyEvent.KEY_TYPED, e -> checkNumber(e, field, maxVal.getAsInt()));
        field.focusedProperty().addListener((v, oldVal, newVal) -> checkRange(field, minVal.getAsInt(), maxVal.getAsInt()));
    }

    public void addNumberFilder(TextField field, int minVal, int maxVal) {
        field.addEventFilter(KeyEvent.KEY_TYPED, e -> checkNumber(e, field, maxVal));
        field.focusedProperty().addListener((v, oldVal, newVal) -> checkRange(field, minVal, maxVal));
    }

    public void checkRange(TextField field, int minVal, int maxVal) {
        try {
            int val = Integer.parseInt(field.getText());

            int bounded = GenericMath.clamp(val, minVal, maxVal);

            if (val != bounded) {
                field.setText("" + bounded);
                field.positionCaret(field.getText().length());
            }
        } catch (NumberFormatException ex) {
            field.setText("" + minVal);
        }
    }

    private void checkNumber(KeyEvent e, TextField field, int maxVal) {
        String c = e.getCharacter();

        if (!c.matches("[0-9]")) {
            e.consume();
            return;
        }

        try {
            int val = Integer.parseInt(field.getText() + c);

            int bounded = GenericMath.clamp(val, 1, maxVal);

            if (val != bounded) {
                e.consume();
                field.setText("" + bounded);
                field.positionCaret(field.getText().length());
            }
        } catch (NumberFormatException ex) {
            e.consume();
        }
    }

    public Vector2d mouseCoords(Pane root) {
        Scene scene = root.getScene();

        Point pointerLocation = MouseInfo.getPointerInfo().getLocation();

        double sceneX = pointerLocation.getX();
        sceneX -= scene.getWindow().getX();
        sceneX -= scene.getX();
        sceneX -= root.getLayoutX();

        double sceneY = pointerLocation.getY();
        sceneY -= scene.getWindow().getY();
        sceneY -= scene.getY();
        sceneY -= root.getLayoutY();

        return new Vector2d(sceneX, sceneY);
    }
}