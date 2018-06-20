package simulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class InstructionSet {

    public static final int MEMORY    = 0x1;
    public static final int INDIRECT  = 0x2;
    public static final int IMMEDIATE = 0x4;

    private List<String> ordered = new ArrayList<>();
    private Map<String, Instruction> map = new HashMap<>();
    private Map<Integer, Instruction> rev = new HashMap<>();

    public InstructionSet() {
        init();
    }

    public abstract void init();

    public abstract int getBitsMask();

    public abstract int getOpCodeMask();

    public abstract int getAddressMask();

    public abstract int getIndirectMask();

    protected void add(Instruction instruction) {
        ordered.add(instruction.getName());
        map.put(instruction.getName(), instruction);
        rev.put(instruction.getBin(), instruction);
    }

    public Instruction get(String name) {
        return map.get(name);
    }

    public Instruction get(int baseBin) {
        return rev.get(baseBin);
    }

    public Collection<Instruction> getAll() {
        return map.values();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String i : ordered) {
            sb.append(i).append(":\t").append(map.get(i).getDescription()).append('\n');
        }
        return sb.toString();
    }

}
