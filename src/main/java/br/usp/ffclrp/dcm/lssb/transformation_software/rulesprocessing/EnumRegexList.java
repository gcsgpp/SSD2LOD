package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public enum EnumRegexList {
	SELECTRULEID("(transformation_rule|condition_block)\\[\\d+"),
	SELECTSUBJECTCLASSNAME("(\".*\")\\s?="), //old: \\[\\d+.*\"\\s?
	SELECTSUBJECTLINE(":(\\s*?)\"(\\w|\\d)"),
	SELECTPREDICATESDIVISIONS("(:|,)\\s*?\"[^:,]*?\"\\s*="),
	SELECTPREDICATE("(.*?)="),
	SELECTCONTENTQUOTATIONMARK("(\"[^\"]*\")"),
	SELECTFIRSTNUMBERS("(\\d)+"),
	SELECTBASEIRIFLAG("\\/BASEIRI\\(\"(.*?)\"\\)"),
	SELECTNAMESPACEBASEIRIFLAG(",\\s*\"(.*)\""),
	SELECTFIXEDCONTENTFLAG("\\/FX\\(\"(.*?)\"\\)"),
	SELECTCONDITIONBLOCKFLAG("\\/CB\\(\\d*?\\)"),
	SELECTSEPARATORFLAG("\\/SP\\(\"(.*?)\"((,(.*?)\\))|\\))"),
	SELECTSEPARATORFLAGRANGENUMBERS("(\\d+\\s*:\\s*\\d+)"),
	SELECTCUSTOMDIDFLAG("\\/\\ID\\(\"(.*?)\"\\)"),
	//CONDITION BLOCK REGEX:
	SELECTCOLUMNCONDITIONBLOCK("(\".*\")\\s(=|!|>|<)"),
	SELECTPREDICATESDIVISIONSCONDITIONBLOCK("(:|,)(\\s*?)\"(.*?)\"\\s?(=|!|>|<)"),
	SELECTOPERATIONCONDITIONBLOCK("(==|!=|>|<)"),
	SELECTALL(".+");
	
	private final String regexExpressions;
	
	private EnumRegexList (String regexExpressions){
		this.regexExpressions = regexExpressions;
	}
	
	public String get(){
		return this.regexExpressions;
	}

}
