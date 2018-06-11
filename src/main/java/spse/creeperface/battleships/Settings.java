package spse.creeperface.battleships;

import lombok.Getter;
import spse.creeperface.battleships.provider.ProviderType;

/**
 * @author CreeperFace
 */
@Getter
public class Settings {

    private final transient Object lock = new Object();
    private final transient BattleShips main = BattleShips.getInstance();

    private String name = "Hrac";
    private ProviderType provider = ProviderType.SQLITE;

    public void setName(String name) {
        synchronized (lock) {
            this.name = name;
            this.main.getController().getName().setText(name);
        }
    }

    public void setProvider(ProviderType provider) {
        synchronized (lock) {
            this.provider = provider;
        }
    }
}
