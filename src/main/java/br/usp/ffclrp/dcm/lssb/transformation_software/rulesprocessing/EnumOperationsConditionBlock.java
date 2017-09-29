package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public enum EnumOperationsConditionBlock {
	EQUAL("="), DIFFERENT("!"), LESSTHAN("<"), GREATERTHAN(">");
	
	private final String operation;
	private EnumOperationsConditionBlock(String operation) {
		this.operation = operation;
	}
	
	public String getOperation(){
		return operation;
	}

}
