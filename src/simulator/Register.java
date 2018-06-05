package simulator;

public class Register {

    private int value;
    private int mask;
    private int max;

    public Register(int bits) {
        max = 1 << bits;
        mask = max - 1;
    }

    public void load(int in) {
        value = in;
    }

    public void increment() {
        value = (value + 1) % max;
    }

    public void clear() {
        value = 0;
    }

    public int getValue() {
        return value;
    }

    public boolean bitAt(int position) {
        return ((value >> position) & 1) == 1;
    }

    public int bitsRange(int from, int to) {
        return (value >> from) & ~(-1 << (to - from + 1));
    }

    public void setBit(int bit, boolean v) {
        value = v ? (value | 1 << bit) : (value & ~(1 << bit));
    }

    public void setBits(int from, int to, int v) {
        int mask = ~(-1 << from) | (-1 << to + 1);
        v = (v << from) & ~mask;
        value = (value & mask) | v;
    }

    public int getMaxValue() {
        return max;
    }

    public int getMask() {
        return mask;
    }
}
