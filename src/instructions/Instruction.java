package instructions;

public class Instruction {

		protected String name;
		protected int bin;
    protected boolean memory;
    protected boolean indirect;
    protected String description;

    public Instruction(String name, int bin, boolean memory, boolean indirect, String description) {
        this.name = name;
        this.bin = bin;
        this.memory = memory;
        this.indirect = indirect;
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
        } else {
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
        } else {
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

