package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.HashMap;
import java.util.Map;

public class Condition {
	private TSVColumn column; //If, later, we decide to accept flags this field may be changed to TSVColumn type.
	private Enum<EnumOperationsConditionBlock> operation;
	private String conditionValue;
	private double parsedValue;
	public Map<String, Double> parsedValues = new HashMap<>();
	
	public Condition(TSVColumn column, Enum<EnumOperationsConditionBlock> operation, String conditionValue){
		this.column = column;
		this.operation = operation;

		if(operation == EnumOperationsConditionBlock.GREATERTHAN || operation == EnumOperationsConditionBlock.LESSTHAN)
			this.parsedValue = Double.parseDouble(conditionValue);
		else
			this.conditionValue = conditionValue;
	}

	public TSVColumn getColumn() {
		return column;
	}

	public Enum<EnumOperationsConditionBlock> getOperation() {
		return operation;
	}

	public Object getConditionValue() {

		if(operation == EnumOperationsConditionBlock.GREATERTHAN || operation == EnumOperationsConditionBlock.LESSTHAN)
			return parsedValue;

		return conditionValue;
	}
	
	public void setOperation(Enum<EnumOperationsConditionBlock> operationParam) {
		this.operation = operationParam;
	}
	
	
}
