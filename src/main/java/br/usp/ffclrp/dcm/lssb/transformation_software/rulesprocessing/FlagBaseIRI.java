package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;


public class FlagBaseIRI extends Flag {
	
	String iri = null;
	
	public FlagBaseIRI() {
		
	}
	
	public FlagBaseIRI(String stringIRI) {
		this.iri = stringIRI; 
	}
	
	public String getIRI(){
		return this.iri;
	}

}
