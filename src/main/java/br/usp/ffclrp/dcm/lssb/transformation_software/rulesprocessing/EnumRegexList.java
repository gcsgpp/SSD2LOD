package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public enum EnumRegexList {
	SELECTRULEID("\\[\\d+"),
	SELECTSUBJECTCLASSNAME("(\".*\")\\s?="), //old: \\[\\d+.*\"\\s?
	SELECTSUBJECTLINE(":(\\s*?)\""),
	SELECTPREDICATESDIVISIONS("(:|,)(\\s*?)\"(.*?)\"\\s?="),
	SELECTPREDICATE("(.*?)="),
	SELECTCONTENTQUOTATIONMARK("(\"[^\"]*\"){1}?"),
	SELECTFIRSTNUMBERS("(\\d)+"),
	SELECTBASEIRIFLAG("\\/BASEIRI\\(\"(.*?)\"\\)"),
	SELECTFIXEDCONTENTFLAG("\\/\\!\\(\"(.*?)\"\\)"),
	SELECTCONDITIONBLOCKFLAG("\\/CB\\(\\d*?\\)"),
	SELECTSEPARATORFLAG("\\/SP\\(\"(.*?)\"((,(.*?)\\))|\\))"),
	//CONDITION BLOCK REGEX:
	SELECTCOLUMNCONDITIONBLOCK("(\".*\")\\s(=|!|>|<)"),
	SELECTPREDICATESDIVISIONSCONDITIONBLOCK("(:|,)(\\s*?)\"(.*?)\"\\s?(=|!|>|<)"),
	SELECTOPERATIONCONDITIONBLOCK("(=|!|>|<)");
	
	private final String regexExpressions;
	
	private EnumRegexList (String regexExpressions){
		this.regexExpressions = regexExpressions;
	}
	
	public String getExpression(){
		return this.regexExpressions;
	}

}
