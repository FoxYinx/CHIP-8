package fr.yinxfox.emulator;

public enum Hardware {
    CHIP8("CHIP-8", 0x200, 64, 32, 12),
    CHIP8HIRES("CHIP-8 (HIRES)", 0x2C0, 64, 64, 10),
    SCHIP8("SCHIP-8", 0x200, 128, 64, 6),
    XOCHIP("XO-CHIP", 0x200, 128, 64, 6),
    NULL("NULL", 0x200, 64, 32, 12);

    private final String name;
    private final int startAddress;
    private final int width;
    private final int height;
    private final int scale;

    Hardware(final String name, final int startAddress, final int width, final int height, final int scale) {
        this.name = name;
        this.startAddress = startAddress;
        this.width = width;
        this.height = height;
        this.scale = scale;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public int getStartAddress() {
        return this.startAddress;
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
}
