package spse.creeperface.battleships.game;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author CreeperFace
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RoundResult {

    private final Vector2i position;

    public static RoundResult of(Vector2i position) {
        Preconditions.checkNotNull(position, "Result position can't be null");
        return new RoundResult(position);
    }
}
