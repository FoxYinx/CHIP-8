package fr.yinxfox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class ExecutionWorker extends Thread {

    private static final String PATH = "C:\\Users\\tolle\\IdeaProjects\\CHIP-8\\src\\main\\resources\\roms\\";
    private static final int START_ADDRESS = 0x200;
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
    private int pc;
    private int sp;
    private int index;
    private final int[] stack;
    private int opcode;
    private int delayTimer;
    private int soundTimer;
    private final Screen video;
    private final Keyboard keyboard;
    private final SoundMaker soundMaker;

    private final Object lock = new Object();

    public ExecutionWorker(String filename, Screen video, Keyboard keyboard, SoundMaker soundMaker) {
        this.registers = new int[16];
        this.memory = new int[4096];
        this.index = 0;
        this.pc = START_ADDRESS;
        this.stack = new int[16];
        this.sp = 0;
        this.delayTimer = 0;
        this.soundTimer = 0;
        this.opcode = 0x0000;
        System.arraycopy(FONTSET, 0, this.memory, FONTSET_START_ADDRESS, FONTSET_SIZE);

        byte[] data;
        try {
            data = Files.readAllBytes(Path.of(PATH + filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < data.length; i++) {
            this.memory[START_ADDRESS + i] = data[i] & 0xFF;
        }

        this.video = video;
        this.keyboard = keyboard;
        this.soundMaker = soundMaker;

        this.start();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                this.cycle();
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void updateTimers() {
        if (this.delayTimer > 0) --this.delayTimer;
        if (this.soundTimer > 0) {
            --this.soundTimer;
            //TODO: Ajouter le support du son
            System.out.println("Make sound!");
        }
    }

    public void cycle() throws InterruptedException {
        opcode = (memory[pc] << 8) | memory[pc + 1];
        pc += 2;

        this.execute();
    }

    private void execute() throws InterruptedException {
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
                        synchronized (lock) {
                            lock.wait();
                        }
                        for (int i = 0; i < 16; i++) {
                            if (keyboard.isPressed(i)) {
                                registers[Vx] = i;
                            }
                        }
                        synchronized (lock) {
                            lock.wait();
                        }
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

    public Object getLock() {
        return lock;
    }
}
