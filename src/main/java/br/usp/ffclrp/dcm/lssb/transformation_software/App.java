package br.usp.ffclrp.dcm.lssb.transformation_software;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;

import com.fasterxml.jackson.databind.util.EnumResolver;

import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Condition;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ContentDirectionTSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumContentDirectionTSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumOperationsConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumRegexList;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FixedContent;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Flag;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagBaseIRI;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.NotMetadata;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Rule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Separator;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObject;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObjectAsColumns;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObjectAsRule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObjectBuilder;

/**
 * Hello world!
 *
 */
public class App
{
	private OntologyHelper ontologyHelper;
	List<Rule> rulesList;
	List<ConditionBlock> conditionsBlocks;
	public static void main( String[] args ) throws Exception
	{
		System.out.println( "Hello World!" );

		App app = new App();
		app.extractRulesFromFile("c:\\masterSoftwareTestFiles\\testingrules.txt", "enchimentdata.owl");
		
		TriplesProcessing triplesProcessing = new TriplesProcessing("c:\\masterSoftwareTestFiles\\enrichedData.tsv");
		triplesProcessing.createTriplesFromRules(app.rulesList, app.conditionsBlocks, "http://purl.org/gabriel/test/");
	}

	public void extractRulesFromFile(String rulesRelativePath, String ontologyRelativePath){
		ontologyHelper = new OntologyHelper();
		ontologyHelper.loadingOntologyFromFile(ontologyRelativePath);
		String fileContent = readFile(rulesRelativePath);
		
		fileContent = fileContent.replaceAll("\n", "").replaceAll("\t", "");
		rulesList = extractRulesFromString(fileContent);
		conditionsBlocks = extractConditionsBlocksFromString(fileContent);

		//printRules(rulesList);
	}

	private List<ConditionBlock> extractConditionsBlocksFromString(String fileContent) {
		List<String> conditionsBlocksListAsText = identifyConditionBlocksFromString(fileContent);
		List<ConditionBlock> cbList = new ArrayList<ConditionBlock>();

		for(String cb : conditionsBlocksListAsText){
			cbList.add(createCBFromString(cb));
		}

		return cbList;
	}

	private ConditionBlock createCBFromString(String cbAsText) {
		Matcher matcher = matchRegexOnString(EnumRegexList.SELECTSUBJECTLINE.getExpression(), cbAsText);
		String subjectLine 				=	splitByIndex(cbAsText, matcher.start())[0];
		String predicatesLinesOneBlock 	= 	splitByIndex(cbAsText, matcher.start())[1];
		
		String conditionBlockId 	= extractIDFromSentence(subjectLine);
		
		
		matcher = matchRegexOnString(EnumRegexList.SELECTPREDICATESDIVISIONSCONDITIONBLOCK.getExpression(), predicatesLinesOneBlock);
		List<Integer> initialOfEachMatch = new ArrayList<Integer>();
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
			
			EnumOperationsConditionBlock operation = retrieveOperation(lineFromBlock);
			
			String column = extractDataFromFirstQuotationMarkInsideRegex(lineFromBlock, EnumRegexList.SELECTCOLUMNCONDITIONBLOCK.getExpression());
			lineFromBlock = removeRegexFromContent(EnumRegexList.SELECTCOLUMNCONDITIONBLOCK.getExpression(), lineFromBlock);
			String value = extractDataFromFirstQuotationMarkInsideRegex(lineFromBlock, EnumRegexList.SELECTCOLUMNCONDITIONBLOCK.getExpression());
			conditions.add(new Condition(column, operation, value));
		}
		
		return new ConditionBlock(conditionBlockId, conditions);
	}

	private EnumOperationsConditionBlock retrieveOperation(String lineFromBlock) {
		String operation = matchRegexOnString(EnumRegexList.SELECTOPERATIONCONDITIONBLOCK.getExpression(), lineFromBlock).group();
		
		if(operation == EnumOperationsConditionBlock.DIFFERENT.getOperation()) 		return EnumOperationsConditionBlock.DIFFERENT;
		if(operation == EnumOperationsConditionBlock.EQUAL.getOperation())			return EnumOperationsConditionBlock.EQUAL;
		if(operation == EnumOperationsConditionBlock.GREATERTHAN.getOperation()) 	return EnumOperationsConditionBlock.GREATERTHAN;
		if(operation == EnumOperationsConditionBlock.LESSTHAN.getOperation())		return EnumOperationsConditionBlock.LESSTHAN;
		return null;
	}

	private void printRules(List<Rule> rulesList) {
		for(Rule r : rulesList){
			System.out.println("** Rule: **\n");
			String out = "ID: " + r.getId() + "\t\t" + "Subject: " + r.getSubject().getIRI() + "\t\t";
			r.getPredicateObjects().forEach( (key, value) -> { printSysoutRules(out, key, value); });

			if(r.getPredicateObjects().size() == 0)
				System.out.println(out);
		}
	}

	private void printSysoutRules(String out, OWLProperty key, TripleObject value) {
		out += "Predicate: " + key + "\t\t\t";
		String outAddedContent = "";
		if(value instanceof TripleObjectAsColumns){
			@SuppressWarnings("unchecked")
			List<TSVColumn> column = (List<TSVColumn>) value.getObject();
			for(TSVColumn c : column){
				outAddedContent += out +  "Object: " + c.getTitle() + "\n";
			}
		}else{
			@SuppressWarnings("unchecked")
			List<Integer> rules = (List<Integer>) value.getObject();

			for(Integer r : rules){
				outAddedContent += out + "Rule: " + r + "\n";
			}
		}
		System.out.println(outAddedContent);
	}
	
	private List<Rule> extractRulesFromString(String fileContent) {

		List<String> rulesListAsText = identifyRulesBlocksFromString(fileContent);		

		List<Rule> ruleList = new ArrayList<Rule>();

		for(String s : rulesListAsText){
			ruleList.add(createRulesFromBlock(s));
		}

		return ruleList;
	}

	private String[] splitByIndex(String content, int index){

		String[] splitContent = new String[2];
		splitContent[0] = content.substring(0, index);
		splitContent[1] = content.substring(index, content.length()); // index not included

		return splitContent;
	}

	private Rule createRulesFromBlock(String blockRulesAsText) {
		Matcher matcher = matchRegexOnString(EnumRegexList.SELECTSUBJECTLINE.getExpression(), blockRulesAsText);
		if(matcher.hitEnd()){
			return exctactRuleFromOneLineRuleBlock(blockRulesAsText);
		}
		String subjectLine 				=	splitByIndex(blockRulesAsText, matcher.start())[0];
		String predicatesLinesOneBlock 	= 	splitByIndex(blockRulesAsText, matcher.start())[1];

		String ruleId 						= extractIDFromSentence(subjectLine);
		OWLClass ruleSubject 				= extractSubjectFromSentence(subjectLine);
		subjectLine = removeRegexFromContent(EnumRegexList.SELECTSUBJECTCLASSNAME.getExpression(), subjectLine);
		List<TSVColumn> subjectTsvcolumns 	= extractTSVColumnsFromSentence(subjectLine);

		matcher = matchRegexOnString(EnumRegexList.SELECTPREDICATESDIVISIONS.getExpression(), predicatesLinesOneBlock);
		List<Integer> initialOfEachMatch = new ArrayList<Integer>();
		while(!matcher.hitEnd()){
			initialOfEachMatch.add(matcher.start());
			matcher.find();
		}

		Map<OWLProperty, TripleObject> predicateObjects = new Hashtable<OWLProperty, TripleObject>();
		for(int i = 0; i <= initialOfEachMatch.size()-1; i++){
			int finalChar;
			if(i == initialOfEachMatch.size()-1) //IF LAST MATCH, GET THE END OF THE SENTENCE
				finalChar = predicatesLinesOneBlock.length();
			else
				finalChar = initialOfEachMatch.get(i+1);

			String lineFromBlock = predicatesLinesOneBlock.substring(initialOfEachMatch.get(i) + 1, // +1 exists to not include the first character, a comma
					finalChar);
			OWLProperty propertyFromLine = extractPredicateFromBlockLine(lineFromBlock);
			lineFromBlock = removePredicateFromBlockLine(lineFromBlock);

			if(tripleObjectIsToAnotherRule(lineFromBlock)){

				Integer ruleNumber = extractRuleNumberAsTripleObject(lineFromBlock);

				if(predicateObjects.containsKey(propertyFromLine)){
					@SuppressWarnings("unchecked")
					List<Integer> ruleObject = (List<Integer>) predicateObjects.get(propertyFromLine).getObject();
					ruleObject.add(ruleNumber);
				}else{
					TripleObjectAsRule ruleObject = (TripleObjectAsRule) TripleObjectBuilder.createObjectToRule(ruleNumber);
					predicateObjects.put(propertyFromLine, ruleObject);
				}

			}else{

				List<TSVColumn> tsvcolumns = extractTSVColumnsFromSentence(lineFromBlock);
				if(predicateObjects.containsKey(propertyFromLine)){
					@SuppressWarnings("unchecked")
					List<TSVColumn> object = (List<TSVColumn>) predicateObjects.get(propertyFromLine);
					for(TSVColumn column : tsvcolumns){
						object.add(column);
					}
				}else{
					predicateObjects.put(propertyFromLine, TripleObjectBuilder.createObjectAsColumns(tsvcolumns));
				}

			}
		}

		return new Rule(ruleId, ruleSubject, subjectTsvcolumns, predicateObjects);
	}
	private Rule exctactRuleFromOneLineRuleBlock(String subjectLine) {
		String ruleId 						= extractIDFromSentence(subjectLine);
		OWLClass ruleSubject 				= extractSubjectFromSentence(subjectLine);
		subjectLine = removeRegexFromContent(EnumRegexList.SELECTSUBJECTCLASSNAME.getExpression(), subjectLine);
		List<TSVColumn> subjectTsvcolumns 	= extractTSVColumnsFromSentence(subjectLine);

		return new Rule(ruleId, ruleSubject, subjectTsvcolumns, new HashMap<OWLProperty, TripleObject>());
	}

	private Integer extractRuleNumberAsTripleObject(String lineFromBlock) {
		Matcher matcher = matchRegexOnString(EnumRegexList.SELECTFIRSTNUMBERS.getExpression(), lineFromBlock);

		return Integer.parseInt(matcher.group());
	}

	private boolean tripleObjectIsToAnotherRule(String lineFromBlock) {

		Matcher matcher = matchRegexOnString(EnumRegexList.SELECTCONTENTQUOTATIONMARK.getExpression(), lineFromBlock);

		return matcher.hitEnd();
	}

	private String removePredicateFromBlockLine(String lineFromBlock) {
		return removeRegexFromContent(EnumRegexList.SELECTPREDICATE.getExpression(), lineFromBlock);
	}

	private OWLProperty extractPredicateFromBlockLine(String lineFromBlock) {		
		String predicateName = extractDataFromFirstQuotationMarkInsideRegex(lineFromBlock, EnumRegexList.SELECTPREDICATE.getExpression());		

		return ontologyHelper.getProperty(predicateName);
	}

	private List<TSVColumn> extractTSVColumnsFromSentence(String sentence){
		List<TSVColumn> listOfColumns = new ArrayList<TSVColumn>();
		String[] eachTSVColumnWithFlags = sentence.split(";");

		for(String s : eachTSVColumnWithFlags){

			String title = extractDataFromFirstQuotationMarkInsideRegex(s, EnumRegexList.SELECTCONTENTQUOTATIONMARK.getExpression());
			//s = removeRegexFromContent(EnumRegexList.SELECTCONTENTQUOTATIONMARK.getExpression(), s);

			List<Flag> flags = extractFlagsFromSentence(s);


			listOfColumns.add(new TSVColumn(title, flags));
		}

		return listOfColumns;
	}

	private List<Flag> extractFlagsFromSentence(String sentence) {
		List<Flag> flagsList = new ArrayList<Flag>();

		Matcher matcher = matchRegexOnString("\\/[DR!]|\\/(NM)|\\/(SP)|\\/(CB)|\\/(BASEIRI)", sentence);

		if(!matcher.hitEnd()){

			if(matcher.group().equals("/R")){
				flagsList.add(new ContentDirectionTSVColumn(EnumContentDirectionTSVColumn.RIGHT));
				sentence = removeRegexFromContent("\\/[R]", sentence);
			}else{
				flagsList.add(new ContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
				sentence = removeRegexFromContent("\\/[D]", sentence);
			}

			while(!matcher.hitEnd()){			

				String matcherString = matcher.group();

				if(matcherString.equals("/SP")){
					flagsList.add(extractDataFromFlagSeparatorFromSentence(sentence, EnumRegexList.SELECTSEPARATORFLAG.getExpression()));
					sentence = removeRegexFromContent(EnumRegexList.SELECTSEPARATORFLAG.getExpression(), sentence);
				}

				if(matcherString.equals("/CB")){
					flagsList.add(extractDataFromFlagConditionFromSentence(sentence, EnumRegexList.SELECTCONDITIONBLOCKFLAG.getExpression()));
					sentence = removeRegexFromContent(EnumRegexList.SELECTCONDITIONBLOCKFLAG.getExpression(), sentence);
				}

				if(matcherString.equals("/!")){
					flagsList.add(extractDataFromFlagFixedContentFromSentence(sentence, EnumRegexList.SELECTFIXEDCONTENTFLAG.getExpression()));
					sentence = removeRegexFromContent(EnumRegexList.SELECTFIXEDCONTENTFLAG.getExpression(), sentence);
				}

				if(matcherString.equals("/NM")){
					flagsList.add(new NotMetadata(true));
					sentence = removeRegexFromContent("\\/(NM)", sentence);
				}

				if(matcherString.equals("/BASEIRI")){
					flagsList.add(extractDataFromFlagBaseIRIFromSentence(sentence, EnumRegexList.SELECTBASEIRIFLAG.getExpression()));
					sentence = removeRegexFromContent(EnumRegexList.SELECTBASEIRIFLAG.getExpression(), sentence);
				}
				matcher.find();
			}
		}

		return flagsList;
	}

	private Flag extractDataFromFlagBaseIRIFromSentence(String sentence, String regex) {
		String iri = extractDataFromFirstQuotationMarkInsideRegex(sentence, regex);


		return new FlagBaseIRI(iri);
	}

	private Flag extractDataFromFlagFixedContentFromSentence(String sentence, String regex) {
		String contentFromQuotationMark = extractDataFromFirstQuotationMarkInsideRegex(sentence, regex);

		return new FixedContent(contentFromQuotationMark);
	}

	private Flag extractDataFromFlagConditionFromSentence(String sentence, String regex) {

		Matcher matchedConditionSelected = matchRegexOnString("\\(\\d*\\)", matchRegexOnString(regex, sentence).group());

		int id = Integer.getInteger(matchedConditionSelected.group().substring(
				1, 											  //remove the first parentheses
				matchedConditionSelected.group().length() - 1 //remove the last parentheses
				));

		return new FlagConditionBlock(id);
	}

	private Flag extractDataFromFlagSeparatorFromSentence(String sentence, String regex) {
		String contentFromQuotationMark = extractDataFromFirstQuotationMarkInsideRegex(sentence, regex);

		List<Integer> columnsSelected = new ArrayList<Integer>();
		Matcher matchedColumnsSelected = matchRegexOnString("(\\d(\\d)*)",
				matchRegexOnString(regex, sentence).group());
		while(!matchedColumnsSelected.hitEnd()){
			columnsSelected.add(Integer.parseInt(matchedColumnsSelected.group()));
			matchedColumnsSelected.find();
		}
		
		if(columnsSelected.size() < 1){
			columnsSelected.add(Integer.MAX_VALUE);
		}

		return new Separator(contentFromQuotationMark, columnsSelected);
	}

	private Matcher matchRegexOnString(String regex, String content){
		Pattern pattern = Pattern.compile(regex);
		Matcher m = pattern.matcher(content);
		m.find();
		return m;
	}

	private String removeRegexFromContent(String regex, String content){
		Matcher matcher = matchRegexOnString(regex, content);

		return matcher.replaceAll("").trim(); 
	}

	private String extractIDFromSentence(String blockRulesAsText) {
		String data = "";

		Matcher matcher = matchRegexOnString(EnumRegexList.SELECTRULEID.getExpression(), blockRulesAsText);
		data = matcher.group().replace("transformation_rule[", "").replace("condition_block[", "");
		return data;
	}

	private OWLClass extractSubjectFromSentence(String blockRulesAsText) { // (\s|,)"(.*?)":{1}

		String subjectString = extractDataFromFirstQuotationMarkInsideRegex(blockRulesAsText, EnumRegexList.SELECTSUBJECTCLASSNAME.getExpression());

		OWLClass subject = ontologyHelper.getClass(subjectString);

		if(subject == null)
			subject = ontologyHelper.factory.getOWLClass("", subjectString);

		return subject;
	}

	private String extractDataFromFirstQuotationMarkInsideRegex(String content, String regex){
		String data = null;

		Matcher matcher = matchRegexOnString(regex, content);
		
		try{
			data = matcher.group(); 
			data = data.substring(data.indexOf("\"")+1, data.indexOf("\"", data.indexOf("\"")+1));
		}catch(IllegalStateException e){
			//used just to identify when the matcher did not find anything.
			
			
			//System.out.println("exception");
		}

		return data;
	}

	private List<String> identifyRulesBlocksFromString(String fileContent) {
		Pattern patternToFind = Pattern.compile("transformation_rule\\[(.*?)\\]");
		Matcher match = patternToFind.matcher(fileContent);

		List<String> identifiedRules = new ArrayList<String>();

		while(match.find()){
			identifiedRules.add(match.group());
		}
		return identifiedRules;
	}
	
	private List<String> identifyConditionBlocksFromString(String fileContent) {
		Pattern patternToFind = Pattern.compile("condition_block\\[(.*?)\\]");
		Matcher match = patternToFind.matcher(fileContent);

		List<String> identifiedCB = new ArrayList<String>();

		while(match.find()){
			identifiedCB.add(match.group());
		}
		return identifiedCB;
	}

	private String readFile(String pathfile){
		String fileContent = "";
		try(Stream<String> stream = Files.lines(Paths.get(pathfile))){

			for(String line : stream.toArray(String[]::new)){
				fileContent += line.replace("\n", "");
			}
		}catch(IOException e){
			e.printStackTrace();
		}

		return fileContent;
	}
}
