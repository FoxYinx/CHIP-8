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

    private static int[][] plane1 = new int[WIDTH][HEIGHT];
    private static int[][] plane2 = new int[WIDTH][HEIGHT];
    private boolean highResolutionMode = false;
    private int selectedPlane = 0;

    public Screen() {
        super(WIDTH * SCALE, HEIGHT * SCALE);
        setFocusTraversable(true);

        this.graphicsContext = this.getGraphicsContext2D();
        setSelectedPlane(3);
        clear();
    }

    public void clear() {
        if ((selectedPlane & 1) == 1) plane1 = new int[WIDTH][HEIGHT];
        if ((selectedPlane & 2) == 2) plane2 = new int[WIDTH][HEIGHT];
    }

    public void render() {
        if (Launcher.getHardware() != Hardware.XOCHIP) {
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    if (plane1[x][y] == 1) {
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
                    if (plane1[x][y] == 0 && plane2[x][y] == 0) {
                        graphicsContext.setFill(Color.BLACK);
                    } else if (plane1[x][y] == 1 && plane2[x][y] == 0) {
                        graphicsContext.setFill(Color.BLUE);
                    } else if (plane1[x][y] == 0 && plane2[x][y] == 1) {
                        graphicsContext.setFill(Color.GREEN);
                    } else {
                        graphicsContext.setFill(Color.WHITE);
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
        if (xPos + col >= plane1.length || yPos + row >= plane1[0].length) return false;
        int screenPixel = plane1[xPos + col][yPos + row];
        plane1[xPos + col][yPos + row] ^= 1;
        return screenPixel == 1;
    }

    public boolean drawSchip8(int xPos, int col, int yPos, int row) {
        if (highResolutionMode) {
            if (xPos + col >= plane1.length) return false;
            if (yPos + row >= plane1[0].length) return true;
            int screenPixel = 0;
            if ((selectedPlane & 1) == 1) {
                screenPixel |= plane1[xPos + col][yPos + row];
                plane1[xPos + col][yPos + row] ^= 1;
            }
            if ((selectedPlane & 2) == 2) {
                screenPixel |= plane2[xPos + col][yPos + row];
                plane2[xPos + col][yPos + row] ^= 1;
            }
            return screenPixel == 1;
        } else {
            int x = (xPos + col) * 2;
            int y = (yPos + row) * 2;
            if (x >= plane1.length || y >= plane1[0].length) return false;
            int collision = 0;
            if ((selectedPlane & 1) == 1) {
                collision = getCollision(x, y, collision, plane1);
            }
            if ((selectedPlane & 2) == 2) {
                collision = getCollision(x, y, collision, plane2);
            }

            return collision == 1;
        }
    }

    private int getCollision(int x, int y, int collision, int[][] plane) {
        collision |= plane[x][y] | plane[x][y + 1] | plane[x + 1][y] | plane[x + 1][y + 1];
        plane[x][y] ^= 1;
        plane[x][y + 1] ^= 1;
        plane[x + 1][y] ^= 1;
        plane[x + 1][y + 1] ^= 1;
        return collision;
    }

    public void scrollUp(int offset) {
        int gap = (highResolutionMode) ? offset : offset * 2;
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT - gap; j++) {
                if ((selectedPlane & 1) == 1) plane1[i][j] = plane1[i][j + gap];
                if ((selectedPlane & 2) == 2) plane2[i][j] = plane2[i][j + gap];
            }
        }
        for (int i = 0; i < WIDTH; i++) {
            for (int j = HEIGHT - gap; j < HEIGHT; j++) {
                if ((selectedPlane & 1) == 1) plane1[i][j] = 0;
                if ((selectedPlane & 2) == 2) plane2[i][j] = 0;
            }
        }
    }

    public void scrollDown(int offset) {
        int gap = (highResolutionMode) ? offset : offset * 2;
        for (int i = 0; i < WIDTH; i++) {
            for (int j = HEIGHT - 1; j >= gap; j--) {
                if ((selectedPlane & 1) == 1) plane1[i][j] = plane1[i][j - gap];
                if ((selectedPlane & 2) == 2) plane2[i][j] = plane2[i][j - gap];
            }
        }
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < gap; j++) {
                if ((selectedPlane & 1) == 1) plane1[i][j] = 0;
                if ((selectedPlane & 2) == 2) plane2[i][j] = 0;
            }
        }
    }

    public void scrollLeft() {
        int gap = (highResolutionMode) ? 4 : 8;
        for (int i = gap; i < WIDTH; i++) {
            if ((selectedPlane & 1) == 1) plane1[i - gap] = plane1[i];
            if ((selectedPlane & 2) == 2) plane2[i - gap] = plane2[i];
        }
        for (int i = WIDTH - gap; i < WIDTH; i++) {
            if ((selectedPlane & 1) == 1) plane1[i] = new int[HEIGHT];
            if ((selectedPlane & 2) == 2) plane2[i] = new int[HEIGHT];
        }
    }

    public void scrollRight() {
        int gap = (highResolutionMode) ? 4 : 8;
        for (int i = WIDTH - gap - 1; i >= 0; i--) {
            if ((selectedPlane & 1) == 1) System.arraycopy(plane1[i], 0, plane1[i + gap], 0, HEIGHT);
            if ((selectedPlane & 2) == 2) System.arraycopy(plane2[i], 0, plane2[i + gap], 0, HEIGHT);
        }
        for (int i = 0; i < gap; i++) {
            if ((selectedPlane & 1) == 1) plane1[i] = new int[HEIGHT];
            if ((selectedPlane & 2) == 2) plane2[i] = new int[HEIGHT];
        }
    }

    public static void updateScreenFormat() {
        WIDTH = Launcher.getHardware().getWidth();
        HEIGHT = Launcher.getHardware().getHeight();
        SCALE = Launcher.getHardware().getScale();
        plane1 = new int[WIDTH][HEIGHT];
        plane2 = new int[WIDTH][HEIGHT];
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
        return plane1;
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
