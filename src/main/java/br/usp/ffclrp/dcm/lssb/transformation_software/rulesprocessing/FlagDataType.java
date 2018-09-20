package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import org.apache.jena.datatypes.xsd.XSDDatatype;

public class FlagDataType extends Flag {
	XSDDatatype datatype = null;
	Boolean literal = false;
	
	public FlagDataType (String type) {
		if(type.equals("literal"))
			this.literal = true;
		else
			this.datatype = new XSDDatatype(type);
	}

	public Object getDatatype() {

		if(literal)
			return "Literal";

		return this.datatype;
	}

}
