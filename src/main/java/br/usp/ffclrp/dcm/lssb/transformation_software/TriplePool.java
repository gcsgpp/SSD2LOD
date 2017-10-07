package br.usp.ffclrp.dcm.lssb.transformation_software;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class TriplePool {
	
	private Resource subject;
	private Property predicate;
	private Integer ruleNumber;
	private Integer lineNumber;
	
	public TriplePool(Resource subject, Property predicate, Integer ruleNumber, Integer lineNumber) {
		this.subject = subject;
		this.predicate = predicate;
		this.ruleNumber = ruleNumber;
		this.lineNumber = lineNumber;
	}

	public Resource getSubject() {
		return subject;
	}

	public Property getPredicate() {
		return predicate;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}

	public Integer getRuleNumber() {
		return ruleNumber;
	}
	

}
