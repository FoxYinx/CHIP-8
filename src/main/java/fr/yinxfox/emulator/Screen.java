package fr.yinxfox.emulator;

import fr.yinxfox.Launcher;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Screen extends Canvas {

    private static int WIDTH = 64;
    private static int HEIGHT = 32;
    private static int SCALE = 12;

    private final GraphicsContext graphicsContext;

    private static int[][] plane = new int[WIDTH][HEIGHT];
    private boolean highResolutionMode = false;
    private int selectedPlane = 0;

    public Screen() {
        super(WIDTH * SCALE, HEIGHT * SCALE);
        setFocusTraversable(true);

        this.graphicsContext = this.getGraphicsContext2D();
        setSelectedPlane(3);
        clear();
        setSelectedPlane(1);
    }

    public void clear() {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                plane[i][j] &= ~selectedPlane;
            }
        }
    }

    public void render() {
        if (Launcher.getHardware() != Hardware.XOCHIP) {
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    if (plane[x][y] == 1) {
                        graphicsContext.setFill(Color.WHITE);
                    } else {
                        graphicsContext.setFill(Color.BLACK);
                    }
                    graphicsContext.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
                }
            }
        } else {
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    int bit = plane[x][y];
                    switch (bit) {
                        case 0 -> graphicsContext.setFill(Color.BLACK);
                        case 1 -> graphicsContext.setFill(Color.WHITE);
                        case 2 -> graphicsContext.setFill(Color.BLUE);
                        case 3 -> graphicsContext.setFill(Color.PURPLE);
                    }
                    graphicsContext.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
                }
            }
        }
    }

    public static int getWIDTH() {
        return WIDTH;
    }

    public static int getHEIGHT() {
        return HEIGHT;
    }

    public static int getSCALE() {
        return SCALE;
    }

    public boolean drawChip8(int xPos, int col, int yPos, int row) {
        if (xPos + col >= plane.length || yPos + row >= plane[0].length) return false;
        int screenPixel = plane[xPos + col][yPos + row];
        plane[xPos + col][yPos + row] ^= 1;
        return screenPixel == 1;
    }

    public boolean drawSchip8(int xPos, int col, int yPos, int row, int selectedPlane) {
        int x = xPos + col;
        int y = yPos + row;
        if (highResolutionMode) {
            if (Launcher.getHardware() == Hardware.XOCHIP) {
                x = x % WIDTH;
                y = y % HEIGHT;
            } else {
                if (xPos + col >= plane.length) return false;
                if (yPos + row >= plane[0].length) return true;
            }
            int screenPixel = 0;
            if (selectedPlane == 1) {
                screenPixel = plane[x][y] & 0b01;
                plane[x][y] ^= 0b01;
            } else if (selectedPlane == 2) {
                screenPixel = plane[x][y] & 0b10;
                plane[x][y] ^= 0b10;
            }
            return screenPixel > 0;
        } else {
            x *= 2;
            y *= 2;
            if (Launcher.getHardware() == Hardware.XOCHIP) {
                x = x % WIDTH;
                y = y % HEIGHT;
            } else if (x >= plane.length || y >= plane[0].length) return false;
            int collision = 0;
            if (selectedPlane == 1) {
                collision = (plane[x][y] | plane[x][y + 1] | plane[x + 1][y] | plane[x + 1][y + 1]) & 0b01;
                plane[x][y] ^= 0b01;
                plane[x][y + 1] ^= 0b01;
                plane[x + 1][y] ^= 0b01;
                plane[x + 1][y + 1] ^= 0b01;
            } else if (selectedPlane == 2) {
                collision = (plane[x][y] | plane[x][y + 1] | plane[x + 1][y] | plane[x + 1][y + 1]) & 0b10;
                plane[x][y] ^= 0b10;
                plane[x][y + 1] ^= 0b10;
                plane[x + 1][y] ^= 0b10;
                plane[x + 1][y + 1] ^= 0b10;
            }
            return collision > 0;
        }
    }

    public void scrollUp(int offset) {
        int gap = (highResolutionMode) ? offset : offset * 2;
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT - gap; j++) {
                plane[i][j] = (plane[i][j] & ~selectedPlane) | (plane[i][j + gap] & selectedPlane);
            }
        }
        for (int i = 0; i < WIDTH; i++) {
            for (int j = HEIGHT - gap; j < HEIGHT; j++) {
                plane[i][j] &= ~selectedPlane;
            }
        }
    }

    public void scrollDown(int offset) {
        int gap = (highResolutionMode) ? offset : offset * 2;
        for (int i = 0; i < WIDTH; i++) {
            for (int j = HEIGHT - 1; j >= gap; j--) {
                plane[i][j] = (plane[i][j] & ~selectedPlane) | (plane[i][j - gap] & selectedPlane);
            }
        }
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < gap; j++) {
                plane[i][j] &= ~selectedPlane;
            }
        }
    }

    public void scrollLeft() {
        int gap = (highResolutionMode) ? 4 : 8;
        for (int i = gap; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                plane[i - gap][j] = (plane[i - gap][j] & ~selectedPlane) | (plane[i][j] & selectedPlane);
            }
        }
        for (int i = WIDTH - gap; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                plane[i][j] &= ~selectedPlane;
            }
        }
    }

    public void scrollRight() {
        int gap = (highResolutionMode) ? 4 : 8;
        for (int i = WIDTH - gap - 1; i >= 0; i--) {
            for (int j = 0; j < HEIGHT; j++) {
                plane[i + gap][j] = (plane[i + gap][j] & ~selectedPlane) | (plane[i][j] & selectedPlane);
            }
        }
        for (int i = 0; i < gap; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                plane[i][j] &= ~selectedPlane;
            }
        }
    }

    public static void updateScreenFormat() {
        WIDTH = Launcher.getHardware().getWidth();
        HEIGHT = Launcher.getHardware().getHeight();
        SCALE = Launcher.getHardware().getScale();
        plane = new int[WIDTH][HEIGHT];
    }

    public void disableHighResolutionMode() {
        int mem = selectedPlane;
        selectedPlane = 3;
        if (Launcher.getHardware() == Hardware.XOCHIP) clear();
        selectedPlane = mem;
        highResolutionMode = false;
    }

    public void enableHighResolutionMode() {
        int mem = selectedPlane;
        selectedPlane = 3;
        if (Launcher.getHardware() == Hardware.XOCHIP) clear();
        selectedPlane = mem;
        highResolutionMode = true;
    }

    public static int[][] getVideo() {
        return plane;
    }

    public boolean isHighResolutionMode() {
        return highResolutionMode;
    }

    public void setSelectedPlane(int selectedPlane) {
        this.selectedPlane = selectedPlane;
    }

    public int getSelectedPlane() {
        return selectedPlane;
    }
}
