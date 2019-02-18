package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import br.usp.ffclrp.dcm.lssb.transformation_software.RuleInterpretor;
import br.usp.ffclrp.dcm.lssb.transformation_software.Utils;
import org.apache.jena.riot.Lang;

import javax.rmi.CORBA.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class RuleConfig {
	private String id = null;
	private Boolean matrix = null;
	private String defaultNS = null;
	private Boolean header = null;
	private Lang syntax = null;
	private Map<String, String> namespaces = null;
//	private String SSDFileType = null;
	
	public RuleConfig(String ID) {
		this.matrix = false;
		this.id = ID;
		this.header = true;
//		this.SSDFileType = "TSV";
		this.namespaces = new HashMap<String, String>();
	}

	public RuleConfig(String ID, String defaultBaseIRI){
		this.matrix = false;
		this.id = ID;
		this.defaultNS = defaultBaseIRI;
		this.header = true;
//		this.SSDFileType = "TSV";
		this.namespaces = new HashMap<String, String>();
	}

	public RuleConfig(RuleConfig rule) {
		this.id = rule.getId();
		this.matrix = rule.getMatrix();
		this.defaultNS = rule.getDefaultBaseIRI();
		this.header = rule.getHeader();
		this.syntax = rule.getSyntax();
//		this.SSDFileType = "TSV";
		this.namespaces = rule.getNamespace();
	}
	
	public String getId() {
		return this.id;
	}
	
	public RuleConfig setMatrix(Boolean setting) {
//		RuleConfig rule = new RuleConfig(this.id, this.defaultNS);
		this.setMatrixProperty(setting);
		return this;
	}

	private void setMatrixProperty(Boolean setting) {
		this.matrix = setting;
	}

	public Boolean getMatrix() {
		return this.matrix;
	}

	public void setDefaultBaseIRI(String defaultBaseIRI) {
		this.defaultNS = defaultBaseIRI;
	}

	public String getDefaultBaseIRI() {
		return this.defaultNS;
	}

	public Lang getSyntax() { return syntax; }

	public void setSyntax(Lang syntax) { this.syntax = syntax;	}

	private static String extractRuleConfigIDFromSentence(String blockRulesAsText) {
		String data = "";

		Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTCONFIGRULEID.get(), blockRulesAsText);
		data = matcher.group().replace("rule_config[", "").trim();
		return data;
	}

	void setHeader(Boolean hasHeader){
		this.header = hasHeader;
	}

	Boolean getHeader(){ return this.header;	}

//	public void setSSDFileType(String fileType){ this.SSDFileType = fileType; }

//	public String getSSDFileType(){ return this.SSDFileType;	}

	private static RuleConfig createRuleConfigFromString(String rcAsText) throws Exception {
		Matcher matcher 				=	Utils.matchRegexOnString(EnumRegexList.SELECTBLOCKBODY.get(), rcAsText);
		String predicatesLinesOneBlock 	= 	matcher.group();

		String ruleConfigId 			= 	"default";


		matcher = Utils.matchRegexOnString(EnumRegexList.SELECTPREDICATESDIVISIONS.get(), predicatesLinesOneBlock);
		List<Integer> initialOfEachMatch = new ArrayList<Integer>();
		while(!matcher.hitEnd()){
			initialOfEachMatch.add(matcher.start());
			matcher.find();
		}


		RuleConfig rule = new RuleConfig(ruleConfigId);
		for(int i = 0; i <= initialOfEachMatch.size()-1; i++){
			int finalChar;
			if(i == initialOfEachMatch.size()-1) //IF LAST MATCH, GET THE END OF THE SENTENCE
				finalChar = predicatesLinesOneBlock.length();
			else
				finalChar = initialOfEachMatch.get(i+1);

			String lineFromBlock = predicatesLinesOneBlock.substring(initialOfEachMatch.get(i), // +1 exists to not include the first character, a comma
					finalChar);

			String column 	= Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTCONFIGPKEYS.get());
			lineFromBlock 	= Utils.removeRegexFromContent(EnumRegexList.SELECTCONFIGPKEYS.get(), lineFromBlock);
			String value 	= null;
			if(column.toLowerCase().equals("namespace") == false)
				value 		= Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTALL.get());
			
			if(column.toLowerCase().equals("default_baseiri")) {
				try {
					rule.setDefaultBaseIRI(value);
				}catch(Exception e) {
					e.printStackTrace();
					throw new Exception("One of the values inside rule_config block was not possible to understand. Rule Config block ID: " + ruleConfigId + " . Value found: " + value);
				}
			}else if(column.toLowerCase().equals("has_non_processable_header")) {
				try {
					rule.setHeader(Boolean.parseBoolean(value));
				}catch(Exception e) {
					e.printStackTrace();
					throw new Exception("One of the values inside rule_config block was not possible to understand. Rule Config block ID: " + ruleConfigId + " . Value found: " + value);
				}
			}else if(ruleConfigId.toLowerCase().equals("default") && column.toLowerCase().equals("export_syntax")) {
				if(		Lang.RDFXML	.getName().toLowerCase().equals(value.toLowerCase()))
					rule.setSyntax(Lang.RDFXML);
				else if(Lang.N3		.getName().toLowerCase().equals(value.toLowerCase()))
					rule.setSyntax(Lang.N3);
				else if(Lang.NTRIPLES.getName().toLowerCase().equals(value.toLowerCase()))
					rule.setSyntax(Lang.NTRIPLES);
				else if(Lang.TURTLE	.getName().toLowerCase().equals(value.toLowerCase()))
					rule.setSyntax(Lang.TURTLE);
//			}else if(column.toLowerCase().equals("ssd_filetype")) {
//				if(value.toLowerCase().trim().equals("tsv"))
//					rule.setSSDFileType("TSV");
//				else if(value.toLowerCase().trim().equals("csv"))
//					rule.setSSDFileType("CSV");
			}else if(column.toLowerCase().equals("namespace")){
				Map<String, String> namespaces = extractNamespaceFromString(lineFromBlock);
				rule.setNamespace(namespaces.get("key"), namespaces.get("value"));
			}
		}

		if(ruleConfigId.toLowerCase().equals("default") && rule.getSyntax() == null)
			rule.setSyntax(Lang.RDFXML);

		return rule;
	}

	private static Map<String, String> extractNamespaceFromString(String lineFromBlock) {
		String ns = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTALL.get());
		lineFromBlock = Utils.removeDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTALL.get());
		String url = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTALL.get());

		Map<String, String> namespace = new HashMap<>();
		namespace.put("key", ns);
		namespace.put("value", url);

		return namespace;

	}

	static public List<RuleConfig> extractRuleConfigFromString(String fileContent) throws Exception {
		List<String> ruleConfigListAsText = RuleInterpretor.identifyConfigBlocksFromString(fileContent);
		List<RuleConfig> rcList = new ArrayList<>();

		for(String rc : ruleConfigListAsText){
			rcList.add(createRuleConfigFromString(rc));
		}

		return rcList;
	}

	static public RuleConfig getDefaultRuleConfigFromRuleList(List<Rule> ruleList) throws Exception {
		for(Rule rule : ruleList){
			if( rule.getConfig().getId().equals("default"));
				return rule.getConfig();
		}

		throw new Exception("There is no default rule configuration component.");
	}

	public void setNamespace(String key, String value)
	{
		this.namespaces.put(key, value);
	}

	public Map<String, String> getNamespace()
	{
		return this.namespaces;
	}
}
