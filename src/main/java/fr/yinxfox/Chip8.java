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
    private Screen video;
    private ExecutionWorker executionWorker;
    private Timeline timeline;

    public Chip8() {
        keyboard = new Keyboard(this);
        executionWorker = null;
    }

    private void loadROM(String filename) {
        timeline.stop();
        if (executionWorker != null) executionWorker.interrupt();
        video.clear();
        executionWorker = new ExecutionWorker(filename, video, keyboard);
        System.out.println(filename + " has been loaded!");
        timeline.play();
    }

    private void initializeStage() {
        video = new Screen();
        video.render();

        mainStage.setTitle("CHIP-8");
        mainStage.setMinWidth(WIDTH + 16);
        mainStage.setMaxWidth(WIDTH + 16);
        mainStage.setMinHeight(HEIGHT + 39);
        mainStage.setMaxHeight(HEIGHT + 39);
        mainStage.setResizable(false);

        VBox root = new VBox();
        root.getChildren().add(video);
        Scene mainScene = new Scene(root);

        timeline = new Timeline(
                new KeyFrame(Duration.seconds((double) 1 / FPS),
                        Event -> {
                            if (executionWorker != null) {
                                Platform.runLater(() -> this.video.render());
                                executionWorker.updateTimers();
                            }
                        })
        );
        timeline.setCycleCount(Animation.INDEFINITE);

        mainScene.setOnKeyPressed(keyEvent -> keyboard.setDown(keyEvent.getCode()));
        mainScene.setOnKeyReleased(keyEvent -> keyboard.setUp(keyEvent.getCode()));

        mainStage.setScene(mainScene);
        mainStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        initializeStage();
        this.loadROM("games/Rush Hour [Hap, 2006].ch8");
    }

    public ExecutionWorker getExecutionWorker() {
        return executionWorker;
    }
}
