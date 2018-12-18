package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

public enum EnumRegexList {
	SELECTELEMENTSBLOCKS("((row_based_rule|column_based_rule)\\s*\\w+\\s*\\[.+?\\]\\s*\\{(.+?)?\\})|(search_element\\s?\\w+\\s?\\[.+?\\]\\s*\\{\\$.+?\\$\\})|(config_element\\s*\\{.+?\\})"),
	SELECTBLOCKBODY("\\{(.+?)?\\}"),

	SELECTRULEID("(column_based_rule|row_based_rule|condition_element)\\s*(\\w+)"),
	SELECTCONFIGRULEID("(?:config_element\\s*\\{)"),
	SELECTCONFIGPKEYS("\".+?\"\\s*="),

	SELECTSEARCHBLOCKID("(?:search_element\\s*\\w+\\s*{.*})"),
	SELECTSEARCHID("(search_element\\s*)(\\w+)"),
	SELECTSEARCHBODY("search_element\\s*\\w+\\s*\\[\\\"(.*)\\\"\\]\\{\\$(.*)\\$\\}"),
	SELECTSEARCHPREDICATESDIVISIONS("((\\{|,)?\\s*?\"[^:,]*?\"\\s*=)"),


	SELECTCONDITIONBLOCK("condition_element\\s?\\w+\\s?\\{.+?\\}"),
	SELECTCONDITIONID("(condition_element\\s*)(\\w+)"),
	SELECTCONDITIONBODY("(condition_element\\s*\\w+)\\s*(\\{.+?\\})"),
	SELECTPREDICATESDIVISIONSCONDITIONBLOCK("((\\{|,)(\\s*?)\"[^\"]*?\"(\\s*?)(\\/COL\\(\\s*?((\\d*?)|(\\\"[^\"]*?\\\"))\\s*?,\\s*?\".*?\"\\s*?\\))?(\\s*?)((==)|(!=)|>|<|<=|>=)(\\s*?)\"(.*?)\")+?"),


	SELECTSUBJECTCLASSNAME("is_equivalent_to\\s*(\".+?\")"),
	SELECTSUBJECTLINE("\\[.+?\\]\\{"),
	//SELECTPREDICATESDIVISIONS("((\\{|,)\\s*?\"[^:,]*?\"\\s*=)"),
	SELECTPREDICATESDIVISIONS("(\\{|,)\\s*"),
	SELECTRULEPREDICATESDIVISIONS("(\\{|,)\\s*links_to"),
	SELECTPREDICATE("using\\s*\".+?\""),
	SELECTCONTENTQUOTATIONMARK("(\".+?\")"),
	SELECTRULEIDFROMPREDICATE("links_to\\s*(\\w+)"),
	SELECTBASEIRIFLAG("(\\/BASEIRI\\(\"(.*?)\"\\))"),
	SELECTNAMESPACEBASEIRIFLAG("(,\\s*\"(.*)\")"),
	SELECTFIXEDCONTENTFLAG("(\\/DefaultValue\\(\"(.*?)\"\\))"),
	SELECTCONDITIONBLOCKFLAG("\\/CE\\(\\w+\\)"),
	SELECTCOLFLAG("\\/COL\\(\\s*?((\\d*?)|(\\\"[^\"]*?\\\"))\\s*?,\\s*?\".*?\"\\s*?\\)"),
	SELECTSEARCHBLOCKFLAG("\\/SE\\((\\w*?),\\s*?(\\?\\w*?)\\)"),
	SELECTSEPARATORFLAG("(\\/SP\\(\"(.*?)\"((,(.*?)\\))|\\)))"),
	SELECTSEPARATORFLAGRANGENUMBERS("((\\d+\\s*:\\s*\\d+))"),
	SELECTCUSTOMDIDFLAG("(\\/ID\\(\"(.*?)\"\\))"),
	SELECTDATATYPEFLAG("(\\/DT\\(\"(.*?)\"\\))"),
	SELECTNOTMETADATA("(\\/(NM)(?:\\W|\\s))"),
	SELECTNODEFLAG("\\/NODE\\(\\\"(.*?)\\\"\\)"),
	//CONDITION BLOCK REGEX:
	SELECTCOLUMNCONDITIONBLOCK("((\".*\")\\s*?(\\/COL\\(\\s*?((\\d*?)|(\\\"[^\"]*?\\\"))\\s*?,\\s*?\".*?\"\\s*?\\))?\\s*?(==|!=|>|<))"),
	//SELECTPREDICATESDIVISIONSSEARCHBLOCK("((:|,)(\\s*?)\"(.*?)\"\\s?(=|!|>|<))"),
	SELECTOPERATIONCONDITIONBLOCK("(==|!=|>|<)"),
	SELECTALL("(.+)");
	
	private final String regexExpressions;
	
	EnumRegexList(String regexExpressions){
		this.regexExpressions = regexExpressions;
	}
	
	public String get(){
		return this.regexExpressions;
	}

}
