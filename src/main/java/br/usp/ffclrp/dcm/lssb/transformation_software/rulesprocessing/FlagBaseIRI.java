package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;


public class FlagBaseIRI extends Flag {

	String iri = null;
	String namespace = null;

	public FlagBaseIRI(String stringIRI, String namespace) {
		this.iri = stringIRI;
		this.namespace = namespace;
	}

	public String getIRI(){
		return this.iri;
	}

	public String getNamespace(){
		return this.namespace;
	}
}
