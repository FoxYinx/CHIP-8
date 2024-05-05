package fr.yinxfox;

import javafx.scene.input.KeyCode;

public class Keyboard {

    private final boolean[] keyboard;
    private Chip8 chip8;

    public Keyboard(Chip8 chip8) {
        keyboard = new boolean[16];
        this.chip8 = chip8;
    }

    public boolean isPressed(int key) {
        return keyboard[key];
    }

    public void setDown(KeyCode code) {
        if (!keyboard[getKey(code.getName())]) {
            synchronized (chip8.getExecutionWorker().getLock()) {
                chip8.getExecutionWorker().getLock().notify();
            }
        }
        keyboard[getKey(code.getName())] = true;
    }

    public void setUp(KeyCode code) {
        if (keyboard[getKey(code.getName())]) {
            synchronized (chip8.getExecutionWorker().getLock()) {
                chip8.getExecutionWorker().getLock().notify();
            }
        }
        keyboard[getKey(code.getName())] = false;
    }

    private int getKey(String name) {
        return switch (name) {
            case "1" -> 1;
            case "2", "Up", "Numpad 8" -> 2;
            case "3" ->3;
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
