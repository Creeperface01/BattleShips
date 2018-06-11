package spse.creeperface.battleships.game.player;

import com.flowpowered.math.vector.Vector2i;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import spse.creeperface.battleships.game.Game;
import spse.creeperface.battleships.game.RoundResult;
import spse.creeperface.battleships.game.map.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * @author CreeperFace
 */

@RequiredArgsConstructor
public class BotPlayer implements Player {

    private final Game game;
    @Getter
    private final String name;


    private boolean roundFinished;
    private RoundResult roundResult;
    private Timeline timeline;
    private int hits;

    @Override
    public void play() {
        KeyFrame frame = new KeyFrame(Duration.seconds(new Random().nextInt(3) + 3));

        List<Vector2i> possible = new ArrayList<>();

        for (int x = 0; x < game.getOptions().getLengthX(); x++) {
            for (int y = 0; y < game.getOptions().getLengthY(); y++) {
                Vector2i pos = new Vector2i(x, y);
                Tile t = game.getTile(pos);

                if (t == Tile.UNKNOWN || t == Tile.SHIP) {
                    possible.add(pos);
                }
            }
        }

        Vector2i pos = possible.get(new Random().nextInt(possible.size()));

        timeline = new Timeline(frame);
        timeline.setOnFinished(e -> {
            roundResult = RoundResult.of(pos);
            roundFinished = true;
        });
        timeline.play();
    }

    @Override
    public boolean roundFinished() {
        return game.getGamePhase() == Game.GamePhase.SELECTING || roundFinished;
    }

    @Override
    public void cancelTurn() {
        timeline.stop();
    }

    @Override
    public void played() {
        this.roundFinished = false;
        this.roundResult = null;
        timeline = null;
    }

    @Override
    public Optional<RoundResult> getRoundResult() {
        return Optional.ofNullable(roundResult);
    }

    @Override
    public void hit() {
        hits++;
    }

    @Override
    public boolean isAlive() {
        return hits < game.getOptions().getShipCount();
    }

    @Override
    public void resetResult() {
        roundResult = null;
    }
}
