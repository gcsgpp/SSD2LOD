package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;

public class Rule {
	private String id;
	private OWLClass subject;
	private List<TSVColumn> subjectTSVColumns = null;
	private Map<OWLProperty, TripleObject> predicateObjects;
	
	public Rule(){
		
	}

	public Rule(String id, OWLClass subject, List<TSVColumn> subjectTSVColumns, Map<OWLProperty, TripleObject> predicateObjects){
		this.id = id;
		this.subject = subject;
		this.subjectTSVColumns = subjectTSVColumns;
		this.predicateObjects = predicateObjects;
	}

	public String getId() {
		return id;
	}

	public OWLClass getSubject() {
		return subject;
	}
	
	public List<TSVColumn> getSubjectTSVColumns(){
		return subjectTSVColumns;
	}

	public Map<OWLProperty, TripleObject> getPredicateObjects() {
		return predicateObjects;
	}
	
	

}
