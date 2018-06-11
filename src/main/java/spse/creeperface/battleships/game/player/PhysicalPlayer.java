package spse.creeperface.battleships.game.player;

import com.flowpowered.math.vector.Vector2i;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import spse.creeperface.battleships.game.Game;
import spse.creeperface.battleships.game.RoundResult;

import java.util.Optional;

/**
 * @author CreeperFace
 */

@RequiredArgsConstructor
public class PhysicalPlayer implements Player {

    private final Game game;
    @Getter
    private final String name;

    private RoundResult roundResult;
    private boolean roundFinished;
    private int shipCount;
    private int hits;

    @Override
    public void play() {

    }

    @Override
    public boolean roundFinished() {
        return roundFinished;
    }

    @Override
    public void cancelTurn() {

    }

    @Override
    public Optional<RoundResult> getRoundResult() {
        return Optional.ofNullable(roundResult);
    }

    @Override
    public void played() {
        roundResult = null;
        roundFinished = false;
    }

    public void placeShip() {
        if (++this.shipCount >= game.getOptions().getShipCount()) {
            this.roundFinished = true;
        }
    }

    public void setResult(Vector2i pos) {
        this.roundResult = pos == null ? null : RoundResult.of(pos);
        this.roundFinished = true;
    }

    @Override
    public void hit() {
        hits++;
    }

    @Override
    public boolean isAlive() {
        return hits < shipCount;
    }

    @Override
    public void resetResult() {
        this.roundResult = null;
    }

    @Override
    public boolean isLocal() {
        return true;
    }
}
