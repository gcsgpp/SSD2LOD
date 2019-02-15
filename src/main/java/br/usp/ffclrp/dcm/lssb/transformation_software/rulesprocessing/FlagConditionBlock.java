package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class FlagConditionBlock extends Flag {
	private String id;
	
	public FlagConditionBlock(String id) {
		this.id = id;
	}
	
	public String getId(){
		return this.id;
	}
}
