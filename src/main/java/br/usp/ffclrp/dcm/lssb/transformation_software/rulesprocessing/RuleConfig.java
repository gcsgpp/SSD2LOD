package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.usp.ffclrp.dcm.lssb.transformation_software.App;
import br.usp.ffclrp.dcm.lssb.transformation_software.Utils;

public class RuleConfig {
	private String id = null;
	private Boolean matrix = null;
	private String defaultNS = null;
	
	public RuleConfig(String ID) {
		this.matrix = false;
		this.id = ID;
	}

	public RuleConfig(String ID, String defaultBaseIRI){
		this.matrix = false;
		this.id = ID;
		this.defaultNS = defaultBaseIRI;
	}
	
	public String getId() {
		return this.id;
	}
	
	public RuleConfig setMatrix(Boolean setting) {
		RuleConfig rule = new RuleConfig(this.id, this.defaultNS);
		rule.setMatrixProperty(setting);
		return rule;
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

	static private String extractRuleConfigIDFromSentence(String blockRulesAsText) {
		String data = "";

		Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTCONFIGRULEID.get(), blockRulesAsText);
		data = matcher.group().replace("rule_config[", "").trim();
		return data;
	}


	
	static private RuleConfig createRuleConfigFromString(String rcAsText) throws Exception {
		Matcher matcher 				=	Utils.matchRegexOnString(EnumRegexList.SELECTSUBJECTLINE.get(), rcAsText);
		String subjectLine 				=	Utils.splitByIndex(rcAsText, matcher.start())[0];
		String predicatesLinesOneBlock 	= 	Utils.splitByIndex(rcAsText, matcher.start())[1];

		String ruleConfigId 			= 	extractRuleConfigIDFromSentence(subjectLine);


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

			String lineFromBlock = predicatesLinesOneBlock.substring(initialOfEachMatch.get(i) + 1, // +1 exists to not include the first character, a comma
					finalChar);

			String column 	= Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTPREDICATE.get());
			lineFromBlock 	= Utils.removeRegexFromContent(EnumRegexList.SELECTPREDICATE.get(), lineFromBlock);
			String value 	= Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTALL.get());
			
			if(column.equals("default BaseIRI")) {
				try {
					rule.setDefaultBaseIRI(value);
				}catch(Exception e) {
					e.printStackTrace();
					throw new Exception("One of the values inside rule_config block was not possible to understand. Rule Config block ID: " + ruleConfigId + " . Value found: " + value);
				}
			}
		}

		return rule;
	}
	
	static public List<RuleConfig> extractRuleConfigFromString(String fileContent) throws Exception {
		List<String> ruleConfigListAsText = App.identifyConfigBlocksFromString(fileContent);
		List<RuleConfig> rcList = new ArrayList<>();

		for(String rc : ruleConfigListAsText){
			rcList.add(createRuleConfigFromString(rc));
		}

		return rcList;
	}
}
