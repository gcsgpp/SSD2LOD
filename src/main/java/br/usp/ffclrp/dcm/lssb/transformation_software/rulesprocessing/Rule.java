package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.jena.rdf.model.Resource;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rule {
	private String id;
	private RuleConfig config;
	private OWLClass subject;
	private List<TSVColumn> subjectTSVColumns = null;
	private MultiValuedMap<OWLProperty, TripleObject> predicateObjects;
	public Map<Integer, List<Resource>> processedLines = new HashMap<>();

	public Rule(String id, RuleConfig ruleConfig, OWLClass subject, List<TSVColumn> subjectTSVColumns, MultiValuedMap<OWLProperty, TripleObject> predicateObjects){
		this.id = id;
		this.config = ruleConfig;
		this.subject = subject;
		this.subjectTSVColumns = subjectTSVColumns;
		this.predicateObjects = predicateObjects;
	}

	public String getId() {
		return this.id;
	}

	public OWLClass getSubject() {
		return this.subject;
	}
	
	public List<TSVColumn> getSubjectTSVColumns(){
		return this.subjectTSVColumns;
	}

	public MultiValuedMap<OWLProperty, TripleObject> getPredicateObjects() {
		return this.predicateObjects;
	}
	
	public Boolean isMatrix() {
		return config.getMatrix();
	}

	public String getDefaultBaseIRI() {
		return config.getDefaultBaseIRI();
	}

	public Integer getStartLineNumber(){
		if(this.config.getHeader())
			return 1; //ignore the header and start processing at the row 1;
		else
			return 0;
	}

	public RuleConfig getConfig() { return this.config;	}
}
