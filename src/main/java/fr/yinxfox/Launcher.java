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

    //fixme: CHIP hires doesn't work

    //TODO: Add tests
    //TODO: Remove Hardware menu and link the hardware selection to the rom
    //TODO: Add SCHIP-8 1.1 support
    //TODO: Add XO-CHIP support

    private static Debugger debugger = null;
    private static boolean isDebuggerEnabled = false;

    private static final double FPS = 60;
    private static Hardware hardware = Hardware.CHIP8;
    private Stage mainStage;
    private static MenuBar menuBar = null;

    private final Keyboard keyboard;
    private Screen video;
    private final SoundMaker soundMaker;
    private ExecutionWorker executionWorker;
    private Timeline timeline;
    private String filePath;

    public Launcher() {
        this.keyboard = new Keyboard(this);
        this.executionWorker = null;
        this.soundMaker = new SoundMaker();
    }

    private void restartEmulator(String filePath) {
        this.timeline.stop();
        if (this.executionWorker != null) this.executionWorker.interrupt();
        this.video.clear();
        this.executionWorker = new ExecutionWorker(filePath, this.video, this.keyboard, this.soundMaker);
        if (isDebuggerEnabled) {
            debugger.setExecutionWorker(executionWorker);
        }
    }

    public void loadROM(String filePath) {
        restartEmulator(filePath);
        System.out.println(filePath + " has been loaded!");
        this.timeline.play();
    }

    private void restart() {
        restartEmulator(filePath);
        System.out.println(filePath + " has been reloaded!");
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
                filePath = file.getPath();
                loadROM(filePath);
            }
        });
        MenuItem restartItem = new MenuItem("Restart");
        restartItem.setOnAction(_ -> restart());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(_ -> System.exit(0));

        menuFile.getItems().add(loadRomItem);
        menuFile.getItems().add(restartItem);
        menuFile.getItems().add(exitItem);

        ToggleGroup hardwareGroup = new ToggleGroup();
        Menu menuHardware = new Menu("Hardware");
        RadioMenuItem chip8Item = new RadioMenuItem(Hardware.CHIP8.toString());
        chip8Item.setSelected(true);
        chip8Item.setOnAction(_ -> {
            this.timeline.stop();
            if (this.executionWorker != null) this.executionWorker.interrupt();
            hardware = Hardware.CHIP8;
            this.mainStage.setTitle(hardware.toString());
            Screen.updateScreenFormat();
            this.initializeStage();
            if (debugger != null) debugger.updatePc();
        });
        RadioMenuItem chip8HiresItem = new RadioMenuItem(Hardware.CHIP8HIRES.toString());
        chip8HiresItem.setOnAction(_ -> {
            this.timeline.stop();
            if (this.executionWorker != null) this.executionWorker.interrupt();
            hardware = Hardware.CHIP8HIRES;
            this.mainStage.setTitle(hardware.toString());
            Screen.updateScreenFormat();
            this.initializeStage();
            if (debugger != null) debugger.updatePc();
        });
        RadioMenuItem schip8Item = new RadioMenuItem(Hardware.SCHIP8.toString());
        schip8Item.setOnAction(_ -> {
            this.timeline.stop();
            if (this.executionWorker != null) this.executionWorker.interrupt();
            hardware = Hardware.SCHIP8;
            this.mainStage.setTitle(hardware.toString());
            Screen.updateScreenFormat();
            this.initializeStage();
            if (debugger != null) debugger.updatePc();
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

    public static void closeDebugger() {
        isDebuggerEnabled = false;
        debugger.close();
        debugger = null;
        ((RadioMenuItem) menuBar.getMenus().filtered(menu -> menu.getText().equals("Debug")).getFirst().getItems().getFirst()).setSelected(false);
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        initializeStage();
    }

    @Override
    public void stop() {
        System.exit(0);
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
