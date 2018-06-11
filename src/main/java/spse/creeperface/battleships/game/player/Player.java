package spse.creeperface.battleships.game.player;

import spse.creeperface.battleships.game.RoundResult;

import java.util.Optional;

/**
 * @author CreeperFace
 */
public interface Player {

    void play();

    boolean roundFinished();

    Optional<RoundResult> getRoundResult();

    void played();

    void resetResult();

    void cancelTurn();

    String getName();

    void hit();

    boolean isAlive();

    default boolean isLocal() {
        return false;
    }
}
