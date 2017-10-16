package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.List;

public class ConditionBlock extends Flag {
	private Integer id;
	private List<Condition> conditions;
	
	public ConditionBlock(String id, List<Condition> conditions) {
		this.id = Integer.parseInt(id);
		this.conditions = conditions;
	}
	
	public List<Condition> getConditions(){
		return this.conditions;
	}
	
	public Integer getId(){
		return this.id;
	}

}
