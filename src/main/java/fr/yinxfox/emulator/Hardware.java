package fr.yinxfox.emulator;

public enum Hardware {
    CHIP8("CHIP-8"),
    CHIP8HIRES("CHIP-8 (HIRES)"),
    SCHIP8("SCHIP-8"),
    XOCHIP("XO-CHIP"),
    NULL("NULL");

    private final String name;

    Hardware(final String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
