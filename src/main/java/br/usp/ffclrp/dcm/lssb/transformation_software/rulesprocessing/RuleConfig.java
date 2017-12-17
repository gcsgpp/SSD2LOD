package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.usp.ffclrp.dcm.lssb.transformation_software.Utils;

public class RuleConfig {
	private String id = null;
	private Boolean matrix = null;
	
	public RuleConfig(String ID) {
		this.matrix = false;
		this.id = ID;
	}
	
	public String getId() {
		return this.id;
	}
	
	public void setMatrix(Boolean setting) {
		this.matrix = setting;
	}

	public Boolean getMatrix() {
		return this.matrix;
	}

	
	static private String extractRuleConfigIDFromSentence(String blockRulesAsText) {
		String data = "";

		Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTRULEID.get(), blockRulesAsText);
		data = matcher.group().replace("rule_config[", "");
		return data;
	}

	static private List<String> identifyRuleConfigBlocksFromString(String fileContent) {
		Pattern patternToFind = Pattern.compile("rule_config\\[(.*?)\\]");
		Matcher match = patternToFind.matcher(fileContent);

		List<String> identifiedRC = new ArrayList<String>();

		while(match.find()){
			identifiedRC.add(match.group());
		}
		return identifiedRC;
	}
	
	static private RuleConfig createRuleConfigFromString(String cbAsText) throws Exception {
		Matcher matcher 				=	Utils.matchRegexOnString(EnumRegexList.SELECTSUBJECTLINE.get(), cbAsText);
		String subjectLine 				=	Utils.splitByIndex(cbAsText, matcher.start())[0];
		String predicatesLinesOneBlock 	= 	Utils.splitByIndex(cbAsText, matcher.start())[1];

		String ruleConfigId 		= 	extractRuleConfigIDFromSentence(subjectLine);


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
			
			if(column.equals("matrix")) {
				try {
					rule.setMatrix(Boolean.parseBoolean(value));
				}catch(Exception e) {
					e.printStackTrace();
					throw new Exception("One of the values inside rule_config block was not possible to understand. Rule Config block ID: " + ruleConfigId + " . Value found: " + value);
				}
			}
		}

		return rule;
	}
	
	static public List<RuleConfig> extractRuleConfigFromString(String fileContent) throws Exception {
		List<String> ruleConfigListAsText = identifyRuleConfigBlocksFromString(fileContent);
		List<RuleConfig> rcList = new ArrayList<RuleConfig>();

		for(String rc : ruleConfigListAsText){
			rcList.add(createRuleConfigFromString(rc));
		}

		return rcList;
	}
}
