package fr.yinxfox;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class Chip8 extends Application {

    //Fixme: L'opcode 0x0A n'attend pas qu'on ait relaché la touche
    //TODO: Ajouter le support du son

    private static final int OPCODE_SECONDS = 500;
    private static final int WIDTH = Screen.getWIDTH() * Screen.getScale();
    private static final int HEIGHT = Screen.getHEIGHT() * Screen.getScale();
    private Stage mainStage;
    Timeline gameLoop;

    private static final int START_ADDRESS = 0x200;
    private static final String PATH = "C:\\Users\\tolle\\IdeaProjects\\CHIP-8\\src\\main\\resources\\roms\\";
    private static final int FONTSET_SIZE = 80;
    private static final int FONTSET_START_ADDRESS = 0x50;
    private static final int[] FONTSET = new int[]{
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    private final int[] registers;
    private final int[] memory;
    private int index;
    private int pc;
    private int[] stack;
    private int sp;
    private int delayTimer;
    private int soundTimer;
    private final Keyboard keyboard;
    private Screen video;
    private int opcode;
    private long timeTimer;

    public Chip8() {
        registers = new int[16];
        memory = new int[4096];
        index = 0;
        pc = START_ADDRESS;
        stack = new int[16];
        sp = 0;
        delayTimer = 0;
        soundTimer = 0;
        keyboard = new Keyboard();
        opcode = 0x0000;
        timeTimer = System.currentTimeMillis();

        System.arraycopy(FONTSET, 0, memory, FONTSET_START_ADDRESS, FONTSET_SIZE);
    }

    private void loadROM(String filename) {
        gameLoop.stop();
        video.clear();
        byte[] data;

        try {
            data = Files.readAllBytes(Path.of(PATH + filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < data.length; i++) {
            memory[START_ADDRESS + i] = data[i] & 0xFF;
        }

        System.out.println(filename + " has been loaded!");
        gameLoop.play();
    }

    public void cycle() {
        opcode = (memory[pc] << 8) | memory[pc + 1];
        pc += 2;

        this.execute();

        if (System.currentTimeMillis() > timeTimer + 16) {
            this.video.render();
            timeTimer = System.currentTimeMillis();
            if (delayTimer > 0) --delayTimer;
            if (soundTimer > 0) {
                --soundTimer;
                System.out.println("Make sound!");
            }
        }
    }

    private void execute() {
        //System.out.println("Read opcode: " + String.format("0x%04X", opcode));
        switch (opcode >> 12) {
            case 0x0 -> {
                if (opcode == 0x00E0) video.clear();
                else {
                    sp--;
                    pc = stack[sp];
                }
            }
            case 0x1 -> pc = opcode & 0x0FFF;
            case 0x2 -> {
                int address = opcode & 0x0FFF;
                stack[sp] = pc;
                sp++;
                pc = address;
            }
            case 0x3 -> {
                int Vx = (opcode & 0x0F00) >> 8;
                int octet = opcode & 0x00FF;
                if (registers[Vx] == octet) pc += 2;
            }
            case 0x4 -> {
                int Vx = (opcode & 0x0F00) >> 8;
                int octet = opcode & 0x00FF;
                if (registers[Vx] != octet) pc += 2;
            }
            case 0x5 -> {
                int Vx = (opcode & 0x0F00) >> 8;
                int Vy = (opcode & 0x00F0) >> 4;
                if (registers[Vx] == registers[Vy]) pc += 2;
            }
            case 0x6 -> {
                int Vx = (opcode & 0x0F00) >> 8;
                int octet = opcode & 0x00FF;
                registers[Vx] = octet;
            }
            case 0x7 -> {
                int Vx = (opcode & 0x0F00) >> 8;
                int octet = opcode & 0x00FF;
                int sum = registers[Vx] += octet;
                registers[Vx] = (sum > 255) ? sum - 256 : sum;
            }
            case 0x8 -> {
                switch (opcode & 0x000F) {
                    case 0x0 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int Vy = (opcode & 0x00F0) >> 4;
                        registers[Vx] = registers[Vy];
                    }
                    case 0x1 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int Vy = (opcode & 0x00F0) >> 4;
                        registers[Vx] |= registers[Vy];
                    }
                    case 0x2 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int Vy = (opcode & 0x00F0) >> 4;
                        registers[Vx] &= registers[Vy];
                    }
                    case 0x3 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int Vy = (opcode & 0x00F0) >> 4;
                        registers[Vx] ^= registers[Vy];
                    }
                    case 0x4 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int Vy = (opcode & 0x00F0) >> 4;
                        int sum = registers[Vx] + registers[Vy];
                        registers[Vx] = sum & 0xFF;
                        registers[0xF] = (sum > 0xFF) ? 1 : 0;
                    }
                    case 0x5 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int Vy = (opcode & 0x00F0) >> 4;
                        int VfValue = (registers[Vx] >= registers[Vy]) ? 1 : 0;
                        registers[Vx] -= registers[Vy];
                        registers[Vx] &= 0xFF;
                        registers[0xF] = VfValue;
                    }
                    case 0x6 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int VfValue = registers[Vx] & 1;
                        registers[Vx] >>>= 1;
                        registers[0xF] = VfValue;
                    }
                    case 0x7 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int Vy = (opcode & 0x00F0) >> 4;
                        int VfValue = (registers[Vy] >= registers[Vx]) ? 1 : 0;
                        registers[Vx] = registers[Vy] - registers[Vx];
                        registers[Vx] &= 0xFF;
                        registers[0xF] = VfValue;
                    }
                    case 0xE -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int VfValue = (registers[Vx] & 0x80) >> 7;
                        registers[Vx] <<= 1;
                        registers[Vx] &= 0xFF;
                        registers[0xF] = VfValue;
                    }
                }
            }
            case 0x9 -> {
                int Vx = (opcode & 0x0F00) >> 8;
                int Vy = (opcode & 0x00F0) >> 4;
                if (registers[Vx] != registers[Vy]) pc += 2;
            }
            case 0xA -> index = opcode & 0x0FFF;
            case 0xB -> {
                int address = opcode & 0x0FFF;
                pc = registers[0] + address;
            }
            case 0xC -> {
                int Vx = (opcode & 0x0F00) >> 8;
                int octet = opcode & 0x00FF;
                registers[Vx] = (new Random()).nextInt(256) & 0x00FF & octet;
            }
            case 0xD -> {
                int Vx = (opcode & 0x0F00) >> 8;
                int Vy = (opcode & 0x00F0) >> 4;
                int height = opcode & 0x000F;

                int xPos = registers[Vx] % Screen.getWIDTH();
                int yPos = registers[Vy] % Screen.getHEIGHT();

                registers[0xF] = 0;

                for (int row = 0; row < height; row++) {
                    int spriteOctet = memory[index + row];
                    for (int col = 0; col < 8; col++) {
                        int spritePixel = spriteOctet & (0x80 >> col);
                        if (spritePixel != 0) {
                            if (xPos + col >= Screen.getWIDTH() || yPos + row >= Screen.getHEIGHT()) break;
                            boolean collision = video.draw(xPos, col, yPos, row);
                            if (collision) registers[0xF] = 1;
                        }
                    }
                }
            }
            case 0xE -> {
                int Vx = (opcode & 0x0F00) >> 8;
                int key = registers[Vx];
                if ((opcode & 0x00FF) == 0x9E) {
                    if (keyboard.isPressed(key)) pc += 2;
                } else {
                    if (!keyboard.isPressed(key)) pc += 2;
                }
            }
            case 0xF -> {
                switch (opcode & 0x00FF) {
                    case 0x07 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        registers[Vx] = delayTimer;
                    }
                    case 0x0A -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        boolean resume = false;
                        for (int i = 0; i < 16; i++) {
                            if (keyboard.isPressed(i)) {
                                registers[Vx] = i;
                                resume = true;
                            }
                        }
                        if (!resume) pc -= 2;
                    }
                    case 0x15 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        delayTimer = registers[Vx];
                    }
                    case 0x18 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        soundTimer = registers[Vx];
                    }
                    case 0x1E -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        index += registers[Vx];
                    }
                    case 0x29 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int digit = registers[Vx];
                        index = FONTSET_START_ADDRESS + (5 * digit);
                    }
                    case 0x33 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int value = registers[Vx];
                        memory[index + 2] = value % 10;
                        value /= 10;
                        memory[index + 1] = value % 10;
                        value /= 10;
                        memory[index] = value % 10;
                    }
                    case 0x55 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        System.arraycopy(registers, 0, memory, index, Vx + 1);
                    }
                    case 0x65 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        for (int i = 0; i <= Vx; i++) {
                            registers[i] = memory[index + i] & 0xFF;
                        }
                    }
                }
            }
        }
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

        mainScene.setOnKeyPressed(keyEvent -> keyboard.setDown(keyEvent.getCode()));
        mainScene.setOnKeyReleased(keyEvent -> keyboard.setUp(keyEvent.getCode()));

        mainStage.setScene(mainScene);

        gameLoop = new Timeline();
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        KeyFrame keyFrame = new KeyFrame(
                Duration.seconds((double) 1 / OPCODE_SECONDS),
                actionEvent -> this.cycle()

        );
        gameLoop.getKeyFrames().add(keyFrame);

        mainStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        initializeStage();
        this.loadROM("games/Space Invaders [David Winter].ch8");
    }
}
