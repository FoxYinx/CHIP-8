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

    private static int[][] video = new int[WIDTH][HEIGHT];
    private boolean highResolutionMode = false;

    public Screen() {
        super(WIDTH * SCALE, HEIGHT * SCALE);
        setFocusTraversable(true);

        this.graphicsContext = this.getGraphicsContext2D();
        clear();
    }

    public void clear() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                video[x][y] = 0;
            }
        }
    }

    public void render() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (video[x][y] == 1) {
                    graphicsContext.setFill(Color.WHITE);
                } else {
                    graphicsContext.setFill(Color.BLACK);
                }
                graphicsContext.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
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
        int screenPixel = video[xPos + col][yPos + row];
        video[xPos + col][yPos + row] ^= 1;
        return screenPixel == 1;
    }

    public boolean drawSchip8(int xPos, int col, int yPos, int row) {
        if (highResolutionMode) {
            if (yPos + row >= video[0].length) return true;
            int screenPixel = video[xPos + col][yPos + row];
            video[xPos + col][yPos + row] ^= 1;
            return screenPixel == 1;
        } else {
            int x = (xPos + col) * 2;
            int y = (yPos + row) * 2;
            if (x >= video.length || y >= video[0].length) return false;
            int collision = video[x][y] | video[x][y + 1] | video[x + 1][y] | video[x + 1][y + 1];
            video[x][y] ^= 1;
            video[x][y + 1] ^= 1;
            video[x + 1][y] ^= 1;
            video[x + 1][y + 1] ^= 1;
            return collision == 1;
        }
    }

    public void scrollDown(int offset) {
        int gap = (highResolutionMode) ? offset : offset * 2;
        for (int i = 0; i < WIDTH; i++) {
            for (int j = HEIGHT - 1; j >= gap; j--) {
                video[i][j] = video[i][j - gap];
            }
        }
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < gap; j++) {
                video[i][j] = 0;
            }
        }
    }

    public void scrollLeft() {
        int gap = (highResolutionMode) ? 4 : 8;
        for (int i = gap; i < WIDTH; i++) {
            video[i - gap] = video[i];
        }
        for (int i = WIDTH - gap - 1; i < WIDTH; i++) {
            video[i] = new int[HEIGHT];
        }
    }

    public void scrollRight() {
        int gap = (highResolutionMode) ? 4 : 8;
        for (int i = WIDTH - gap - 1; i >= 0; i--) {
            System.arraycopy(video[i], 0, video[i + gap], 0, HEIGHT);
        }
        for (int i = 0; i < gap; i++) {
            video[i] = new int[HEIGHT];
        }
    }

    public static void updateScreenFormat() {
        WIDTH = Launcher.getHardware().getWidth();
        HEIGHT = Launcher.getHardware().getHeight();
        SCALE = Launcher.getHardware().getScale();
        video = new int[WIDTH][HEIGHT];
    }

    public void disableHighResolutionMode() {
        highResolutionMode = false;
    }

    public void enableHighResolutionMode() {
        highResolutionMode = true;
    }

    public static int[][] getVideo() {
        return video;
    }

    public boolean isHighResolutionMode() {
        return highResolutionMode;
    }
}
