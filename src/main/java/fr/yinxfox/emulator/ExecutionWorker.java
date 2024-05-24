package fr.yinxfox.emulator;

import fr.yinxfox.Launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class ExecutionWorker extends Thread {

    private static int OPPS = 500;
    public static boolean UNLOCKED = false;
    public static boolean PAUSED = false;

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
    private static final int FONTSET_SIZE = FONTSET.length;

    private static final int FONTSET_HIGHRES_START_ADDRESS = 0x50 + FONTSET_SIZE;
    private static final int[] FONTSET_HIGHRES = new int[]{
            0xC67C, 0xDECE, 0xF6D6, 0xC6E6, 0x007C, // 0
            0x3010, 0x30F0, 0x3030, 0x3030, 0x00FC, // 1
            0xCC78, 0x0CCC, 0x3018, 0xCC60, 0x00FC, // 2
            0xCC78, 0x0C0C, 0x0C38, 0xCC0C, 0x0078, // 3
            0x1C0C, 0x6C3C, 0xFECC, 0x0C0C, 0x001E, // 4
            0xC0FC, 0xC0C0, 0x0CF8, 0xCC0C, 0x0078, // 5
            0x6038, 0xC0C0, 0xCCF8, 0xCCCC, 0x0078, // 6
            0xC6FE, 0x06C6, 0x180C, 0x3030, 0x0030, // 7
            0xCC78, 0xECCC, 0xDC78, 0xCCCC, 0x0078, // 8
            0xC67C, 0xC6C6, 0x0C7E, 0x3018, 0x0070  // 9
    };
    private static final int FONTSET_HIGHRES_SIZE = FONTSET_HIGHRES.length;

    private final int[] registers;
    private final int[] memory;
    private int pc;
    private int sp;
    private int index;
    private final int[] stack;
    private final int[] rpl;
    private int opcode;
    private int delayTimer;
    private int soundTimer;
    private final Screen video;
    private final Keyboard keyboard;
    private final SoundMaker soundMaker;

    private final Object lock = new Object();
    private final Object sleep = new Object();
    private final Object pause = new Object();

    public ExecutionWorker(String filename) {
        this.registers = new int[16];
        this.memory = new int[4096];
        this.index = 0;
        this.pc = 0x0200;
        this.stack = (Launcher.getHardware() == Hardware.CHIP8 || Launcher.getHardware() == Hardware.CHIP8HIRES) ? new int[12] :  new int[16];
        this.rpl = new int[8];
        this.sp = 0;
        this.delayTimer = 0;
        this.soundTimer = 0;
        this.opcode = 0x0000;
        System.arraycopy(FONTSET, 0, this.memory, FONTSET_START_ADDRESS, FONTSET_SIZE);
        //fixme: That is probably wrong
        System.arraycopy(FONTSET_HIGHRES, 0, this.memory, FONTSET_HIGHRES_START_ADDRESS, FONTSET_HIGHRES_SIZE);

        byte[] data;
        try {
            data = Files.readAllBytes(Path.of(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < data.length; i++) {
            this.memory[this.pc + i] = data[i] & 0xFF;
        }

        this.video = new Screen();
        this.keyboard = null;
        this.soundMaker = null;

        UNLOCKED = true;

        this.start();
    }

    public ExecutionWorker(String filename, Screen video, Keyboard keyboard, SoundMaker soundMaker) {
        this.registers = new int[16];
        this.memory = new int[4096];
        this.index = 0;
        this.pc = 0x0200;
        this.stack = (Launcher.getHardware() == Hardware.CHIP8 || Launcher.getHardware() == Hardware.CHIP8HIRES) ? new int[12] :  new int[16];
        this.rpl = new int[8];
        this.sp = 0;
        this.delayTimer = 0;
        this.soundTimer = 0;
        this.opcode = 0x0000;
        System.arraycopy(FONTSET, 0, this.memory, FONTSET_START_ADDRESS, FONTSET_SIZE);
        System.arraycopy(FONTSET_HIGHRES, 0, this.memory, FONTSET_HIGHRES_START_ADDRESS, FONTSET_HIGHRES_SIZE);

        byte[] data;
        try {
            data = Files.readAllBytes(Path.of(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < data.length; i++) {
            this.memory[this.pc + i] = data[i] & 0xFF;
        }

        this.video = video;
        this.video.disableHighResolutionMode();
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
            this.soundMaker.playBuzzer();
        } else this.soundMaker.stopBuzzer();
    }

    public void cycle() throws InterruptedException {
        if (!UNLOCKED) {
            if (PAUSED) {
                synchronized (pause) {
                    pause.wait();
                }
            } else {
                synchronized (sleep) {
                    sleep.wait((long) 1000 / OPPS);
                }
            }
        }

        opcode = (memory[pc] << 8) | memory[pc + 1];
        pc += 2;

        this.execute();
    }

    private void execute() throws InterruptedException {
        //System.out.println("Read opcode: " + String.format("0x%04X", opcode) + " at PC: " + String.format("0x%04X", pc - 2));
        switch (opcode >> 12) {
            case 0x0 -> {
                if (opcode == 0x00E0 || opcode == 0x0230) video.clear();
                else if (opcode == 0x00EE) {
                    sp--;
                    pc = stack[sp];
                } else if (opcode == 0x00FD) {
                    System.out.println("Exit interpreter");
                    this.interrupt();
                } else if (opcode == 0x00FE) {
                    video.disableHighResolutionMode();
                } else if (opcode == 0x00FF) {
                    video.enableHighResolutionMode();
                } else if ((opcode & 0xFFF0) == 0x00C0) {
                    int N = opcode & 0x000F;
                    video.scrollDown(N);
                } else if (opcode == 0x00FB) {
                    video.scrollRight();
                } else if (opcode == 0x00FC) {
                    video.scrollLeft();
                } else {
                    handleUnknownOpcode();
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
                if ((opcode & 0x000F) == 0) {
                    int Vx = (opcode & 0x0F00) >> 8;
                    int Vy = (opcode & 0x00F0) >> 4;
                    if (registers[Vx] == registers[Vy]) pc += 2;
                } else handleUnknownOpcode();
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
                        if (Launcher.getHardware() == Hardware.CHIP8 || Launcher.getHardware() == Hardware.CHIP8HIRES) registers[0xF] = 0;
                    }
                    case 0x2 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int Vy = (opcode & 0x00F0) >> 4;
                        registers[Vx] &= registers[Vy];
                        if (Launcher.getHardware() == Hardware.CHIP8 || Launcher.getHardware() == Hardware.CHIP8HIRES) registers[0xF] = 0;
                    }
                    case 0x3 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int Vy = (opcode & 0x00F0) >> 4;
                        registers[Vx] ^= registers[Vy];
                        if (Launcher.getHardware() == Hardware.CHIP8 || Launcher.getHardware() == Hardware.CHIP8HIRES) registers[0xF] = 0;
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
                        int Vy = (opcode & 0x00F0) >> 4;
                        if (Launcher.getHardware() == Hardware.CHIP8 || Launcher.getHardware() == Hardware.CHIP8HIRES) registers[Vx] = registers[Vy];
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
                        int Vy = (opcode & 0x00F0) >> 4;
                        if (Launcher.getHardware() == Hardware.CHIP8 || Launcher.getHardware() == Hardware.CHIP8HIRES) registers[Vx] = registers[Vy];
                        int VfValue = (registers[Vx] & 0x80) >> 7;
                        registers[Vx] <<= 1;
                        registers[Vx] &= 0xFF;
                        registers[0xF] = VfValue;
                    }
                    default -> handleUnknownOpcode();
                }
            }
            case 0x9 -> {
                if ((opcode & 0x000F) == 0) {
                    int Vx = (opcode & 0x0F00) >> 8;
                    int Vy = (opcode & 0x00F0) >> 4;
                    if (registers[Vx] != registers[Vy]) pc += 2;
                } else handleUnknownOpcode();
            }
            case 0xA -> index = opcode & 0x0FFF;
            case 0xB -> {
                if (Launcher.getHardware() == Hardware.CHIP8 || Launcher.getHardware() == Hardware.CHIP8HIRES) {
                    int address = opcode & 0x0FFF;
                    pc = registers[0] + address;
                } else if (Launcher.getHardware() == Hardware.SCHIP8) {
                    int Vx = (opcode & 0x0F00) >> 8;
                    int address = opcode & 0x0FFF;
                    pc = registers[Vx] + address;
                }
            }
            case 0xC -> {
                int Vx = (opcode & 0x0F00) >> 8;
                int octet = opcode & 0x00FF;
                registers[Vx] = (new Random()).nextInt(256) & 0x00FF & octet;
            }
            case 0xD -> {
                if ((opcode & 0x000F) != 0) {
                    int Vx = (opcode & 0x0F00) >> 8;
                    int Vy = (opcode & 0x00F0) >> 4;
                    int height = opcode & 0x000F;

                    int xPos = registers[Vx] % Screen.getWIDTH();
                    int yPos = registers[Vy] % Screen.getHEIGHT();

                    registers[0xF] = 0;

                    for (int row = 0; row < height; row++) {
                        int spriteOctet = memory[index + row];
                        boolean collisionLine = false;
                        for (int col = 0; col < 8; col++) {
                            int spritePixel = spriteOctet & (0x80 >> col);
                            if (spritePixel != 0) {
                                if (xPos + col >= Screen.getWIDTH() || yPos + row >= Screen.getHEIGHT()) break;
                                if (Launcher.getHardware() == Hardware.CHIP8 || Launcher.getHardware() == Hardware.CHIP8HIRES) {
                                    boolean collision = video.drawChip8(xPos, col, yPos, row);
                                    if (collision) registers[0xF] = 1;
                                } else if (Launcher.getHardware() == Hardware.SCHIP8) {
                                    collisionLine |= video.drawSchip8(xPos, col, yPos, row);
                                }
                            }
                        }
                        if (collisionLine && Launcher.getHardware() == Hardware.SCHIP8) registers[0xF]++;
                    }
                    if (Launcher.getHardware() == Hardware.SCHIP8 && registers[0xF] > 0 && !video.isHighResolutionMode()) registers[0xF] = 1;
                } else {
                    if (video.isHighResolutionMode()){
                        int Vx = (opcode & 0x0F00) >> 8;
                        int Vy = (opcode & 0x00F0) >> 4;

                        int xPos = registers[Vx] % Screen.getWIDTH();
                        int yPos = registers[Vy] % Screen.getHEIGHT();

                        registers[0xF] = 0;

                        for (int row = 0; row < 16; row++) {
                            int spriteOctet = (memory[index + row * 2] << 8) + memory[index + row * 2 + 1];
                            boolean collisionLine = false;
                            for (int col = 0; col < 16; col++) {
                                int spritePixel = spriteOctet & (0x8000 >> col);
                                if (spritePixel != 0) {
                                    if (xPos + col >= Screen.getWIDTH() || yPos + row >= Screen.getHEIGHT()) break;
                                    collisionLine |= video.drawSchip8(xPos, col, yPos, row);
                                }
                            }
                            if (collisionLine) registers[0xF]++;
                        }
                    }
                }
            }
            case 0xE -> {
                int Vx = (opcode & 0x0F00) >> 8;
                int key = registers[Vx];
                if ((opcode & 0x00FF) == 0x9E) {
                    if (keyboard.isPressed(key)) pc += 2;
                } else if ((opcode & 0x00FF) == 0xA1) {
                    if (!keyboard.isPressed(key)) pc += 2;
                } else handleUnknownOpcode();
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
                    case 0x30 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        int digit = registers[Vx];
                        index = FONTSET_HIGHRES_START_ADDRESS + (10 * digit);
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
                        if (Launcher.getHardware() == Hardware.CHIP8 || Launcher.getHardware() == Hardware.CHIP8HIRES) {
                            index += Vx + 1;
                        }
                    }
                    case 0x65 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        for (int i = 0; i <= Vx; i++) {
                            registers[i] = memory[index + i] & 0xFF;
                        }
                        if (Launcher.getHardware() == Hardware.CHIP8 || Launcher.getHardware() == Hardware.CHIP8HIRES) {
                            index += Vx + 1;
                        }
                    }
                    case 0x75 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        System.arraycopy(registers, 0, rpl, 0, Vx + 1);
                    }
                    case 0x85 -> {
                        int Vx = (opcode & 0x0F00) >> 8;
                        System.arraycopy(rpl, 0, registers, 0, Vx + 1);
                    }
                    default -> handleUnknownOpcode();
                }
            }
        }
    }

    private void handleUnknownOpcode() {
        System.out.println("At PC: " + String.format("0x%04X", pc - 2));
        System.out.println("Found unknown opcode: " + String.format("0x%04X", opcode));
    }

    public Object getLock() {
        return lock;
    }

    public int getPc() {
        return pc;
    }

    public int getSp() {
        return sp;
    }

    public int[] getStack() {
        return stack;
    }

    public int getIndex() {
        return index;
    }

    public int[] getRegisters() {
        return registers;
    }

    public int getDelayTimer() {
        return delayTimer;
    }

    public int getSoundTimer() {
        return soundTimer;
    }

    public int[] getMemory() {
        return memory;
    }

    public static void setOPPS(int OPPS) {
        ExecutionWorker.OPPS = OPPS;
    }

    public Object getPause() {
        return pause;
    }
}
