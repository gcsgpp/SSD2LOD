package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public enum EnumOperationsConditionBlock {
	EQUAL("=="), DIFFERENT("!="), LESSTHAN("<"), GREATERTHAN(">"), GREATERTHANEQUALTO(">="), LESSTHANEQUALTO("<=");
	
	private final String operation;
	EnumOperationsConditionBlock(String operation) {
		this.operation = operation;
	}
	
	public String getOperation(){
		return operation;
	}

}
