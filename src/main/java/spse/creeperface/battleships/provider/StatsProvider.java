package spse.creeperface.battleships.provider;

import lombok.AllArgsConstructor;
import lombok.Getter;
import spse.creeperface.battleships.BattleShips;
import spse.creeperface.battleships.game.statistics.GameStatistic;

import java.util.List;

/**
 * @author CreeperFace
 */
public interface StatsProvider {

    int RESULT_COUNT = 50;

    void init(BattleShips main);

    void saveStatistic(GameStatistic statistic);

    void close();

    ProviderType getType();

    default List<Entry> list() {
        return list(0);
    }

    default List<Entry> list(int offset) {
        return list(offset, RESULT_COUNT);
    }

    List<Entry> list(int offset, int max);

    @AllArgsConstructor
    @Getter
    class Entry {

        private final long time;
        private final String name;
        private final boolean win;
        private final int hits;
        private final int misses;
    }
}
