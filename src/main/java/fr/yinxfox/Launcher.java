package fr.yinxfox;

import fr.yinxfox.debugger.Debugger;
import fr.yinxfox.emulator.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

public class Launcher extends Application {

    //TODO: Add tests
    //TODO: Add a debugger
    //TODO: Add SCHIP-8 1.1 support
    //TODO: Add XO-CHIP support

    //fixme: cross button should exit program

    private static Debugger debugger = null;
    private static boolean isDebuggerEnabled = false;

    private static final double FPS = 60;
    private static boolean isWindows;
    private static Hardware hardware = Hardware.CHIP8;
    private Stage mainStage;
    private MenuBar menuBar = null;

    private final Keyboard keyboard;
    private Screen video;
    private final SoundMaker soundMaker;
    private ExecutionWorker executionWorker;
    private Timeline timeline;

    public Launcher() {
        this.keyboard = new Keyboard(this);
        this.executionWorker = null;
        this.soundMaker = new SoundMaker();
    }

    private void loadROM(String filename) {
        this.timeline.stop();
        if (this.executionWorker != null) this.executionWorker.interrupt();
        this.video.clear();
        this.executionWorker = new ExecutionWorker(filename, this.video, this.keyboard, this.soundMaker);
        if (isDebuggerEnabled) {
            debugger.setExecutionWorker(executionWorker);
        }
        System.out.println(filename + " has been loaded!");
        this.timeline.play();
    }

    private void createMenuBar() {
        menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem loadRomItem = new MenuItem("Load ROM");
        loadRomItem.setOnAction(_ -> {
            FileChooser f = new FileChooser();
            f.setTitle("Open ROM File");
            File file = f.showOpenDialog(mainStage);

            if (file != null) {
                loadROM(file.getPath());
            }
        });
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(_ -> System.exit(0));

        ToggleGroup hardwareGroup = new ToggleGroup();
        Menu menuHardware = new Menu("Hardware");
        RadioMenuItem chip8Item = new RadioMenuItem(Hardware.CHIP8.toString());
        chip8Item.setSelected(true);
        chip8Item.setOnAction(_ -> {
            this.timeline.stop();
            if (this.executionWorker != null) this.executionWorker.interrupt();
            hardware = Hardware.CHIP8;
            this.mainStage.setTitle(hardware.toString());
            Screen.setHEIGHT(32);
            Screen.setScale(12);
            this.initializeStage();
        });
        RadioMenuItem chip8HiresItem = new RadioMenuItem(Hardware.CHIP8HIRES.toString());
        chip8HiresItem.setOnAction(_ -> {
            this.timeline.stop();
            if (this.executionWorker != null) this.executionWorker.interrupt();
            hardware = Hardware.CHIP8HIRES;
            this.mainStage.setTitle(hardware.toString());
            Screen.setHEIGHT(64);
            Screen.setScale(10);
            this.initializeStage();
        });
        RadioMenuItem schip8Item = new RadioMenuItem(Hardware.SCHIP8.toString());
        schip8Item.setOnAction(_ -> {
            hardware = Hardware.SCHIP8;
            this.mainStage.setTitle(hardware.toString());
        });
        RadioMenuItem xochipItem = new RadioMenuItem(Hardware.XOCHIP.toString());
        xochipItem.setOnAction(_ -> {
            hardware = Hardware.XOCHIP;
            this.mainStage.setTitle(hardware.toString());
        });
        chip8Item.setToggleGroup(hardwareGroup);
        chip8HiresItem.setToggleGroup(hardwareGroup);
        schip8Item.setToggleGroup(hardwareGroup);
        xochipItem.setToggleGroup(hardwareGroup);

        menuFile.getItems().add(loadRomItem);
        menuFile.getItems().add(exitItem);
        menuHardware.getItems().add(chip8Item);
        menuHardware.getItems().add(chip8HiresItem);
        menuHardware.getItems().add(schip8Item);
        menuHardware.getItems().add(xochipItem);

        Menu menuSpeed = new Menu("Speed");
        RadioMenuItem unlockedItem = new RadioMenuItem("Unlocked");
        unlockedItem.setOnAction(_ -> ExecutionWorker.UNLOCKED = !ExecutionWorker.UNLOCKED);
        menuSpeed.getItems().add(unlockedItem);

        Menu menuDebug = new Menu("Debug");
        RadioMenuItem enableDebugger = new RadioMenuItem("Debugger");
        enableDebugger.setOnAction(_ -> toggleDebugger());
        menuDebug.getItems().add(enableDebugger);

        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(menuHardware);
        menuBar.getMenus().add(menuSpeed);
        menuBar.getMenus().add(menuDebug);
    }

    private void initializeStage() {
        this.video = new Screen();
        this.video.render();

        if (menuBar == null) this.createMenuBar();

        VBox root = new VBox();
        root.getChildren().add(menuBar);
        root.getChildren().add(this.video);
        Scene mainScene = new Scene(root);

        this.timeline = new Timeline(
                new KeyFrame(Duration.seconds((double) 1 / FPS),
                        _ -> {
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
            //fixme: probably doesn't work
            this.mainStage.setMaxWidth(mainScene.getWidth() + 16);
            this.mainStage.setMinWidth(mainScene.getWidth() + 16);
            this.mainStage.setMaxHeight(mainScene.getHeight() + 39);
            this.mainStage.setMinHeight(mainScene.getHeight() + 39);
        }
    }

    private void toggleDebugger() {
        if (isDebuggerEnabled) {
            isDebuggerEnabled = false;
            debugger.close();
            debugger = null;
        } else {
            isDebuggerEnabled = true;
            debugger = (executionWorker != null) ? new Debugger(executionWorker) : new Debugger();
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

    public static Hardware getHardware() {
        return hardware;
    }

    public static double getFPS() {
        return FPS;
    }
}
