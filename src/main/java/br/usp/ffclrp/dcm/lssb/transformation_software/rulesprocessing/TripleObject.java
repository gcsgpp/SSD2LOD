package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;

public class TripleObject {
	
	private OWLClass objectAsClass = null;
	private List<TSVColumn> objectAsTSVColumns = null;
	
	public TripleObject(OWLClass obj){
		this.objectAsClass = obj;
		this.objectAsTSVColumns = null;
	}
	
	public TripleObject(List<TSVColumn> obj){
		this.objectAsTSVColumns = obj;
		this.objectAsClass = null;
	}
	
	public Object getObject(){
		if (this.objectAsClass == null) return this.objectAsTSVColumns;
		else return this.objectAsClass;
	}

}
