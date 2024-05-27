package fr.yinxfox.debugger;

import fr.yinxfox.Launcher;
import fr.yinxfox.emulator.Hardware;

public class OpcodeTranslator {

    public static String decodeOp(int opcode) {
        switch (opcode >> 12) {
            case 0x0 -> {
                switch (opcode) {
                    case 0x00E0, 0x0230 -> {
                        return "CLEAR";
                    }
                    case 0x00EE -> {
                        return "RETURN";
                    }
                    case 0x00FD -> {
                        return "EXIT";
                    }
                    case 0x00FE -> {
                        return "LORES";
                    }
                    case 0x00FF -> {
                        return "HIRES";
                    }
                    case 0x00FB -> {
                        return "SCROLL-RIGHT";
                    }
                    case 0x00FC -> {
                        return "SCROLL-LEFT";
                    }
                    default -> {
                        if ((opcode & 0xFFF0) == 0x00C0) return "SCROLL-DOWN " + (opcode & 0x000F);
                        if ((opcode & 0xFFF0) == 0x00D0) return "SCROLL-UP " + (opcode & 0x000F);
                    }
                }
            }
            case 0x1 -> {
                return "JUMP " + String.format("%03X", opcode & 0x0FFF);
            }
            case 0x2 -> {
                return ":CALL " + String.format("%03X", opcode & 0x0FFF);
            }
            case 0x3 -> {
                return "IF v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " != " + String.format("%02X", opcode & 0x00FF) + " THEN";
            }
            case 0x4 -> {
                return "IF v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " == " + String.format("%02X", opcode & 0x00FF) + " THEN";
            }
            case 0x5 -> {
                switch (opcode & 0xF) {
                    case 0 -> {
                        return "IF v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " != v" + String.format("%01X", (opcode & 0x00F0) >> 4) + " THEN";
                    }
                    case 2 -> {
                        return "SAVE v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " - v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 3 -> {
                        return "LOAD v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " - v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                }
            }
            case 0x6 -> {
                return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " := " + String.format("%02X", opcode & 0x00FF);
            }
            case 0x7 -> {
                return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " += " + String.format("%02X", opcode & 0x00FF);
            }
            case 0x8 -> {
                switch (opcode & 0x000F) {
                    case 0x0 -> {
                        return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " := v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x1 -> {
                        return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " |= v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x2 -> {
                        return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " &= v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x3 -> {
                        return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " ^= v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x4 -> {
                        return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " += v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x5 -> {
                        return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " -= v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x6 -> {
                        return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " >>= v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x7 -> {
                        return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " =- v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0xE -> {
                        return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " <<= v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                }
            }
            case 0x9 -> {
                return "IF v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " == v" + String.format("%01X", (opcode & 0x00F0) >> 4) + " THEN";
            }
            case 0xA -> {
                return "I := " + String.format("%03X", opcode & 0x0FFF);
            }
            case 0xB -> {
                if (Launcher.getHardware() != Hardware.SCHIP8) {
                    return "JUMP0 " + String.format("%03X", opcode & 0x0FFF);
                } else {
                    return "JUMP " + String.format("%02X", opcode & 0x00FF) + " + v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                }
            }
            case 0xC -> {
                return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " := random " + String.format("%02X", opcode & 0x00FF);
            }
            case 0xD -> {
                if ((opcode & 0x000F) != 0) {
                    return "SPRITE v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " v" + String.format("%01X", (opcode & 0x00F0) >> 4) + " " + String.format("%01X", opcode & 0x000F);
                } else {
                    return "SPRITE v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " v" + String.format("%01X", (opcode & 0x00F0) >> 4) + " 0";
                }
            }
            case 0xE -> {
                if ((opcode & 0x00FF) == 0x9E) {
                    return "IF v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " -KEY THEN";
                } else if ((opcode & 0x00FF) == 0xA1) {
                    return "IF v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " KEY THEN";
                }
            }
            case 0xF -> {
                switch (opcode & 0x00FF) {
                    // case 0x00 is handled directly in the debugger
                    case 0x01 -> {
                        return "PLANE " + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x02 -> {
                        return "AUDIO";
                    }
                    case 0x07 -> {
                        return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " := DELAY";
                    }
                    case 0x0A -> {
                        return "v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " := KEY";
                    }
                    case 0x15 -> {
                        return "DELAY := v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x18 -> {
                        return "BUZZER := v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x1E -> {
                        return "I += v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x29 -> {
                        return "I := hex v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x30 -> {
                        return "I := bighex v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x33 -> {
                        return "BCD v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x3A -> {
                        return "PITCH := v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x55 -> {
                        return "SAVE v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x65 -> {
                        return "LOAD v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x75 -> {
                        return "SAVEFLAGS v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x85 -> {
                        return "SAVEFLAGS rpl v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                }
            }
        }
        return "null";
    }

}
