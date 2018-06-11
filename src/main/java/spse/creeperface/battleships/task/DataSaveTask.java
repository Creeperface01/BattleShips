package spse.creeperface.battleships.task;

import com.google.gson.Gson;
import spse.creeperface.battleships.BattleShips;
import spse.creeperface.battleships.Settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * @author CreeperFace
 */
public class DataSaveTask implements Runnable {

    private final BattleShips main;
    private boolean running = true;

    public DataSaveTask(BattleShips main) {
        this.main = main;
    }

    @Override
    public void run() {
        while (running) {
            try {
                File file = new File(System.getProperty("user.dir") + "/game.properties");

                BufferedWriter writer = new BufferedWriter(new FileWriter(file));

                Settings settings = main.getSettings();

                synchronized (settings.getLock()) {
                    writer.write(new Gson().toJson(settings));
                }

                writer.flush();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public void stop() {
        running = false;
    }
}
