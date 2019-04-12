package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Condition {
	private TSVColumn column; //If, later, we decide to accept flags this field may be changed to TSVColumn type.
	private Enum<EnumOperationsConditionBlock> operation;
	private String conditionValue;
	private BigDecimal parsedValue;
	public Map<String, BigDecimal> parsedValues = new HashMap<>();
	
	public Condition(TSVColumn column, Enum<EnumOperationsConditionBlock> operation, String conditionValue){
		this.column = column;
		this.operation = operation;

		if(operation == EnumOperationsConditionBlock.GREATERTHAN || operation == EnumOperationsConditionBlock.LESSTHAN)
			this.parsedValue = new BigDecimal(conditionValue);
		else
			this.conditionValue = conditionValue;
	}

	public TSVColumn getColumn() {
		return column;
	}

	public Enum<EnumOperationsConditionBlock> getOperation() {
		return operation;
	}

	public String getConditionValue() {

		return conditionValue;
	}

	public BigDecimal getParsedValue() {

		return parsedValue;
	}
	
	public void setOperation(Enum<EnumOperationsConditionBlock> operationParam) {
		this.operation = operationParam;
	}
	
	
}
