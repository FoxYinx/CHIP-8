package fr.yinxfox;

import javafx.scene.input.KeyCode;

public class Keyboard {

    private final boolean[] keyboard;

    public Keyboard() {
        keyboard = new boolean[16];
    }

    public boolean isPressed(int key) {
        return keyboard[key];
    }

    public void setDown(KeyCode code) {
        switch (code.getName()) {
            case "1" -> keyboard[1] = true;
            case "2", "Up", "Numpad 8" -> keyboard[2] = true;
            case "3" -> keyboard[3] = true;
            case "4" -> keyboard[12] = true;
            case "A", "Left", "Numpad 4" -> keyboard[4] = true;
            case "Z" -> keyboard[5] = true;
            case "E", "Right", "Numpad 6" -> keyboard[6] = true;
            case "R" -> keyboard[13] = true;
            case "Q" -> keyboard[7] = true;
            case "S", "Down", "Numpad 2" -> keyboard[8] = true;
            case "D" -> keyboard[9] = true;
            case "F" -> keyboard[14] = true;
            case "W" -> keyboard[10] = true;
            case "X" -> keyboard[0] = true;
            case "C" -> keyboard[11] = true;
            case "V" -> keyboard[15] = true;
        }
    }

    public void setUp(KeyCode code) {
        switch (code.getName()) {
            case "1" -> keyboard[1] = false;
            case "2", "Up", "Numpad 8" -> keyboard[2] = false;
            case "3" -> keyboard[3] = false;
            case "4" -> keyboard[12] = false;
            case "A", "Left", "Numpad 4" -> keyboard[4] = false;
            case "Z" -> keyboard[5] = false;
            case "E", "Right", "Numpad 6" -> keyboard[6] = false;
            case "R" -> keyboard[13] = false;
            case "Q" -> keyboard[7] = false;
            case "S", "Down", "Numpad 2" -> keyboard[8] = false;
            case "D" -> keyboard[9] = false;
            case "F" -> keyboard[14] = false;
            case "W" -> keyboard[10] = false;
            case "X" -> keyboard[0] = false;
            case "C" -> keyboard[11] = false;
            case "V" -> keyboard[15] = false;
        }
    }
}
