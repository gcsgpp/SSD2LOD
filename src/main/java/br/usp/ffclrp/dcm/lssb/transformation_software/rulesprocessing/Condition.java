package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class Condition {
	private String column; //If, later, we decide to accept flags this field may be changed to TSVColumn type.
	private Enum<EnumOperationsConditionBlock> operation;
	private String conditionValue;
	
	public Condition(String column, Enum<EnumOperationsConditionBlock> operation, String conditionValue){
		this.column = column;
		this.operation = operation;
		this.conditionValue = conditionValue;
	}
	
	public Condition(){
		
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public void setOperation(Enum<EnumOperationsConditionBlock> operation) {
		this.operation = operation;
	}

	public void setConditionValue(String conditionValue) {
		this.conditionValue = conditionValue;
	}

	public String getColumn() {
		return column;
	}

	public Enum<EnumOperationsConditionBlock> getOperation() {
		return operation;
	}

	public String getConditionValue() {
		return conditionValue;
	}
	
	
}
