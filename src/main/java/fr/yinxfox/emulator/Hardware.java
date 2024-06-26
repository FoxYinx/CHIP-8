package fr.yinxfox.emulator;

public enum Hardware {
    CHIP8("CHIP-8", 64, 32, 12, 500),
    CHIP8HIRES("CHIP-8 (HIRES)", 64, 64, 10, 500),
    SCHIP8("SCHIP-8", 128, 64, 6, 1000),
    XOCHIP("XO-CHIP", 128, 64, 6, 1000),
    NULL("NULL", 64, 32, 12, 500);

    private final String name;
    private final int width;
    private final int height;
    private final int scale;
    private final int speed;

    Hardware(final String name, final int width, final int height, final int scale, final int speed) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.speed = speed;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getScale() {
        return scale;
    }

    public int getSpeed() {
        return speed;
    }
}
