package br.usp.ffclrp.dcm.lssb.transformation_software;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Triple;
import org.hamcrest.core.IsInstanceOf;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;

import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ContentDirectionTSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumContentDirectionTSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FixedContent;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Flag;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.NotMetadata;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Rule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Separator;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObject;

/**
 * Hello world!
 *
 */
public class App 
{
	private OntologyHelper ontologyHelper;
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
        App app = new App();
        app.extractRulesFromFile("c:\\testingrules.txt");
        
    }
    
    public void extractRulesFromFile(String pathfile){
    	ontologyHelper = new OntologyHelper();
    	ontologyHelper.loadingOntologyFromFile("enchimentdata.owl");
    	String fileContent = readFile(pathfile);
    	//System.out.println(fileContent);
    	List<Rule> rulesList = extractRulesFromString(fileContent.replaceAll("\n", ""));
    	
    	printRules(rulesList);
    }
    
    private void printRules(List<Rule> rulesList) {
		for(Rule r : rulesList){
			System.out.println("** Rule: **\n");
			String out = "ID: " + r.getId() + "\t\t" + "Subject: " + r.getSubject() + "\t\t";
			r.getPredicateObjects().forEach( (key, value) -> { printSysoutRules(out, key, value); });
			
		}
		
	}
    

	private void printSysoutRules(String out, OWLProperty key, TripleObject value) {
		out += "Predicate: " + key + "\t\t" + "Object: ";
		if(value.getObject() instanceof TSVColumn){
			@SuppressWarnings("unchecked")
			List<TSVColumn> column = (List<TSVColumn>) value.getObject();
			for(TSVColumn c : column){
				out += c.getTitle();
			}
		}
	
}
private List<Rule> extractRulesFromString(String fileContent) {
    	
    	List<String> listRulesAsText = identifyRulesBlocksFromString(fileContent);
    	
		List<Rule> ruleList = new ArrayList<Rule>();
		
		for(String s : listRulesAsText){
				ruleList.add(createRulesFromBlock(s));
		}
		
		return ruleList;
	}

	private Rule createRulesFromBlock(String blockRulesAsText) {
		
		String ruleId = extractIDFromSentence(blockRulesAsText); // \[(\s*?)"(.*?)"(,|\s) //remove the string
		OWLClass ruleSubject = extractSubjectFromSentence(blockRulesAsText); // (\s|,)"(.*?)":{1} //remove the string
		//List<OWLProperty> ruleProperties = extractPropertyFromSentence(listRulesAsText); // ((,(.?))|(:(.?)))"\w*"(\s*?)= //remove the string
		
		String ruleCleaned = removeIdAndSubjectFromRule(blockRulesAsText);
		ruleCleaned = removeRegexFromContent("]", ruleCleaned);
		Matcher matcher = matchRegexOnString("((,?(.?))|(:(.?)))\"\\w*\"(\\s*?)=", ruleCleaned);
		
		List<Integer> initialOfEachMatch = new ArrayList<Integer>();
		while(!matcher.hitEnd()){
			initialOfEachMatch.add(matcher.start());
			matcher.find();
		}
		
		//List<String> predicateLinesList = new ArrayList<String>();
		
		Map<OWLProperty, TripleObject> predicateObjects = new Hashtable<OWLProperty, TripleObject>();
		for(int i = 0; i <= initialOfEachMatch.size(); i++){
			String lineFromBlock = ruleCleaned.substring(initialOfEachMatch.get(i) + 1, // +1 exists to not include the first character, a comma
														  initialOfEachMatch.get(i+1) - 1); // -1 exists to not include the first character of the other match, a comma
			OWLProperty propertyFromLine = exctratPredicateFromBlockLine(lineFromBlock);
			lineFromBlock = removePredicateFromBlockLine(lineFromBlock);
			
			List<TSVColumn> tsvcolumns = extractTSVColumnsFromSentence(lineFromBlock);
			if(predicateObjects.containsKey(propertyFromLine)){
				@SuppressWarnings("unchecked")
				List<TSVColumn> object = (List<TSVColumn>) predicateObjects.get(propertyFromLine);
				for(TSVColumn column : tsvcolumns){
					object.add(column);
				}
			}else{
				predicateObjects.put(propertyFromLine, new TripleObject(tsvcolumns));
			}
		}
		
		
		return new Rule(ruleId, ruleSubject, predicateObjects);
	}
	private String removePredicateFromBlockLine(String lineFromBlock) {
		return removeRegexFromContent("((,?(.?))|(:(.?)))\"\\w*\"(\\s*?)=", lineFromBlock);
	}

	private OWLProperty exctratPredicateFromBlockLine(String lineFromBlock) {
		Rule rule = new Rule();
		
		String predicateName = extractDataFromFirstQuotationMarkInsideRegex(lineFromBlock, "((,?(.?))|(:(.?)))\"\\w*\"(\\s*?)=");
		
		
		
		//buscar aqui a propriedade na ontologia
		
		
		return null;
	}
	
	private List<TSVColumn> extractTSVColumnsFromSentence(String sentence){
		List<TSVColumn> listOfColumns = new ArrayList<TSVColumn>();
		String[] eachTSVColumnWithFlags = sentence.split("((\\D);(\\D))");
		
		for(String s : eachTSVColumnWithFlags){
			
			String title = extractDataFromFirstQuotationMarkInsideRegex(s, "\"(.*?)\"");
			s = removeRegexFromContent("\"(.*?)\"", s);
			
			List<Flag> flags = extractFlagsFromSentence(s);
			
			
			listOfColumns.add(new TSVColumn(title, flags));
		}
		
		return listOfColumns;
	}

	private List<Flag> extractFlagsFromSentence(String sentence) {
		List<Flag> flagsList = new ArrayList<Flag>();
		
		Matcher matcher = matchRegexOnString("\\/[DR!]|\\/(NM)|\\/(SP)|\\/(CB)", sentence);
		while(matcher.find() && !matcher.hitEnd()){
			
			if(matcher.group().equals("/SP")){
				String regex = "\\/S\\(\"(.*?)\",(.*?)\\)";
				flagsList.add(extractDataFromFlagSeparatorFromSentence(sentence, regex));
				sentence = removeRegexFromContent(regex, sentence);
				
			}
			
			if(matcher.group().equals("/CB")){
				String regex = "\\/CB\\(\\d*?\\)";
				flagsList.add(extractDataFromFlagConditionFromSentence(sentence, regex));
				sentence = removeRegexFromContent(regex, sentence);
			}
			
			if(matcher.group().equals("/!")){
				String regex = "\\/\\!\\(\"(.*?)\"\\)";
				flagsList.add(extractDataFromFlagFixedContentFromSentence(sentence, regex));
				sentence = removeRegexFromContent(regex, sentence);
			}
			
			if(matcher.group().equals("/R")){
				flagsList.add(new ContentDirectionTSVColumn(EnumContentDirectionTSVColumn.RIGHT));
				sentence = removeRegexFromContent("\\/[R]", sentence);
			}else{
				flagsList.add(new ContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
				sentence = removeRegexFromContent("\\/[D]", sentence);
			}
			
			if(matcher.group().equals("/NM")){
				flagsList.add(new NotMetadata(true));
				sentence = removeRegexFromContent("\\/(NM)", sentence);
			}
		}
		
		return null;
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
		Matcher matchedColumnsSelected = matchRegexOnString("(\\d(\\d)*)", matchRegexOnString(regex, sentence).group());
		while(!matchedColumnsSelected.hitEnd()){
			columnsSelected.add(Integer.getInteger(matchedColumnsSelected.group()));
		}
		
		return new Separator(contentFromQuotationMark, columnsSelected);
	}

	private Matcher matchRegexOnString(String regex, String content){
		Pattern pattern = Pattern.compile(regex);
		return pattern.matcher(content);
	}
	
	private String removeIdAndSubjectFromRule(String blockRulesAsText) {
		
		String cleaned = removeRegexFromContent("\\[(\\s*?)\"(.*?)\"(,|\\s)", blockRulesAsText);
			   cleaned = removeRegexFromContent("(\\s|,)\"(.*?)\":{1}", cleaned);
			   
			   
		return cleaned;
		
	}
	
	private String removeRegexFromContent(String regex, String content){
		String contentCleaned = "";
		/*
		//Removing ID
		Matcher matcher = matchRegexOnString("\\[(\\s*?)\"(.*?)\"(,|\\s)", blockRulesAsText);
		blockRulesCleaned += blockRulesAsText.substring(0, matcher.start() - 1);
		blockRulesCleaned += blockRulesAsText.substring(matcher.end() + 1, blockRulesAsText.length() - 1);
		System.out.println(blockRulesCleaned);
		
		//Removing Subject
		matcher = matchRegexOnString("(\\s|,)\"(.*?)\":{1}", blockRulesAsText);
		blockRulesCleaned += blockRulesAsText.substring(0, matcher.start() - 1);
		blockRulesCleaned += blockRulesAsText.substring(matcher.end() + 1, blockRulesAsText.length() - 1);
		*/
		
		Matcher matcher = matchRegexOnString(regex, content);
		contentCleaned = matcher.replaceAll("");

		System.out.println(contentCleaned);
		
		return contentCleaned.trim(); 
	}
	
	private String extractIDFromSentence(String blockRulesAsText) { // \[(\s*?)"(.*?)"(,|\s)
		
		return extractDataFromFirstQuotationMarkInsideRegex(blockRulesAsText, "\\[(\\s*?)\"(.*?)\"(,|\\s)");
	}

	private OWLClass extractSubjectFromSentence(String blockRulesAsText) { // (\s|,)"(.*?)":{1}
		
		String subject = extractDataFromFirstQuotationMarkInsideRegex(blockRulesAsText, "(\\s|,)\"(.*?)\":{1}");
				
		return ontologyHelper.getClass(subject);
	}
	
	private String extractDataFromFirstQuotationMarkInsideRegex(String content, String regex){
		String data = "";
		
		Matcher matcher = matchRegexOnString(regex, content);
		//data = content.substring(matcher.start(), matcher.end());
		matcher.find();
		data = matcher.group();
		data = data.substring(data.indexOf("\"")+1, data.lastIndexOf("\""));
		
		System.out.println(data);
		
		return data;
	}

	private List<String> identifyRulesBlocksFromString(String fileContent) {
		Pattern patternToFind = Pattern.compile("\\[(.*?)\\]");
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
