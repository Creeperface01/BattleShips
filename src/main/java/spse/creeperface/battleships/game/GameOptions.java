package spse.creeperface.battleships.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CreeperFace
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameOptions {

    private int lengthX = 10;
    private int lengthY = 10;

    private int shipCount = 5;

    private int roundTime = 60;

    private boolean forceSpaceBetweenShips = true;
}
