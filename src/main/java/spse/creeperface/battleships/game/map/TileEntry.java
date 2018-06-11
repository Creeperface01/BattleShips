package spse.creeperface.battleships.game.map;

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
public class TileEntry {

    private final Vector2i pos;
    private final Tile tile;

    public static TileEntry of(Vector2i pos, Tile tile) {
        Preconditions.checkNotNull(pos, "Tile position cannot be null");
        Preconditions.checkNotNull(tile, "Tile cannot be null");

        return new TileEntry(pos, tile);
    }
}
