package instructions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class InstructionSet {

    private Map<String, Instruction> map = new HashMap<>();
    private Map<Integer, Instruction> rev = new HashMap<>();

    public abstract void init();

    protected void add(Instruction instruction) {
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

}
