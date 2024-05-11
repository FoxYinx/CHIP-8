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

    private final static Font LIBE = new Font("Liberation Mono", 20);
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
        this.mainScene = new Scene(this.root, 450, 450);
        this.mainScene.setFill(Color.RED);
        this.mainStage = new Stage();

        setupDisplay();
        this.start();
    }

    public Debugger(ExecutionWorker executionWorker) {
        this.executionWorker = executionWorker;
        this.root = new VBox();
        this.mainScene = new Scene(this.root, 450, 450);
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
        for (Label label:labels) {
            if (executionWorker != null && label.getText().contains("PC")) {
                int pc = executionWorker.getPc();
                label.setText("PC " + String.format("0x%04X", pc));
            }
        }
    }

    public void setupRegisters() {
        Label REGISTERS = new Label("REGISTERS");
        labels.add(REGISTERS);
        Label RegValue = new Label("Reg Value");
        labels.add(RegValue);
        Label separator = new Label("---------");
        labels.add(separator);
        Label pc = new Label("PC 0x0000");
        labels.add(pc);

        for (Label label:labels) {
            label.setFont(LIBE);
            this.root.getChildren().add(label);
        }
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
