package spse.creeperface.battleships.game;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.Getter;
import spse.creeperface.battleships.BattleShips;
import spse.creeperface.battleships.game.map.Tile;
import spse.creeperface.battleships.game.map.TileEntry;
import spse.creeperface.battleships.game.player.BotPlayer;
import spse.creeperface.battleships.game.player.PhysicalPlayer;
import spse.creeperface.battleships.game.player.Player;
import spse.creeperface.battleships.game.statistics.GameStatistic;
import spse.creeperface.battleships.game.statistics.Stat;
import spse.creeperface.battleships.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author CreeperFace
 */
public final class Game {

    @Getter
    private GameState state;
    @Getter
    private GamePhase gamePhase;

    @Getter(AccessLevel.PACKAGE)
    private GameController controller;
    @Getter
    private final BattleShips main;
    private Timeline timeline;

    @Getter
    private final Tile[] tiles;
    @Getter
    private final Tile[] opponentTiles;

    @Getter
    private final GameOptions options;

    @Getter
    private final GameMode gameMode;

    @Getter
    private boolean local = true;

    @Getter
    private PhysicalPlayer localPlayer;
    private Player opponent;

    private List<Player> players = new ArrayList<>(2);

    private int currentPlayerIndex = -1;
    private int roundTime;
    private int tick;
    private int nextRoundDelay = -1;

    @Getter
    private final GameStatistic stats;

    public Game(BattleShips main, GameOptions opt, GameMode gameMode) {
        this.options = opt;
        this.gameMode = gameMode;
        this.stats = new GameStatistic(main.getSettings().getName());

        Range<Integer> range = Range.closed(5, 256);
        Preconditions.checkArgument(range.contains(opt.getLengthX()), "The playing area width must be between 10 and 256");
        Preconditions.checkArgument(range.contains(opt.getLengthY()), "The playing area height must be between 10 and 256");

        this.main = main;
        this.tiles = new Tile[256 * 256];
        this.opponentTiles = new Tile[256 * 256];

        this.localPlayer = new PhysicalPlayer(this, main.getSettings().getName());
        players.add(localPlayer);
        if (gameMode == GameMode.SINGLE) {
            opponent = new BotPlayer(this, Utils.pickRandomBotName(main.getSettings().getName()));
            players.add(opponent);
        }
    }

    void initController(GameController controller) {
        Preconditions.checkState(this.controller == null, "GameController already instantiated");
        this.controller = controller;
    }

    public void start() {
        Preconditions.checkState(state == null || state == GameState.STOPPED);

        Arrays.fill(this.tiles, Tile.UNKNOWN);
        Arrays.fill(this.opponentTiles, Tile.UNKNOWN);

        state = GameState.RUNNING;
        gamePhase = GamePhase.SELECTING;

        Pane pane = GameSceneBuilder.createGameScene(this);

        Utils.switchScene(main.getPrimaryStage(), pane);

        GridPane area = controller.getArea().getPane();
        GridPane opponentArea = controller.getOpponentArea().getPane();

        opponentArea.setVisible(true);
        area.setVisible(true);

        opponentArea.setScaleY(0.1);
        opponentArea.setScaleX(0.1);
        area.setScaleX(0.1);
        area.setScaleY(0.1);
        controller.onAreaChanged();

        controller.getMissed().setText("0");
        controller.getHits().setText("0");
        controller.getTotal().setText("0");
        controller.getShipsLeft().setText("" + this.options.getShipCount());

        Platform.runLater(() -> this.controller.initListeners());

        opponentArea.setVisible(false);

        if (gameMode == GameMode.SINGLE) {
            placeBotShips();
        }

        roundTime = getOptions().getRoundTime();
        controller.getEndTurnButton().setVisible(true);
        controller.getCurrentPlayer().setText(localPlayer.getName());

        tickProcessor();
    }

    public void stop() {
        timeline.stop();
        state = GameState.STOPPED;
    }

    private void tick() {
        tick++;

        if (this.tick % 20 == 0) {
            this.roundTime--;

            String time = String.valueOf(this.roundTime / 60) + ":" + (this.roundTime % 60);
            this.getController().getTime().setText(time);
        }

        if (nextRoundDelay-- == 0) {
            if (this.gamePhase == GamePhase.SELECTING) {
                updatePhase();
            } else {
                nextRound();
            }

            return;
        }

        if (this.isLocal() && this.roundTime <= 0) {
            if (this.gamePhase == GamePhase.SELECTING) {
                updatePhase();
            } else {
                nextRound();
            }

            return;
        }

        checkRoundState();
    }

    private void tickProcessor() {
        timeline = TimelineBuilder.create()
                .keyFrames(
                        new KeyFrame(
                                new Duration(50),
                                e -> tick()
                        )
                )
                .cycleCount(Timeline.INDEFINITE)
                .build();

        timeline.play();
    }

    private void checkRoundState() {
        if (!isLocal()) {
            return;
        }

        boolean finished = true;

        if (this.gamePhase == GamePhase.SELECTING) {
            for (Player player : this.players) {
                if (!player.roundFinished()) {
                    finished = false;
                }
            }
        } else {
            Player winner = null;

            if (!localPlayer.isAlive()) {
                winner = opponent;
            } else if (!opponent.isAlive()) {
                winner = localPlayer;
                stats.addStatistic(Stat.WIN);
            }

            if (winner != null) {
                stats.setWinner(winner.getName());

                Platform.runLater(() -> controller.showEndStage());
                this.main.saveStatistic(this.stats);

                stop();
                return;
            }

            Player p = getCurrentPlayer();

            if (!p.roundFinished()) {
                finished = false;
            } else {
                p.getRoundResult().ifPresent(r -> processRoundResult(p, r));
            }
        }


        if (finished && nextRoundDelay < 0) {
            nextRoundDelay = 60;
        }
    }

    private void processRoundResult(Player p, RoundResult result) {
        Vector2i pos = result.getPosition();

        Tile hit = getTile(pos, p.isLocal() ? this.opponentTiles : this.tiles);

        if (hit == null) {
            p.resetResult();
            return;
        }

        if (p.isLocal()) {
            List<TileEntry> entries = new ArrayList<>();

            if (hit == Tile.SHIP) {
                addStat(Stat.HIT);
                this.opponent.hit();

                entries.add(TileEntry.of(pos, Tile.HIT));

                for (Vector2i side : Utils.getNeighbours(pos)) {
                    Tile t = getTile(side, this.opponentTiles);

                    if (t == Tile.UNKNOWN) {
                        setTile(side, Tile.MARKED, opponentTiles);
                        entries.add(TileEntry.of(side, Tile.MARKED));
                    }
                }
            } else if (hit == Tile.UNKNOWN) {
                addStat(Stat.MISS);

                setTile(pos, Tile.MARKED, opponentTiles);
                entries.add(TileEntry.of(pos, Tile.MARKED));
            }

            this.controller.updateOpponentTiles(entries.toArray(new TileEntry[0]));
        } else {

            if (hit == Tile.SHIP) {
                this.localPlayer.hit();
                this.controller.updateTiles(pos, Tile.HIT);

                for (Vector2i side : Utils.getNeighbours(pos)) {
                    setTile(side, Tile.MARKED);
                }
            } else {
                setTile(pos, Tile.MARKED);
                Platform.runLater(() -> this.controller.pulseTile(this.controller.getArea(), pos, new Color(1, 1, 1, 0.2)));
                //TODO: something
            }
        }

        p.resetResult();
    }

    private void updatePhase() {
        Preconditions.checkState(this.gamePhase != GamePhase.ATTACKING);
        this.gamePhase = GamePhase.ATTACKING;
        //TODO: notify players

        nextRound();
    }

    private void nextRound() {
        this.currentPlayerIndex = ++this.currentPlayerIndex % players.size();
        this.roundTime = options.getRoundTime();

        players.forEach(Player::played);

        Player p = getCurrentPlayer();

        getController().getCurrentPlayer().setText(p.getName());

        if (p.isLocal()) {
            getController().turnMap(this.controller.getArea().getPane(), this.controller.getOpponentArea().getPane());

            this.controller.getEndTurnButton().setVisible(true);
        } else {
            getController().turnMap(this.controller.getOpponentArea().getPane(), this.controller.getArea().getPane());
            this.controller.getEndTurnButton().setVisible(false);
            p.play();
        }
    }

    public Tile getTile(Vector2i vec) {
        return getTile(vec, this.tiles);
    }

    public Tile getTile(Vector2i vec, Tile[] tiles) {
        if (vec.getX() > getOptions().getLengthX() || vec.getY() > getOptions().getLengthY() || vec.getX() < 0 || vec.getY() < 0) {
            return null;
        }

        return tiles[positionHash(vec)];
    }

    void setTile(Vector2i vec, Tile tile) {
        setTile(vec, tile, this.tiles);
    }

    void setTile(Vector2i vec, Tile tile, Tile[] tiles) {
        if (vec.getX() > getOptions().getLengthX() || vec.getY() > getOptions().getLengthY() || vec.getX() < 0 || vec.getY() < 0) {
            return;
        }

        tiles[positionHash(vec)] = tile;
    }

    Player getCurrentPlayer() {
        return players.get(this.currentPlayerIndex);
    }

    private void placeBotShips() {
        int count = getOptions().getShipCount();

        List<Vector2i> availablePositions = new ArrayList<>(getOptions().getLengthX() * getOptions().getLengthY());

        for (int x = 0; x < getOptions().getLengthX(); x++) {
            for (int y = 0; y < getOptions().getLengthY(); y++) {
                availablePositions.add(new Vector2i(x, y));
            }
        }

        placeBotShip(availablePositions, count);
    }

    private void placeBotShip(List<Vector2i> available, int count) {
        if (available.isEmpty() || count <= 0) {
            return;
        }

        Vector2i pos = available.get(new Random().nextInt(available.size()));

        for (Vector2i side : Utils.getNeighbours(pos)) {
            for (int i = 0; i < available.size(); i++) {
                Vector2i vec = available.get(i);

                if (vec.equals(side)) {
                    available.remove(i);
                }
            }
        }

        setTile(pos, Tile.SHIP, opponentTiles);
        placeBotShip(available, --count);
    }

    private void tryPlaceShip(Player p, Vector2i pos) {
        Tile[] tiles = p.isLocal() ? this.tiles : this.opponentTiles;

        if (getTile(pos, tiles) != Tile.UNKNOWN) {
            return;
        }

        for (Vector2i side : Utils.getNeighbours(pos)) {
            if (getTile(side, tiles) != Tile.UNKNOWN) {
                return; //TODO: log
            }
        }

        setTile(pos, Tile.SHIP, tiles);

        if (p.isLocal()) {
            this.controller.updateTiles(pos, Tile.SHIP);
        }
    }

    private void addStat(Stat stat) {
        int c = stats.addStatistic(stat);
        String count = String.valueOf(c);

        controller.getTotal().setText("" + (stats.getStatistic(Stat.HIT) + stats.getStatistic(Stat.MISS)));

        switch (stat) {
            case MISS:
                controller.getMissed().setText(count);
                break;
            case HIT:
                controller.getHits().setText(count);
                controller.getShipsLeft().setText("" + (this.options.getShipCount() - stats.getStatistic(Stat.HIT)));
                break;
        }
    }

    static int positionHash(Vector2i vec) {
        return vec.getX() << 8 | vec.getY();
    }

    public enum GameState {
        RUNNING,
        STOPPED
    }

    public enum GamePhase {
        SELECTING,
        ATTACKING
    }
}
