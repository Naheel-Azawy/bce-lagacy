package computer;

public class Register {

    private short value;
    private short mask;
    private int max;

    public Register(int bits) {
        max = 1 << bits;
        mask = (short) (max - 1);
    }

    public void load(short in) {
        value = in;
    }

    public void increment() {
        value = (short) ((value + 1) % max);
    }

    public void clear() {
        value = 0;
    }

    public short getValue() {
        return value;
    }

    public boolean bitAt(int position) {
        return ((value >> position) & 1) == 1;
    }

    public short bitsRange(int from, int to) {
        return (short) ((value >> from) & ~(-1 << (to - from + 1)));
    }

    public void setBit(int bit, boolean to) {
        value = (short) (to ? (value | 1 << bit) : (value & ~(1 << bit)));
    }

    public int getMaxValue() {
        return max;
    }

    public short getMask() {
        return mask;
    }
}