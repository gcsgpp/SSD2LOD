package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import org.apache.jena.datatypes.xsd.XSDDatatype;

public class FlagDataType extends Flag {
	XSDDatatype datatype = null;
	
	public FlagDataType (String type) {
		
		this.datatype = new XSDDatatype(type);
	}
	
	public XSDDatatype getDatatype() {
		return this.datatype;
	}

}
