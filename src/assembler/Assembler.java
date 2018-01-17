package assembler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Queue;

public class Assembler {

	public static Assembler a;
	private Map<String, Instruction> instructionSet = new HashMap<>();
	private Map<Integer, Instruction> revInstructionSet = new HashMap<>();
	private Map<String, Instruction> instructionSetMR = new HashMap<>();
	private Map<Integer, Instruction> revInstructionSetMR = new HashMap<>();
	private Map<String, Instruction> instructionSetData = new HashMap<>();
	private String strOrg, strAdr, strEnd;
	private Pattern lblPattern;
	private static final int[] ADR_NOT_FOUND = new int[0];

	private Map<String, Integer> labels = new HashMap<>();
	private boolean secondRound;

	static {
		a = new Assembler();
		a.init();
	}

	public static short[] assemble(String[] lines) {
		return intArrToShortArr(a.assemblePrivate(lines));
	}

	public static String disassemble(int bin) {
		return a.disassemblePrivate(bin);
	}

	protected void createInstruction(String name, int bin) {
		Instruction i = new Instruction(name, bin);
		instructionSet.put(name, i);
		revInstructionSet.put(bin, i);
	}

	protected void createMRInstruction(String name, int bin) {
		Instruction i = new MRInstruction(name, bin);
		instructionSetMR.put(name, i);
		revInstructionSetMR.put(bin, i);
	}

	protected void createInstructions() {
		createMRInstruction("AND", 0x0000);
		createMRInstruction("ADD", 0x1000);
		createMRInstruction("LDA", 0x2000);
		createMRInstruction("STA", 0x3000);
		createMRInstruction("BUN", 0x4000);
		createMRInstruction("BSA", 0x5000);
		createMRInstruction("ISZ", 0x6000);

		createInstruction("CLA", 0x7800);
		createInstruction("CLE", 0x7400);
		createInstruction("CMA", 0x7200);
		createInstruction("CME", 0x7100);
		createInstruction("CIR", 0x7080);
		createInstruction("CIL", 0x7040);
		createInstruction("INC", 0x7020);
		createInstruction("SPA", 0x7010);
		createInstruction("SNA", 0x7008);
		createInstruction("SZA", 0x7004);
		createInstruction("SZE", 0x7002);
		createInstruction("HLT", 0x7001);

		createInstruction("INP", 0xF800);
		createInstruction("OUT", 0xF400);
		createInstruction("SKI", 0xF200);
		createInstruction("SKO", 0xF100);
		createInstruction("ION", 0xF080);
		createInstruction("IOF", 0xF040);

		createInstruction("NOP", 0xFFFF);
	}

	protected void init() {
		strOrg = "ORG";
		strAdr = "ADR";
		strEnd = "END";
		instructionSetData.put("DEC", new DecData());
		instructionSetData.put("BIN", new BinData());
		instructionSetData.put("HEX", new HexData());
		instructionSetData.put("CHR", new CharData());
		instructionSetData.put("STR", new StringData());
		createInstructions();
		lblPattern = Pattern.compile("[A-Za-z_0-9]+ *,");
	}

	protected Instruction getInstruction(String name) {
		Instruction i = instructionSet.get(name);
		if (i == null) {
			i = instructionSetMR.get(name);
		}
		if (i == null) {
			i = instructionSetData.get(name);
		}
		return i;
	}

	protected int[] assembleInstruction(int lineNumber, String inst) {
		String instName;
		try {
			instName = inst.substring(0, inst.indexOf(' ')).toUpperCase();
		} catch (IndexOutOfBoundsException e) {
			instName = inst.toUpperCase();
		}
		if (instName.equals(strAdr)) {
			String[] sp = inst.split(" ");
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
		if (i instanceof MRInstruction) {
			String[] sp = inst.split(" ");
			if (sp.length == 1) {
				err(lineNumber, "Expected an address after " + instName);
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
				if (sp[2].equals("i"))
					indirect = true;
				else {
					unknown(lineNumber, inst);
				}
			}
			if (sp.length > 3) {
				unknown(lineNumber, inst);
			}
			return new int[] { ((MRInstruction) i).getBin(adr, indirect) };
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

	private int[] assemblePrivate(String[] lines) {
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
					labels.put(lines[i].substring(0, lblMatcher.end() - 1), adr);
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

		labels.clear();
		secondRound = false;
		return arr;
	}

	public String disassemblePrivate(int bin) {
		bin = bin & 0xffff;
		Instruction i = revInstructionSet.get(bin);
		if (i == null) {
			i = revInstructionSetMR.get(bin & 0x7000);
		}
		if (i == null) {
			return String.format("%04X", (short) bin);
		} else if (i instanceof MRInstruction) {
			return ((MRInstruction) i).getString(bin & 0x0fff, (bin & 0x8000) != 0);
		} else {
			return i.getString();
		}
	}

	private static short[] intArrToShortArr(int[] arr) {
		short[] res = new short[arr.length];
		for (int i = 0; i < arr.length; ++i)
			res[i] = (short) arr[i];
		return res;
	}

	private static void err(int i, String msg) {
		throw new RuntimeException(String.format("%d: %s\n", i + 1, msg));
	}

	private static void unknown(int i, String inst) {
		err(i, "Unknown instruction format " + inst);
	}

	protected class Instruction {
		protected String name;
		protected int baseBin;

		public Instruction(String name, int baseBin) {
			this.name = name;
			this.baseBin = baseBin;
		}

		public int getBin() {
			return baseBin;
		}

		public String getString() {
			return name;
		}
	}

	protected class MRInstruction extends Instruction {

		public MRInstruction(String name, int baseBin) {
			super(name, baseBin);
		}

		public int getBin(int address, boolean indirect) {
			return baseBin | address | (indirect ? 0x8000 : 0x0);
		}

		public String getString(int address, boolean indirect) {
			String res = name + " " + String.format("%04d", address);
			if (indirect)
				res += " I";
			return res;
		}
	}

	protected class Data extends Instruction {

		public Data() {
			super(null, 0);
		}

		public int getBin(String data) {
			return Integer.parseInt(data);
		}
	}

	protected class DecData extends Data {
	}

	protected class HexData extends Data {
		@Override
		public int getBin(String data) {
			return Integer.parseInt(data, 16);
		}
	}

	protected class BinData extends Data {
		@Override
		public int getBin(String data) {
			return Integer.parseInt(data, 2);
		}
	}

	protected class CharData extends Data {
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

	protected class StringData extends Data {
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
