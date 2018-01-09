package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public enum EnumRegexList {
	SELECTRULEID("(matrix_rule|condition_block|rule_config|simple_rule)\\[\\d+"),
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
	SELECTDATATYPEFLAG("\\/\\DT\\(\"(.*?)\"\\)"),
	//CONDITION BLOCK REGEX:
	SELECTCOLUMNCONDITIONBLOCK("(\".*\")\\s(=|!|>|<)"),
	SELECTPREDICATESDIVISIONSCONDITIONBLOCK("(:|,)(\\s*?)\"(.*?)\"\\s?(=|!|>|<)"),
	SELECTOPERATIONCONDITIONBLOCK("(==|!=|>|<)"),
	SELECTALL(".+");
	
	private final String regexExpressions;
	
	EnumRegexList(String regexExpressions){
		this.regexExpressions = regexExpressions;
	}
	
	public String get(){
		return this.regexExpressions;
	}

}
