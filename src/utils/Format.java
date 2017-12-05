package utils;

import assembler.Assembler;
import utils.Logger;

public class Format {

    private static final String WRD = "%2s %04d: 0x%04X %8d %-10s";
    private static final String REG = "%2s 0x%04X %8d";
    private static final String PNT = "->";
    private static final String NO_PNT = "  ";
    private static final String[] REGS = new String[] { "AR", "PC", "DR", "AC", "IR", "TR", "SC", "E", "S" };

    public static String memory(int lines, int mStart, short[] M, short PC) {
        int start = 0;
        String res = "   addr  hex     decimal text\n";
        res += "------------------------------------\n";
        int l;
        String p;
        for (int i = 0; (i < lines - 4 || lines == -1) && i < M.length; ++i) {
            l = i + start + mStart;
            p = l == PC - 1 ? PNT : NO_PNT;
            res += String.format(WRD, p, l, M[l], M[l], Assembler.disassemble(M[l]));
            res += "\n";
        }
        return res;
    }

    public static String registers(boolean S, short AR, short PC, short DR, short AC, short IR, short TR, byte SC,
            boolean E) {
        String res = "   hex     decimal\n";
        res += "------------------\n";
        short[] shorts = new short[] { AR, PC, DR, AC, IR, TR, SC, (short) (E ? 1 : 0), (short) (S ? 1 : 0) };
        for (int i = 0; i < REGS.length; ++i) {
            res += String.format(REG, REGS[i], shorts[i], shorts[i]);
            res += "\n";
        }
        return res;
    }

    public static String all(Logger logger, int lines, int mStart, boolean S, short[] M, short AR, short PC, short DR,
            short AC, short IR, short TR, byte SC, boolean E) {
        int start = 0;
        String res = "   Memory                           | CPU Registers\n";
        res += "   addr  hex     decimal text       |    hex     decimal\n";
        res += "------------------------------------|-------------------\n";
        short[] shorts = new short[] { AR, PC, DR, AC, IR, TR, SC, (short) (E ? 1 : 0), (short) (S ? 1 : 0) };
        int l;
        String p;
        int logI;
        int maxLog = lines - REGS.length - 6;
        for (int i = 0; (i < lines - 4 || lines == -1) && i < M.length; ++i) {
            l = i + start + mStart;
            p = l == PC - 1 ? PNT : NO_PNT;
            logI = i - REGS.length - 2;
            res += String.format(WRD, p, l, M[l], M[l], Assembler.disassemble(M[l]));
            if (i < REGS.length)
                res += String.format(" | " + REG, REGS[i], shorts[i], shorts[i]);
            else if (i == REGS.length || i == REGS.length + 2)
                res += " |-------------------";
            else if (i == REGS.length + 1)
                res += " | Logs:";
            else if (logger.size() != 0 && logI < logger.size())
                res += " | " + logger.get((logger.size() - maxLog < 0 ? 0 : logger.size() - maxLog) + logI);
            res += "\n";
        }
        return res;
    }

}