package io.naheel.scs.base.simulator;

import java.util.Locale;

public class Instruction {

    protected String name;
    protected int bin;
    protected boolean memory;
    protected boolean indirect;
    protected boolean immediate;
    protected String description;

    public Instruction(String name, int bin, int flags, String description) {
        this.name = name;
        this.bin = bin;
        this.memory = (flags & InstructionSet.MEMORY) != 0;
        this.indirect = (flags & InstructionSet.INDIRECT) != 0;
        this.immediate = (flags & InstructionSet.IMMEDIATE) != 0;
        this.description = description;
    }

    public int getBin() {
        return bin;
		}

    public int getBin(int arg, int indirectBit) {
        if (!memory && !immediate) {
            throw new RuntimeException("Arguments not allowed");
        }
        int b = bin | arg;
        if (indirect) {
            b |= indirectBit;
        } else if (indirectBit != 0) {
            throw new RuntimeException("Indirect references not allowed here");
        }
        return b;
    }

    public String getAsm() {
        return name;
		}

    public String getAsm(int arg, int indirectBit) {
        if (!memory && !immediate) {
            throw new RuntimeException("Arguments not allowed");
        }
        String res = name + " " + String.format(Locale.US, "%04d", arg);
        if (indirect) {
            if (indirectBit != 0)
                res += " I";
        } else if (indirectBit != 0) {
            throw new RuntimeException("Indirect references not allowed here");
        }
        return res;
    }

    public String getName() {
        return name;
    }

    public boolean isArg() {
        return memory | immediate;
    }

    public boolean isIndirect() {
        return indirect;
    }

    public String getDescription() {
        return description;
    }
}

