package fr.yinxfox.debugger;

import fr.yinxfox.Launcher;
import fr.yinxfox.emulator.ExecutionWorker;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;

public class Debugger extends Thread {

    private final static int WIDTH = 450;
    private final static int HEIGHT = 600;

    private final static Font LIBE = new Font("Liberation Mono", 18);
    private final ArrayList<Label> registers = new ArrayList<>();
    private final ArrayList<Label> stack = new ArrayList<>();
    private final ArrayList<Label> memory = new ArrayList<>();
    private static final double FPS = Launcher.getFPS();

    private Stage mainStage;
    private Scene mainScene;
    private AnchorPane anchor;
    private GridPane grid;
    private ExecutionWorker executionWorker;
    private Timeline timeline;

    public Debugger() {
        this.executionWorker = null;
        this.grid = new GridPane();
        this.anchor = new AnchorPane();
        this.anchor.getChildren().add(grid);
        this.mainScene = new Scene(this.anchor, WIDTH, HEIGHT);
        this.mainScene.setFill(Color.RED);
        this.mainStage = new Stage();

        setupDisplay();
        this.start();
    }

    public Debugger(ExecutionWorker executionWorker) {
        this.executionWorker = executionWorker;
        this.grid = new GridPane();
        this.anchor = new AnchorPane();
        this.anchor.getChildren().add(grid);
        this.mainScene = new Scene(this.anchor, WIDTH, HEIGHT);
        this.mainScene.setFill(Color.RED);
        this.mainStage = new Stage();

        setupDisplay();
        this.start();
    }

    public void setupDisplay() {
        AnchorPane.setTopAnchor(grid, 0.0);
        AnchorPane.setBottomAnchor(grid, 0.0);
        AnchorPane.setLeftAnchor(grid, 0.0);
        AnchorPane.setRightAnchor(grid, 0.0);

        this.mainStage.setResizable(false);
        this.mainStage.setTitle("Debugger");
        this.mainStage.setScene(this.mainScene);

        this.timeline = new Timeline(
                new KeyFrame(Duration.seconds((double) 1 / FPS),
                        _ -> this.updateRegisters())
        );
        this.timeline.setCycleCount(Animation.INDEFINITE);

        this.setupRegisters();
        this.setupStack();
        this.setupMemory();

        this.mainStage.show();
    }

    private void updateRegisters() {
        if (executionWorker != null) {
            for (int i = 0; i < registers.size(); i++) {
                if (registers.get(i).getText().startsWith("PC")) {
                    int pc = executionWorker.getPc();
                    registers.get(i).setText("PC " + String.format("0x%04X", pc));
                } else if (registers.get(i).getText().startsWith("I")) {
                    int index = executionWorker.getIndex();
                    registers.get(i).setText("I   " + String.format("0x%03X", index));
                } else if (registers.get(i).getText().startsWith("v")) {
                    for (int j = 0; j < 16; j++) {
                        registers.get(i + j).setText("v" + String.format("%01X", j) + " " + String.format("0x%04X", executionWorker.getRegisters()[j]));
                    }
                    i += 15;
                } else if (registers.get(i).getText().startsWith("DT")) {
                    int delayTimer = executionWorker.getDelayTimer();
                    registers.get(i).setText("DT     " + String.format("%02d", delayTimer));
                } else if (registers.get(i).getText().startsWith("ST")) {
                    int soundTimer = executionWorker.getSoundTimer();
                    registers.get(i).setText("ST     " + String.format("%02d", soundTimer));
                }
            }
        }
    }

    public void setupRegisters() {
        createLabel("REGISTERS", registers);
        createLabel("Reg Value", registers);
        createLabel("---------", registers);
        createLabel("PC 0x0000", registers);
        createLabel("         ", registers);
        createLabel("I   0x000", registers);
        for (int i = 0; i < 16; i++) {
            createLabel("v" + String.format("%01X", i) + " 0x0000", registers);
        }
        createLabel("         ", registers);
        createLabel("DT     00", registers);
        createLabel("ST     00", registers);

        for (int i = 0; i < registers.size(); i++) {
            this.grid.add(registers.get(i), 0, i);
        }
    }

    //Fixme: la taille du stack change avec le CHIP choisit

    public void setupStack() {
        createLabel("STACK", stack);
        createLabel("Lv Value", stack);
        createLabel("---------", stack);
        createLabel("SP     00", stack);
        createLabel("         ", stack);
        for (int i = 0; i < 12; i++) {
            createLabel(String.format("%02d", i) + "  0x000", stack);
        }

        for (int i = 0; i < stack.size(); i++) {
            this.grid.add(stack.get(i), 1, i);
        }
    }

    public void setupMemory() {
        createLabel("MEMORY", memory);

        for (int i = 0; i < memory.size(); i++) {
            this.grid.add(memory.get(i), 2, i);
        }
    }

    private void createLabel(String name, ArrayList<Label> liste) {
        Label label = new Label(name);
        label.setFont(LIBE);
        GridPane.setHalignment(label, HPos.CENTER);
        GridPane.setHgrow(label, Priority.ALWAYS);
        liste.add(label);
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
