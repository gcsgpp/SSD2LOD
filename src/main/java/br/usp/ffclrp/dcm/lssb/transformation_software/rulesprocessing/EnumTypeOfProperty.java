package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;


public enum EnumTypeOfProperty {
	
	OBJECTPROPERTY("OWLObjectProperty"),
	DATATYPEPROPERTY("OWLDataProperty");
	
	private final String typeOfProperty;
	
	private EnumTypeOfProperty(String typeOfProperty) {
		this.typeOfProperty = typeOfProperty;
	}
	
	public String getTypeOfProperty(){
		return typeOfProperty;
	}

}
