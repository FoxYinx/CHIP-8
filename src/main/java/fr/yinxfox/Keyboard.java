package fr.yinxfox;

import javafx.scene.input.KeyCode;

public class Keyboard {

    //Fixme: Upper row not usable on Linux

    private final boolean[] keyboard;
    private final Chip8 chip8;

    public Keyboard(Chip8 chip8) {
        this.keyboard = new boolean[16];
        this.chip8 = chip8;
    }

    public boolean isPressed(int key) {
        return this.keyboard[key];
    }

    public void setDown(KeyCode code) {
        int key = getKey(code.getName());
        if (key != -1) {
            if (!this.keyboard[key]) {
                synchronized (this.chip8.getExecutionWorker().getLock()) {
                    this.chip8.getExecutionWorker().getLock().notify();
                }
            }
            this.keyboard[key] = true;
        }
    }

    public void setUp(KeyCode code) {
        int key = getKey(code.getName());
        if (key != -1) {
            if (this.keyboard[key]) {
                synchronized (this.chip8.getExecutionWorker().getLock()) {
                    this.chip8.getExecutionWorker().getLock().notify();
                }
            }
            this.keyboard[key] = false;
        }
    }

    private int getKey(String name) {
        return switch (name) {
            case "1" -> 1;
            case "2", "Up", "Numpad 8" -> 2;
            case "3" -> 3;
            case "4" -> 12;
            case "A", "Left", "Numpad 4" -> 4;
            case "Z" -> 5;
            case "E", "Right", "Numpad 6" -> 6;
            case "R" -> 13;
            case "Q" -> 7;
            case "S", "Down", "Numpad 2" -> 8;
            case "D" -> 9;
            case "F" -> 14;
            case "W" -> 10;
            case "X" -> 0;
            case "C" -> 11;
            case "V" -> 15;

            default -> -1;
        };
    }
}
