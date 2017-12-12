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

import br.usp.ffclrp.dcm.lssb.custom_exceptions.ClassNotFoundInOntology;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.CustomExceptions;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.PropertyNotExist;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagContentDirectionTSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagCustomID;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagDataType;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumContentDirectionTSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumRegexList;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagFixedContent;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Flag;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagBaseIRI;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagNotMetadata;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ObjectAsRule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Rule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagSeparator;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObject;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObjectAsRule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObjectBuilder;

/**
 * Hello world!
 *
 */
public class App
{
	public OntologyHelper ontologyHelper;
	public List<Rule> rulesList;
	public Map<Integer, ConditionBlock> conditionsBlocks = new HashMap<Integer, ConditionBlock>();

	public static void main( String[] args )
	{
		App app = new App();
		List<String> listOfOntologies = new ArrayList<String>();
		listOfOntologies.add("testFiles/normalizedFile/fourth_attempt/ontology-imported.owl");
		try {
			app.extractRulesFromFile("testFiles/normalizedFile/fourth_attempt/rules.txt", listOfOntologies);
			TriplesProcessing triplesProcessing = new TriplesProcessing("testFiles/normalizedFile/fourth_attempt/NormalizedData.txt", "testFiles/normalizedFile/fourth_attempt/ontology-imported.owl");
			triplesProcessing.createTriplesFromRules(app.rulesList, app.conditionsBlocks, "http://www.example.org/onto/individual/");
		} catch (CustomExceptions e) { 
			e.getMessage();
			System.out.println(e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Finished!");
		}


	}

	public void extractRulesFromFile(String rulesRelativePath, List<String> listOfOntologies) throws Exception{
		ontologyHelper = new OntologyHelper();
		ontologyHelper.loadingOntologyFromFile(listOfOntologies);
		String fileContent = readFile(rulesRelativePath);

		fileContent = fileContent.replaceAll("\n", "").replaceAll("\t", "");
		rulesList = extractRulesFromString(fileContent);
		List<ConditionBlock> listConditionBlock = ConditionBlock.extractConditionsBlocksFromString(fileContent);

		for(ConditionBlock conditionBlock : listConditionBlock){
			conditionsBlocks.put(conditionBlock.getId(), conditionBlock);
		}
	}

	/*	private void printRules(List<Rule> rulesList) {
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
	}*/

	public List<Rule> extractRulesFromString(String fileContent) throws Exception {

		List<String> rulesListAsText = identifyRulesBlocksFromString(fileContent);		

		List<Rule> ruleList = new ArrayList<Rule>();

		for(String s : rulesListAsText){
			ruleList.add(createRulesFromBlock(s));
		}

		return ruleList;
	}

	public Rule createRulesFromBlock(String blockRulesAsText) throws Exception {
		Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTSUBJECTLINE.get(), blockRulesAsText);
		if(matcher.hitEnd()){
			return exctactRuleFromOneLineRuleBlock(blockRulesAsText);
		}
		String subjectLine 				=	Utils.splitByIndex(blockRulesAsText, matcher.start())[0];
		String predicatesLinesOneBlock 	= 	Utils.splitByIndex(blockRulesAsText, matcher.start())[1];

		String ruleId 						= extractRuleIDFromSentence(subjectLine);
		OWLClass ruleSubject 				= extractSubjectFromSentence(subjectLine);
		subjectLine = Utils.removeRegexFromContent(EnumRegexList.SELECTSUBJECTCLASSNAME.get(), subjectLine);
		List<TSVColumn> subjectTsvcolumns 	= extractTSVColumnsFromSentence(subjectLine);

		matcher = Utils.matchRegexOnString(EnumRegexList.SELECTPREDICATESDIVISIONS.get(), predicatesLinesOneBlock);
		List<Integer> initialOfEachMatch = new ArrayList<Integer>();
		while(!matcher.hitEnd()){
			initialOfEachMatch.add(matcher.start());
			matcher.find();
		}

		Map<OWLProperty, TripleObject> predicateObjects = new Hashtable<OWLProperty, TripleObject>();
		for(int i = 0; i < initialOfEachMatch.size(); i++){
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
				List<Flag> flagsFromSentence = extractFlagsFromSentence(lineFromBlock);

				if(predicateObjects.containsKey(propertyFromLine)){
					@SuppressWarnings("unchecked")
					List<ObjectAsRule> ruleObjects = (List<ObjectAsRule>) predicateObjects.get(propertyFromLine).getObject();
					ruleObjects.add(new ObjectAsRule(ruleNumber, flagsFromSentence));
				}else{
					ObjectAsRule object = new ObjectAsRule(ruleNumber, flagsFromSentence);
					TripleObjectAsRule ruleObject = (TripleObjectAsRule) TripleObjectBuilder.createObjectToRule(object);
					predicateObjects.put(propertyFromLine, ruleObject);
				}

			}else{
				List<TSVColumn> tsvcolumns = extractTSVColumnsFromSentence(lineFromBlock);
				if(predicateObjects.containsKey(propertyFromLine)){
					@SuppressWarnings("unchecked")
					List<TSVColumn> object = (List<TSVColumn>) predicateObjects.get(propertyFromLine).getObject();
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

	public Rule exctactRuleFromOneLineRuleBlock(String subjectLine) throws Exception {
		String ruleId 						= extractRuleIDFromSentence(subjectLine);
		OWLClass ruleSubject 				= extractSubjectFromSentence(subjectLine);
		subjectLine = Utils.removeRegexFromContent(EnumRegexList.SELECTSUBJECTCLASSNAME.get(), subjectLine);
		List<TSVColumn> subjectTsvcolumns 	= extractTSVColumnsFromSentence(subjectLine);

		return new Rule(ruleId, ruleSubject, subjectTsvcolumns, new HashMap<OWLProperty, TripleObject>());
	}

	public Integer extractRuleNumberAsTripleObject(String lineFromBlock) {
		Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTFIRSTNUMBERS.get(), lineFromBlock);

		return Integer.parseInt(matcher.group());
	}

	public boolean tripleObjectIsToAnotherRule(String lineFromBlock) {

		Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTCONTENTQUOTATIONMARK.get(), lineFromBlock);

		return matcher.hitEnd();
	}

	private String removePredicateFromBlockLine(String lineFromBlock) {
		return Utils.removeRegexFromContent(EnumRegexList.SELECTPREDICATE.get(), lineFromBlock);
	}

	private OWLProperty extractPredicateFromBlockLine(String lineFromBlock) throws Exception {		
		String predicateName = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTPREDICATE.get());		

		OWLProperty prop = ontologyHelper.getProperty(predicateName);

		if(prop == null)
			throw new PropertyNotExist("Property does not exist in ontology. Instruction: " + lineFromBlock);

		return prop;
	}

	public List<TSVColumn> extractTSVColumnsFromSentence(String sentence) throws Exception{
		List<TSVColumn> listOfColumns = new ArrayList<TSVColumn>();
		String[] eachTSVColumnWithFlags = sentence.split(";");

		for(String s : eachTSVColumnWithFlags){

			String title = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(s, EnumRegexList.SELECTCONTENTQUOTATIONMARK.get());
			//s = Utils.removeRegexFromContent(EnumRegexList.SELECTCONTENTQUOTATIONMARK.getExpression(), s);

			List<Flag> flags = extractFlagsFromSentence(s);


			listOfColumns.add(new TSVColumn(title, flags));
		}

		return listOfColumns;
	}

	public List<Flag> extractFlagsFromSentence(String sentence) throws Exception {
		List<Flag> flagsList = new ArrayList<Flag>();

		Matcher matcher = Utils.matchRegexOnString("(\\/D\\s)|(\\/R\\s)", sentence);

		try{
			if(matcher.group().equals("/R")){
				flagsList.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.RIGHT));
				sentence = Utils.removeRegexFromContent("\\/[R]", sentence);
			}else{
				flagsList.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
				sentence = Utils.removeRegexFromContent("\\/[D]", sentence);
			}
		}catch(Exception e){
			flagsList.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		}


		matcher = Utils.matchRegexOnString("\\/(NM)|\\/(SP)|\\/(CB)|\\/(BASEIRI)|\\/(ID)|\\/(FX)|\\/(DT)", sentence);
		while(!matcher.hitEnd()){			

			String matcherString = matcher.group();

			if(matcherString.equals("/SP")){
				flagsList.add(extractDataFromFlagSeparatorFromSentence(sentence, EnumRegexList.SELECTSEPARATORFLAG.get()));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTSEPARATORFLAG.get(), sentence);
			}

			if(matcherString.equals("/CB")){
				flagsList.add(extractDataFromFlagConditionFromSentence(sentence, EnumRegexList.SELECTCONDITIONBLOCKFLAG.get()));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTCONDITIONBLOCKFLAG.get(), sentence);
			}

			if(matcherString.equals("/FX")){
				flagsList.add(extractDataFromFlagFixedContentFromSentence(sentence, EnumRegexList.SELECTFIXEDCONTENTFLAG.get()));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTFIXEDCONTENTFLAG.get(), sentence);
			}

			if(matcherString.equals("/NM")){
				flagsList.add(new FlagNotMetadata(true));
				sentence = Utils.removeRegexFromContent("\\/(NM)", sentence);
			}

			if(matcherString.equals("/BASEIRI")){
				flagsList.add(extractDataFromFlagBaseIRIFromSentence(sentence, EnumRegexList.SELECTBASEIRIFLAG.get()));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTBASEIRIFLAG.get(), sentence);
			}

			if(matcherString.equals("/ID")){
				flagsList.add(extractDataFromFlagCustomIDFromSentence(sentence, EnumRegexList.SELECTCUSTOMDIDFLAG.get()));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTCUSTOMDIDFLAG.get(), sentence);
			}

			if(matcherString.equals("/DT")){
				flagsList.add(extractDataFromFlagDataTypeFromSentence(sentence, EnumRegexList.SELECTDATATYPEFLAG.get()));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTDATATYPEFLAG.get(), sentence);
			}

			matcher.find();
		}

		return flagsList;
	}

	private Flag extractDataFromFlagDataTypeFromSentence(String sentence, String regex) {
		String contentFromQuotationMark = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(sentence, regex);

		return new FlagDataType(contentFromQuotationMark);
	}

	private Flag extractDataFromFlagCustomIDFromSentence(String sentence, String regex) {
		String contentFromQuotationMark = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(sentence, regex);

		return new FlagCustomID(contentFromQuotationMark);
	}

	private Flag extractDataFromFlagBaseIRIFromSentence(String sentence, String regex) {
		sentence = Utils.matchRegexOnString(regex, sentence).group();

		String iri = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(sentence, regex);
		String namespace = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(sentence, EnumRegexList.SELECTNAMESPACEBASEIRIFLAG.get());


		return new FlagBaseIRI(iri, namespace);
	}

	private Flag extractDataFromFlagFixedContentFromSentence(String sentence, String regex) {
		String contentFromQuotationMark = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(sentence, regex);

		return new FlagFixedContent(contentFromQuotationMark);
	}

	private Flag extractDataFromFlagConditionFromSentence(String sentence, String regex) {

		String cbFlagTerm = Utils.matchRegexOnString(regex, sentence).group();
		String matchedConditionSelected = Utils.matchRegexOnString("\\d+", cbFlagTerm).group(); 

		int id = Integer.parseInt(matchedConditionSelected);

		return new FlagConditionBlock(id);
	}

	private Flag extractDataFromFlagSeparatorFromSentence(String sentence, String regex) throws Exception {

		sentence = Utils.matchRegexOnString(regex, sentence).group();
		String contentFromQuotationMark = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(sentence, regex);
		String contentWithoutQuotationMark = removeDataFromFirstQuotationMarkBlockInsideRegex(sentence, regex);


		List<Integer> columnsSelected = new ArrayList<Integer>();


		//Exctact range
		Matcher matchedRange = Utils.matchRegexOnString(EnumRegexList.SELECTSEPARATORFLAGRANGENUMBERS.get(), contentWithoutQuotationMark);
		while(!matchedRange.hitEnd()){
			int start, end = 0;
			Matcher matchedColumnsSelected = Utils.matchRegexOnString("(\\d(\\d)*)", matchedRange.group());
			try {
				start = Integer.parseInt(matchedColumnsSelected.group());
				matchedColumnsSelected.find();
				end = Integer.parseInt(matchedColumnsSelected.group());
			}catch (Exception e){
				throw new Exception("Error trying to extract range numbers from Separator Flag.", e);
			}

			for(int i = start; i <= end; i++)
				columnsSelected.add(i - 1);

			matchedRange.find();
		}

		String contentWithoutQuotationAndRange = Utils.removeRegexFromContent(EnumRegexList.SELECTSEPARATORFLAGRANGENUMBERS.get(), contentWithoutQuotationMark);

		//Exctact individuals columns
		Matcher matchedIndividualColumns = Utils.matchRegexOnString("\\s*\\d+", contentWithoutQuotationAndRange);
		while(!matchedIndividualColumns.hitEnd()){
			columnsSelected.add(Integer.parseInt(matchedIndividualColumns.group().trim()) - 1);
			matchedIndividualColumns.find();
		}

		//Case there are no columns specified
		if(columnsSelected.size() < 1){
			columnsSelected.add(Integer.MAX_VALUE);
		}

		return new FlagSeparator(contentFromQuotationMark, columnsSelected);
	}

	private String extractRuleIDFromSentence(String blockRulesAsText) {
		String data = "";

		Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTRULEID.get(), blockRulesAsText);
		data = matcher.group().replace("transformation_rule[", "").replace("condition_block[", "");
		return data;
	}

	private OWLClass extractSubjectFromSentence(String blockRulesAsText) throws Exception {

		String subjectString = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(blockRulesAsText, EnumRegexList.SELECTSUBJECTCLASSNAME.get());

		OWLClass subject = ontologyHelper.getClass(subjectString);

		if(subject == null)
			throw new ClassNotFoundInOntology("Not found any ontology class with label '" + subjectString + "'");

		return subject;
	}

	private String removeDataFromFirstQuotationMarkBlockInsideRegex(String content, String regex){
		String data = null;

		Matcher matcher = Utils.matchRegexOnString(regex, content);

		try{
			data = matcher.group(); 
			int firstQuotationMark = data.indexOf("\"");
			String quotationBlock = data.substring(firstQuotationMark + 1, data.indexOf("\"", firstQuotationMark +1));
			content = content.replace(quotationBlock, "");
		}catch(IllegalStateException e){
			//used just to identify when the matcher did not find anything.
		}

		return content;
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
