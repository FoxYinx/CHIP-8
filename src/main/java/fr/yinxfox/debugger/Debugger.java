package fr.yinxfox.debugger;

import fr.yinxfox.Launcher;
import fr.yinxfox.emulator.ExecutionWorker;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
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
    private static final double FPS = Launcher.getFPS();

    private Stage mainStage;
    private Scene mainScene;
    private GridPane grid;
    private ExecutionWorker executionWorker;
    private Timeline timeline;

    public Debugger() {
        this.executionWorker = null;
        this.grid = new GridPane();
        this.mainScene = new Scene(this.grid, WIDTH, HEIGHT);
        this.mainScene.setFill(Color.RED);
        this.mainStage = new Stage();

        setupDisplay();
        this.start();
    }

    public Debugger(ExecutionWorker executionWorker) {
        this.executionWorker = executionWorker;
        this.grid = new GridPane();
        this.mainScene = new Scene(this.grid, WIDTH, HEIGHT);
        this.mainScene.setFill(Color.RED);
        this.mainStage = new Stage();

        setupDisplay();
        this.start();
    }

    public void setupDisplay() {
        //grid.setPadding(new Insets(0, 5, 0, 5));
        grid.setHgap(5);
        grid.setAlignment(Pos.CENTER);

        this.mainStage.setResizable(false);
        this.mainStage.setTitle("Debugger");
        this.mainStage.setScene(this.mainScene);

        this.timeline = new Timeline(
                new KeyFrame(Duration.seconds((double) 1 / FPS),
                        _ -> this.updateRegisters())
        );
        this.timeline.setCycleCount(Animation.INDEFINITE);

        this.setupRegisters();

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
        createRegister("REGISTERS");
        createRegister("Reg Value");
        createRegister("---------");
        createRegister("PC 0x0000");
        createRegister("         ");
        createRegister("I   0x000");
        for (int i = 0; i < 16; i++) {
            createRegister("v" + String.format("%01X", i) + " 0x0000");
        }
        createRegister("         ");
        createRegister("DT     00");
        createRegister("ST     00");

        for (int i = 0; i < registers.size(); i++) {
            registers.get(i).setFont(LIBE);
            this.grid.add(registers.get(i), 0, i);
        }
    }

    private void createRegister(String name) {
        Label label = new Label(name);
        registers.add(label);
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
