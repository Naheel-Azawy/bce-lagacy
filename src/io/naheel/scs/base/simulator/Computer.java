package io.naheel.scs.base.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.naheel.scs.base.assembler.Assembler;

import io.naheel.scs.base.utils.Logger;
import io.naheel.scs.base.utils.Utils;

public abstract class Computer {

    public static final int TYPE_ASM = 0;
    public static final int TYPE_HEX = 1;
    public static final int TYPE_DEC = 2;
    public static final int TYPE_BIN = 3;

    public int mStart = 0;
    protected Memory M;
    protected RegistersSet regSet = new RegistersSet();
    protected Map<Integer, String> memLabels = new HashMap<>();
    protected List<Listener> listeners = new ArrayList<>();
    protected Logger logger;
    protected String src = "";
    protected String srcPath = null;
    protected int srcType = TYPE_ASM;
    protected int period = 0;
    private double avgFreq = 0;

    private List<Character> inpBuffer = new ArrayList<>();
    private List<Character> outBuffer = new ArrayList<>();
    protected boolean FGI, FGO;
    protected boolean ioCleared;
    protected List<CharListener> outListeners = new ArrayList<>();
    protected List<CharListener> inpListeners = new ArrayList<>();

    protected String name;
    protected String description;

    public Computer(String name, String description) {
        this.logger = new Logger();
        this.name = name;
        this.description = description;
    }

    public Computer from(Computer c) {

        List<Listener> listenersTmp = this.listeners;
        Logger loggerTmp = this.logger;
        String srcTmp = this.src;
        String srcPathTmp = this.srcPath;
        int srcTypeTmp = this.srcType;
        int periodTmp = this.period;
        List<Character> inpBufferTmp = this.inpBuffer;
        List<Character> outBufferTmp = this.outBuffer;
        List<CharListener> outListenersTmp = this.outListeners;
        List<CharListener> inpListenersTmp = this.inpListeners;

        this.listeners = c.listeners;
        this.logger = c.logger;
        this.src = c.src;
        this.srcPath = c.srcPath;
        this.srcType = c.srcType;
        this.period = c.period;
        this.inpBuffer = c.inpBuffer;
        this.outBuffer = c.outBuffer;
        this.outListeners = c.outListeners;
        this.inpListeners = c.inpListeners;

        c.listeners = listenersTmp;
        c.logger = loggerTmp;
        c.src = srcTmp;
        c.srcPath = srcPathTmp;
        c.srcType = srcTypeTmp;
        c.period = periodTmp;
        c.inpBuffer = inpBufferTmp;
        c.outBuffer = outBufferTmp;
        c.outListeners = outListenersTmp;
        c.inpListeners = inpListenersTmp;

        return this;
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
            loadMemory(Assembler.assemble(getInstructionSet(), program.split("\n"), memLabels));
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
            loadMemory(Utils.parseIntArray((src).split("\n"), 2));
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
            loadMemory(Utils.parseIntArray(program.split("\n"), 16));
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
            loadMemory(Utils.parseIntArray(program.split("\n")));
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
        void onUpdate(Formatter formatter);
    }

    public synchronized void connectOnUpdate(Listener l) {
        listeners.add(l);
        runListeners();
    }

    public Logger getLogger() {
        return logger;
    }

    public interface CharListener {
        void onGot(char c);
    }

    protected void putOut(char c) {
        ioCleared = false;
        outBuffer.add(c);
        for (CharListener l : outListeners)
            l.onGot(c);
        FGO = true;
    }

    public void putInp(char c) {
        ioCleared = false;
        inpBuffer.add(c);
        for (CharListener l : inpListeners)
            l.onGot(c);
        FGI = true;
    }

    protected char getInp() {
        return inpBuffer.isEmpty() ? '\0' : inpBuffer.remove(0);
    }

    protected void checkFGI() {
        if (!inpBuffer.isEmpty())
            FGI = true;
    }

    public void putInpStr(String inp) {
        for (char c : inp.toCharArray())
            putInp(c);
        putInp('\0');
    }

    public boolean isIoCleared() {
        return ioCleared;
    }

    public void connectOnOut(CharListener l) {
        outListeners.add(l);
    }

    public void connectOnInp(CharListener l) {
        inpListeners.add(l);
    }

    public void clearIO() {
        inpBuffer.clear();
        outBuffer.clear();
        ioCleared = true;
        for (CharListener l : inpListeners)
            l.onGot('\0');
        for (CharListener l : outListeners)
            l.onGot('\0');
    }

    public RegistersSet getRegisters() {
        return regSet;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append('\n');
        for (int i = 0; i < name.length(); ++i)
            sb.append('-');
        sb.append('\n').append(description).append("\n\n");
        sb.append("Word size:\t").append(M.getWordSize()).append(" bits\n");
        sb.append("Memory size:\t").append(M.getSize()).append(" words\n");
        sb.append("\nRegisters:\n").append(getRegisters().toString()).append('\n');
        sb.append("Instruction Set:\n").append(getInstructionSet().toString());
        return sb.toString();
    }

    public abstract InstructionSet getInstructionSet();

    public abstract void startEnable();

    public abstract void start();

    public abstract void tick();

    public abstract void stop();

    public abstract boolean isRunning();

    public abstract boolean isIOSupported();

    public abstract void clear();

    public abstract void loadMemory(int[] in);

    public abstract int[] getMemory();

    public abstract int getPC();

    public abstract void clearMem();

    public abstract void clearReg();

    public abstract void runListeners();

    public abstract Formatter getFormatter();

    public static class RegistersSet {

        private List<String> names = new ArrayList<>();
        private List<String> descs = new ArrayList<>();

        public void add(Register r) {
            names.add(r.getName());
            descs.add(r.getDescription());
        }

        public void add(String name, String description) {
            names.add(name);
            descs.add(description);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < names.size(); ++i) {
                sb.append(names.get(i)).append(":\t").append(descs.get(i)).append('\n');
            }
            return sb.toString();
        }

    }

    private static final String MEM_HEADER = "   addr  hex     decimal     text       ";
    private static final String MEM_LINES = "----------------------------------------";
    private static final String REG_HEADER = "      hex     decimal   ";
    private static final String REG_LINES = "------------------------";
    private static final String WRD_P1 = "%2s %-4s: 0x%0";
    private static final String WRD_P2 = "X %8d %s %-10s";
    private static final String REG_P1 = "%2s\t0x%0";
    private static final String REG_P2 = "X %8d %s";
    private static final String PNT = "->";
    private static final String NO_PNT = "  ";

    public static abstract class Formatter {

        private Computer c;
        private String reg, wrd;
        private String[] regsNames;

        public Formatter(Computer c, int wordSize) {
            this.c = c;
            int hexSize = (int) Math.ceil(wordSize / 4);
            reg = REG_P1 + hexSize + REG_P2;
            wrd = WRD_P1 + hexSize + WRD_P2;
        }

        public abstract String[] getRegsNames();

        public abstract int[] getRegsValues();

        public String[] getRegsNames2() {
            return regsNames == null ? regsNames = getRegsNames() : regsNames;
        }

        public String getRegisters(int width, int height) {
            StringBuilder res = new StringBuilder(REG_HEADER + "\n" + REG_LINES + "\n");
            String[] names = getRegsNames2();
            int[] vals = getRegsValues();
            int i;
            int extentionShift = 64 /* sizeof(int) */ - c.M.getWordSize();
            int extendedReg;
            for (i = 0; i < names.length; ++i) {
                extendedReg = (vals[i] << extentionShift) >> extentionShift; // extending the MSB
                res.append(String.format(reg, names[i], vals[i], extendedReg, Utils.intToPrintableCharString(vals[i])));
                res.append("\n");
            }
            for (; i < height - 3; ++i) {
                res.append("\n");
            }
            return res.toString();
        }

        public String getMemory(int mStart, int width, int height) {
            int start = 0;
            StringBuilder res = new StringBuilder(MEM_HEADER + "\n" + MEM_LINES + "\n");
            int l;
            String p;
            int[] m = c.getMemory();
            String lbl;
            int pc = c.getPC();
            int i;
            int extentionShift = 64 /* sizeof(int) */ - c.M.getWordSize();
            int extendedM;
            for (i = 0; (i < height - 3 || height == -1) && i < m.length; ++i) {
                l = i + start + mStart;
                p = l == pc - 1 ? PNT : NO_PNT;
                lbl = c.memLabels.get(l);
                if (lbl == null)
                    lbl = String.format(Locale.US, "%04d", l);
                else if (lbl.length() > 4)
                    lbl = lbl.substring(0, 2) + "..";
                extendedM = (m[l] << extentionShift) >> extentionShift; // extending the MSB
                res.append(String.format(wrd, p, lbl, m[l], extendedM, Utils.intToPrintableCharString(m[l]), Assembler.disassemble(c.getInstructionSet(), m[l])));
                res.append("\n");
            }
            for (; i < height - 3; ++i) {
                res.append("\n");
            }
            return res.toString();
        }

        public String getAll(int mStart, int width, int height) {
            int start = 0;
            StringBuilder res = new StringBuilder("   Memory                               | CPU Registers\n");
            res.append(MEM_HEADER + "| " + REG_HEADER + "\n");
            res.append(MEM_LINES + "|" + REG_LINES + "\n");
            String[] names = getRegsNames2();
            int[] vals = getRegsValues();
            int l;
            String p;
            int logI;
            int maxLog = height - names.length - 5;
            int[] m = c.getMemory();
            String lbl;
            int pc = c.getPC();
            int i;
            int extentionShift = 64 /* sizeof(int) */ - c.M.getWordSize();
            int extendedReg;
            int extendedM;
            for (i = 0; (i < height - 3 || height == -1) && i < m.length; ++i) {
                l = i + start + mStart;
                p = l == pc - 1 ? PNT : NO_PNT;
                logI = i - names.length - 2;
                lbl = c.memLabels.get(l);
                if (lbl == null)
                    lbl = String.format(Locale.US, "%04d", l);
                else if (lbl.length() > 4)
                    lbl = lbl.substring(0, 2) + "..";
                extendedM = (m[l] << extentionShift) >> extentionShift; // extending the MSB
                res.append(String.format(wrd, p, lbl, m[l], extendedM, Utils.intToPrintableCharString(m[l]), Assembler.disassemble(c.getInstructionSet(), m[l])));
                if (i < names.length) {
                    extendedReg = (vals[i] << extentionShift) >> extentionShift; // extending the MSB
                    res.append(String.format(" | " + reg, names[i], vals[i], extendedReg, Utils.intToPrintableCharString(vals[i])));
                } else if (i == names.length || i == names.length + 2)
                    res.append(" |-------------------");
                else if (i == names.length + 1)
                    res.append(" | Logs:");
                else if (c.logger.size() != 0 && logI < c.logger.size())
                    res.append(" | ").append(c.logger.get((c.logger.size() - maxLog < 0 ? 0 : c.logger.size() - maxLog) + logI));
                res.append("\n");
            }
            for (; i < height - 3; ++i) {
                res.append("\n");
            }
            return res.toString();
        }

    }

}
