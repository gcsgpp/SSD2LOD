package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.List;

public class ObjectAsRule {
	private String ruleId = null;
	private List<Flag> flags = null;
	
	public ObjectAsRule(String ruleId, List<Flag> flags) {
		this.ruleId = ruleId;
		this.flags = flags;
	}

	public String getRuleId() {
		return ruleId;
	}

	public List<Flag> getFlags() {
		return flags;
	}
}
