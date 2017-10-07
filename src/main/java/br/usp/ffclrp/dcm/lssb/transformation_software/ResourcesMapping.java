package br.usp.ffclrp.dcm.lssb.transformation_software;

import org.apache.jena.rdf.model.Resource;

public class ResourcesMapping {
	
	private Integer ruleNumber;
	private Integer lineNumber;
	private Resource tripleSubject;
	
	public ResourcesMapping(Integer ruleNumber, Integer lineNumber, Resource tripleSubject) {
		this.ruleNumber = ruleNumber;
		this.lineNumber = lineNumber;
		this.tripleSubject = tripleSubject;
	}

	public Integer getRuleNumber() {
		return ruleNumber;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}

	public Resource getTripleSubject() {
		return tripleSubject;
	}
	
	

}
