package simulator;

public class Memory {

    private int[] data;
    private Register AR;
    private int wordSize;
    private int wordMask;

    public Memory(int size, int wordSize) {
        this.wordSize = wordSize;
        data = new int[size];
        wordMask = (1 << wordSize) - 1;
    }

    public void setAR(Register AR) {
        this.AR = AR;
    }

    public void read(Register dest) {
        dest.load(data[AR.getValue()]);
    }

    public void write(Register src) {
        data[AR.getValue()] = src.getValue() & wordMask;
    }

    public void setContent(int[] in) {
        int i;
        for (i = 0; i < in.length; i++)
            data[i] = in[i] & wordMask;
        for (; i < data.length; i++)
            data[i] = 0;
    }

    public void clear() {
        for (int i = 0; i < data.length; ++i)
            data[i] = 0;
    }

    public int[] getData() {
        return data;
    }

    public int getWordSize() {
        return wordSize;
    }

    public int getSize() {
        return data.length;
    }

}
