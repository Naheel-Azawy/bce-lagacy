package assembler;

public class AssemblerAC extends AssemblerAbstract {

	@Override
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

	@Override
	protected boolean isIndirectSupported() {
		return true;
	}

}
