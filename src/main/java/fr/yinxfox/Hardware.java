package fr.yinxfox;

public enum Hardware {
    CHIP8("CHIP-8"),
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
