package spse.creeperface.battleships;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import spse.creeperface.battleships.game.Game;
import spse.creeperface.battleships.game.GameMode;
import spse.creeperface.battleships.game.GameOptions;
import spse.creeperface.battleships.provider.ProviderType;
import spse.creeperface.battleships.util.SceneBuilder;
import spse.creeperface.battleships.util.SideBar;
import spse.creeperface.battleships.util.Utils;

import java.io.File;
import java.io.IOException;

/**
 * @author CreeperFace
 */

@RequiredArgsConstructor
public class Controller {

    private final BattleShips main;

    @Getter
    private TextField name;

    public void init(TextField name) {
        this.name = name;
    }

    public void onStartButtonClick(ActionEvent e) {
        SceneBuilder.get(SceneBuilder.SceneType.GAME_CREATE).ifPresent(scene -> {
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Nová hra");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(main.getPrimaryStage());
            stage.setWidth(500);
            stage.setHeight(250);
            stage.setResizable(false);

            stage.showAndWait();
        });
    }

    public void onStartGameResponded(Window window, String lenX, String lenY, String ships, String roundTime) {
        try {
            Game game = main.getGame();

            if (game == null || game.getState() == Game.GameState.STOPPED) {

                int _lenX = Integer.parseInt(lenX);
                int _lenY = Integer.parseInt(lenY);
                int _ships = Integer.parseInt(ships);
                int _time = Integer.parseInt(roundTime);

                GameOptions opt = new GameOptions(_lenX, _lenY, _ships, _time, true);

                game = new Game(main, opt, GameMode.SINGLE);
                game.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ((Stage) window).close();
    }

    public void onMouseOverMainSceneMoved(MouseEvent e, SideBar sideBar) {
        if (e.getX() < 40) {
            sideBar.show();
        }
    }

    public void onNickNamePropertyFocused(boolean newVal, TextField nameField) {
        if (!newVal) {
            String text = nameField.getText();

            if (text == null || text.length() < 3) {
                Utils.showTip(nameField, "Jméno musí být dlouhé minimálně 3 znaky!");
                nameField.setText(main.getSettings().getName());
                return;
            }

            Character c;
            if ((c = Utils.checkNickname(text)) != null) {
                Utils.showTip(nameField, "Jméno nesmí obsahovat symbol '" + c + "'");
                return;
            }

            main.getSettings().setName(text);
        }
    }

    public void onNickNamePropertyChanged(KeyEvent e, TextField nameField) {
        String c = e.getCharacter();

        TextField field = (TextField) e.getSource();
        String text = field.getText();

        if (text != null && text.length() > 16) {//max length
            e.consume();
            Utils.showTip(nameField, "Jméno nesmí být delší než 16 znaků!");
        }

        if (!c.matches("[a-zA-z0-9_\\-]")) {
            e.consume();
            Utils.showTip(nameField, "Tento znak není povolen");
        }
    }

    public void onStatsButtonClick(ActionEvent e) {
        SceneBuilder.get(SceneBuilder.SceneType.STATS).ifPresent(scene -> {
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Statistiky");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(main.getPrimaryStage());
            stage.setWidth(600);
            stage.setHeight(600);

            stage.showAndWait();
        });
    }

    public void onInitialSettingsDone(TextField name, ChoiceBox<ProviderType> providerType) {
        String text = name.getText();

        if (text == null || text.length() < 3) {
            Utils.showAlert(Alert.AlertType.ERROR, name.getScene().getWindow(), "Špatně zadané údaje", "Jméno musí být dlouhé minimálně 3 znaky!");
            return;
        }

        Character c;
        if ((c = Utils.checkNickname(text)) != null) {
            Utils.showAlert(Alert.AlertType.ERROR, name.getScene().getWindow(), "Špatně zadané údaje", "Jméno nesmí obsahovat symbol '" + c + "'");
            return;
        }

        try {
            new File(System.getProperty("user.dir") + "/game.properties").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ((Stage) name.getScene().getWindow()).close();
        main.getSettings().setName(text);
        main.getSettings().setProvider(providerType.getValue());

        new Thread(main.getSaveTask()).start();
    }
}
