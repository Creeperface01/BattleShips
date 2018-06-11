package spse.creeperface.battleships;

import com.google.gson.Gson;
import javafx.animation.KeyFrame;
import javafx.animation.TimelineBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import spse.creeperface.battleships.game.Game;
import spse.creeperface.battleships.game.GameSceneBuilder;
import spse.creeperface.battleships.game.statistics.GameStatistic;
import spse.creeperface.battleships.provider.SQLiteProvider;
import spse.creeperface.battleships.provider.StatsProvider;
import spse.creeperface.battleships.server.Server;
import spse.creeperface.battleships.task.DataSaveTask;
import spse.creeperface.battleships.util.SceneBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * @author CreeperFace
 */
public class BattleShips extends Application {

    public static final String VERSION = "1.0";

    @Getter
    private static BattleShips instance;

    @Getter
    private static boolean serverInstance;


    @Getter
    private Settings settings;

    @Getter
    private Game game;

    @Getter
    private Scene mainScene;
    @Getter
    private Stage primaryStage;

    @Getter
    private final StatsProvider statsProvider;

    @Getter
    private final Controller controller;

    @Getter
    private final DataSaveTask saveTask = new DataSaveTask(this);

    public BattleShips() {
        instance = this;
        this.settings = new Settings();

        this.controller = new Controller(this);

        this.statsProvider = new SQLiteProvider();
        this.statsProvider.init(this);

        SceneBuilder.init(this);
    }

    @Override
    public void stop() throws Exception {
        statsProvider.close();
        saveTask.stop();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("BattleShips v" + VERSION);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

        AnchorPane pane = new AnchorPane();

        ImageView imageView = new ImageView();
        imageView.setImage(new Image(getClass().getClassLoader().getResourceAsStream("stack-overflow.png")));
        imageView.fitHeightProperty().bind(pane.heightProperty());
        imageView.fitWidthProperty().bind(pane.widthProperty());
        imageView.setPreserveRatio(true);

        pane.getChildren().add(imageView);

        primaryStage.setScene(new Scene(pane));
        primaryStage.show();

        GameSceneBuilder.init();
        primaryStage.setOnCloseRequest(e -> Platform.exit());

        TimelineBuilder.create().keyFrames(
                new KeyFrame(Duration.millis(2000), e -> imageView.setImage(new Image(getClass().getClassLoader().getResourceAsStream("spse.png")))),
                new KeyFrame(Duration.millis(4000), e -> {
                    SceneBuilder.get(SceneBuilder.SceneType.MAIN, 600, 600).ifPresent((scene) -> this.mainScene = scene);

                    primaryStage.setScene(this.mainScene);

                    Platform.runLater(this::loadSettings);
                })
        ).build().play();
    }

    public void saveStatistic(GameStatistic statistic) {
        this.statsProvider.saveStatistic(statistic);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            for (String s : args) {
                if (s.equals("--server")) {
                    serverInstance = true;
                }
            }
        }

        if (!isServerInstance()) {
            launch(args);
        } else {
            new Server(); //TODO: server
        }
    }

    private void loadSettings() {
        File file = new File(System.getProperty("user.dir") + "/game.properties");

        if (!file.exists()) {
            SceneBuilder.get(SceneBuilder.SceneType.SETTINGS).ifPresent(scene -> {
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Nastavení");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(getPrimaryStage());
                stage.setWidth(300);
                stage.setHeight(500);
                stage.setResizable(false);
                stage.setOnCloseRequest(Event::consume);

                stage.showAndWait();
            });
        } else {
            try {
                this.settings = new Gson().fromJson(new InputStreamReader(new FileInputStream(file)), Settings.class);
                this.controller.getName().setText(this.settings.getName());

                new Thread(this.saveTask).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
