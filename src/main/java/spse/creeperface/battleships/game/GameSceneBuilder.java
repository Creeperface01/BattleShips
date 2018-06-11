package spse.creeperface.battleships.game;

import com.flowpowered.math.vector.Vector2i;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.experimental.UtilityClass;
import spse.creeperface.battleships.fx.ResizableCanvas;
import spse.creeperface.battleships.game.map.Tile;
import spse.creeperface.battleships.game.map.TileEntry;
import spse.creeperface.battleships.game.statistics.GameStatistic;
import spse.creeperface.battleships.game.statistics.Stat;
import spse.creeperface.battleships.util.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author CreeperFace
 */
@UtilityClass
public class GameSceneBuilder {

    private static final Map<ResourceType, Image> images = new EnumMap<>(ResourceType.class);

    static {
        loadImages();
    }

    public static void init() {

    }

    static Pane createGameScene(Game game) {
        AnchorPane root = new AnchorPane();

        VBox shipBox = new VBox();
        shipBox.setAlignment(Pos.TOP_CENTER);
        shipBox.setStyle("-fx-border-color: red; -fx-border-width: 1; -fx-background-color: white");

        StackPane gamePane = new StackPane();
        gamePane.setAlignment(Pos.CENTER);
        gamePane.setStyle("-fx-border-color: blue; -fx-border-width: 1");

        Image ship = images.get(ResourceType.SHIP);

        Image[] shipArray = new Image[game.getOptions().getShipCount()];
        Arrays.fill(shipArray, ship);

        ListView<Image> ships = new ListView<>(FXCollections.observableArrayList(shipArray));
        ships.toFront();

        ships.setCellFactory(listView -> {
            ShipCell cell = new ShipCell(shipBox);
            cell.setOnDragDetected(e -> game.getController().onShipDragDetected(cell, e));

            return cell;
        });

        ships.setMaxWidth(200);
        GridPane gameArea = createGameArea(game);
        GridPane opponentArea = createGameArea(game);

        opponentArea.setVisible(false);

        ImageView borders = new ImageView();
        borders.toFront();
        borders.fitHeightProperty().bind(gamePane.heightProperty());
        borders.fitWidthProperty().bind(gamePane.widthProperty());
        borders.setPreserveRatio(true);

        ImageView fades = new ImageView();
        fades.fitHeightProperty().bind(gamePane.heightProperty());
        fades.fitWidthProperty().bind(gamePane.widthProperty());
        fades.setPreserveRatio(true);

        GridPane score = new GridPane();
        score.setAlignment(Pos.CENTER);
        score.setHgap(5);
        score.setVgap(5);

        Label time = new Label("0");
        Label hits = new Label("0");
        Label missed = new Label("0");
        Label total = new Label("0");
        Label shipsLeft = new Label("0");
        Label currentPlayer = new Label("0");

        score.add(new Label("Hráč na tahu:"), 0, 0);
        score.add(currentPlayer, 1, 0);

        score.add(new Label("Zbývající čas:"), 0, 1);
        score.add(time, 1, 1);

        score.add(new Label("Trefeno:"), 0, 2);
        score.add(hits, 1, 2);

        score.add(new Label("Netrefeno: "), 0, 3);
        score.add(missed, 1, 3);

        score.add(new Label("Počet tahů: "), 0, 4);
        score.add(total, 1, 4);

        score.add(new Label("Lodě k sestřelení: "), 0, 5);
        score.add(shipsLeft, 1, 5);

        Button endTurn = new Button("Ukončit tah");
        endTurn.setAlignment(Pos.CENTER);
        endTurn.setVisible(false);
        VBox.setMargin(endTurn, new Insets(20, 0, 0, 0));

        shipBox.getChildren().addAll(ships, score, endTurn);
        gamePane.getChildren().addAll(gameArea, opponentArea, borders, fades);

        AnchorPane.setLeftAnchor(shipBox, 0d);
        AnchorPane.setTopAnchor(shipBox, 0d);
        AnchorPane.setBottomAnchor(shipBox, 0d);

        AnchorPane.setLeftAnchor(ships, 0d);
        AnchorPane.setTopAnchor(ships, 0d);
        AnchorPane.setBottomAnchor(ships, 0d);

        AnchorPane.setRightAnchor(gamePane, 0d);
        AnchorPane.setTopAnchor(gamePane, 0d);
        AnchorPane.setBottomAnchor(gamePane, 0d);
        AnchorPane.setLeftAnchor(gamePane, 200d);

        root.getChildren().addAll(gamePane, shipBox);

        game.initController(new GameController(game, root, gamePane, new GameArea(gameArea), new GameArea(opponentArea), borders, fades, shipBox, ships, time, hits, missed, total, shipsLeft, currentPlayer, endTurn));
        return root;
    }

    private static GridPane createGameArea(Game game) {
        Image water = images.get(ResourceType.WATER);
        GridPane gridPane = new GridPane();
        gridPane.setVgap(0);
        gridPane.setHgap(0);

        double width = water.getWidth();
        double height = water.getHeight();

        for (int x = 0; x < game.getOptions().getLengthX(); x++) {
            for (int y = 0; y < game.getOptions().getLengthY(); y++) {
                ResizableCanvas canvas = new ResizableCanvas(width, height);

                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.drawImage(water, 0, 0);

                gridPane.add(canvas, x, y);
            }
        }

        return gridPane;
    }

    static Image drawBorders(Game game, Pane pane, GridPane origin) {
        return drawBorders(game, pane, origin, null, null);
    }

    static Image drawBorders(Game game, Pane pane, GridPane origin, Number overrideWidth, Number overrideHeight) {
        int width = overrideWidth != null ? overrideWidth.intValue() : (int) pane.getWidth();
        int height = overrideHeight != null ? overrideHeight.intValue() : (int) pane.getHeight();

        if (width <= 0 || height <= 0)
            return null;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();

        Bounds bounds = origin.getBoundsInParent();
        double minX = (bounds.getMinX() + (((1 - origin.getScaleX()) * (bounds.getMaxX() - bounds.getMinX())) / 1000)); //nevim proc 1000 ale funguje to tak proc ne ¯\_(ツ)_/¯
        double maxX = (bounds.getMaxX() - (((1 - origin.getScaleX()) * (bounds.getMaxX() - bounds.getMinX())) / 1000));

        double minY = (bounds.getMinY() + (((1 - origin.getScaleY()) * (bounds.getMaxY() - bounds.getMinY())) / 1000));
        double maxY = (bounds.getMaxY() - (((1 - origin.getScaleY()) * (bounds.getMaxY() - bounds.getMinY())) / 1000));

        double imgWidth = maxX - minX;
        double imgHeight = maxY - minY;

        BoundingBox bb = new BoundingBox(minX, minY, maxX - minX, maxY - minY);
        game.getController().setAreaBounds(bb);

        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, width - 1, height - 1);

        double tileSizeX = imgWidth / game.getOptions().getLengthX();
        double tileSizeY = imgHeight / game.getOptions().getLengthY();

        g.setColor(Color.GRAY);
        for (int x = 0; x <= game.getOptions().getLengthX(); x++) {
            double offsetX = (x * tileSizeX) + minX;

            if (offsetX < 0 || offsetX > maxX) {
                continue;
            }

            g.drawLine((int) offsetX, (int) Math.min(imgHeight + minY, height - minY), (int) offsetX, (int) Math.max(minY, 0));
        }

        for (int y = 0; y <= game.getOptions().getLengthY(); y++) {
            double offsetY = (y * tileSizeY) + minY;

            if (offsetY < 0 || offsetY > maxY) {
                continue;
            }

            g.drawLine((int) Math.max(0, minX), (int) offsetY, (int) Math.min(imgWidth + minX, width - minX), (int) offsetY);
        }

        g.setColor(Color.GRAY);
        g.fillRect(0, 0, width - 1, 20);
        g.fillRect(0, 0, 20, height - 1);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(3));
        for (int x = 0; x < game.getOptions().getLengthX(); x++) {
            double offsetX = (x * tileSizeX) + minX + (tileSizeX / 2);

            g.drawString(String.valueOf(x + 1), (int) offsetX, 12);
        }

        for (int y = 0; y < game.getOptions().getLengthY(); y++) {
            double offsetY = (y * tileSizeY) + minY + (tileSizeY / 2);

            g.drawString(Utils.getAlphabetNumber(y), 12, (int) offsetY);
        }

        return SwingFXUtils.toFXImage(image, null);
    }

    static Image onSelectedTileChanged(Game game, Pane pane) {
        int width = (int) pane.getWidth();
        int height = (int) pane.getHeight();

        if (width <= 0 || height <= 0)
            return null;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(66, 179, 244, 50));

        Vector2i pos = game.getController().getCurrentTilePos();

        if (pos != null) {
            BoundingBox tileBB = game.getController().getTileBounds(pos);

            g.fillRect(0, (int) tileBB.getMinY(), (int) tileBB.getMaxX(), (int) tileBB.getHeight());
            g.fillRect((int) tileBB.getMinX(), 0, (int) (tileBB.getMaxX() - tileBB.getMinX()), (int) tileBB.getMaxY());
        }

        return SwingFXUtils.toFXImage(image, null);
    }

    static void updateBackground(GridPane pane, TileEntry... entries) {
        for (TileEntry entry : entries) {
            Vector2i pos = entry.getPos();
            Tile tile = entry.getTile();
            Canvas canvas = null;

            for (Node node : pane.getChildren()) {
                if (GridPane.getColumnIndex(node) == pos.getX() && GridPane.getRowIndex(node) == pos.getY()) {
                    canvas = (Canvas) node;
                }
            }

            if (canvas == null) {
                return;
            }

            GraphicsContext gc = canvas.getGraphicsContext2D();

            switch (tile) {
                case SHIP:
                    Image source = images.get(ResourceType.SHIP);
                    gc.drawImage(source, -40, 0);
                    break;
                case HIT:
                    gc.setStroke(javafx.scene.paint.Color.RED);
                    gc.setLineWidth(10);
                    gc.strokeLine(0, 0, canvas.getWidth(), canvas.getHeight());
                    gc.strokeLine(0, canvas.getHeight(), canvas.getWidth(), 0);
                    break;
                case MARKED:
                    gc.setStroke(javafx.scene.paint.Color.WHITE);
                    gc.setLineWidth(10);
                    gc.strokeLine(0, 0, canvas.getWidth(), canvas.getHeight());
                    gc.strokeLine(0, canvas.getHeight(), canvas.getWidth(), 0);
                    break;
            }
        }
    }

    static Parent createEndGameScene(GameController controller, Stage stage, GameStatistic statistic) {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);

        String winnerName = statistic.getWinner();
        Label stat = new Label(statistic.win() ? "Výhra" : "Prohra");

        String titleColor = statistic.win() ? "green" : "red";
        stat.setStyle("-fx-font-size: 40; -fx-font-weight: bold; -fx-text-fill: " + titleColor);

        Label winner = new Label(winnerName != null ? "Vítěz: " + winnerName : "Remíza");
        winner.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-underline: true");

        Label spacer = new Label("-------------------------------------------");
        spacer.setStyle("-fx-font-weight: bold");
        spacer.setPadding(new Insets(0, 0, 20, 0));

        GridPane statPane = new GridPane();
        statPane.setAlignment(Pos.CENTER);
        statPane.setHgap(15);
        statPane.setVgap(10);

        Label hits = new Label("" + statistic.getStatistic(Stat.HIT));
        hits.setStyle("-fx-font-weight: bold");

        Label misses = new Label("" + statistic.getStatistic(Stat.MISS));
        misses.setStyle("-fx-font-weight: bold");

        statPane.add(new Label("Počet trefených střel: "), 0, 0);
        statPane.add(hits, 1, 0);

        statPane.add(new Label("Počet křivých střel: "), 0, 1);
        statPane.add(misses, 1, 1);

        Button closeBtn = new Button("Zavřít");
        closeBtn.setAlignment(Pos.BASELINE_CENTER);
        closeBtn.setMinWidth(70);
        closeBtn.setOnAction(e -> controller.onGameStatsCloseBtnClick(stage));

        VBox.setMargin(closeBtn, new Insets(30, 0, 0, 0));

        box.getChildren().addAll(stat, winner, spacer, statPane, closeBtn);
        return box;
    }

    private static void loadImages() {
        images.put(ResourceType.SHIP, loadImage("ship.png"));
        images.put(ResourceType.WATER, loadImage("water.png"));
    }

    private static Image loadImage(String name) {
        try {
            return new Image(GameSceneBuilder.class.getClassLoader().getResourceAsStream(name));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private enum ResourceType {
        SHIP,
        WATER
    }

    public static class ShipCell extends ListCell<Image> {

        static final String SHIP = "ship_content";

        private final ImageView imageView = new ImageView();
        private final Region parent;

        private ShipCell(Region parent) {
            this.parent = parent;
        }

        @Override
        protected void updateItem(Image item, boolean empty) {
            super.updateItem(item, empty);

            imageView.setImage(item);
            imageView.setPreserveRatio(true);
            imageView.fitWidthProperty().bind(parent.widthProperty());
            imageView.fitHeightProperty().bind(parent.heightProperty());
            setGraphic(imageView);
        }
    }
}
