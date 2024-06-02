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

    //TODO: Add XO-CHIP support
    //TODO: Add same quirks as here : http://johnearnest.github.io/Octo/
    //TODO: Change JavaFX display to PixelBuffer (https://foojay.io/today/high-performance-rendering-in-javafx/)

    private static Debugger debugger = null;
    private static boolean isDebuggerEnabled = false;

    private static final double FPS = 60;
    private static Hardware hardware = Hardware.CHIP8;
    private Stage mainStage;
    private static MenuBar menuBar = null;
    private static boolean audioPlaying;
    private static boolean shiftQuirk = false;
    private static boolean loadAndSaveQuirk = false;
    private static boolean clearVfQuirk = false;

    private final Keyboard keyboard;
    private Screen video;
    private final SoundMaker soundMaker;
    private ExecutionWorker executionWorker;
    private Timeline timeline;
    private String filePath;

    public Launcher() {
        Screen.setColorPalette(ColorPalette.GREY);

        this.keyboard = new Keyboard(this);
        this.executionWorker = null;
        this.soundMaker = new SoundMaker();
    }

    private void restartEmulator(String filePath) {
        this.timeline.stop();
        if (this.executionWorker != null) this.executionWorker.interrupt();
        this.video.clear();
        this.video.setSelectedPlane(1);
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
        chip8Item.setOnAction(_ -> changeHardware(Hardware.CHIP8));
        RadioMenuItem chip8HiresItem = new RadioMenuItem(Hardware.CHIP8HIRES.toString());
        chip8HiresItem.setOnAction(_ -> changeHardware(Hardware.CHIP8HIRES));
        RadioMenuItem schip8Item = new RadioMenuItem(Hardware.SCHIP8.toString());
        schip8Item.setOnAction(_ -> changeHardware(Hardware.SCHIP8));
        RadioMenuItem xochipItem = new RadioMenuItem(Hardware.XOCHIP.toString());
        xochipItem.setOnAction(_ -> changeHardware(Hardware.XOCHIP));
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

        ToggleGroup speedGroup = new ToggleGroup();
        RadioMenuItem tenItem = new RadioMenuItem("10");
        tenItem.setOnAction(_ -> ExecutionWorker.setOPPS(10));
        menuSpeed.getItems().add(tenItem);
        RadioMenuItem hundredItem = new RadioMenuItem("100");
        hundredItem.setOnAction(_ -> ExecutionWorker.setOPPS(100));
        menuSpeed.getItems().add(hundredItem);
        RadioMenuItem fiveHundredItem = new RadioMenuItem("500");
        fiveHundredItem.setSelected(true);
        fiveHundredItem.setOnAction(_ -> ExecutionWorker.setOPPS(500));
        menuSpeed.getItems().add(fiveHundredItem);
        RadioMenuItem thousandItem = new RadioMenuItem("1000");
        thousandItem.setOnAction(_ -> ExecutionWorker.setOPPS(1000));
        menuSpeed.getItems().add(thousandItem);
        tenItem.setToggleGroup(speedGroup);
        hundredItem.setToggleGroup(speedGroup);
        fiveHundredItem.setToggleGroup(speedGroup);
        thousandItem.setToggleGroup(speedGroup);

        Menu menuDebug = new Menu("Debug");
        RadioMenuItem enableDebugger = new RadioMenuItem("Debugger");
        enableDebugger.setOnAction(_ -> toggleDebugger());
        menuDebug.getItems().add(enableDebugger);

        ToggleGroup colorToggle = new ToggleGroup();
        Menu menuColor = new Menu("Color");
        RadioMenuItem grayItem = new RadioMenuItem(ColorPalette.GREY.getName());
        grayItem.setSelected(true);
        grayItem.setOnAction(_ -> Screen.setColorPalette(ColorPalette.GREY));
        menuColor.getItems().add(grayItem);
        RadioMenuItem magentaCyanItem = new RadioMenuItem(ColorPalette.MAGENTACYAN.getName());
        magentaCyanItem.setOnAction(_ -> Screen.setColorPalette(ColorPalette.MAGENTACYAN));
        menuColor.getItems().add(magentaCyanItem);
        RadioMenuItem blackAndWhiteItem = new RadioMenuItem(ColorPalette.BLACKANDWHITE.getName());
        blackAndWhiteItem.setOnAction(_ -> Screen.setColorPalette(ColorPalette.BLACKANDWHITE));
        menuColor.getItems().add(blackAndWhiteItem);
        RadioMenuItem orangeItem = new RadioMenuItem(ColorPalette.ORANGE.getName());
        orangeItem.setOnAction(_ -> Screen.setColorPalette(ColorPalette.ORANGE));
        menuColor.getItems().add(orangeItem);
        grayItem.setToggleGroup(colorToggle);
        magentaCyanItem.setToggleGroup(colorToggle);
        blackAndWhiteItem.setToggleGroup(colorToggle);
        orangeItem.setToggleGroup(colorToggle);

        Menu menuQuirk = new Menu("Quirk");
        RadioMenuItem shiftItem = new RadioMenuItem("<<= and >>= modify vx in place and ignore vy");
        shiftItem.setOnAction(_ -> shiftQuirk = !shiftQuirk);
        RadioMenuItem loadSaveItem = new RadioMenuItem("load and store operations leave i unchanged");
        loadSaveItem.setOnAction(_ -> loadAndSaveQuirk = !loadAndSaveQuirk);
        RadioMenuItem vFItem = new RadioMenuItem("clear vF after vx |= vy, vx &= vy, and vx ^= vy");
        vFItem.setOnAction(_ -> clearVfQuirk = !clearVfQuirk);
        menuQuirk.getItems().add(shiftItem);
        menuQuirk.getItems().add(loadSaveItem);
        menuQuirk.getItems().add(vFItem);

        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(menuHardware);
        menuBar.getMenus().add(menuSpeed);
        menuBar.getMenus().add(menuDebug);
        menuBar.getMenus().add(menuColor);
        menuBar.getMenus().add(menuQuirk);
    }

    private void changeHardware(Hardware hd) {
        this.timeline.stop();
        if (this.executionWorker != null) this.executionWorker.interrupt();
        hardware = hd;
        ExecutionWorker.setOPPS(hd.getSpeed());
        ((RadioMenuItem) menuBar.getMenus().filtered(menu -> menu.getText().equals("Speed")).getFirst().getItems().filtered(item -> item.getText().contains(String.valueOf(hd.getSpeed()))).getFirst()).setSelected(true);
        if (debugger != null) debugger.updateStackDisplay();
        this.mainStage.setTitle(hardware.toString());
        Screen.updateScreenFormat();
        this.initializeStage();
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
                                audioPlaying = (ExecutionWorker.getSoundTimer() > 0);
                            }
                        })
        );
        this.timeline.setCycleCount(Animation.INDEFINITE);

        mainScene.setOnKeyPressed(keyEvent -> this.keyboard.setDown(keyEvent.getCode()));
        mainScene.setOnKeyReleased(keyEvent -> this.keyboard.setUp(keyEvent.getCode()));

        this.mainStage.setResizable(false);
        this.mainStage.setScene(mainScene);
        this.mainStage.show();

        if (!System.getProperty("os.name").contains("Windows") && System.getenv("XDG_SESSION_TYPE").contains("11")) mainScene.getWindow().centerOnScreen();
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

    public static void setHardware(Hardware hardware) {
        Launcher.hardware = hardware;
    }

    public static double getFPS() {
        return FPS;
    }

    public static boolean isAudioPlaying() {
        return audioPlaying;
    }

    public static boolean isShiftQuirk() {
        return shiftQuirk;
    }

    public static boolean isLoadAndSaveQuirk() {
        return loadAndSaveQuirk;
    }

    public static boolean isClearVfQuirk() {
        return clearVfQuirk;
    }
}
