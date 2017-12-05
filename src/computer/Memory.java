package computer;

public class Memory {

    private short[] data;
    private Register AR;

    public Memory(int size) {
        data = new short[size];
    }

    public void setAR(Register AR) {
        this.AR = AR;
    }

    public void read(Register src) {
        src.load(data[AR.getValue()]);
    }

    public void write(Register dest) {
        data[AR.getValue()] = dest.getValue();
    }

    public void setContent(short[] in) {
        int i;
        for (i = 0; i < in.length; i++)
            data[i] = in[i];
        for (; i < data.length; i++)
            data[i] = 0;
    }

    public void clear() {
        for (int i = 0; i < data.length; ++i)
            data[i] = 0;
    }

    public short[] getData() {
        return data;
    }

}