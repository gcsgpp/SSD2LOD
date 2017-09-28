package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public enum OperationsConditionBlock {
	EQUAL("="), DIFFERENT("!"), LESSTHAN("<"), GREATERTHAN(">");
	
	private final String operation;
	private OperationsConditionBlock(String operation) {
		this.operation = operation;
	}
	
	public String getOperation(){
		return operation;
	}

}
