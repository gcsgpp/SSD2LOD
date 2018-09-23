package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.ConditionBlockException;
import br.usp.ffclrp.dcm.lssb.transformation_software.RuleInterpretor;
import br.usp.ffclrp.dcm.lssb.transformation_software.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionBlock extends Flag {
	private String id;
	private List<Condition> conditions;
	
	public ConditionBlock(String id, List<Condition> conditions) {
		this.id = id;
		this.conditions = conditions;
	}
	
	public List<Condition> getConditions(){
		return this.conditions;
	}
	
	public String getId(){
		return this.id;
	}
	
	static private String extractConditionBlockIDFromSentence(String blockRulesAsText) {
		String data = "";

		Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTRULEID.get(), blockRulesAsText);
		data = matcher.group(2);
		//data = matcher.group().replace("condition_block[", "");
		return data;
	}
	
	static private List<String> identifyConditionBlocksFromString(String fileContent) {
		Pattern patternToFind = Pattern.compile(EnumRegexList.SELECTCONDITIONBLOCK.get());
		Matcher match = patternToFind.matcher(fileContent);

		List<String> identifiedCB = new ArrayList<String>();

		while(match.find()){
			identifiedCB.add(match.group());
		}
		return identifiedCB;
	}

	static private ConditionBlock createConditionBlockFromString(String cbAsText) throws Exception {
		//Matcher matcher 				=	Utils.matchRegexOnString(EnumRegexList.SELECTSUBJECTLINE.get(), cbAsText);
		//String subjectLine 				=	Utils.splitByIndex(cbAsText, matcher.start())[0];
		//String predicatesLinesOneBlock 	= 	Utils.splitByIndex(cbAsText, matcher.start())[1];

		String conditionBlockId 		= 	Utils.matchRegexOnString(EnumRegexList.SELECTCONDITIONID.get(), cbAsText).group(2);
		String predicatesLinesOneBlock 	=	Utils.matchRegexOnString(EnumRegexList.SELECTCONDITIONBODY.get(), cbAsText).group(2);


		List<Condition> conditions = new ArrayList<Condition>();
		Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTPREDICATESDIVISIONSCONDITIONBLOCK.get(), predicatesLinesOneBlock);
		while(!matcher.hitEnd()){
			String lineFromBlock = matcher.group();

			EnumOperationsConditionBlock operation = null;

			operation = retrieveOperation(lineFromBlock);


			TSVColumn column = new TSVColumn();
			column.setTitle(Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTCOLUMNCONDITIONBLOCK.get()));
			column.setFlags(new RuleInterpretor().extractFlagsFromSentence(lineFromBlock));

			lineFromBlock 	= Utils.removeRegexFromContent(EnumRegexList.SELECTCOLUMNCONDITIONBLOCK.get(), lineFromBlock);
			String value 	= Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTALL.get());
			conditions.add(new Condition(column, operation, value));
			matcher.find();
		}

		/*List<Integer> initialOfEachMatch = new ArrayList<Integer>();
		while(!matcher.hitEnd()){
			initialOfEachMatch.add(matcher.start());
			matcher.find();
		}


		List<Condition> conditions = new ArrayList<Condition>();
		for(int i = 0; i <= initialOfEachMatch.size()-1; i++){
			int finalChar;
			if(i == initialOfEachMatch.size()-1) //IF LAST MATCH, GET THE END OF THE SENTENCE
				finalChar = predicatesLinesOneBlock.length();
			else
				finalChar = initialOfEachMatch.get(i+1);

			String lineFromBlock = predicatesLinesOneBlock.substring(initialOfEachMatch.get(i) + 1, // +1 exists to not include the first character, a comma
					finalChar);

			EnumOperationsConditionBlock operation = null;
			try{
				operation = retrieveOperation(lineFromBlock);
			}catch(IllegalStateException e) {
				throw new ConditionBlockException("No valid condition operator identified in a condition block.");
			}

			TSVColumn column = new TSVColumn();
			column.setTitle(Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTCOLUMNCONDITIONBLOCK.get()));
			column.setFlags(new App().extractFlagsFromSentence(lineFromBlock));

			lineFromBlock 	= Utils.removeRegexFromContent(EnumRegexList.SELECTCOLUMNCONDITIONBLOCK.get(), lineFromBlock);
			String value 	= Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTALL.get());
			conditions.add(new Condition(column, operation, value));

		}*/

		if(conditions.size() == 0)
			throw new ConditionBlockException("No conditions found in the condition block " + conditionBlockId);

		return new ConditionBlock(conditionBlockId, conditions);
	}
	
	static private EnumOperationsConditionBlock retrieveOperation(String lineFromBlock) throws ConditionBlockException {
		String operation;
		operation = Utils.matchRegexOnString(EnumRegexList.SELECTOPERATIONCONDITIONBLOCK.get(), lineFromBlock).group();


		if(operation.equals(EnumOperationsConditionBlock.DIFFERENT.getOperation())) 	return EnumOperationsConditionBlock.DIFFERENT;
		else if(operation.equals(EnumOperationsConditionBlock.EQUAL.getOperation()))			return EnumOperationsConditionBlock.EQUAL;
		else if(operation.equals(EnumOperationsConditionBlock.GREATERTHAN.getOperation())) 	return EnumOperationsConditionBlock.GREATERTHAN;
		else if(operation.equals(EnumOperationsConditionBlock.LESSTHAN.getOperation()))		return EnumOperationsConditionBlock.LESSTHAN;
		else throw new ConditionBlockException("No valid condition operator identified in a condition block.");
	}
	
	static public List<ConditionBlock> extractConditionsBlocksFromString(String fileContent) throws Exception {
		List<String> conditionsBlocksListAsText = identifyConditionBlocksFromString(fileContent);
		List<ConditionBlock> cbList = new ArrayList<ConditionBlock>();

		for(String cb : conditionsBlocksListAsText){
			cbList.add(createConditionBlockFromString(cb));
		}

		return cbList;
	}

}
