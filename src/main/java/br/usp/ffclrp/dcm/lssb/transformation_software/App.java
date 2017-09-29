package br.usp.ffclrp.dcm.lssb.transformation_software;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;

import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ContentDirectionTSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumContentDirectionTSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Flag;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.NotMetadata;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Rule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TSVColumn;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
        App app = new App();
        app.extractRulesFromFile("c:\\testingrules.txt");
        
    }
    
    public void extractRulesFromFile(String pathfile){
    	String fileContent = readFile(pathfile);
    	//System.out.println(fileContent);
    	List<Rule> rulesList = extractRulesFromString(fileContent);
    }
    
    private List<Rule> extractRulesFromString(String fileContent) {
    	
    	List<String> listRulesAsText = identifyRulesBlocksFromString(fileContent);
    	
		List<Rule> ruleList = new ArrayList<Rule>();
		
		for(String s : listRulesAsText){
			for(Rule r : createRulesFromBlock(s))
				ruleList.add(r);
		}
		
		return null;
	}

	private List<Rule> createRulesFromBlock(String blockRulesAsText) {
		
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
		
		List<String> predicateLinesList = new ArrayList<String>();
		for(int i = 0; i <= initialOfEachMatch.size(); i++){
			String linesFromBlock = ruleCleaned.substring(initialOfEachMatch.get(i) + 1 // +1 exists to not include the first character, a comma
													, initialOfEachMatch.get(i+1) - 1); // -1 exists to not include the first character of the other match, a comma
			Rule rule = exctratOjectsFromSentence(linesFromBlock);
		}
		
		for(String s : predicateLinesList)
			System.out.println(s);
		
		List<Rule> rules = new ArrayList<Rule>();
		for(String s : predicateLinesList){
			String[] sentences = s.split("((\\D);(\\D))");
			rules.add);
		}
		
		return null;
	}
	
	private Rule exctratOjectsFromSentence(String sentence) {
		Rule rule = new Rule();
		
		String predicateName = extractDataFromFirstQuotationMarkInsideRegex(sentence, "((,?(.?))|(:(.?)))\"\\w*\"(\\s*?)=");
		sentence = removeRegexFromContent("((,?(.?))|(:(.?)))\"\\w*\"(\\s*?)=", sentence);
		rule.setPredicate(); //an individual of a class
		
		
		
		rule.setFlags(extractFlagsFromSentence(sentence));
		
		return null;
	}
	
	private List<TSVColumn> extractTSVColumnsFromSentence(String sentence){
		List<TSVColumn> listOfColumns = new ArrayList<TSVColumn>();
		String[] eachTSVColumnWithFlags = sentence.split("((\\D);(\\D))");
		
		for(String s : eachTSVColumnWithFlags){
			String title = "", contentDirection = "", metadata = "";
			
			title = extractDataFromFirstQuotationMarkInsideRegex(s, "\"(.*?)\"");
			s = removeRegexFromContent("\"(.*?)\"", s);
			
			extractFlagsFromSentence(s);
			
			
			listOfColumns.add(new TSVColumn(title, contentDirection, metadata));
		}
		
		return null;
	}

	private List<Flag> extractFlagsFromSentence(String sentence) {
		List<Flag> flagsList = new ArrayList<Flag>();
		
		Matcher matcher = matchRegexOnString("\\/[DR!]|\\/(NM)|\\/(SP)|\\/(CB)", sentence);
		while(matcher.find() && !matcher.hitEnd()){
			
			if(matcher.group().equals("/SP")) flagsList.add(extractDataSeparatorFlagFromSentence(sentence));
			if(matcher.group().equals("/CB")) flagsList.add(extractDataConditionFlagFromSentence(sentence));
			if(matcher.group().equals("/!")) flagsList.add(extractDataFixedContentFlagFromSentence(sentence));
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
		
		extractDataFromFirstQuotationMarkInsideRegex(blockRulesAsText, "(\\s|,)\"(.*?)\":{1}");
		
		return;
	}
	
	private String extractDataFromFirstQuotationMarkInsideRegex(String content, String regex){
		String data = "";
		
		Matcher matcher = matchRegexOnString(regex, content);
		//data = content.substring(matcher.start(), matcher.end());
		data = matcher.group();
		data = data.substring(data.indexOf("\""), data.lastIndexOf("\""));
		
		System.out.println(data);
		
		return data;
	}

	private List<String> identifyRulesBlocksFromString(String fileContent) {
		Pattern patternToFind = Pattern.compile("\\[(.*?)\\]");
		Matcher match = patternToFind.matcher(fileContent);
		
		List<String> identifiedRules = new ArrayList<String>();

		while(match.find()){
			identifiedRules.add(fileContent.substring(match.start(), match.end()));
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
