package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;

public class Rule {
	private String id;
	private OWLClass subject;
	private OWLProperty predicate;
	private List<TSVColumn> object;
	
	public Rule(){
		
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public OWLClass getSubject() {
		return subject;
	}

	public void setSubject(OWLClass subject) {
		this.subject = subject;
	}

	public OWLProperty getPredicate() {
		return predicate;
	}

	public void setPredicate(OWLProperty predicate) {
		this.predicate = predicate;
	}

	public List<TSVColumn> getObject() {
		return object;
	}

	public void setObject(List<TSVColumn> object) {
		this.object = object;
	}

	public List<Flag> getFlags() {
		return flags;
	}

	public void setFlags(List<Flag> flags) {
		this.flags = flags;
	}

	public Rule(String idP, OWLClass subjectP, OWLProperty predicateP, List<TSVColumn> objectP, List<Flag> flagsP){
		this.id = idP;
		this.subject = subjectP;
		this.predicate = predicateP;
		this.object = objectP;
		this.flags = flagsP;
	}

}
