package fr.yinxfox.debugger;

public class OpcodeTranslator {

    public static String decodeOp(int opcode) {
        switch (opcode >> 12) {
            case 0x0 -> {
                if (opcode == 0x00E0 || opcode == 0x0230) return "CLEAR";
                else return "RETURN";
            }
            case 0x1 -> {
                return "JMP " + String.format("%03X", opcode & 0x0FFF);
            }
            case 0x2 -> {
                return "CAL " + String.format("%03X", opcode & 0x0FFF);
            }
            case 0x3 -> {
                return "SKP v" + String.format("%01X", (opcode & 0x0F00) >> 8) + "==" + String.format("%02X", opcode & 0x00FF);
            }
            case 0x4 -> {
                return "SKP v" + String.format("%01X", (opcode & 0x0F00) >> 8) + "!=" + String.format("%02X", opcode & 0x00FF);
            }
            case 0x5 -> {
                return "SKP v" + String.format("%01X", (opcode & 0x0F00) >> 8) + "==v" + String.format("%01X", (opcode & 0x00F0) >> 4);
            }
            case 0x6 -> {
                return "SET v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " " + String.format("%02X", opcode & 0x00FF);
            }
            case 0x7 -> {
                return "ADD v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " " + String.format("%02X", opcode & 0x00FF);
            }
            case 0x8 -> {
                switch (opcode & 0x000F) {
                    case 0x0 -> {
                        return "SET v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x1 -> {
                        return "OR v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x2 -> {
                        return "AND v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x3 -> {
                        return "XOR v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x4 -> {
                        return "ADD v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x5 -> {
                        return "SUB v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x6 -> {
                        return "LSR v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                    case 0x7 -> {
                        return "SUB v" + String.format("%01X", (opcode & 0x00F0) >> 4) + " v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0xE -> {
                        return "LSL v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " v" + String.format("%01X", (opcode & 0x00F0) >> 4);
                    }
                }
            }
            case 0x9 -> {
                return "SKP v" + String.format("%01X", (opcode & 0x0F00) >> 8) + "!=v" + String.format("%01X", (opcode & 0x00F0) >> 4);
            }
            case 0xA -> {
                return "SET I " + String.format("%03X", opcode & 0x0FFF);
            }
            case 0xB -> {
                return "JMP " + String.format("%03X", opcode & 0x0FFF) + " v0";
            }
            case 0xC -> {
                return "SET v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " rand " + String.format("%02X", opcode & 0x00FF);
            }
            case 0xD -> {
                return "DRW v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " v" + String.format("%01X", (opcode & 0x00F0) >> 4) + " " + String.format("%01X", opcode & 0x000F);
            }
            case 0xE -> {
                if ((opcode & 0x00FF) == 0x9E) {
                    return "SKP v" + String.format("%01X", (opcode & 0x0F00) >> 8) + "PRSD";
                } else {
                    return "SKP v" + String.format("%01X", (opcode & 0x0F00) >> 8) + "NPRSD";
                }
            }
            case 0xF -> {
                switch (opcode & 0x00FF) {
                    case 0x07 -> {
                        return "SET v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " DT";
                    }
                    case 0x0A -> {
                        return "SET v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " KEY";
                    }
                    case 0x15 -> {
                        return "SET DT v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x18 -> {
                        return "SET ST v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x1E -> {
                        return "ADD I v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x29 -> {
                        return "SET I v" + String.format("%01X", (opcode & 0x0F00) >> 8) + " HEX";
                    }
                    case 0x33 -> {
                        return "BCD v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x55 -> {
                        return "SAVE v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                    case 0x65 -> {
                        return "LOAD v" + String.format("%01X", (opcode & 0x0F00) >> 8);
                    }
                }
            }
        }
        return "null";
    }

}
