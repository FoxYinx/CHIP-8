package fr.yinxfox;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class Chip8 extends Application {

    private static final int WIDTH = Screen.getWIDTH() * Screen.getScale();
    private static final int HEIGHT = Screen.getHEIGHT() * Screen.getScale();
    private static final double FPS = 60;
    private Stage mainStage;

    private final Keyboard keyboard;
    private final Screen video;
    private final SoundMaker soundMaker;
    private ExecutionWorker executionWorker;
    private Timeline timeline;

    public Chip8() {
        this.keyboard = new Keyboard(this);
        this.video = new Screen();
        this.executionWorker = null;
        this.soundMaker = new SoundMaker();
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

        this.mainStage.setTitle("CHIP-8");
        this.mainStage.setMinWidth(WIDTH + 16);
        this.mainStage.setMaxWidth(WIDTH + 16);
        this.mainStage.setMinHeight(HEIGHT + 39);
        this.mainStage.setMaxHeight(HEIGHT + 39);
        this.mainStage.setResizable(false);

        VBox root = new VBox();
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

        this.mainStage.setScene(mainScene);
        this.mainStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        initializeStage();
        //this.loadROM("games/Rush Hour [Hap, 2006].ch8");
        this.loadROM("demos/Trip8 Demo (2008) [Revival Studios].ch8");
    }

    public ExecutionWorker getExecutionWorker() {
        return this.executionWorker;
    }
}
