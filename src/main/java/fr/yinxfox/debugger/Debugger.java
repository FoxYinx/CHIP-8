package fr.yinxfox.debugger;

import fr.yinxfox.Launcher;
import fr.yinxfox.emulator.ExecutionWorker;
import fr.yinxfox.emulator.Hardware;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;


public class Debugger extends Thread {

    //TODO: Add opcode decoder
    //TODO: Add ways to change execution speed / pause it

    private final static int WIDTH = 600;
    private final static int HEIGHT = 600;

    private final static Font LIBE = new Font("Liberation Mono", 18);
    private final ArrayList<Label> registers = new ArrayList<>();
    private final ArrayList<Label> stack = new ArrayList<>();
    private final ArrayList<Label> memory = new ArrayList<>();
    private final int nbMemory = 18;
    private final ArrayList<Label> opcodes = new ArrayList<>();
    private static final double FPS = Launcher.getFPS();
    private static int initialPc;

    private final Stage mainStage;
    private final Scene mainScene;
    private final AnchorPane anchor;
    private final GridPane grid;
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

    private void setupDisplay() {
        this.grid.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(200));

        AnchorPane.setTopAnchor(grid, 0.0);
        AnchorPane.setBottomAnchor(grid, 0.0);
        AnchorPane.setLeftAnchor(grid, 0.0);
        AnchorPane.setRightAnchor(grid, 0.0);

        this.mainStage.setResizable(false);
        this.mainStage.setTitle("Debugger");
        this.mainStage.setScene(this.mainScene);
        this.mainStage.setOnCloseRequest(_ -> Launcher.closeDebugger());

        this.timeline = new Timeline(
                new KeyFrame(Duration.seconds((double) 1 / FPS),
                        _ -> {
                    if (executionWorker != null) {
                        this.updateRegisters();
                        this.updateStack();
                        this.updateMemory();
                        this.updateOpcodes();
                    }
                })
        );
        this.timeline.setCycleCount(Animation.INDEFINITE);

        this.setupRegisters();
        this.setupStack();
        this.setupMemory();
        this.setupOpcodes();

        this.mainStage.show();
    }

    private void updateRegisters() {
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

    private void updateStack() {
        for (int i = 0; i < stack.size(); i++) {
            if (stack.get(i).getText().startsWith("SP")) {
                stack.get(i).setText("SP     " + String.format("%02d", executionWorker.getSp()));
            } else if (stack.get(i).getText().startsWith("00")) {
                for (int j = 0; j < 12; j++) {
                    stack.get(i + j).setText(String.format("%02d", j) + "  " + String.format("0x%03X", executionWorker.getStack()[j]));
                }
                i += 11;
            }
        }
    }

    private void updateMemory() {
        int pc = executionWorker.getPc();
        int j = 3;
        for (int i = pc - nbMemory; i <= pc + nbMemory; i += 2) {
            memory.get(j).setText(String.format("%04X", i) + " " + String.format("%04X", (executionWorker.getMemory()[i] << 8) | executionWorker.getMemory()[i + 1]));
            j++;
        }
    }

    private void updateOpcodes() {
        int pc = executionWorker.getPc();
        int j = 3;
        for (int i = pc - nbMemory; i <= pc + nbMemory; i += 2) {
            opcodes.get(j).setText(OpcodeTranslator.decodeOp((executionWorker.getMemory()[i] << 8) | executionWorker.getMemory()[i + 1]));
            j++;
        }
    }

    private void setupRegisters() {
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

        this.updatePc();
    }

    public void updatePc() {
        initialPc = (Launcher.getHardware() == Hardware.CHIP8) ? ExecutionWorker.getStartAddress() : ExecutionWorker.getStartAddressHires();
        for (Label register : registers) {
            if (register.getText().startsWith("PC")) {
                register.setText("PC " + String.format("0x%04X", initialPc));
            }
        }
    }

    //Fixme: la taille du stack change avec le CHIP choisit

    private void setupStack() {
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

    private void setupMemory() {
        createLabel("MEMORY", memory);
        createLabel("Adr Value", memory);
        createLabel("---------", memory);
        for (int i = initialPc - nbMemory; i <= initialPc + nbMemory; i += 2) {
            createLabel(String.format("%04X", i) + " 0000", memory);
        }

        for (int i = 0; i < memory.size(); i++) {
            this.grid.add(memory.get(i), 2, i);
        }
    }

    private void setupOpcodes() {
        createLabel("OPCODE", opcodes);
        createLabel("Inst", opcodes);
        createLabel("---------", opcodes);
        for (int i = initialPc - nbMemory; i <= initialPc + nbMemory; i += 2) {
            createLabel("null", opcodes);
        }

        for (int i = 0; i < opcodes.size(); i++) {
            this.grid.add(opcodes.get(i), 3, i);
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
