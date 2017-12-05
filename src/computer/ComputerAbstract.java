package computer;

import java.util.ArrayList;
import java.util.List;

import assembler.Assembler;
import utils.Logger;
import utils.Utils;

public abstract class ComputerAbstract {

    public static final int MEM_SIZE = 4096;

    public static final int TYPE_ASM = 0;
    public static final int TYPE_HEX = 1;
    public static final int TYPE_DEC = 2;
    public static final int TYPE_BIN = 3;

    protected List<Listener> listeners = new ArrayList<>();
    protected Logger logger;
    protected String src = "";
    protected String srcPath = null;
    protected int srcType = TYPE_ASM;

    public ComputerAbstract(Logger logger) {
        this.logger = logger;
    }

    public boolean loadProgram(String program) {
        return loadProgram(program, null);
    }

    public boolean loadBinProgram(String program) {
        return loadBinProgram(program, null);
    }

    public boolean loadHexProgram(String program) {
        return loadHexProgram(program, null);
    }

    public boolean loadDecProgram(String program) {
        return loadDecProgram(program, null);
    }

    public boolean loadProgram(String program, String path) {
        srcType = TYPE_ASM;
        if (path != null) {
            logger.log("Loaded program from \'" + path + '\'');
            srcPath = path;
        } else
            logger.log("Loaded program");
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

    public boolean loadBinProgram(String program, String path) {
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

    public boolean loadHexProgram(String program, String path) {
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

    public boolean loadDecProgram(String program, String path) {
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

    public void loadProgramFromFile(String path) {
        loadProgram(Utils.readFile(path), path);
    }

    public void loadBinProgramFromFile(String path) {
        loadBinProgram(Utils.readFile(path));
    }

    public void loadHexProgramFromFile(String path) {
        loadHexProgram(Utils.readFile(path));
    }

    public void loadDecProgramFromFile(String path) {
        loadDecProgram(Utils.readFile(path));
    }

    public abstract void loadMemory(short[] in);

    public abstract short[] getMemory();

    public abstract void start();

    public void startAsync() {
        new Thread(this::start).start();
    }

    public abstract void tick();

    public void tickAsync() {
        new Thread(this::tick).start();
    }

    public abstract void stop();

    public abstract boolean isRunning();

    public abstract void clear();

    public abstract void clearMem();

    public abstract void clearReg();

    public Logger getLogger() {
        return logger;
    }

    public interface Listener {
        void onUpdate(boolean S, short[] M, short AR, short PC, short DR, short AC, short IR, short TR, byte SC,
                boolean E);
    }

    public void connectOnUpdate(Listener l) {
        listeners.add(l);
        runListeners();
    }

    public abstract void runListeners();

    public String getSource() {
        return src;
    }

    public String getSourcePath() {
        return srcPath;
    }

    public int getSourceType() {
        return srcType;
    }

}