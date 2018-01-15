package assembler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

public class Assembler {

	public static short[] assemble(String[] lines) {
		Map<Integer, Short> map = new HashMap<>();
		Map<String, Integer> labels = new HashMap<>();
		int ld = 0;
		String line;
		String inst;
		String[] instSp;
		Queue<Thing<Integer, Integer, String>> q = new LinkedList<>();
		mainLoop: for (int i = 0; i < lines.length; i++) {
			line = lines[i].split(";")[0].trim();
			if (line.length() == 0)
				continue;
			switch ((instSp = line.split(" "))[0]) {
			case "ORG":
				try {
					ld = Integer.parseInt(instSp[1]);
				} catch (NumberFormatException e) {
					err(i, "Expected a number for ORG");
				}
				break;
			case "END":
				break mainLoop;
			default:
				int label = line.indexOf(',');
				if (label != -1) {
					labels.put(line.substring(0, label), ld);
					inst = line.substring(label + 1);
				} else {
					inst = line;
				}
				map.put(ld, assemble(i, inst, ld, labels, q));
				ld++;
			}
		}
		Thing<Integer, Integer, String> p;
		while (!q.isEmpty()) {
			p = q.remove();
			map.put(p.b, assemble(p.a, p.c, p.b, labels, null));
		}
		int max = 0;
		for (Entry<Integer, Short> e : map.entrySet())
			if (e.getKey() > max)
				max = e.getKey();
		short[] arr = new short[max + 1];
		for (Entry<Integer, Short> e : map.entrySet())
			arr[e.getKey()] = e.getValue();
		return arr;
	}

	private static short assemble(int i, String inst, int ld, Map<String, Integer> labels,
			Queue<Thing<Integer, Integer, String>> q) {
		short bin = 0;
		String[] sp = inst.trim().split(" ");
		switch (sp[0] = sp[0].toUpperCase()) {
		case "AND":
		case "ADD":
		case "LDA":
		case "STA":
		case "BUN":
		case "BSA":
		case "ISZ":
			boolean I = sp.length == 3 && sp[2].toUpperCase().equals("I");
			short resMem = (short) (I ? 0x8000 : 0x0);
			switch (sp[0]) {
			case "AND":
				resMem |= 0x0000;
				break;
			case "ADD":
				resMem |= 0x1000;
				break;
			case "LDA":
				resMem |= 0x2000;
				break;
			case "STA":
				resMem |= 0x3000;
				break;
			case "BUN":
				resMem |= 0x4000;
				break;
			case "BSA":
				resMem |= 0x5000;
				break;
			case "ISZ":
				resMem |= 0x6000;
				break;
			}
			short addr = 0;
			try {
				addr = Short.parseShort(sp[1]);
			} catch (NumberFormatException e) {
				if (labels == null) {
					err(i, "Expected a number for address");
				} else {
					Integer loc = labels.get(sp[1]);
					if (q == null && loc == null) {
						err(i, "Could not find label \'" + sp[1] + "\'");
					} else if (q != null) {
						q.add(new Thing<Integer, Integer, String>(i, ld, inst));
					} else {
						addr = (short) ((int) loc);
					}
				}
			}
			bin = (short) (resMem | addr);
			break;
		case "CLA":
			bin = 0x7800;
			break;
		case "CLE":
			bin = 0x7400;
			break;
		case "CMA":
			bin = 0x7200;
			break;
		case "CME":
			bin = 0x7100;
			break;
		case "CIR":
			bin = 0x7080;
			break;
		case "CIL":
			bin = 0x7040;
			break;
		case "INC":
			bin = 0x7020;
			break;
		case "SPA":
			bin = 0x7010;
			break;
		case "SNA":
			bin = 0x7008;
			break;
		case "SZA":
			bin = 0x7004;
			break;
		case "SZE":
			bin = 0x7002;
			break;
		case "HLT":
			bin = 0x7001;
			break;
		case "INP":
			bin = (short) 0xf800;
			break;
		case "OUT":
			bin = (short) 0xf400;
			break;
		case "SKI":
			bin = (short) 0xf200;
			break;
		case "SKO":
			bin = (short) 0xf100;
			break;
		case "ION":
			bin = (short) 0xf080;
			break;
		case "IOF":
			bin = (short) 0xf040;
			break;
		case "BIN":
			try {
				bin = (short) Integer.parseInt(sp[1], 2);
			} catch (NumberFormatException e) {
				err(i, "Expected a number for BIN");
			}
			break;
		case "HEX":
			try {
				bin = (short) Integer.parseInt(sp[1], 16);
			} catch (NumberFormatException e) {
				err(i, "Expected a number for HEX");
			}
			break;
		case "DEC":
			try {
				bin = (short) Integer.parseInt(sp[1]);
			} catch (NumberFormatException e) {
				err(i, "Expected a number for DEC");
			}
			break;
		default:
			if (sp.length == 1)
				try {
					sp[0] = sp[0].toLowerCase();
					if (sp[0].startsWith("0x"))
						sp[0] = sp[0].substring(2);
					bin = (short) Integer.parseInt(sp[0], 16);
				} catch (NumberFormatException e) {
					err(i, "Expected hexadecimal a number \'" + sp[0] + "\'");
				}
			else {
				err(i, "Unknown instruction \'" + sp[0] + "\'");
			}
		}
		return bin;
	}

	public static String disassemble(short bin) {
		String res = null;
		switch (bin) {
		case 0x7800:
			res = "CLA";
			break;
		case 0x7400:
			res = "CLE";
			break;
		case 0x7200:
			res = "CMA";
			break;
		case 0x7100:
			res = "CME";
			break;
		case 0x7080:
			res = "CIR";
			break;
		case 0x7040:
			res = "CIL";
			break;
		case 0x7020:
			res = "INC";
			break;
		case 0x7010:
			res = "SPA";
			break;
		case 0x7008:
			res = "SNA";
			break;
		case 0x7004:
			res = "SZA";
			break;
		case 0x7002:
			res = "SZE";
			break;
		case 0x7001:
			res = "HLT";
			break;
		case (short) 0xf800:
			res = "INP";
			break;
		case (short) 0xf400:
			res = "OUT";
			break;
		case (short) 0xf200:
			res = "SKI";
			break;
		case (short) 0xf100:
			res = "SKO";
			break;
		case (short) 0xf080:
			res = "ION";
			break;
		case (short) 0xf040:
			res = "IOF";
			break;
		default:
			short b = bin;
			boolean I = (b & 0x8000) != 0;
			short adr = (short) (b & 0x0fff);
			b &= 0x7000;
			switch (b) {
			case 0x0000:
				res = "AND";
				break;
			case 0x1000:
				res = "ADD";
				break;
			case 0x2000:
				res = "LDA";
				break;
			case 0x3000:
				res = "STA";
				break;
			case 0x4000:
				res = "BUN";
				break;
			case 0x5000:
				res = "BSA";
				break;
			case 0x6000:
				res = "ISZ";
				break;
			}
			if (res == null)
				return String.format("%04X", bin);
			res += " " + String.format("%04X", adr);
			if (I)
				res += " I";
		}
		return res == null ? String.format("%04X", bin) : res;
	}

	private static void err(int i, String msg) {
		throw new RuntimeException(String.format("%d: %s\n", i + 1, msg));
	}

	private static class Thing<A, B, C> {
		A a;
		B b;
		C c;

		Thing(A a, B b, C c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}
	}

}
