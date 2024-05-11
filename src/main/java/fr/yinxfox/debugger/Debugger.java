package fr.yinxfox.debugger;

import fr.yinxfox.Launcher;
import fr.yinxfox.emulator.ExecutionWorker;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;

public class Debugger extends Thread {

    private final static int WIDTH = 450;
    private final static int HEIGHT = 600;

    private final static Font LIBE = new Font("Liberation Mono", 18);
    private final ArrayList<Label> labels = new ArrayList<>();
    private static final double FPS = Launcher.getFPS();

    private Stage mainStage;
    private Scene mainScene;
    private VBox root;
    private ExecutionWorker executionWorker;
    private Timeline timeline;

    public Debugger() {
        this.executionWorker = null;
        this.root = new VBox();
        this.mainScene = new Scene(this.root, WIDTH, HEIGHT);
        this.mainScene.setFill(Color.RED);
        this.mainStage = new Stage();

        setupDisplay();
        this.start();
    }

    public Debugger(ExecutionWorker executionWorker) {
        this.executionWorker = executionWorker;
        this.root = new VBox();
        this.mainScene = new Scene(this.root, WIDTH, HEIGHT);
        this.mainScene.setFill(Color.RED);
        this.mainStage = new Stage();

        setupDisplay();
        this.start();
    }

    public void setupDisplay() {
        this.mainStage.setResizable(false);
        this.mainStage.setTitle("Debugger");
        this.mainStage.setScene(this.mainScene);

        this.timeline = new Timeline(
                new KeyFrame(Duration.seconds((double) 1 / FPS),
                        _ -> this.updateLabels())
        );
        this.timeline.setCycleCount(Animation.INDEFINITE);

        this.setupRegisters();

        this.mainStage.show();
    }

    private void updateLabels() {
        if (executionWorker != null) {
            for (int i = 0; i < labels.size(); i++) {
                if (labels.get(i).getText().startsWith("PC")) {
                    int pc = executionWorker.getPc();
                    labels.get(i).setText("PC " + String.format("0x%04X", pc));
                } else if (labels.get(i).getText().startsWith("I")) {
                    int index = executionWorker.getIndex();
                    labels.get(i).setText("I   " + String.format("0x%03X", index));
                } else if (labels.get(i).getText().startsWith("v")) {
                    for (int j = 0; j < 16; j++) {
                        labels.get(i + j).setText("v" + String.format("%01X", j) + " " + String.format("0x%04X", executionWorker.getRegisters()[j]));
                    }
                    i += 15;
                } else if (labels.get(i).getText().startsWith("DT")) {
                    int delayTimer = executionWorker.getDelayTimer();
                    labels.get(i).setText("DT     " + String.format("%02d", delayTimer));
                } else if (labels.get(i).getText().startsWith("ST")) {
                    int soundTimer = executionWorker.getSoundTimer();
                    labels.get(i).setText("ST     " + String.format("%02d", soundTimer));
                }
            }
        }
    }

    public void setupRegisters() {
        createLabel("REGISTERS");
        createLabel("Reg Value");
        createLabel("---------");
        createLabel("PC 0x0000");
        createLabel("         ");
        createLabel("I   0x000");
        for (int i = 0; i < 16; i++) {
            createLabel("v" + String.format("%01X", i) + " 0x0000");
        }
        createLabel("         ");
        createLabel("DT     00");
        createLabel("ST     00");

        for (Label label:labels) {
            label.setFont(LIBE);
            this.root.getChildren().add(label);
        }
    }

    private void createLabel(String name) {
        Label label = new Label(name);
        labels.add(label);
    }

    @Override
    public void run() {
        super.run();
        timeline.play();
    }

    public void close() {
        timeline.stop();
        this.mainStage.close();
    }

    public void setExecutionWorker(ExecutionWorker executionWorker) {
        this.executionWorker = executionWorker;
    }
}
