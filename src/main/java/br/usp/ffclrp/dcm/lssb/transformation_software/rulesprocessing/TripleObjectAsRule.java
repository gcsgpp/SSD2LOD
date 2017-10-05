package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.ArrayList;
import java.util.List;

public class TripleObjectAsRule implements TripleObject {

	
	private List<Integer> ruleNumber = null;
	
	public TripleObjectAsRule() {
		
	}
	
	public TripleObjectAsRule(Integer ruleNumbers){
		this.ruleNumber = new ArrayList<Integer>();
		this.ruleNumber.add(ruleNumbers);
	}
	
	@Override
	public List<Integer> getObject() {
		return ruleNumber;
	}

}
