package io.naheel.scs.base.computers;

import io.naheel.scs.base.simulator.Instruction;
import io.naheel.scs.base.simulator.InstructionSet;

import io.naheel.scs.base.simulator.Computer;
import io.naheel.scs.base.simulator.Memory;
import io.naheel.scs.base.simulator.Register;

public class ComputerBen extends Computer {

    public static final String NAME = "Ben's Computer";
    public static final String DESCRIPTION = "Even simpler computer built by Ben Eater on a breadboard.\nhttps://www.youtube.com/user/eaterbc";

    public static final InstructionSet INSTRUCTION_SET = new InstructionSet() {
            @Override
            public void init() {
                add(new Instruction("NOP", 0x00, 0, "No operation"));
                add(new Instruction("LDA", 0x10, MEMORY, "Load from memory to A"));
                add(new Instruction("ADD", 0x20, MEMORY, "Add from memory to A"));
                add(new Instruction("SUB", 0x30, MEMORY, "Subtract memory from A"));
                add(new Instruction("STA", 0x40, MEMORY, "Store A to memory"));
                add(new Instruction("LDI", 0x50, IMMEDIATE, "Load immediate"));
                add(new Instruction("JMP", 0x60, MEMORY, "Jump"));
                add(new Instruction("JC", 0x70, MEMORY, "Jump if carry"));
                add(new Instruction("JZ", 0x80, MEMORY, "Jump if A is zero"));
                add(new Instruction("OUT", 0xE0, 0, "Copy to output register"));
                add(new Instruction("HLT", 0xF0, 0, "Halt"));
            }

            @Override
            public int getBitsMask() { return 0xff; }

            @Override
            public int getOpCodeMask() { return 0xf0; }

            @Override
            public int getAddressMask() { return 0x0f; }

            @Override
            public int getIndirectMask() { return 0x00; }

        };

    private Computer.Formatter formatter = new Computer.Formatter(this, 8) {

            @Override
            public String[] getRegsNames() {
                return new String[] { "A", "B", "ALU", "OUT", "PC", "IR", "MAR", "CF", "ZF", "SC", "S" };
            }

            @Override
            public int[] getRegsValues() {
                return new int[] { A.getValue(), B.getValue(), ALU.getValue(), OUT.getValue(), PC.getValue(), IR.getValue(), MAR.getValue(), (CF ? 1 : 0), (ZF ? 1 : 0), SC, (S ? 1 : 0) };
            }

        };

    private Register A, B, ALU, OUT, PC, IR, MAR;
    private boolean S, CF, ZF;
    private byte SC;
    private boolean T0, T1, T2, T3, T4;
    private boolean LDA, ADD, SUB, STA, LDI, JMP, JC, JZ, OUTI, HLT;

    public ComputerBen() {
        super(NAME, DESCRIPTION);
        setupDataUnit();
        M = new Memory(16, 8);
        M.setAR(MAR);
        clear();
    }

    private void setupDataUnit() {
        regSet.add(A = new Register(8, "A", "Register A"));
        regSet.add(B = new Register(8, "B", "Register B"));
        regSet.add(ALU = new Register(8, "ALU", "ALU Register"));
        regSet.add(OUT = new Register(8, "OUT", "Output Register"));
        regSet.add(PC = new Register(4, "PC", "Program Counter"));
        regSet.add(IR = new Register(8, "IR", "Instruction Register"));
        regSet.add(MAR = new Register(4, "MAR", "Memory Address Register"));
        regSet.add("CF", "Carry Flag");
        regSet.add("ZF", "Zero Flag");
        regSet.add("SC", "Sequence Counter");
        regSet.add("S", "Start Flag");
    }

    private void controlUnitRun() {

        // Fetch
        if (T0) {
            logger.log("T0: CO MI");
            MAR.load(PC.getValue());
            incSC();
        } else if (T1) {
            logger.log("T1: RO II CE");
            M.read(IR);
            PC.increment();
            int op = IR.bitsRange(4, 7);
            LDA = op == 1;
            ADD = op == 2;
            SUB = op == 3;
            STA = op == 4;
            LDI = op == 5;
            JMP = op == 6;
            JC  = op == 7;
            JZ  = op == 8;
            OUTI= op == 0xE;
            HLT = op == 0xF;
            incSC();
        }

        // Memory-Reference
        // LDA
        if (LDA && T2) {
            logger.log("LDA T2: IO MI");
            MAR.load(IR.bitsRange(0, 3));
            incSC();
        } else if (LDA && T3) {
            logger.log("LDA T3: RO AI");
            M.read(A);
            incSC();
        } else if (LDA && T4) {
            logger.log("LDA T4: NOP");
            clrSC();
        }
        // ADD
        else if (ADD && T2) {
            logger.log("ADD T2: IO MI");
            MAR.load(IR.bitsRange(0, 3));
            incSC();
        } else if (ADD && T3) {
            logger.log("ADD T3: RO BI");
            M.read(B);
            incSC();
        } else if (ADD && T4) {
            logger.log("ADD T4: ΣO AI FI");
            int res = A.getValue() + B.getValue();
            ALU.load(res % A.getMaxValue());
            CF = (res & A.getMaxValue()) != 0;
            ZF = res == 0;
            A.load(ALU.getValue());
            clrSC();
        }
        // SUB
        else if (SUB && T2) {
            logger.log("SUB T2: IO MI");
            MAR.load(IR.bitsRange(0, 3));
            incSC();
        } else if (SUB && T3) {
            logger.log("SUB T3: RO BI");
            M.read(B);
            incSC();
        } else if (SUB && T4) {
            logger.log("SUB T4: SU ΣO AI FI");
            int res = B.getValue();
            res = (~res + 1) & B.getMask();
            res += A.getValue();
            ALU.load(res % A.getMaxValue());
            CF = (res & A.getMaxValue()) != 0;
            ZF = res == 0;
            A.load(ALU.getValue());
            clrSC();
        }
        // STA
        else if (STA && T2) {
            logger.log("STA T2: IO MI");
            MAR.load(IR.bitsRange(0, 3));
            incSC();
        } else if (STA && T3) {
            logger.log("STA T3: AO RI");
            M.write(A);
            incSC();
        } else if (STA && T4) {
            logger.log("STA T4: NOP");
            clrSC();
        }
        // LDI
        else if (LDI && T2) {
            logger.log("LDA T2: IO AI");
            A.load(IR.bitsRange(0, 3));
            incSC();
        } else if (LDI && T3) {
            logger.log("LDI T3: NOP");
            incSC();
        } else if (LDI && T4) {
            logger.log("LDI T4: NOP");
            clrSC();
        }
        // JMP
        else if (JMP && T2) {
            logger.log("JMP T2: IO J");
            PC.load(IR.getValue());
            incSC();
        } else if (JMP && T3) {
            logger.log("JMP T3: NOP");
            incSC();
        } else if (JMP && T4) {
            logger.log("JMP T4: NOP");
            clrSC();
        }
        // JC
        else if (JC && T2) {
            if (CF) {
                logger.log("JC T2: IO J");
                PC.load(IR.getValue());
            } else {
                logger.log("JC T2: NOP");
            }
            incSC();
        } else if (JC && T3) {
            logger.log("JC T3: NOP");
            incSC();
        } else if (JC && T4) {
            logger.log("JC T4: NOP");
            clrSC();
        }
        // JZ
        else if (JZ && T2) {
            if (ZF) {
                logger.log("JZ T2: IO J");
                PC.load(IR.getValue());
            } else {
                logger.log("JZ T2: NOP");
            }
            incSC();
        } else if (JZ && T3) {
            logger.log("JZ T3: NOP");
            incSC();
        } else if (JZ && T4) {
            logger.log("JZ T4: NOP");
            clrSC();
        }

        // Register-Reference
        // OUT
        else if (OUTI && T2) {
            logger.log("OUT T2: AO OI");
            OUT.load(A.getValue());
            incSC();
        } else if (OUTI && T3) {
            logger.log("OUT T3: NOP");
            incSC();
        } else if (OUTI && T4) {
            logger.log("OUT T4: NOP");
            clrSC();
        }
        // HLT
        else if (HLT && T2) {
            logger.log("HLT T2: HLT");
            S = false;
            incSC();
        } else if (HLT && T3) {
            logger.log("HTL T3: NOP");
            incSC();
        } else if (HLT && T4) {
            logger.log("HLT T4: NOP");
            clrSC();
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
    }

    @Override
    public InstructionSet getInstructionSet() {
        return INSTRUCTION_SET;
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
        A.clear();
        B.clear();
        ALU.clear();
        OUT.clear();
        PC.clear();
        IR.clear();
        MAR.clear();
        clrSC();
        S = false;
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
        A.clear();
        B.clear();
        ALU.clear();
        OUT.clear();
        PC.clear();
        IR.clear();
        MAR.clear();
        clrSC();
        S = false;
        runListeners();
    }

    @Override
    public Computer.Formatter getFormatter() {
        return formatter;
    }

    @Override
    public boolean isIOSupported() {
        return false;
    }

    @Override
    public int getPC() {
        return PC.getValue();
    }

}
