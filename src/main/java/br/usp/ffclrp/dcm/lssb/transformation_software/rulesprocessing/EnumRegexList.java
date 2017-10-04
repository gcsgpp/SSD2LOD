package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public enum EnumRegexList {
	SELECTRULEID("\\[\\d+"),
	SELECTSUBJECTCLASSNAME("\\[\\d+.*\"\\s?"),
	SELECTSUBJECTLINE(":(\\s*?)\""),
	SELECTPREDICATESDIVISIONS("(:|,)(\\s*?)\"(.*?)\"\\s?="),
	SELECTPREDICATE("(.*?)="),
	SELECTALLCONTENTBYQUOTATIONMARK("\"(.*?)\"");
	
	private final String regexExpressions;
	
	private EnumRegexList (String regexExpressions){
		this.regexExpressions = regexExpressions;
	}
	
	public String getExpression(){
		return this.regexExpressions;
	}

}
