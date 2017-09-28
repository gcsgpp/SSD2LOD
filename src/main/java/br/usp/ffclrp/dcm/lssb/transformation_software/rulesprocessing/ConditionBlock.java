package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class ConditionBlock extends Flag {
	private String id;
	private TSVColumn column;
	private Enum<OperationsConditionBlock> operation;
	public ConditionBlock(String id, TSVColumn column, Enum<OperationsConditionBlock> operation){
		this.id = id;
		this.column = column;
		this.operation = operation;
	}

}
