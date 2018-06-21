package computers;

import utils.Logger;

import simulator.Instruction;
import simulator.InstructionSet;

import simulator.Computer;
import simulator.Memory;
import simulator.Register;

public class ComputerAC extends Computer {

    public static final InstructionSet INSTURCTION_SET = new InstructionSet() {
            @Override
            public void init() {
                add(new Instruction("AND", 0x0000, MEMORY | INDIRECT, "Logical AND memory with AC"));
                add(new Instruction("ADD", 0x1000, MEMORY | INDIRECT, "Arithmetic ADD memory with AC"));
                add(new Instruction("LDA", 0x2000, MEMORY | INDIRECT, "Load from memory to AC"));
                add(new Instruction("STA", 0x3000, MEMORY | INDIRECT, "Store AC to memory"));
                add(new Instruction("BUN", 0x4000, MEMORY | INDIRECT, "Branch unconditional"));
                add(new Instruction("BSA", 0x5000, MEMORY | INDIRECT, "Branch and save return address"));
                add(new Instruction("ISZ", 0x6000, MEMORY | INDIRECT, "Increment and skip if zero"));

                add(new Instruction("CLA", 0x7800, 0, "Clear AC"));
                add(new Instruction("CLE", 0x7400, 0, "Clear E"));
                add(new Instruction("CMA", 0x7200, 0, "Complement AC"));
                add(new Instruction("CME", 0x7100, 0, "Complement E"));
                add(new Instruction("CIR", 0x7080, 0, "Circulate right (AC and E)"));
                add(new Instruction("CIL", 0x7040, 0, "Circulate left (AC and E)"));
                add(new Instruction("INC", 0x7020, 0, "Increment AC"));
                add(new Instruction("SPA", 0x7010, 0, "Skip if positive AC"));
                add(new Instruction("SNA", 0x7008, 0, "Skip if negative AC"));
                add(new Instruction("SZA", 0x7004, 0, "Skip if zero AC"));
                add(new Instruction("SZE", 0x7002, 0, "Skip if zero E"));
                add(new Instruction("HLT", 0x7001, 0, "Halt"));

                add(new Instruction("INP", 0xF800, 0, "Input a character to AC"));
                add(new Instruction("OUT", 0xF400, 0, "Output a character from AC"));
                add(new Instruction("SKI", 0xF200, 0, "Skip if input flag"));
                add(new Instruction("SKO", 0xF100, 0, "Skip if output flag"));
                add(new Instruction("ION", 0xF080, 0, "Interrupt on"));
                add(new Instruction("IOF", 0xF040, 0, "Interrupt off"));

                add(new Instruction("NOP", 0xFFFF, 0, "No operation"));
            }

            @Override
            public int getBitsMask() { return 0xffff; }

            @Override
            public int getOpCodeMask() { return 0x7000; }

            @Override
            public int getAddressMask() { return 0x0fff; }

            @Override
            public int getIndirectMask() { return 0x8000; }

        };

    private Computer.Formatter formatter = new Computer.Formatter(this, 16) {

            @Override
            public String[] getRegsNames() {
                return new String[] { "AR", "PC", "DR", "AC", "IR", "TR", "SC", "E", "S", "R", "IEN", "FGI", "FGO", "INPR", "OUTR" };
            }

            @Override
            public int[] getRegsValues() {
                return new int[] { AR.getValue(), PC.getValue(), DR.getValue(), AC.getValue(), IR.getValue(), TR.getValue(), SC, (E ? 1 : 0), (S ? 1 : 0), (R ? 1 : 0), (IEN ? 1 : 0), (FGI ? 1 : 0), (FGO ? 1 : 0), INPR.getValue(), OUTR.getValue() };
            }

        };

    private Register AR, PC, DR, AC, IR, TR, INPR, OUTR;
    private boolean E, S;

    private byte SC;
    private boolean T0, T1, T2, T3, T4, T5, T6;
    private boolean D0, D1, D2, D3, D4, D5, D6, D7;
    private boolean I;
    private boolean R, IEN;
    private boolean r, p;
    private boolean B11, B10, B9, B8, B7, B6, B5, B4, B3, B2, B1, B0;

    public ComputerAC() {
        super("Accumulator Computer", "A simple computer with a single accumulator register and I/O support.");
        setupDataUnit();
        M = new Memory(4096, 16);
        M.setAR(AR);
        clear();
    }

    private void setupDataUnit() {
        regSet.add(AR = new Register(12, "AR", "Address Register"));
        regSet.add(PC = new Register(12, "PC", "Program Counter"));
        regSet.add(DR = new Register(16, "DR", "Data Register"));
        regSet.add(AC = new Register(16, "AC", "Accumulator"));
        regSet.add(IR = new Register(16, "IR", "Instruction Register"));
        regSet.add(TR = new Register(16, "TR", "Temprory Register"));
        regSet.add(INPR = new Register(8, "INPR", "Input Register"));
        regSet.add(OUTR = new Register(8, "OUTR", "Output Register"));
        regSet.add("SC", "Sequence Counter");
        regSet.add("E", "Extended AC");
        regSet.add("S", "Start Flag");
        regSet.add("R", "Interrupt");
        regSet.add("IEN", "Interrupt Enabled");
        regSet.add("FGI", "Input Flag");
        regSet.add("FGO", "Output Flag");
    }

    private void controlUnitRun() {

        // Fetch
        if (!R && T0) {
            logger.log("R'T0: AR <- PC");
            AR.load(PC.getValue());
            incSC();
        } else if (!R && T1) {
            logger.log("R'T1: IR <- M[AR], PC <- PC + 1");
            M.read(IR);
            PC.increment();
            incSC();
        }

        // Decode
        if (!R && T2) {
            logger.log("R'T2: D0, ..., D7 <- Decode IR(12-14), AR <- IR(0-11), I <- IR(15)");
            int op = IR.bitsRange(12, 14);
            D0 = op == 0;
            D1 = op == 1;
            D2 = op == 2;
            D3 = op == 3;
            D4 = op == 4;
            D5 = op == 5;
            D6 = op == 6;
            D7 = op == 7;
            AR.load(IR.bitsRange(0, 11));
            I = IR.bitAt(15);
            incSC();
        }

        r = D7 && !I && T3;
        p = D7 && I && T3;
        if (r || p) {
            byte B = (byte) (Math.log(IR.bitsRange(0, 11)) / Math.log(2));
            B11 = B == 11;
            B10 = B == 10;
            B9 = B == 9;
            B8 = B == 8;
            B7 = B == 7;
            B6 = B == 6;
            B5 = B == 5;
            B4 = B == 4;
            B3 = B == 3;
            B2 = B == 2;
            B1 = B == 1;
            B0 = B == 0;
            clrSC();
        }

        // Indirect
        if (!D7 && I && T3) {
            logger.log("D7'IT3: AR <- M[AR]");
            M.read(AR);
            incSC();
        } else if (!D7 && !I && T3) {
            logger.log("D7'IT3: NOOP");
            incSC();
        }

        // Interrupt
        else if (IEN && R && T0) {
            logger.log("AR <- 0, TR <- PC");
            AR.clear();
            TR.load(PC.getValue());
        } else if (IEN && R && T1) {
            logger.log("M[AR] <- TR, PC <- 0");
            M.write(TR);
            PC.clear();
        } else if (IEN && R && T2) {
            logger.log("PC <- PC + 1, IEN <- 0, R <- 0, SC <- 0");
            PC.increment();
            IEN = false;
            R = false;
            clrSC();
        }

        // Memory-Reference
        // AND
        else if (D0 && T4) {
            logger.log("D0T4: DR <- M[AR]");
            M.read(DR);
            incSC();
        } else if (D0 && T5) {
            logger.log("D0T5: AC <- AC ^ DR, SC <- 0");
            AC.load((short) (AC.getValue() & DR.getValue()));
            clrSC();
        }
        // ADD
        else if (D1 && T4) {
            logger.log("D1T4: DR <- M[AR]");
            M.read(DR);
            incSC();
        } else if (D1 && T5) {
            logger.log("D1T5: AC <- AC + DR, E <- Cout, SC <- 0");
            int res = AC.getValue() + DR.getValue();
            AC.load(res % AC.getMaxValue());
            E = (res & AC.getMaxValue()) != 0;
            clrSC();
        }
        // LDA
        else if (D2 && T4) {
            logger.log("D2T4: DR <- M[AR]");
            M.read(DR);
            incSC();
        } else if (D2 && T5) {
            logger.log("D2T4: AC <- DR, SC <- 0");
            AC.load(DR.getValue());
            clrSC();
        }
        // STA
        else if (D3 && T4) {
            logger.log("D3T4: M[AR] <- AC, SC <- 0");
            M.write(AC);
            clrSC();
        }
        // BUN
        else if (D4 && T4) {
            logger.log("D4T4: PC <- AR, SC <- 0");
            PC.load(AR.getValue());
            clrSC();
        }
        // BSA
        else if (D5 && T4) {
            logger.log("D5T4: M[AR] <- PC, AR <- AR + 1");
            M.write(PC);
            AR.increment();
            incSC();
        } else if (D5 && T5) {
            logger.log("D5T5: PC <- AR, SC <- 0");
            PC.load(AR.getValue());
            clrSC();
        }
        // ISZ
        else if (D6 && T4) {
            logger.log("D6T4: DR <- M[AR]");
            M.read(DR);
            incSC();
        } else if (D6 && T5) {
            logger.log("D6T5: DR <- DR + 1");
            DR.increment();
            incSC();
        } else if (D6 && T6) {
            logger.log("D6T6: M[AR] <- DR, if (DR = 0) then (PC <- PC + 1), SC <- 0");
            M.write(DR);
            if (DR.getValue() == 0)
                PC.increment();
            clrSC();
        }

        // Register-Reference
        // CLA
        else if (r && B11) {
            logger.log("D7I'T3B11: AC <- 0, SC <- 0");
            AC.clear();
        }
        // CLE
        else if (r && B10) {
            logger.log("D7I'T3B10: E <- 0, SC <- 0");
            E = false;
        }
        // CMA
        else if (r && B9) {
            logger.log("D7I'T3B9: AC <- AC', SC <- 0");
            AC.load((short) (~AC.getValue() & AC.getMask()));
        }
        // CME
        else if (r && B8) {
            logger.log("D7I'T3B8: E <- E', SC <- 0");
            E = !E;
        }
        // CIR
        else if (r && B7) {
            logger.log("D7I'T3B7: AC <- shr(AC), AC(15) <- E, E <- AC(0), SC <- 0");
            int value = (int) 0x0000ffff & AC.getValue();
            boolean lsb = (value & 1) != 0;
            value >>= 1;
            if (E)
                value |= (short) (AC.getMaxValue() >> 1);
            E = lsb;
            AC.load((short) value);
        }
        // CIL
        else if (r && B6) {
            logger.log("D7I'T3B6: AC <- shl(AC), AC(0) <- E, E <- AC(15), SC <- 0");
            int value = AC.getValue();
            boolean msb = (value & (AC.getMaxValue() >> 1)) != 0;
            value = (short) ((value << 1) & AC.getMask());
            if (E)
                value |= 1;
            E = msb;
            AC.load(value);
        }
        // INC
        else if (r && B5) {
            logger.log("D7I'T3B5: AC <- AC + 1, SC <- 0");
            AC.increment();
        }
        // SPA
        else if (r && B4) {
            logger.log("D7I'T3B4: if (AC(15) = 0) then (PC <- PC + 1), SC <- 0");
            if (!AC.bitAt(15))
                PC.increment();
        }
        // SNA
        else if (r && B3) {
            logger.log("D7I'T3B3: if (AC(15) = 1) then (PC <- PC + 1), SC <- 0");
            if (AC.bitAt(15))
                PC.increment();
        }
        // SZA
        else if (r && B2) {
            logger.log("D7I'T3B2: if (AC = 0) then (PC <- PC + 1), SC <- 0");
            if (AC.getValue() == 0)
                PC.increment();
        }
        // SZE
        else if (r && B1) {
            logger.log("D7I'T3B1: if (E = 0) then (PC <- PC + 1), SC <- 0");
            if (!E)
                PC.increment();
        }
        // HLT
        else if (r && B0) {
            logger.log("D7I'T3B0: S <- 0, SC <- 0");
            S = false;
        }

        // Input-Output
        // INP
        else if (p && B11) {
            INPR.load((short) getInp());
            logger.log("AC(0-7) <- INPR, FGI <- 0");
            AC.setBits(0, 7, INPR.getValue());
            FGI = false;
            checkFGI();
        }
        // OUT
        else if (p && B10) {
            logger.log("OUTR <- AC(0-7), FGO <- 0");
            OUTR.load(AC.bitsRange(0, 7));
            putOut((char) OUTR.getValue());
            FGO = false;
        }
        // SKI
        else if (p && B9) {
            logger.log("if (FGI = 1) then (PC <- PC + 1)");
            if (FGI)
                PC.increment();
        }
        // SKO
        else if (p && B8) {
            logger.log("if (FGO = 1) then (PC <- PC + 1)");
            if (FGO)
                PC.increment();
        }
        // ION
        else if (p && B7) {
            logger.log("IEN <- 1");
            IEN = true;
        }
        // IOF
        else if (p && B6) {
            logger.log("IEN <- 0");
            IEN = false;
        }

    }

    private void incSC() {
        setSC((byte) (SC + 1));
    }

    private void clrSC() {
        setSC((byte) 0);
    }

    private void setSC(byte v) {
        SC = v;
        T0 = SC == 0;
        T1 = SC == 1;
        T2 = SC == 2;
        T3 = SC == 3;
        T4 = SC == 4;
        T5 = SC == 5;
        T6 = SC == 6;
    }

    @Override
    public InstructionSet getInstructionSet() {
        return INSTURCTION_SET;
    }

    @Override
    public void startEnable() {
        S = true;
    }

    @Override
    public void tick() {
        if (!S)
            return;
        controlUnitRun();
        runListeners();
    }

    @Override
    public void start() {
        if (S)
            return;
        S = true;
        loop();
    }

    @Override
    public void stop() {
        S = false;
        runListeners();
    }

    @Override
    public void runListeners() {
        for (Listener l : listeners)
            l.onUpdate(formatter);
    }

    @Override
    public boolean isRunning() {
        return S;
    }

    @Override
    public void loadMemory(int[] in) {
        M.setContent(in);
        runListeners();
    }

    @Override
    public int[] getMemory() {
        return M.getData();
    }

    @Override
    public void clear() {
        M.clear();
        memLabels.clear();
        AR.clear();
        PC.clear();
        DR.clear();
        AC.clear();
        IR.clear();
        TR.clear();
        INPR.clear();
        OUTR.clear();
        clrSC();
        E = false;
        S = false;
        R = false;
        FGI = false;
        FGO = false;
        runListeners();
    }

    @Override
    public void clearMem() {
        M.clear();
        memLabels.clear();
        runListeners();
    }

    @Override
    public void clearReg() {
        AR.clear();
        PC.clear();
        DR.clear();
        AC.clear();
        IR.clear();
        TR.clear();
        INPR.clear();
        OUTR.clear();
        clrSC();
        E = false;
        S = false;
        R = false;
        FGI = false;
        FGO = false;
        runListeners();
    }

    @Override
    public Computer.Formatter getFormatter() {
        return formatter;
    }

    @Override
    public boolean isIOSupported() {
        return true;
    }

    @Override
    public int getPC() {
        return PC.getValue();
    }

}
