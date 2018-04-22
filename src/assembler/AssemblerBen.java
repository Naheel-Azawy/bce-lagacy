package assembler;

public class AssemblerBen extends AssemblerAbstract {

	@Override
	protected void createInstructions() {
		createInstruction("NOP", 0x00);
		createMRInstruction("LDA", 0x10);
		createMRInstruction("ADD", 0x20);
		createMRInstruction("SUB", 0x30);
		createMRInstruction("STA", 0x40);
		createMRInstruction("LDI", 0x50);
		createMRInstruction("JMP", 0x60);
		createMRInstruction("JC", 0x70);
		createMRInstruction("JZ", 0x80);
		createInstruction("OUT", 0xE0);
		createInstruction("HLT", 0xF0);
	}

	@Override
	protected boolean isIndirectSupported() {
		return false;
	}

}
