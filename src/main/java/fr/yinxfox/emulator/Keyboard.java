package fr.yinxfox.emulator;

import fr.yinxfox.Launcher;
import javafx.scene.input.KeyCode;

public class Keyboard {

    private final boolean[] keyboard;
    private final Launcher launcher;

    public Keyboard(Launcher launcher) {
        this.keyboard = new boolean[16];
        this.launcher = launcher;
    }

    public boolean isPressed(int key) {
        return this.keyboard[key];
    }

    public void setDown(KeyCode code) {
        int key = getKey(code.getCode());
        if (key != -1) {
            if (!this.keyboard[key]) {
                synchronized (this.launcher.getExecutionWorker().getLock()) {
                    this.launcher.getExecutionWorker().getLock().notify();
                }
            }
            this.keyboard[key] = true;
        }
    }

    public void setUp(KeyCode code) {
        int key = getKey(code.getCode());
        if (key != -1) {
            if (this.keyboard[key]) {
                synchronized (this.launcher.getExecutionWorker().getLock()) {
                    this.launcher.getExecutionWorker().getLock().notify();
                }
            }
            this.keyboard[key] = false;
        }
    }

    private int getKey(int code) {
        return switch (code) {
            case 150 -> 1;
            case 0, 38, 104 -> 2;
            case 152 -> 3;
            case 222 -> 12;
            case 65, 37, 100 -> 4;
            case 90 -> 5;
            case 69, 39, 102 -> 6;
            case 82 -> 13;
            case 81 -> 7;
            case 83, 40, 98 -> 8;
            case 68 -> 9;
            case 70 -> 14;
            case 87 -> 10;
            case 88 -> 0;
            case 67 -> 11;
            case 86 -> 15;

            default -> -1;
        };
    }
}
