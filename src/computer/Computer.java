package computer;

import utils.Logger;

public class Computer extends ComputerAbstract {

    private Memory M;
    private Register AR, PC, DR, AC, IR, TR;
    private boolean E, S;

    private byte SC;
    private boolean T0, T1, T2, T3, T4, T5, T6;
    private boolean D0, D1, D2, D3, D4, D5, D6, D7;
    private boolean I;
    private boolean R; // TODO: implement interrupt and io
    private boolean r;
    private boolean B11, B10, B9, B8, B7, B6, B5, B4, B3, B2, B1, B0;

    public Computer(Logger logger) {
        super(logger);
        setupDataUnit();
        M = new Memory(MEM_SIZE);
        M.setAR(AR);
        setSC((byte) 0);
        S = true;
    }

    private void setupDataUnit() {
        AR = new Register(12);
        PC = new Register(12);
        DR = new Register(16);
        AC = new Register(16);
        IR = new Register(16);
        TR = new Register(16);
    }

    private void controlUnitRun() {
        // Fetch
        if (!R && T0) {
            logger.log("R'T0: AR <- PC");
            AR.load(PC.getValue());
            incSC();
        }
        if (!R && T1) {
            logger.log("R'T1: IR <- M[AR], PC <- PC + 1");
            M.read(IR);
            PC.increment();
            incSC();
        }

        // Decode
        if (!R && T2) {
            logger.log("R'T2: D0, ..., D7 <- Decode IR(12-14), AR <- IR(0-11), I <- IR(15)");
            short op = IR.bitsRange(12, 14);
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

        // Indirect
        if (!D7 && I && T3) {
            logger.log("D7'IT3: AR <- M[AR]");
            M.read(AR);
            incSC();
        }
        if (!D7 && !I && T3) {
            logger.log("D7'IT3: NOOP");
            incSC();
        }

        // Memory-Reference
        // AND
        if (D0 && T4) {
            logger.log("D0T4: DR <- M[AR]");
            M.read(DR);
            incSC();
        }
        if (D0 && T5) {
            logger.log("D0T5: AC <- AC ^ DR, SC <- 0");
            AC.load((short) (AC.getValue() & DR.getValue()));
            clrSC();
        }
        // ADD
        if (D1 && T4) {
            logger.log("D1T4: DR <- M[AR]");
            M.read(DR);
            incSC();
        }
        if (D1 && T5) {
            logger.log("D1T5: AC <- AC + DR, E <- Cout, SC <- 0");
            int res = AC.getValue() + DR.getValue();
            AC.load((short) (res % AC.getMaxValue()));
            E = (res & AC.getMaxValue()) != 0;
            clrSC();
        }
        // LDA
        if (D2 && T4) {
            logger.log("D2T4: DR <- M[AR]");
            M.read(DR);
            incSC();
        }
        if (D2 && T5) {
            logger.log("D2T4: AC <- DR, SC <- 0");
            AC.load(DR.getValue());
            clrSC();
        }
        // STA
        if (D3 && T4) {
            logger.log("D3T4: M[AR] <- AC, SC <- 0");
            M.write(AC);
            clrSC();
        }
        // BUN
        if (D4 && T4) {
            logger.log("D4T4: PC <- AR, SC <- 0");
            PC.load(AR.getValue());
            clrSC();
        }
        // BSA
        if (D5 && T4) {
            logger.log("D5T4: M[AR] <- PC, AR <- AR + 1");
            M.write(PC);
            AR.increment();
            incSC();
        }
        if (D5 && T5) {
            logger.log("D5T5: PC <- AR, SC <- 0");
            PC.load(AR.getValue());
            clrSC();
        }
        // ISZ
        if (D6 && T4) {
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

        r = D7 && !I && T3;

        // Register-Reference
        if (r) {
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
        // CLA
        if (r && B11) {
            logger.log("D7I'T3B11: AC <- 0, SC <- 0");
            AC.clear();
        }
        // CLE
        if (r && B10) {
            logger.log("D7I'T3B10: E <- 0, SC <- 0");
            E = false;
        }
        // CMA
        if (r && B9) {
            logger.log("D7I'T3B9: AC <- AC', SC <- 0");
            AC.load((short) (~AC.getValue() & AC.getMask()));
        }
        // CME
        if (r && B8) {
            logger.log("D7I'T3B8: E <- E', SC <- 0");
            E = !E;
        }
        // CIR
        if (r && B7) {
            logger.log("D7I'T3B7: AC <- shr(AC), AC(15) <- E, E <- AC(0), SC <- 0");
            short value = AC.getValue();
            boolean lsb = (value & 1) != 0;
            value >>= 1;
            if (E)
                value |= (short) (AC.getMaxValue() >> 1);
            E = lsb;
            AC.load(value);
        }
        // CIL
        if (r && B6) {
            logger.log("D7I'T3B6: AC <- shl(AC), AC(0) <- E, E <- AC(15), SC <- 0");
            short value = AC.getValue();
            boolean msb = (value & (short) (AC.getMaxValue() >> 1)) != 0;
            value = (short) ((value << 1) & AC.getMask());
            if (E)
                value |= 1;
            E = msb;
            AC.load(value);
        }
        // INC
        if (r && B5) {
            logger.log("D7I'T3B5: AC <- AC + 1, SC <- 0");
            AC.increment();
        }
        // SPA
        if (r && B4) {
            logger.log("D7I'T3B4: if (AC(15) = 0) then (PC <- PC + 1), SC <- 0");
            if (!AC.bitAt(15))
                PC.increment();
        }
        // SNA
        if (r && B3) {
            logger.log("D7I'T3B3: if (AC(15) = 1) then (PC <- PC + 1), SC <- 0");
            if (AC.bitAt(15))
                PC.increment();
        }
        // SZA
        if (r && B2) {
            logger.log("D7I'T3B2: if (AC = 0) then (PC <- PC + 1), SC <- 0");
            if (AC.getValue() == 0)
                PC.increment();
        }
        // SZE
        if (r && B1) {
            logger.log("D7I'T3B1: if (E = 0) then (PC <- PC + 1), SC <- 0");
            if (!E)
                PC.increment();
        }
        // HLT
        if (r && B0) {
            logger.log("D7I'T3B0: S <- 0, SC <- 0");
            S = false;
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
    public void tick() {
        if (!S)
            return;
        controlUnitRun();
        runListeners();
    }

    @Override
    public void start() {
        S = true;
        while (S)
            tick();
    }

    @Override
    public void stop() {
        S = false;
        runListeners();
    }

    @Override
    public void runListeners() {
        for (Listener l : listeners)
            l.onUpdate(S, M.getData(), AR.getValue(), PC.getValue(), DR.getValue(), AC.getValue(), IR.getValue(),
                    TR.getValue(), SC, E);
    }

    @Override
    public boolean isRunning() {
        return S;
    }

    @Override
    public void loadMemory(short[] in) {
        M.setContent(in);
        runListeners();
    }

    @Override
    public short[] getMemory() {
        return M.getData();
    }

    @Override
    public void clear() {
        M.clear();
        AR.clear();
        PC.clear();
        DR.clear();
        AC.clear();
        IR.clear();
        TR.clear();
        clrSC();
        E = false;
        S = true;
        runListeners();
    }

    @Override
    public void clearMem() {
        M.clear();
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
        clrSC();
        E = false;
        S = true;
        runListeners();
    }

}
