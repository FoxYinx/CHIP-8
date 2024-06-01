package fr.yinxfox.emulator;

import javafx.scene.paint.Color;

public enum ColorPalette {

    GREY("Shades of Gray", Color.BLACK, Color.DARKGRAY, Color.DIMGRAY, Color.WHITE),
    MAGENTACYAN("Magenta / Cyan", Color.BLACK, Color.MAGENTA, Color.CYAN, Color.WHITE),
    BLACKANDWHITE("Black and White", Color.BLACK, Color.WHITE, Color.GRAY, Color.DIMGRAY);

    private final String name;
    private final Color background;
    private final Color firstPlane;
    private final Color secondPlane;
    private final Color overlap;

    ColorPalette(String name, Color background, Color firstPlane, Color secondPlane, Color overlap) {
        this.name = name;
        this.background = background;
        this.firstPlane = firstPlane;
        this.secondPlane = secondPlane;
        this.overlap = overlap;
    }

    public String getName() {
        return name;
    }

    public Color getBackground() {
        return background;
    }

    public Color getFirstPlane() {
        return firstPlane;
    }

    public Color getSecondPlane() {
        return secondPlane;
    }

    public Color getOverlap() {
        return overlap;
    }
}
