package spse.creeperface.battleships.game;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import spse.creeperface.battleships.BattleShips;
import spse.creeperface.battleships.game.map.Tile;
import spse.creeperface.battleships.game.map.TileEntry;
import spse.creeperface.battleships.game.player.PhysicalPlayer;
import spse.creeperface.battleships.util.Utils;

import java.util.Objects;

/**
 * @author CreeperFace
 */
@RequiredArgsConstructor
class GameController {

    private static final double MIN_AREA_SCALE = 0.04;
    private static final double MAX_AREA_SCALE = 1;

    private final Game game;

    private final Pane mainPane;
    private final Pane gamePane;

    @Getter
    private final GameArea area, opponentArea;

    @Getter
    private final ImageView borders, fades;

    private final VBox shipBox;
    private final ListView<Image> ships;

    @Getter(AccessLevel.PACKAGE)
    private final Label time, hits, missed, total, shipsLeft, currentPlayer;

    @Getter
    private final Button endTurnButton;

    private boolean initialized;

    @Getter
    private boolean processingLayout;

    @Setter
    @Getter
    private BoundingBox areaBounds;

    @Getter
    private Vector2i cursorPosition;
    @Getter
    private Vector2i currentTilePos;

    void initListeners() {
        Preconditions.checkState(!initialized, "GameController already initialized");
        initialized = true;

        gamePane.setOnMouseClicked(this::onMouseClick);
        gamePane.setOnScroll(this::onAreaScrolled);

        gamePane.widthProperty().addListener((o, old, val) -> onAreaChanged(val, null));
        gamePane.heightProperty().addListener((o, old, val) -> onAreaChanged(null, val));

        gamePane.setOnDragOver(this::onDragOverField);
        gamePane.setOnDragDone(DragEvent::consume);
        gamePane.setOnDragDropped(this::onShipDragDropped);
        gamePane.setOnMouseMoved(this::onMouseMoved);

        endTurnButton.setOnAction(this::onEndTurnButtonClick);
    }

    private void onEndTurnButtonClick(ActionEvent e) {
        PhysicalPlayer p = game.getLocalPlayer();

        if (game.getGamePhase() == Game.GamePhase.SELECTING || game.getCurrentPlayer() == p) {
            p.setResult(null);
        }
    }

    private void onMouseMoved(MouseEvent e) {
        this.cursorPosition = new Vector2i(e.getX(), e.getY());

        Vector2i tilePos = currentTilePos;
        currentTilePos = getTilePos(this.cursorPosition);

        if (!Objects.equals(tilePos, this.currentTilePos)) {
            fades.setImage(GameSceneBuilder.onSelectedTileChanged(this.game, this.gamePane));
        }
    }

    void onAreaChanged() {
        onAreaChanged(false);
    }

    void onAreaChanged(boolean immediate) {
        onAreaChanged(null, null, immediate);
    }

    private void onAreaChanged(Number width, Number height) {
        onAreaChanged(width, height, false);
    }

    private void onAreaChanged(Number width, Number height, boolean immediate) {
        Runnable r = () -> {
            if (area.getPane().isVisible()) {
                borders.setImage(GameSceneBuilder.drawBorders(game, gamePane, area.getPane()));
            }

            if (opponentArea.getPane().isVisible()) {
                borders.setImage(GameSceneBuilder.drawBorders(game, gamePane, opponentArea.getPane()));
            }
        };

        if (immediate) {
            r.run();
            return;
        }

        Platform.runLater(r);
    }

    private void onMouseClick(MouseEvent e) {
        if (game.getGamePhase() != Game.GamePhase.ATTACKING || processingLayout || !game.getCurrentPlayer().isLocal()) {
            return;
        }

        Vector2i pos = getTilePosFromCoords(e.getX(), e.getY());

        if (pos == null) {
            return;
        }

        Tile tile = game.getTile(pos, game.getOpponentTiles());

        if (tile != Tile.UNKNOWN && tile != Tile.SHIP) {
            return;
        }

        PhysicalPlayer p = this.game.getLocalPlayer();

        if (!p.roundFinished()) {
            p.setResult(pos);
        }
    }

    private void onAreaScrolled(ScrollEvent e) {
        if (processingLayout) {
            return;
        }
        double delta = e.getDeltaY();

        double scale = area.getPane().getScaleX();

        if (delta > 0) {
            scale *= 1.2;
        } else {
            scale /= 1.2;
        }

        scale = GenericMath.clamp(scale, MIN_AREA_SCALE, MAX_AREA_SCALE);

        area.getPane().setScaleX(scale);
        area.getPane().setScaleY(scale);
        opponentArea.getPane().setScaleX(scale);
        opponentArea.getPane().setScaleY(scale);

        onAreaChanged();
        if (this.currentTilePos != null) {
            this.fades.setImage(GameSceneBuilder.onSelectedTileChanged(this.game, this.gamePane));
        }
    }

    void onShipDragDetected(ListCell<Image> cell, MouseEvent e) {
        if (cell.getItem() == null || game.getGamePhase() != Game.GamePhase.SELECTING) {
            return;
        }

        Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
        dragboard.setDragView(cell.getItem());

        ClipboardContent content = new ClipboardContent();
        content.putString(GameSceneBuilder.ShipCell.SHIP);
        dragboard.setContent(content);

        e.consume();
    }

    private void onShipDragDropped(DragEvent e) {
        try {
            if (game.getGamePhase() != Game.GamePhase.SELECTING) {
                return;
            }

            Dragboard db = e.getDragboard();
            boolean success = false;

            Node node = (Node) e.getSource();

            if (db.hasString() && db.getString().equals(GameSceneBuilder.ShipCell.SHIP) && node == this.gamePane) {
                Vector2i pos = getTilePosFromCoords(e.getX(), e.getY());

                if (pos != null) {
                    Tile tile = game.getTile(pos);

                    if (tile == Tile.UNKNOWN && checkNeighbours(pos)) {
                        success = true;

                        this.game.setTile(pos, Tile.SHIP);
                        updateTiles(pos, Tile.SHIP);
                        this.game.getLocalPlayer().placeShip();
                        ships.getItems().remove(0);
                    }
                }
            }

            e.setDropCompleted(success);
            e.consume();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void onDragOverField(DragEvent e) {
        if (e.getDragboard().hasString()) {
            e.acceptTransferModes(TransferMode.MOVE);
        }

        e.consume();
    }

    void onGameStatsCloseBtnClick(Stage stage) {
        BattleShips main = game.getMain();

        if (stage != null) {
            stage.close();
        }

        Utils.switchScene(main.getPrimaryStage(), main.getMainScene());
    }


    void updateTiles(Vector2i pos, Tile tile) {
        updateTiles(TileEntry.of(pos, tile));
    }

    void updateTiles(TileEntry... entries) {
        updateTiles(area, entries);
    }

    void updateOpponentTiles(Vector2i pos, Tile tile) {
        updateOpponentTiles(TileEntry.of(pos, tile));
    }

    void updateOpponentTiles(TileEntry... entries) {
        updateTiles(opponentArea, entries);
    }

    void updateTiles(GameArea area, TileEntry... entries) {
        if (entries.length <= 0) {
            return;
        }

        if (game.getGamePhase() == Game.GamePhase.SELECTING) {
            GameSceneBuilder.updateBackground(area.getPane(), entries);
            return;
        }

        processingLayout = true;

        Animation a1 = new Transition() {

            {
                setCycleDuration(Duration.millis(300));
            }

            @Override
            protected void interpolate(double frac) {
                for (TileEntry entry : entries) {
                    Canvas canvas = area.getNode(entry.getPos());

                    if (canvas == null) {
                        continue;
                    }

                    canvas.setScaleX(1 - frac);
                }
            }
        };

        Animation a2 = new Transition() {

            {
                setCycleDuration(Duration.millis(300));
            }

            @Override
            protected void interpolate(double frac) {
                for (TileEntry entry : entries) {
                    Canvas canvas = area.getNode(entry.getPos());

                    if (canvas == null) {
                        continue;
                    }

                    canvas.setScaleX(frac);
                }
            }
        };

        a1.setOnFinished(e -> {
            GameSceneBuilder.updateBackground(area.getPane(), entries);
            a2.play();
        });

        a2.setOnFinished(e -> processingLayout = false);

        a1.play();
    }

    Vector2i getTilePos(Vector2i pos) {
        return getTilePosFromCoords(pos.getX(), pos.getY());
    }

    Vector2i getTilePosFromCoords(double x, double y) {
        if (!this.areaBounds.contains(x, y)) {
            return null;
        }

        int tileX = (int) Math.floor((x - areaBounds.getMinX()) / ((areaBounds.getMaxX() - areaBounds.getMinX()) / game.getOptions().getLengthX()));
        int tileY = (int) Math.floor((y - areaBounds.getMinY()) / ((areaBounds.getMaxY() - areaBounds.getMinY()) / game.getOptions().getLengthY()));

        return new Vector2i(tileX, tileY);
    }

    BoundingBox getTileBounds(Vector2i pos) {
        Bounds bb = gamePane.getBoundsInLocal();

        double offsetX = areaBounds.getMinX() - bb.getMinX();
        double offsetY = areaBounds.getMinY() - bb.getMinY();

        double minX = offsetX + pos.getX() * (areaBounds.getWidth() / game.getOptions().getLengthX());
        double maxX = offsetX + (pos.getX() + 1) * (areaBounds.getWidth() / game.getOptions().getLengthX());

        double minY = offsetY + pos.getY() * (areaBounds.getHeight() / game.getOptions().getLengthY());
        double maxY = offsetY + (pos.getY() + 1) * (areaBounds.getHeight() / game.getOptions().getLengthY());

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    private boolean checkNeighbours(Vector2i pos) {
        for (Vector2i vec : Utils.getNeighbours(pos)) {
            Tile t = game.getTile(vec);

            if (t != null && t != Tile.UNKNOWN) {
                return false;
            }
        }

        return true;
    }

    void pulseTile(GameArea area, Vector2i pos, Color color) {
        processingLayout = true;
        Canvas node = area.getNode(pos);

        if (node == null) {
            return;
        }

        Canvas copy = Utils.cloneCanvas(node);

        GraphicsContext gc = node.getGraphicsContext2D();

        gc.setFill(color);
        gc.fillRect(0, 0, copy.getWidth(), copy.getHeight());

        node.toFront();

        Transition pulse = new Transition() {

            {
                setCycleDuration(Duration.millis(1000));
            }

            @Override
            protected void interpolate(double frac) {
                double scale = 1 + (Math.sin(frac * Math.PI) * 0.7);

                node.setScaleX(scale);
                node.setScaleY(scale);
            }
        };

        pulse.setOnFinished(e -> area.setNode(pos, copy));

        pulse.play();
    }

    void turnMap(Node toHide, Node toShow) {
        turnMap(toHide, toShow, null);
    }

    void turnMap(Node toHide, Node toShow, Runnable onComplete) {
        processingLayout = true;
        toShow.setVisible(false);
        borders.setVisible(false);
        fades.setVisible(false);
        double currentScale = toHide.getScaleY();

        Transition hide = new Transition() {

            {
                setCycleDuration(new Duration(1000));
            }

            @Override
            protected void interpolate(double frac) {
                double f = (1 - frac);

                toHide.setScaleY(currentScale * f);
            }
        };

        Transition show = new Transition() {
            {
                setCycleDuration(new Duration(1000));
            }

            @Override
            protected void interpolate(double frac) {
                toShow.setScaleY(currentScale * frac);
            }
        };

        hide.setOnFinished(e -> {
            toHide.setVisible(false);

            toShow.setScaleY(0);
            toShow.setVisible(true);

            show.play();
        });

        show.setOnFinished(e -> {
            processingLayout = false;

            if (onComplete != null) {
                onComplete.run();
            }

            borders.setVisible(true);
            fades.setVisible(true);
        });

        hide.play();
    }

    void showEndStage() {
        Stage stage = new Stage();
        stage.setTitle("Výsledky hry");
        stage.setHeight(500);
        stage.setWidth(300);
        stage.initOwner(game.getMain().getPrimaryStage());
        stage.initModality(Modality.APPLICATION_MODAL);

        Parent root = GameSceneBuilder.createEndGameScene(this, stage, this.game.getStats());
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> onGameStatsCloseBtnClick(null));


        stage.showAndWait();
    }
}
