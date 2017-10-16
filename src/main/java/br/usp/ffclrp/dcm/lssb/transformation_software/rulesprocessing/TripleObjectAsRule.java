package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.ArrayList;
import java.util.List;

public class TripleObjectAsRule implements TripleObject {
	
	private List<ObjectAsRule> object;
	
	public TripleObjectAsRule() {
		
	}
	
	public TripleObjectAsRule(ObjectAsRule object){
		this.object = new ArrayList<ObjectAsRule>();
		this.object.add(object);
	}
	
	@Override
	public List<ObjectAsRule> getObject() {
		return this.object;
	}

}
