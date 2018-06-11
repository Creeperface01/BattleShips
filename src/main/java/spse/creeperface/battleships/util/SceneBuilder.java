package spse.creeperface.battleships.util;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import spse.creeperface.battleships.BattleShips;
import spse.creeperface.battleships.Controller;
import spse.creeperface.battleships.Settings;
import spse.creeperface.battleships.provider.ProviderType;
import spse.creeperface.battleships.provider.StatsProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author CreeperFace
 */

@UtilityClass
public class SceneBuilder {

    private BattleShips main;

    public void init(BattleShips main) {
        Preconditions.checkState(SceneBuilder.main == null, "SceneBuilder already initialized");
        SceneBuilder.main = main;
    }

    private final LoadingCache<SceneType, Parent> sceneCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(new CacheLoader<SceneType, Parent>() {
                @Override
                public Parent load(SceneType sceneType) {
                    return sceneType.get();
                }
            });

    private Parent createMainScene() {
        Controller controller = main.getController();
        Settings settings = main.getSettings();

        AnchorPane root = new AnchorPane();

        Button newGameButton = new Button("Nová hra");
        newGameButton.setStyle("-fx-font-size: 16px; -fx-pref-width: 15em");
        newGameButton.setAlignment(Pos.CENTER);
        newGameButton.setOnAction(controller::onStartButtonClick);

        Button statsButton = new Button("Statistiky");
        statsButton.setStyle("-fx-font-size: 16px; -fx-pref-width: 15em");
        statsButton.setAlignment(Pos.CENTER);
        statsButton.setOnAction(controller::onStatsButtonClick);

        Button exitButton = new Button("Konec");
        exitButton.setStyle("-fx-font-size: 16px; -fx-pref-width: 15em");
        exitButton.setAlignment(Pos.CENTER);
        exitButton.setOnAction(e -> System.exit(0));

        SideBar settingsBar = new SideBar("menu");
        settingsBar.setStyle("-fx-background-color: #d9d9d9;-fx-border-width: 1px; -fx-border-color: black; -fx-border-radius: 4");
        VBox.setVgrow(root, Priority.ALWAYS);
        VBox.setVgrow(settingsBar, Priority.ALWAYS);

        settingsBar.setPrefWidth(200);
        settingsBar.setTranslateX(-190);

        TextField settingsName = new TextField(settings.getName());
        settingsName.addEventFilter(KeyEvent.KEY_TYPED, (e) -> controller.onNickNamePropertyChanged(e, settingsName));

        settingsName.focusedProperty().addListener((ob, oldVal, newVal) -> controller.onNickNamePropertyFocused(newVal, settingsName));

        settingsName.setStyle("-fx-border-width: 2px; -fx-border-radius: 5px; -fx-border-style: solid; -fx-border-color: #aaa8a1");

        Label settingsLabel = new Label("Nastavení");
        settingsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold");

        Label nickLabel = new Label("Nickname");
        nickLabel.setStyle("-fx-font-weight: bold");

        VBox.setMargin(settingsLabel, new Insets(0, 0, 10, 0));
        settingsBar.getChildren().addAll(settingsLabel, nickLabel, settingsName);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(15);
        vBox.getChildren().addAll(newGameButton, statsButton, exitButton);

        AnchorPane.setBottomAnchor(vBox, 0d);
        AnchorPane.setLeftAnchor(vBox, 0d);
        AnchorPane.setRightAnchor(vBox, 0d);
        AnchorPane.setTopAnchor(vBox, 0d);

        AnchorPane.setTopAnchor(settingsBar, 0d);
        AnchorPane.setBottomAnchor(settingsBar, 0d);

        root.getChildren().addAll(vBox, settingsBar);

        root.addEventFilter(MouseEvent.MOUSE_MOVED, e -> controller.onMouseOverMainSceneMoved(e, settingsBar));

        main.getController().init(settingsName);
        return root;
    }

    private Parent createStartGameScene() {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);

        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setVgap(5);
        pane.setHgap(20);

        TextField lenX = new TextField("10");
        Utils.addNumberFilder(lenX, 5, 256);

        TextField lenY = new TextField("10");
        Utils.addNumberFilder(lenY, 5, 256);

        TextField ships = new TextField("5");
        Utils.addNumberFilder(ships, 1, () -> (Integer.parseInt(lenX.getText()) * Integer.parseInt(lenY.getText())) / 2);

        TextField roundTime = new TextField("60");
        Utils.addNumberFilder(roundTime, 5, 300);

        pane.add(new Label("Šířka hrací plochy: "), 0, 0);
        pane.add(lenX, 1, 0);

        pane.add(new Label("Výška hrací plochy: "), 0, 1);
        pane.add(lenY, 1, 1);

        pane.add(new Label("Počet lodí: "), 0, 2);
        pane.add(ships, 1, 2);

        pane.add(new Label("Čas na kolo: "), 0, 3);
        pane.add(roundTime, 1, 3);

        Button startButton = new Button("Start");
        startButton.setMinWidth(70);
        startButton.setAlignment(Pos.CENTER);
        VBox.setMargin(startButton, new Insets(30, 0, 0, 0));

        startButton.setOnAction(e -> {
            Utils.checkRange(lenX, 5, 256);
            Utils.checkRange(lenY, 5, 256);
            Utils.checkRange(ships, 1, (Integer.parseInt(lenX.getText()) * Integer.parseInt(lenY.getText())) / 2);
            Utils.checkRange(roundTime, 5, 300);

            main.getController().onStartGameResponded(startButton.getScene().getWindow(), lenX.getText(), lenY.getText(), ships.getText(), roundTime.getText());
        });

        box.getChildren().addAll(pane, startButton);

        return box;
    }

    private Parent createStatsScene() {
        HBox root = new HBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);
        root.setPadding(new Insets(20, 20, 20, 20));

        GridPane pane = new GridPane();
        pane.setHgap(30);
        pane.setVgap(5);
        pane.setMaxWidth(400);
        pane.setPrefWidth(300);

        Label name = new Label();
        Label date = new Label();
        Label state = new Label();
        Label hits = new Label();
        Label misses = new Label();
        Label rounds = new Label();

        pane.add(new Label("Datum: "), 0, 0);
        pane.add(date, 1, 0);

        pane.add(new Label("Jméno: "), 0, 1);
        pane.add(name, 1, 1);

        pane.add(new Label("Výsledek: "), 0, 3);
        pane.add(state, 1, 3);

        pane.add(new Label("Počet tahů: "), 0, 4);
        pane.add(rounds, 1, 4);

        pane.add(new Label("Počet tref: "), 0, 5);
        pane.add(hits, 1, 5);

        pane.add(new Label("Počet minutí: "), 0, 6);
        pane.add(misses, 1, 6);

        ListView<StatsProvider.Entry> stats = new ListView<>();
        stats.setStyle("-fx-border-width: 2; -fx-border-color: black; -fx-border-radius: 5");
        stats.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        stats.setPrefHeight(Region.USE_COMPUTED_SIZE);
        stats.setPrefWidth(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(stats, Priority.SOMETIMES);

        stats.setCellFactory((listView) ->
                new ListCell<StatsProvider.Entry>() {

                    @Override
                    protected void updateItem(StatsProvider.Entry item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setText(null);
                            return;
                        }

                        setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(item.getTime())));
                    }
                }
        );

        stats.setOnScrollTo(e -> {
            Integer index = e.getScrollTarget();

            if (index != stats.getItems().size() - 1) {
                return;
            }

            int count = stats.getItems().size() + StatsProvider.RESULT_COUNT;

            stats.setItems(FXCollections.observableList(main.getStatsProvider().list(count)));
        });

        Runnable showDescription = () -> {
            StatsProvider.Entry entry = stats.getSelectionModel().getSelectedItem();

            name.setText(entry.getName());
            date.setText(new SimpleDateFormat("dd. MM. yyy, HH:mm:ss").format(new Date(entry.getTime())));
            state.setText(entry.isWin() ? "Výhra" : "Prohra");
            hits.setText("" + entry.getHits());
            misses.setText("" + entry.getMisses());
            rounds.setText("" + (entry.getHits() + entry.getMisses()));
        };

        stats.setOnMouseClicked(e -> showDescription.run());

        stats.setItems(FXCollections.observableList(main.getStatsProvider().list()));

        root.getChildren().addAll(stats, pane);

        return root;
    }

    private Parent createSettingsScene() {
        Controller controller = main.getController();

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);

        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setVgap(15);
        pane.setHgap(20);
        pane.setPrefWidth(300);
        pane.setPrefHeight(500);

        TextField name = new TextField("Hrac");
        name.addEventFilter(KeyEvent.KEY_TYPED, (e) -> controller.onNickNamePropertyChanged(e, name));
        name.focusedProperty().addListener((ob, oldVal, newVal) -> controller.onNickNamePropertyFocused(newVal, name));

        ChoiceBox<ProviderType> providers = new ChoiceBox<>();
        providers.setConverter(new StringConverter<ProviderType>() {
            @Override
            public String toString(ProviderType object) {
                return object.name();
            }

            @Override
            public ProviderType fromString(String string) {
                return ProviderType.valueOf(string);
            }
        });

        providers.setItems(FXCollections.observableArrayList(ProviderType.SQLITE));
        providers.setValue(ProviderType.SQLITE);

        pane.add(new Label("Prezdivka: "), 0, 0);
        pane.add(name, 1, 0);

        pane.add(new Label("Provider statistik: "), 0, 1);
        pane.add(providers, 1, 1);

        Button button = new Button("Hotovo");
        button.setAlignment(Pos.CENTER);
        button.setMinWidth(70);
        button.setOnAction(e -> controller.onInitialSettingsDone(name, providers));
        VBox.setMargin(button, new Insets(30, 0, 20, 0));

        vBox.getChildren().addAll(pane, button);

        return vBox;
    }

    public Optional<Scene> get(SceneType type) {
        return get(type, -1, -1);
    }

    public Optional<Scene> get(SceneType type, double width, double height) {
        Scene scene = null;

        Parent root = type.get();

        if (root != null) {
            scene = new Scene(root, width, height);
        }

        return Optional.ofNullable(scene);
    }

    @RequiredArgsConstructor
    public enum SceneType {
        MAIN(SceneBuilder::createMainScene),
        GAME_CREATE(SceneBuilder::createStartGameScene),
        STATS(SceneBuilder::createStatsScene),
        SETTINGS(SceneBuilder::createSettingsScene);

        private final Supplier<Parent> supplier;

        private Parent get() {
            return supplier.get();
        }
    }
}
