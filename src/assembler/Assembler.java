package assembler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import simulator.Instruction;
import simulator.InstructionSet;

public class Assembler {

    public static int[] assemble(InstructionSet instructionSet, String[] lines, Map<Integer, String> memLabels) {
        return new Assembler(instructionSet).assemble(lines, memLabels);
    }

    public static int[] assemble(InstructionSet instructionSet, String[] lines) {
        return new Assembler(instructionSet).assemble(lines, null);
    }

    public static String disassemble(InstructionSet instructionSet, int bin) {
        return new Assembler(instructionSet).disassemble(bin);
    }

    private static Map<String, Instruction> instructionSetData = new HashMap<>();
    private static String strOrg, strAdr, strEnd;
    private static Pattern lblPattern;
    private static final int[] ADR_NOT_FOUND = new int[0];

    static {
        strOrg = "ORG";
        strAdr = "ADR";
        strEnd = "END";
        instructionSetData.put("DEC", new DecData());
        instructionSetData.put("BIN", new BinData());
        instructionSetData.put("HEX", new HexData());
        instructionSetData.put("CHR", new CharData());
        instructionSetData.put("STR", new StringData());
        lblPattern = Pattern.compile("[A-Za-z_0-9]+\\s*,");
    }

    private InstructionSet instructionSet;
    private Map<String, Integer> labels = new HashMap<>();
    private boolean secondRound;

    private Assembler(InstructionSet instructionSet) {
        this.instructionSet = instructionSet;
    }

    protected Instruction getInstruction(String name) {
        Instruction i = instructionSetData.get(name);
        if (i == null)
            i = instructionSet.get(name);
        return i;
    }

    protected int[] assembleInstruction(int lineNumber, String inst) {
        String[] sp = inst.split("\\s+");
        String instName = sp[0].toUpperCase();
        if (instName.equals(strAdr)) {
            if (sp.length == 1) {
                err(lineNumber, "Expected an address after " + instName);
            }
            Integer l = labels.get(sp[1]);
            if (!secondRound) {
                return ADR_NOT_FOUND;
            } else if (l == null) {
                err(lineNumber, "Unknown address " + sp[1]);
            } else {
                return new int[] { l };
            }
        }
        Instruction i = getInstruction(instName);
        if (i == null) {
            try {
                return new int[] { parseNumber(inst) };
            } catch (NumberFormatException e) {
                err(lineNumber, "Unknown instruction \'" + instName + "\'");
            }
        }
        if (i.isArg()) {
            if (sp.length == 1) {
                err(lineNumber, "Expected an argument after " + instName);
            }
            int adr = 0;
            try {
                adr = parseNumber(sp[1]);
            } catch (NumberFormatException e) {
                Integer l = labels.get(sp[1]);
                if (!secondRound) {
                    return ADR_NOT_FOUND;
                } else if (l == null) {
                    err(lineNumber, "Unknown address " + sp[1]);
                } else {
                    adr = (int) l;
                }
            }
            boolean indirect = false;
            if (sp.length > 2) {
                sp[2] = sp[2].toLowerCase();
                if (sp[2].equals("i")) {
                    if (!i.isIndirect())
                        err(lineNumber, "Indirect not supported");
                    indirect = true;
                } else {
                    unknown(lineNumber, inst);
                }
            }
            if (sp.length > 3) {
                unknown(lineNumber, inst);
            }
            try {
                return new int[] { i.getBin(adr, indirect ? instructionSet.getIndirectMask() : 0) };
            } catch (Exception e) {
                err(lineNumber, e.getMessage());
                return null;
            }
        } else if (i instanceof Data) {
            String data = inst.substring(3).trim();
            if (data.length() == 0)
                unknown(lineNumber, inst);
            if (i instanceof StringData) {
                int[] res = ((StringData) i).getBins(data);
                if (res == null)
                    err(lineNumber, "Expected a string \'" + data + "\'");
                return res;
            } else {
                int res = 0;
                if (i instanceof DecData) {
                    try {
                        res = ((Data) i).getBin(data);
                    } catch (Exception e) {
                        err(lineNumber, "Expected a decimal number \'" + data + "'");
                    }
                } else if (i instanceof HexData) {
                    try {
                        res = ((Data) i).getBin(data);
                    } catch (Exception e) {
                        err(lineNumber, "Expected a hexadecimal number \'" + data + "'");
                    }
                } else if (i instanceof BinData) {
                    try {
                        res = ((Data) i).getBin(data);
                    } catch (Exception e) {
                        err(lineNumber, "Expected a binary number \'" + data + "'");
                    }
                } else if (i instanceof CharData) {
                    res = ((Data) i).getBin(data);
                    if (res == -1)
                        err(lineNumber, "Expected a character \'" + data + "'");
                }
                return new int[] { res };
            }
        } else {
            return new int[] { i.getBin() };
        }
    }

    private static int parseNumber(String s) {
        int res;
        s = s.toLowerCase();
        if (s.startsWith("0x")) {
            s = s.substring(2);
            res = Integer.parseInt(s, 16);
        } else if (s.startsWith("0b")) {
            s = s.substring(2);
            res = Integer.parseInt(s, 2);
        } else {
            res = Integer.parseInt(s, 10);
        }
        return res;
    }

    private int[] assemble(String[] lines, Map<Integer, String> memLabels) {
        labels.clear();
        secondRound = false;
        Map<Integer, Integer> resMap = new HashMap<>();
        Queue<QueueItem> notFoundAdrQueue = new LinkedList<>();
        Matcher lblMatcher;
        int adr = 0;
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].split(";")[0].trim();
            if (lines[i].length() == 0)
                continue;
            int spaceLoc = lines[i].indexOf(' ');
            String name, args;
            try {
                name = lines[i].substring(0, spaceLoc).toUpperCase();
                args = lines[i].substring(spaceLoc + 1);
            } catch (IndexOutOfBoundsException e) {
                name = lines[i].toUpperCase();
                args = "";
            }
            if (name.equals(strOrg)) {
                boolean p = false;
                if (args.charAt(0) == '+') {
                    p = true;
                    args = args.substring(1);
                }
                try {
                    int o = Integer.parseInt(args);
                    if (p)
                        adr += o;
                    else
                        adr = o;
                } catch (NumberFormatException e) {
                    err(i, "Expected a number for ORG");
                }
            } else if (name.equals(strEnd)) {
                break;
            } else {
                lblMatcher = lblPattern.matcher(lines[i]);
                if (lblMatcher.find() && lblMatcher.start() == 0) {
                    labels.put(lines[i].substring(0, lblMatcher.end() - 1).trim(), adr);
                    lines[i] = lines[i].substring(lblMatcher.end()).trim();
                }
                int[] bins = assembleInstruction(i, lines[i]);
                if (bins == ADR_NOT_FOUND) {
                    notFoundAdrQueue.add(new QueueItem(adr, i, lines[i]));
                    adr++;
                } else {
                    for (int j = 0; j < bins.length; ++j) {
                        resMap.put(adr++, bins[j]);
                    }
                }
            }
        }

        secondRound = true;
        QueueItem queueItem;
        while (!notFoundAdrQueue.isEmpty()) {
            queueItem = notFoundAdrQueue.remove();
            resMap.put(queueItem.adr, assembleInstruction(queueItem.line, queueItem.inst)[0]);
        }
        int max = 0;
        for (Entry<Integer, Integer> e : resMap.entrySet())
            if (e.getKey() > max)
                max = e.getKey();
        int[] arr = new int[max + 1];
        for (Entry<Integer, Integer> e : resMap.entrySet())
            arr[e.getKey()] = e.getValue();

        if (memLabels != null) {
            for (String k : labels.keySet()) {
                memLabels.put(labels.get(k), k);
            }
        }

        return arr;
    }

    public String disassemble(int bin) {
        bin = bin & instructionSet.getBitsMask();
        Instruction i = instructionSet.get(bin);
        if (i == null) {
            i = instructionSet.get(bin & instructionSet.getOpCodeMask());
        }
        if (i == null) {
            return String.format("%04X", (short) bin);
        } else if (i.isArg()) {
            return i.getAsm(bin & instructionSet.getAddressMask(), bin & instructionSet.getIndirectMask());
        } else {
            return i.getAsm();
        }
    }

    private static void err(int i, String msg) {
        throw new RuntimeException(String.format("%d: %s\n", i + 1, msg));
    }

    private static void unknown(int i, String inst) {
        err(i, "Unknown instruction format " + inst);
    }

    protected static class Data extends Instruction {

        public Data() {
            super(null, 0, 0, null);
        }

        public int getBin(String data) {
            return Integer.parseInt(data);
        }
    }

    protected static class DecData extends Data {
    }

    protected static class HexData extends Data {
        @Override
        public int getBin(String data) {
            return Integer.parseInt(data, 16);
        }
    }

    protected static class BinData extends Data {
        @Override
        public int getBin(String data) {
            return Integer.parseInt(data, 2);
        }
    }

    protected static class CharData extends Data {
        @Override
        public int getBin(String s) {
            if (s.length() == 1) {
                return (int) s.charAt(0);
            } else {
                if (s.charAt(s.length() - 1) == '\'') {
                    if (s.length() == 4 && s.charAt(1) == '\\')
                        return (short) s.charAt(2);
                    else
                        return (short) s.charAt(1);
                } else {
                    return -1;
                }
            }
        }
    }

    protected static class StringData extends Data {
        public int[] getBins(String s) {
            if (s.charAt(0) != '"' || s.charAt(s.length() - 1) != '"') {
                return null;
            } else {
                s = s.substring(1, s.length() - 1);
                char[] arr = s.toCharArray();
                int[] res = new int[arr.length + 1];
                for (int i = 0; i < arr.length; ++i)
                    res[i] = (int) arr[i];
                res[arr.length] = '\0';
                return res;
            }
        }
    }

    private static class QueueItem {
        int adr;
        int line;
        String inst;

        QueueItem(int a, int l, String i) {
            adr = a;
            line = l;
            inst = i;
        }
    }

}
