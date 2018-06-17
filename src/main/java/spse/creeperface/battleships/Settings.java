package spse.creeperface.battleships;

import com.google.common.base.Preconditions;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import spse.creeperface.battleships.provider.ProviderType;

/**
 * @author CreeperFace
 */
@Getter
public class Settings {

    private final transient Object lock = new Object();
    private final transient BattleShips main = BattleShips.getInstance();

    private transient StringProperty nameProperty;

    private String name; //json variable
    private ProviderType provider = ProviderType.SQLITE;


    public void initProperties(StringProperty name) {
        Preconditions.checkState(this.nameProperty == null, "Properties already initialized");

        this.nameProperty = name;
        this.nameProperty.addListener((o, oldVal, newVal) -> this.name = newVal);
        this.nameProperty.setValue(this.name);
    }

    public void setName(String name) {
        synchronized (lock) {
            this.nameProperty.setValue(name);
        }
    }

    public String getName() {
        synchronized (lock) {
            return nameProperty.getValue();
        }
    }

    public void setProvider(ProviderType provider) {
        synchronized (lock) {
            this.provider = provider;
        }
    }
}
