package utils;

import assembler.Assembler;
import utils.Logger;

public class Format {

    private static final String MEM_HEADER = "   addr  hex     decimal     text       ";
    private static final String MEM_LINES = "----------------------------------------";
    private static final String REG_HEADER = "      hex     decimal   ";
    private static final String REG_LINES = "------------------------";
    private static final String WRD = "%2s %04d: 0x%04X %8d %s %-10s";
    private static final String REG = "%2s\t0x%04X %8d %s";
    private static final String PNT = "->";
    private static final String NO_PNT = "  ";
    private static final String[] REGS = new String[] { "AR", "PC", "DR", "AC", "IR", "TR", "SC", "E", "S", "R", "IEN",
            "FGI", "FGO", "INPR", "OUTR" };

    public static String memory(int lines, int mStart, short[] M, short PC) {
        int start = 0;
        String res = MEM_HEADER + "\n" + MEM_LINES + "\n";
        int l;
        String p;
        for (int i = 0; (i < lines - 4 || lines == -1) && i < M.length; ++i) {
            l = i + start + mStart;
            p = l == PC - 1 ? PNT : NO_PNT;
            res += String.format(WRD, p, l, M[l], M[l], ch(M[l]), Assembler.disassemble(M[l]));
            res += "\n";
        }
        return res;
    }

    public static String registers(boolean S, short AR, short PC, short DR, short AC, short IR, short TR, byte SC,
            boolean E, boolean R, boolean IEN, boolean FGI, boolean FGO, short INPR, short OUTR) {
        String res = REG_HEADER + "\n" + REG_LINES + "\n";
        short[] shorts = new short[] { AR, PC, DR, AC, IR, TR, SC, (short) (E ? 1 : 0), (short) (S ? 1 : 0),
                (short) (R ? 1 : 0), (short) (IEN ? 1 : 0), (short) (FGI ? 1 : 0), (short) (FGO ? 1 : 0), INPR, OUTR };
        for (int i = 0; i < REGS.length; ++i) {
            res += String.format(REG, REGS[i], shorts[i], shorts[i], ch(shorts[i]));
            res += "\n";
        }
        return res;
    }

    public static String all(Logger logger, int lines, int mStart, boolean S, short[] M, short AR, short PC, short DR,
            short AC, short IR, short TR, byte SC, boolean E, boolean R, boolean IEN, boolean FGI, boolean FGO,
            short INPR, short OUTR) {
        int start = 0;
        String res = "   Memory                               | CPU Registers\n";
        res += MEM_HEADER + "| " + REG_HEADER + "\n";
        res += MEM_LINES + "|" + REG_LINES + "\n";
        short[] shorts = new short[] { AR, PC, DR, AC, IR, TR, SC, (short) (E ? 1 : 0), (short) (S ? 1 : 0),
                (short) (R ? 1 : 0), (short) (IEN ? 1 : 0), (short) (FGI ? 1 : 0), (short) (FGO ? 1 : 0), INPR, OUTR };
        int l;
        String p;
        int logI;
        int maxLog = lines - REGS.length - 6;
        for (int i = 0; (i < lines - 4 || lines == -1) && i < M.length; ++i) {
            l = i + start + mStart;
            p = l == PC - 1 ? PNT : NO_PNT;
            logI = i - REGS.length - 2;
            res += String.format(WRD, p, l, M[l], M[l], ch(M[l]), Assembler.disassemble(M[l]));
            if (i < REGS.length)
                res += String.format(" | " + REG, REGS[i], shorts[i], shorts[i], ch(shorts[i]));
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

    private static String ch(int c) {
        return Character.isISOControl(c) ? "   " : ("'" + ((char) c) + "'");
    }

}