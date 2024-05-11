package fr.yinxfox.emulator;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Screen extends Canvas {

    private static final int WIDTH = 64;
    private static int HEIGHT = 32;
    private static int scale = 12;

    private final GraphicsContext graphicsContext;

    private final int[][] video = new int[WIDTH][HEIGHT];

    public Screen() {
        super(WIDTH * scale, HEIGHT * scale);
        setFocusTraversable(true);

        this.graphicsContext = this.getGraphicsContext2D();
        clear();
    }

    public void clear() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                this.video[x][y] = 0;
            }
        }
    }

    public void render() {
        for(int x = 0; x < WIDTH; x++) {
            for(int y = 0; y < HEIGHT; y++) {
                if (this.video[x][y] == 1) {
                    graphicsContext.setFill(Color.WHITE);
                } else {
                    graphicsContext.setFill(Color.BLACK);
                }
                graphicsContext.fillRect(x * scale, y * scale, scale, scale);
            }
        }
    }

    public static int getWIDTH() {
        return WIDTH;
    }

    public static int getHEIGHT() {
        return HEIGHT;
    }

    public static int getScale() {
        return scale;
    }

    public boolean draw(int xPos, int col, int yPos, int row) {
        int screenPixel = video[xPos + col][yPos + row];
        this.video[xPos + col][yPos + row] ^= 1;
        return screenPixel == 1;
    }

    public static void setHEIGHT(int height) {
        Screen.HEIGHT = height;
    }

    public static void setScale(int scale) {
        Screen.scale = scale;
    }
}
