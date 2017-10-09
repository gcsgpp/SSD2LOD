package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public enum EnumRegexList {
	SELECTRULEID("(transformation_rule|condition_block)\\[\\d+"),
	SELECTSUBJECTCLASSNAME("(\".*\")\\s?="), //old: \\[\\d+.*\"\\s?
	SELECTSUBJECTLINE(":(\\s*?)\"(\\w|\\d)"),
	SELECTPREDICATESDIVISIONS("(:|,)(\\s*?)\"(.*?)\"\\s?="),
	SELECTPREDICATE("(.*?)="),
	SELECTCONTENTQUOTATIONMARK("(\"[^\"]*\")"),
	SELECTFIRSTNUMBERS("(\\d)+"),
	SELECTBASEIRIFLAG("\\/BASEIRI\\(\"(.*?)\"\\)"),
	SELECTNAMESPACEBASEIRIFLAG(",\\s*\"(.*)\""),
	SELECTFIXEDCONTENTFLAG("\\/\\!\\(\"(.*?)\"\\)"),
	SELECTCONDITIONBLOCKFLAG("\\/CB\\(\\d*?\\)"),
	SELECTSEPARATORFLAG("\\/SP\\(\"(.*?)\"((,(.*?)\\))|\\))"),
	//CONDITION BLOCK REGEX:
	SELECTCOLUMNCONDITIONBLOCK("(\".*\")\\s(=|!|>|<)"),
	SELECTPREDICATESDIVISIONSCONDITIONBLOCK("(:|,)(\\s*?)\"(.*?)\"\\s?(=|!|>|<)"),
	SELECTOPERATIONCONDITIONBLOCK("(=|!|>|<)"),
	SELECTALL(".+");
	
	private final String regexExpressions;
	
	private EnumRegexList (String regexExpressions){
		this.regexExpressions = regexExpressions;
	}
	
	public String getExpression(){
		return this.regexExpressions;
	}

}
