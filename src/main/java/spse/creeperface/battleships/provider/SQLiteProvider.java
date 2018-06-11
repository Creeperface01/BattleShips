package spse.creeperface.battleships.provider;

import lombok.Cleanup;
import spse.creeperface.battleships.BattleShips;
import spse.creeperface.battleships.game.statistics.GameStatistic;
import spse.creeperface.battleships.game.statistics.Stat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CreeperFace
 */
public class SQLiteProvider implements StatsProvider {

    private Connection connection;

    @Override
    public void init(BattleShips main) {
        try {
            connection = getConnection();

            @Cleanup PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS stats (" +
                    "time BIGINT NOT NULL," +
                    "name VARCHAR(16) NOT NULL," +
                    "result BIT NOT NULL," +
                    "hits INT NOT NULL," +
                    "misses INT NOT NULL" +
                    ")");

            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveStatistic(GameStatistic statistic) {
        try {
            @Cleanup PreparedStatement statement = connection.prepareStatement("INSERT INTO stats (time, name, result, hits, misses) VALUES (?, ?, ?, ?, ?)");
            statement.setLong(1, statistic.getTime());
            statement.setString(2, statistic.getPlayer());
            statement.setBoolean(3, statistic.win());
            statement.setInt(4, statistic.getStatistic(Stat.HIT));
            statement.setInt(5, statistic.getStatistic(Stat.MISS));

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Entry> list(int offset, int max) {
        List<Entry> entries = new ArrayList<>();

        try {
            @Cleanup PreparedStatement statement = connection.prepareStatement("SELECT * FROM stats ORDER BY time DESC LIMIT ? OFFSET ?");
            statement.setInt(1, max);
            statement.setInt(2, offset);

            @Cleanup ResultSet result = statement.executeQuery();

            while (result.next()) {
                long time = result.getLong(1);
                String name = result.getString(2);
                boolean win = result.getBoolean(3);
                int hits = result.getInt(4);
                int misses = result.getInt(5);

                entries.add(new Entry(time, name, win, hits, misses));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return entries;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ProviderType getType() {
        return ProviderType.SQLITE;
    }

    private Connection getConnection() throws SQLException {
        return connection = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.dir") + "/stats.sql");
    }
}
