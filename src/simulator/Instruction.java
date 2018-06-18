package simulator;

public class Instruction {

		protected String name;
		protected int bin;
    protected boolean memory;
    protected boolean indirect;
    protected String description;

    public Instruction(String name, int bin, int flags, String description) {
        this.name = name;
        this.bin = bin;
        this.memory = (flags & InstructionSet.MEMORY) != 0;
        this.indirect = (flags & InstructionSet.INDIRECT) != 0;
        this.description = description;
    }

    public int getBin() {
        return bin;
		}

    public int getBin(int address, int indirectBit) {
        if (!memory) {
            throw new RuntimeException("Not a memory reference instruction. Arguments not allowed");
        }
        int b = bin | address;
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

    public String getAsm(int address, int indirectBit) {
        if (!memory) {
            throw new RuntimeException("Not a memory reference instruction. Arguments not allowed");
        }
        String res = name + " " + String.format("%04d", address);
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

    public boolean isMemory() {
        return memory;
    }

    public boolean isIndirect() {
        return indirect;
    }

    public String getDescription() {
        return description;
    }
}

