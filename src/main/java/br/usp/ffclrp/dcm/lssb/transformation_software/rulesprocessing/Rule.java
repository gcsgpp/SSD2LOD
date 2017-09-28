package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;

public class Rule {
	private String id;
	private OWLClass subject;
	private OWLProperty predicate;
	private List<TSVColumn> object;
	private List<Flag> flags;
	
	public Rule(String idP, OWLClass subjectP, OWLProperty predicateP, List<TSVColumn> objectP, List<Flag> flagsP){
		this.id = idP;
		this.subject = subjectP;
		this.predicate = predicateP;
		this.object = objectP;
		this.flags = flagsP;
	}

}
