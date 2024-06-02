package fr.yinxfox.debugger;

import fr.yinxfox.Launcher;
import fr.yinxfox.emulator.ExecutionWorker;
import fr.yinxfox.emulator.Hardware;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;


public class Debugger extends Thread {

    private final static int WIDTH = 600;
    private final static int HEIGHT = -1;

    private final static Font LIBE = new Font("Liberation Mono", 18);
    private final ArrayList<Label> registers = new ArrayList<>();
    private final ArrayList<Label> stack = new ArrayList<>();
    private final ArrayList<Label> memory = new ArrayList<>();
    private final int nbMemory = 18;
    private int stackSize;
    private final ArrayList<Label> opcodes = new ArrayList<>();
    private static final double FPS = Launcher.getFPS();
    private final static int initialPc = 0x0200;

    private final Stage mainStage;
    private final Scene mainScene;
    private final AnchorPane anchor;
    private MenuBar menuBar;
    private final GridPane grid;
    private ExecutionWorker executionWorker;
    private Timeline timeline;

    public Debugger() {
        this.executionWorker = null;
        this.grid = new GridPane();
        this.anchor = new AnchorPane();
        this.anchor.getChildren().add(grid);
        this.createMenuBar();
        VBox root = new VBox();
        root.getChildren().add(menuBar);
        root.getChildren().add(this.anchor);
        this.mainScene = new Scene(root, WIDTH, HEIGHT);
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
        this.createMenuBar();
        VBox root = new VBox();
        root.getChildren().add(menuBar);
        root.getChildren().add(this.anchor);
        this.mainScene = new Scene(root, WIDTH, HEIGHT);
        this.mainScene.setFill(Color.RED);
        this.mainStage = new Stage();

        setupDisplay();
        this.start();
    }

    private void setupDisplay() {
        this.grid.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(180));

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

    private void createMenuBar() {
        menuBar = new MenuBar();

        Menu controlMenu = new Menu("Control");
        RadioMenuItem pauseItem = new RadioMenuItem("Pause");
        pauseItem.setOnAction(_ -> {
            if (executionWorker != null && ExecutionWorker.PAUSED) {
                synchronized (executionWorker.getPause()) {
                    executionWorker.getPause().notify();
                }
            }
            ExecutionWorker.PAUSED = !ExecutionWorker.PAUSED;
        });
        pauseItem.setAccelerator(new KeyCodeCombination(KeyCode.F6));
        MenuItem stepItem = new MenuItem("Step");
        stepItem.setOnAction(_ -> {
            if (executionWorker != null && ExecutionWorker.PAUSED) {
                synchronized (executionWorker.getPause()) {
                    executionWorker.getPause().notify();
                }
            }
        });
        stepItem.setAccelerator(new KeyCodeCombination(KeyCode.F3));
        controlMenu.getItems().add(pauseItem);
        controlMenu.getItems().add(stepItem);

        menuBar.getMenus().add(controlMenu);
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
                int soundTimer = ExecutionWorker.getSoundTimer();
                registers.get(i).setText("ST     " + String.format("%02d", soundTimer));
            }
        }
    }

    private void updateStack() {
        for (int i = 0; i < stack.size(); i++) {
            if (stack.get(i).getText().startsWith("SP")) {
                stack.get(i).setText("SP     " + String.format("%02d", executionWorker.getSp()));
            } else if (stack.get(i).getText().startsWith("00")) {
                for (int j = 0; j < stackSize; j++) {
                    stack.get(i + j).setText(String.format("%02d", j) + "  " + String.format("0x%03X", executionWorker.getStack()[j]));
                }
                i += stackSize - 1;
            }
        }
    }

    private void updateMemory() {
        int pc = executionWorker.getPc();
        int j = 3;
        int memorySize = executionWorker.getMemory().length;
        for (int i = pc - nbMemory; i <= pc + nbMemory; i += 2) {
            if (i == memorySize || i - 1 == memorySize) break;
            memory.get(j).setText(String.format("%04X", i) + " " + String.format("%04X", (executionWorker.getMemory()[i] << 8) | executionWorker.getMemory()[i + 1]));
            j++;
        }
    }

    private void updateOpcodes() {
        int pc = executionWorker.getPc();
        int j = 3;
        for (int i = pc - nbMemory; i <= pc + nbMemory; i += 2) {
            if (i == pc - 2) opcodes.get(j - 1).setBackground(Background.EMPTY);
            if (i == pc) opcodes.get(j - 1).setBackground(Background.fill(Color.GREEN));
            if (i == pc + 2) opcodes.get(j - 1).setBackground(Background.fill(Color.YELLOW));
            if (((executionWorker.getMemory()[i - 2] << 8) | executionWorker.getMemory()[i - 1]) == 0xF000) {
                opcodes.get(j).setText("");
            } else if (((executionWorker.getMemory()[i] << 8) | executionWorker.getMemory()[i + 1]) == 0xF000) {
                opcodes.get(j).setText("I := LONG " + String.format("%04X", (executionWorker.getMemory()[i + 2] << 8) | executionWorker.getMemory()[i + 3]));
            } else {
                opcodes.get(j).setText(OpcodeTranslator.decodeOp((executionWorker.getMemory()[i] << 8) | executionWorker.getMemory()[i + 1]));
            }
            j++;
        }
    }

    private void setupRegisters() {
        createLabel("REGISTERS", registers);
        createLabel("Reg Value", registers);
        createLabel("---------", registers);
        createLabel("PC 0x0200", registers);
        createLabel("I   0x000", registers);
        createLabel("         ", registers);
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

    private void setupStack() {
        stackSize = (Launcher.getHardware() == Hardware.CHIP8 || Launcher.getHardware() == Hardware.CHIP8HIRES) ? 12 : 16;
        createLabel("STACK", stack);
        createLabel("Lv Value", stack);
        createLabel("---------", stack);
        createLabel("SP     00", stack);
        createLabel("         ", stack);
        for (int i = 0; i < 16; i++) {
            createLabel(String.format("%02d", i) + "  0x000", stack);
        }
        createLabel("         ", stack);
        createLabel("         ", stack);
        createLabel("         ", stack);
        createLabel("PLANE  01", stack);

        if (stackSize == 12) {
            for (int i = 17; i < 17 + 4; i++) {
                stack.get(i).setText("");
            }
        }

        for (int i = 0; i < stack.size(); i++) {
            this.grid.add(stack.get(i), 1, i);
        }
    }

    public void updateStackDisplay() {
        stackSize = (Launcher.getHardware() == Hardware.CHIP8 || Launcher.getHardware() == Hardware.CHIP8HIRES) ? 12 : 16;
        if (stackSize == 12) {
            for (int i = 17; i < 17 + 4; i++) {
                stack.get(i).setText("");
            }
        } else {
            for (int i = 17; i < 17 + 4; i++) {
                stack.get(i).setText(String.format("%02d", i - 5) + "  0x000");
            }
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
