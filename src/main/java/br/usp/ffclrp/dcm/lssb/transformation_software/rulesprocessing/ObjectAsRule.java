package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.List;

public class ObjectAsRule {
	private Integer ruleNumber = null;
	private List<Flag> flags = null;
	
	public ObjectAsRule(Integer ruleNumber, List<Flag> flags) {
		this.ruleNumber = ruleNumber;
		this.flags = flags;
	}

	public Integer getRuleNumber() {
		return ruleNumber;
	}

	public List<Flag> getFlags() {
		return flags;
	}
}
