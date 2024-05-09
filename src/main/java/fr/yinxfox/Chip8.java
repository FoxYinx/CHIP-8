package fr.yinxfox;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.util.Scanner;

public class Chip8 extends Application {

    private static final double FPS = 60;
    private static boolean isWindows;
    private static Hardware hardware = Hardware.CHIP8;
    private Stage mainStage;

    private final Keyboard keyboard;
    private final Screen video;
    private SoundMaker soundMaker;
    private ExecutionWorker executionWorker;
    private Timeline timeline;

    public Chip8() {
        this.keyboard = new Keyboard(this);
        this.video = new Screen();
        this.executionWorker = null;
        if (isWindows) this.soundMaker = new SoundMaker();
    }

    private void loadROM(String filename) {
        this.timeline.stop();
        if (this.executionWorker != null) this.executionWorker.interrupt();
        this.video.clear();
        this.executionWorker = new ExecutionWorker(filename, this.video, this.keyboard, this.soundMaker);
        System.out.println(filename + " has been loaded!");
        this.timeline.play();
    }

    private void initializeStage() {
        this.video.render();

        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem loadRomItem = new MenuItem("Load ROM");
        loadRomItem.setOnAction(e -> {
            FileChooser f = new FileChooser();
            f.setTitle("Open ROM File");
            File file = f.showOpenDialog(mainStage);

            if (file != null) {
                loadROM(file.getPath());
            }
        });
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> System.exit(0));

        ToggleGroup hardwareGroup = new ToggleGroup();
        Menu menuHardware = new Menu("Hardware");
        RadioMenuItem chip8Item = new RadioMenuItem("CHIP-8");
        chip8Item.setSelected(true);
        chip8Item.setOnAction(actionEvent -> {
            hardware = Hardware.CHIP8;
            this.mainStage.setTitle(hardware.toString());
        });
        RadioMenuItem schip8Item = new RadioMenuItem("SCHIP-8");
        schip8Item.setOnAction(actionEvent -> {
            hardware = Hardware.SCHIP8;
            this.mainStage.setTitle(hardware.toString());
        });
        RadioMenuItem xochipItem = new RadioMenuItem("XO-CHIP");
        xochipItem.setOnAction(actionEvent -> {
            hardware = Hardware.XOCHIP;
            this.mainStage.setTitle(hardware.toString());
        });
        chip8Item.setToggleGroup(hardwareGroup);
        schip8Item.setToggleGroup(hardwareGroup);
        xochipItem.setToggleGroup(hardwareGroup);

        menuFile.getItems().add(loadRomItem);
        menuFile.getItems().add(exitItem);
        menuHardware.getItems().add(chip8Item);
        menuHardware.getItems().add(schip8Item);
        menuHardware.getItems().add(xochipItem);

        Menu menuSpeed = new Menu("Speed");
        RadioMenuItem unlockedItem = new RadioMenuItem("UNLOCKED");
        unlockedItem.setSelected(true);
        unlockedItem.setOnAction(actionEvent -> ExecutionWorker.UNLOCKED = !ExecutionWorker.UNLOCKED);
        menuSpeed.getItems().add(unlockedItem);

        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(menuHardware);
        menuBar.getMenus().add(menuSpeed);

        VBox root = new VBox();
        root.getChildren().add(menuBar);
        root.getChildren().add(this.video);
        Scene mainScene = new Scene(root);

        this.timeline = new Timeline(
                new KeyFrame(Duration.seconds((double) 1 / FPS),
                        Event -> {
                            if (this.executionWorker != null) {
                                Platform.runLater(this.video::render);
                                this.executionWorker.updateTimers();
                            }
                        })
        );
        this.timeline.setCycleCount(Animation.INDEFINITE);

        mainScene.setOnKeyPressed(keyEvent -> this.keyboard.setDown(keyEvent.getCode()));
        mainScene.setOnKeyReleased(keyEvent -> this.keyboard.setUp(keyEvent.getCode()));

        this.mainStage.setResizable(false);
        this.mainStage.setScene(mainScene);
        this.mainStage.show();

        this.mainStage.setTitle(hardware.toString());
        if (isWindows) {
            this.mainStage.setMinWidth(mainScene.getWidth() + 16);
            this.mainStage.setMaxWidth(mainScene.getWidth() + 16);
            this.mainStage.setMinHeight(mainScene.getHeight() + 39);
            this.mainStage.setMaxHeight(mainScene.getHeight() + 39);
        } else {
            this.mainStage.setMinWidth(mainScene.getWidth());
            this.mainStage.setMaxWidth(mainScene.getWidth());
            this.mainStage.setMinHeight(mainScene.getHeight());
            this.mainStage.setMaxHeight(mainScene.getHeight());
        }
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        isWindows = System.getProperty("os.name").contains("Windows");
        this.mainStage = stage;
        initializeStage();
    }

    public ExecutionWorker getExecutionWorker() {
        return this.executionWorker;
    }

    public static boolean getIsWindows() {
        return isWindows;
    }
}
