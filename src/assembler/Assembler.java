package assembler;

public class Assembler {

	public static AssemblerAbstract a;

	static {
		a = new AssemblerAC();
		a.init();
	}

	public static short[] assemble(String[] lines) {
		return intArrToShortArr(a.assemble(lines));
	}

	public static String disassemble(int bin) {
		return a.disassemble(bin);
	}

	private static short[] intArrToShortArr(int[] arr) {
		short[] res = new short[arr.length];
		for (int i = 0; i < arr.length; ++i)
			res[i] = (short) arr[i];
		return res;
	}

}
