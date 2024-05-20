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
            int screenPixel = video[xPos + col][yPos + row];
            video[xPos + col][yPos + row] ^= 1;
            return screenPixel == 1;
        } else {
            //fixme: (SCHIP & XO-CHIP) vF should be equals to the number of row that collides
            int x = (xPos + col) * 2;
            int y = (yPos + row) * 2;
            video[x][y] ^= 1;
            video[x][y + 1] ^= 1;
            video[x + 1][y] ^= 1;
            video[x + 1][y + 1] ^= 1;
            return false;
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
}
