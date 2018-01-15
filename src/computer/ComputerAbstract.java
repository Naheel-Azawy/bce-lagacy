package computer;

import java.util.ArrayList;
import java.util.List;

import assembler.Assembler;
import utils.Logger;
import utils.Utils;

public abstract class ComputerAbstract {

    public static final int TYPE_ASM = 0;
    public static final int TYPE_HEX = 1;
    public static final int TYPE_DEC = 2;
    public static final int TYPE_BIN = 3;

    protected List<Listener> listeners = new ArrayList<>();
    protected Logger logger;
    protected String src = "";
    protected String srcPath = null;
    protected int srcType = TYPE_ASM;
    protected int period = 0;
    private double avgFreq = 0;

    public ComputerAbstract(Logger logger) {
        this.logger = logger;
    }

    public boolean loadProgram(int type, String program, String path) {
        if (type != -1)
            srcType = type;
        switch (srcType) {
        case TYPE_ASM:
            return loadAsmProgram(program, path);
        case TYPE_HEX:
            return loadHexProgram(program, path);
        case TYPE_DEC:
            return loadDecProgram(program, path);
        case TYPE_BIN:
            return loadBinProgram(program, path);
        }
        return false;
    }

    public boolean loadProgramSrc(int type, String program) {
        return loadProgram(type, program, null);
    }

    public boolean loadProgramFile(int type, String path) {
        return loadProgram(type, Utils.readFile(path), path);
    }

    private boolean loadAsmProgram(String program, String path) {
        srcType = TYPE_ASM;
        if (path != null) {
            logger.log("Loaded Assembly program from \'" + path + '\'');
            srcPath = path;
        } else
            logger.log("Loaded Assembly program");
        src = program;
        try {
            loadMemory(Assembler.assemble(program.split("\n")));
            return true;
        } catch (Exception e) {
            logger.log("Error: " + e.getMessage());
            runListeners();
            return false;
        }
    }

    private boolean loadBinProgram(String program, String path) {
        srcType = TYPE_BIN;
        if (path != null) {
            logger.log("Loaded binary program from \'" + path + '\'');
            srcPath = path;
        } else
            logger.log("Loaded binary program");
        src = program.replace(" ", "");
        try {
            loadMemory(Utils.parseShortArray((src).split("\n"), 2));
            return true;
        } catch (Exception e) {
            logger.log("Error: " + e.getMessage());
            runListeners();
            return false;
        }
    }

    private boolean loadHexProgram(String program, String path) {
        srcType = TYPE_HEX;
        if (path != null) {
            logger.log("Loaded hexadecimal program from \'" + path + '\'');
            srcPath = path;
        } else
            logger.log("Loaded hexadecimal program");
        src = program;
        try {
            loadMemory(Utils.parseShortArray(program.split("\n"), 16));
            return true;
        } catch (Exception e) {
            logger.log("Error: " + e.getMessage());
            runListeners();
            return false;
        }
    }

    private boolean loadDecProgram(String program, String path) {
        srcType = TYPE_DEC;
        if (path != null) {
            logger.log("Loaded decimal program from \'" + path + '\'');
            srcPath = path;
        } else
            logger.log("Loaded decimal program");
        src = program;
        try {
            loadMemory(Utils.parseShortArray(program.split("\n")));
            return true;
        } catch (Exception e) {
            logger.log("Error: " + e.getMessage());
            runListeners();
            return false;
        }
    }

    public String getSource() {
        return src;
    }

    public String getSourcePath() {
        return srcPath;
    }

    public int getSourceType() {
        return srcType;
    }

    public void startAsync() {
        new Thread(this::start).start();
    }

    public void tickAsync() {
        new Thread(this::tick).start();
    }

    protected void loop() {
        long t;
        double f;
        while (isRunning()) {
            t = System.currentTimeMillis();
            tick();
            Utils.sleep(period - (System.currentTimeMillis() - t));
            if (avgFreq == 0) {
                avgFreq = 1 / ((System.currentTimeMillis() - t) / 1000d);
            } else {
                f = 1 / ((System.currentTimeMillis() - t) / 1000d);
                if (Double.isFinite(f))
                    avgFreq = (avgFreq + f) / 2;
            }
        }
    }

    public double getAvgFrequency() {
        return avgFreq;
    }

    public int getFrequency() {
        return period <= 0 ? -1 : 1 / (period / 1000);
    }

    public void setFrequency(int f) {
        period = f == 0 ? -1 : Math.round(1 / (f / 1000f));
        if (period < 0)
            period = -1;
    }

    public interface Listener {
        void onUpdate(boolean S, short[] M, short AR, short PC, short DR, short AC, short IR, short TR, byte SC,
                boolean E, boolean R, boolean IEN, boolean FGI, boolean FGO, short INPR, short OUTR);
    }

    public void connectOnUpdate(Listener l) {
        listeners.add(l);
        runListeners();
    }

    public Logger getLogger() {
        return logger;
    }

    public abstract void startEnable();

    public abstract void start();

    public abstract void tick();

    public abstract void stop();

    public abstract boolean isRunning();

    public abstract void clear();

    public abstract void loadMemory(short[] in);

    public abstract short[] getMemory();

    public abstract void clearMem();

    public abstract void clearReg();

    public abstract void runListeners();

}