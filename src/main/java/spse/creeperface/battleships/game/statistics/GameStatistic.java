package spse.creeperface.battleships.game.statistics;

import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author CreeperFace
 */
public class GameStatistic {

    private final Map<Stat, Integer> stats = new EnumMap<>(Stat.class);

    @Getter
    private final long time = System.currentTimeMillis();

    @Setter
    @Getter
    private String winner;

    @Getter
    private final String player;

    public GameStatistic(String playerName) {
        this.player = playerName;

        for (Stat stat : Stat.values()) {
            stats.put(stat, 0);
        }
    }

    public int addStatistic(Stat stat) {
        return stats.compute(stat, (k, v) -> ++v);
    }

    public int getStatistic(Stat stat) {
        return stats.getOrDefault(stat, 0);
    }

    public boolean win() {
        return stats.get(Stat.WIN) > 0;
    }
}
