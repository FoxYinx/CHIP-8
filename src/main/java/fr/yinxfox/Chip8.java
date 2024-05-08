package fr.yinxfox;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Scanner;

public class Chip8 extends Application {

    //Fixme: White line appearing on bottom row

    private static final double FPS = 60;
    private static boolean isWindows;
    private static Hardware hardware = Hardware.NULL;
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

        menuFile.getItems().add(loadRomItem);
        menuFile.getItems().add(exitItem);

        menuBar.getMenus().add(menuFile);

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
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose which system to launch:");
        System.out.println("1 - CHIP-8");
        System.out.println("2 - SUPER CHIP-8");
        System.out.println("3 - XO-CHIP-8");
        while (hardware == Hardware.NULL) {
            switch (scanner.nextLine()){
                case "1" -> hardware = Hardware.CHIP8;
                case "2" -> hardware = Hardware.SCHIP8;
                case "3" -> hardware = Hardware.XOCHIP8;
            }
        }
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
