package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public class FlagConditionBlock extends Flag {
	private Integer id;
	
	public FlagConditionBlock() {
	}
	public FlagConditionBlock(Integer id) {
		this.id = id;
	}
	
	public Integer getId(){
		return this.id;
	}
}
