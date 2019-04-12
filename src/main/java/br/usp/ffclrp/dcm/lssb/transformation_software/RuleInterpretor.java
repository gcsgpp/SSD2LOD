package br.usp.ffclrp.dcm.lssb.transformation_software;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.*;
import br.usp.ffclrp.dcm.lssb.transformation_manager.EnumActivityState;
import br.usp.ffclrp.dcm.lssb.transformation_manager.TransformationManagerImpl;
import br.usp.ffclrp.dcm.lssb.transformation_manager.custom_exceptions.ErrorFileException;
import br.usp.ffclrp.dcm.lssb.transformation_manager.custom_exceptions.StatusFileException;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.*;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 *
 */
public class RuleInterpretor implements Runnable
{
	public 	OntologyHelper ontologyHelper;
	public 	List<Rule> rulesList;
	public 	Map<String	, ConditionBlock> 	conditionsBlocks 	= new HashMap<>();
	public 	Map<String	, RuleConfig> 		ruleConfigs 		= new HashMap<>();
	public 	Map<String	, SearchBlock> 		searchBlocks		= new HashMap<>();

	//Parameters needed to implement Run()
	TransformationManagerImpl	fileSystemManager;
	String 						transformationId;
	List<String> 				listOfOntologies;
	String 						rulesFilePath;
	String 						relativePathOntologyFile;
	List<String> 				dataPaths;

	public RuleInterpretor(){

	}

	public static void main (String[] args) {

//	       Test
//	    args = new String[3];
//	    args[0] = "runTransformation";
//	    args[1] = "TC2-nova-ontologia-alterada";
//	    args[2] = "50bea367-a756-415b-8bb3-00933c07810a";
	    /* End test */

		RuleInterpretor ruleInterpretor = new RuleInterpretor();

		if(args[0].equals("runTransformation")) {
			ruleInterpretor.transformationId = args[1];
			System.out.println("=============== Transformation ID: " + ruleInterpretor.transformationId);
			ruleInterpretor.run();
		}else if(args[0].equals("runQuery")){

			ruleInterpretor.runQuery(args[1], args[2]);

		}

		System.out.println("Finished!");
	}

	public void 			setTransformationParameters(String transformationId,
											List<String> listOfOntologies,
											String rulesFilePath,
											String relativePathOntologyFile,
											List<String> dataPaths){
		this.transformationId = transformationId;
		this.listOfOntologies = listOfOntologies;
		this.rulesFilePath = rulesFilePath;
		this.relativePathOntologyFile = relativePathOntologyFile;
		this.dataPaths = dataPaths;

	}

	public void 			setTransformationParameters() throws Exception {
		fileSystemManager = new TransformationManagerImpl();

		try{

			List<String> ontologiesList = fileSystemManager.getAllOntologies(transformationId);


			this.listOfOntologies 			= ontologiesList;
			this.dataPaths   				= fileSystemManager.getAllDatasets(transformationId);
			this.rulesFilePath  			= fileSystemManager.getRulesFilePath(transformationId);
			this.relativePathOntologyFile	= ontologiesList.get(0);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void 			run() {

		try {
			setTransformationParameters();

			System.out.println("=============== Ontology file: " + relativePathOntologyFile);
			extractRulesFromFile(rulesFilePath, listOfOntologies);
			TriplesProcessing triplesProcessing = new TriplesProcessing();
			for (String path : dataPaths) {
				triplesProcessing.addDatasetToBeProcessed(path);
			}

			triplesProcessing.createTriplesFromRules(rulesList, conditionsBlocks, searchBlocks, ruleConfigs.get("default"), listOfOntologies);
			triplesProcessing.checkConsistency();

			writeRDF(triplesProcessing, transformationId);


			fileSystemManager.updateStatus(transformationId, EnumActivityState.SUCCEEDED);
			fileSystemManager.logTransformationError(transformationId, null);

		} catch (Exception e) {
			try {
				fileSystemManager.updateStatus(transformationId, EnumActivityState.FAILED);
				fileSystemManager.logTransformationError(transformationId, e);

			} catch (StatusFileException | ErrorFileException w) {
				System.out.println(w.getMessage());
				w.printStackTrace();
			}
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public void 			extractRulesFromFile(String rulesRelativePath, List<String> listOfOntologies) throws Exception{
		String fileContent = Utils.readFile(rulesRelativePath);

		fileContent = fileContent.replaceAll("\n", "").replaceAll("\t", "");


		
		List<RuleConfig> listRuleConfig = RuleConfig.extractRuleConfigFromString(fileContent);
		for(RuleConfig rc : listRuleConfig){
			ruleConfigs.put(rc.getId(), rc);
		}

		//Ontology must be after the read of the ruleConfigs because of possible presence of the namespaces key
		ontologyHelper = new OntologyHelper();
		if(ruleConfigs.get("default") != null)
			ontologyHelper.setNamespaces(ruleConfigs.get("default").getNamespace());
		ontologyHelper.loadingOntologyFromFile(listOfOntologies);

		
		List<ConditionBlock> listConditionBlock = ConditionBlock.extractConditionsBlocksFromString(fileContent);
		for(ConditionBlock conditionBlock : listConditionBlock){
			conditionsBlocks.put(conditionBlock.getId(), conditionBlock);
		}

		List<SearchBlock> listSearchBlocks = SearchBlock.extractSearchBlockFromString(fileContent);
		for(SearchBlock sb : listSearchBlocks){
			searchBlocks.put(sb.getId(), sb);
		}
		
		rulesList = extractRulesFromString(fileContent);
	}

	public List<Rule> 		extractRulesFromString(String fileContent) throws Exception {

		List<String> rulesListAsText = identifyRuleBlocksFromString(fileContent);

		List<Rule> ruleList = new ArrayList<Rule>();

		for(String s : rulesListAsText){
			ruleList.add(createRulesFromBlock(s));
		}

		return ruleList;
	}

	public Rule 			createRulesFromBlock(String blockRulesAsText) throws Exception {
		Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTSUBJECTLINE.get(), blockRulesAsText);
		if(matcher.hitEnd()){
			return extractRuleFromOneLineRuleBlock(blockRulesAsText);
		}
//		String subjectLine 				=	Utils.splitByIndex(blockRulesAsText, matcher.start())[0];
//		String predicatesLinesOneBlock 	= 	Utils.splitByIndex(blockRulesAsText, matcher.start())[1];
		String subjectLine 				= 	matcher.group();
				matcher 				= 	Utils.matchRegexOnString(EnumRegexList.SELECTBLOCKBODY.get(), blockRulesAsText);
		String predicatesLinesOneBlock 	= 	matcher.group();

		String ruleId 						= extractRuleIDFromSentence(blockRulesAsText);

		RuleConfig ruleConfig				= ruleConfigs.get(ruleId);
		if(ruleConfig == null){
			ruleConfig = new RuleConfig(ruleConfigs.get("default"));
			if(ruleConfig == null)
				throw new Exception("Not found a config rule block for rule id '" + ruleId + "' neither a 'default' config rule block.");
		}
		if(blockRulesAsText.trim().startsWith("row_based_rule")) {
			ruleConfig = new RuleConfig(ruleConfig.setMatrix(true));
		}
			
		OWLClass ruleSubject 				= extractSubjectFromSentence(subjectLine);
		subjectLine = Utils.removeRegexFromContent(EnumRegexList.SELECTSUBJECTCLASSNAME.get(), subjectLine);
		List<TSVColumn> subjectTsvcolumns 	= extractTSVColumnsFromSentence(subjectLine);

		matcher = Utils.matchRegexOnString(EnumRegexList.SELECTRULEPREDICATESDIVISIONS.get(), predicatesLinesOneBlock);
		List<Integer> initialOfEachMatch = new ArrayList<Integer>();
		while(!matcher.hitEnd()){
			initialOfEachMatch.add(matcher.start());
			matcher.find();
		}

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<OWLProperty, TripleObject>();
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

				String ruleNumber = extractRuleIdAsTripleObject(lineFromBlock);
				List<Flag> flagsFromSentence = extractFlagsFromSentence(lineFromBlock);


				ObjectAsRule object = new ObjectAsRule(ruleNumber, flagsFromSentence);
				TripleObjectAsRule ruleObject = (TripleObjectAsRule) TripleObjectBuilder.createObjectToRule(object);
				predicateObjects.put(propertyFromLine, ruleObject);



				/*if(predicateObjects.containsKey(propertyFromLine)){
					@SuppressWarnings("unchecked")
					List<ObjectAsRule> ruleObjects = (Collection<ObjectAsRule>) predicateObjects.get(propertyFromLine).getObject();
					//List<ObjectAsRule> ruleObjects = (List<ObjectAsRule>) predicateObjects.get(propertyFromLine).getObject();
					ruleObjects.add(new ObjectAsRule(ruleNumber, flagsFromSentence));
				}else{
					ObjectAsRule object = new ObjectAsRule(ruleNumber, flagsFromSentence);
					TripleObjectAsRule ruleObject = (TripleObjectAsRule) TripleObjectBuilder.createObjectToRule(object);
					predicateObjects.put(propertyFromLine, ruleObject);
				}*/



			}else{
				List<TSVColumn> tsvcolumns = extractTSVColumnsFromSentence(lineFromBlock);
				predicateObjects.put(propertyFromLine, TripleObjectBuilder.createObjectAsColumns(tsvcolumns));



				/*if(predicateObjects.containsKey(propertyFromLine)){
					@SuppressWarnings("unchecked")
					List<TSVColumn> object = (List<TSVColumn>) predicateObjects.get(propertyFromLine).getObject();
					for(TSVColumn column : tsvcolumns){
						object.add(column);
					}
				}else{
					predicateObjects.put(propertyFromLine, TripleObjectBuilder.createObjectAsColumns(tsvcolumns));
				}*/

			}
		}
		
		Rule rule = new Rule(ruleId, ruleConfig, ruleSubject, subjectTsvcolumns, predicateObjects);

		return rule;
	}

	public Rule 			extractRuleFromOneLineRuleBlock(String subjectLine) throws Exception {
		String ruleId 						= extractRuleIDFromSentence(subjectLine);
		RuleConfig ruleConfig				= ruleConfigs.get(ruleId);
		if(ruleConfig == null){
			ruleConfig = ruleConfigs.get("default");
			if(ruleConfig == null)
				throw new Exception("Not found a config rule block for rule id '" + ruleId + "' neither a 'default' config rule block.");
		}
		if(subjectLine.contains("matrix_rule[")) {
			ruleConfig = ruleConfig.setMatrix(true);
		}

		OWLClass ruleSubject 				= extractSubjectFromSentence(subjectLine);
		subjectLine = Utils.removeRegexFromContent(EnumRegexList.SELECTSUBJECTCLASSNAME.get(), subjectLine);
		List<TSVColumn> subjectTsvcolumns 	= extractTSVColumnsFromSentence(subjectLine);

		return new Rule(ruleId, ruleConfig, ruleSubject, subjectTsvcolumns, new ArrayListValuedHashMap<OWLProperty, TripleObject>());
	}

	public String 			extractRuleIdAsTripleObject(String lineFromBlock) {
		Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTRULEIDFROMPREDICATE.get(), lineFromBlock);

		return matcher.group(1);
	}

	public boolean		 	tripleObjectIsToAnotherRule(String lineFromBlock) {

		Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTCONTENTQUOTATIONMARK.get(), lineFromBlock);

		return matcher.hitEnd();
	}

	private String 			removePredicateFromBlockLine(String lineFromBlock) {
		return Utils.removeRegexFromContent(EnumRegexList.SELECTPREDICATE.get(), lineFromBlock);
	}

	private OWLProperty 	extractPredicateFromBlockLine(String lineFromBlock) throws Exception {
		String predicateName = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTPREDICATE.get());

		OWLProperty prop = ontologyHelper.getProperty(predicateName);

		return prop;
	}

	public List<TSVColumn> 	extractTSVColumnsFromSentence(String sentence) throws Exception{
		List<TSVColumn> listOfColumns = new ArrayList<TSVColumn>();
		String[] eachTSVColumnWithFlags = sentence.split("/;");

		for(String s : eachTSVColumnWithFlags){

			String title = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(s, EnumRegexList.SELECTCONTENTQUOTATIONMARK.get());
			//s = Utils.removeRegexFromContent(EnumRegexList.SELECTCONTENTQUOTATIONMARK.getExpression(), s);

			List<Flag> flags = extractFlagsFromSentence(s);


			listOfColumns.add(new TSVColumn(title, flags));
		}

		return listOfColumns;
	}

	public List<Flag> 		extractFlagsFromSentence(String sentence) throws Exception {
		List<Flag> flagsList = new ArrayList<Flag>();


		String flagsToCheck = 	EnumRegexList.SELECTNOTMETADATA.get() 			+ "|" +
								EnumRegexList.SELECTSEPARATORFLAG.get() 		+ "|" +
								EnumRegexList.SELECTCONDITIONBLOCKFLAG.get() 	+ "|" +
								EnumRegexList.SELECTBASEIRIFLAG.get() 			+ "|" +
								EnumRegexList.SELECTFIXEDCONTENTFLAG.get() 		+ "|" +
								EnumRegexList.SELECTDATATYPEFLAG.get()			+ "|" +
								EnumRegexList.SELECTCUSTOMDIDFLAG.get()			+ "|" +
								EnumRegexList.SELECTSEARCHBLOCKFLAG.get()		+ "|" +
								EnumRegexList.SELECTSEARCHBLOCKFLAG.get()		+ "|" +
								EnumRegexList.SELECTCOLFLAG.get()				+ "|" +
								EnumRegexList.SELECTNODEFLAG.get();


		Matcher	matcher = Utils.matchRegexOnString(flagsToCheck, sentence);
		while(!matcher.hitEnd()){

			String matcherString = matcher.group();

			if(matcherString.contains("/SP")){
				flagsList.add(extractDataFromFlagSeparatorFromSentence(sentence, EnumRegexList.SELECTSEPARATORFLAG.get()));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTSEPARATORFLAG.get(), sentence);
			}

			else if(matcherString.contains("/CE")){
				flagsList.add(extractDataFromFlagConditionFromSentence(sentence, EnumRegexList.SELECTCONDITIONBLOCKFLAG.get()));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTCONDITIONBLOCKFLAG.get(), sentence);
			}

			else if(matcherString.contains("/SE")){
				flagsList.add(extractDataFromFlagSearchBlockFromSentence(sentence, EnumRegexList.SELECTSEARCHBLOCKFLAG.get()));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTSEARCHBLOCKFLAG.get(), sentence);
			}

			else if(matcherString.contains("/DefaultValue")){
				flagsList.add(extractDataFromFlagFixedContentFromSentence(sentence, EnumRegexList.SELECTFIXEDCONTENTFLAG.get()));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTFIXEDCONTENTFLAG.get(), sentence);
			}

			else if(matcherString.contains("/NM")){
				flagsList.add(new FlagNotMetadata(true));
				sentence = Utils.removeRegexFromContent("\\/(NM)", sentence);
			}

			else if(matcherString.contains("/BASEIRI")){
				flagsList.add(extractDataFromFlagBaseIRIFromSentence(sentence, EnumRegexList.SELECTBASEIRIFLAG.get()));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTBASEIRIFLAG.get(), sentence);
			}

			else if(matcherString.contains("/ID")){
				flagsList.add(extractDataFromFlagCustomIDFromSentence(sentence, EnumRegexList.SELECTCUSTOMDIDFLAG.get()));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTCUSTOMDIDFLAG.get(), sentence);
			}

			else if(matcherString.contains("/DT")){
				flagsList.add(extractDataFromFlagDataTypeFromSentence(sentence, EnumRegexList.SELECTDATATYPEFLAG.get()));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTDATATYPEFLAG.get(), sentence);
			}

			else if(matcherString.contains("/COL")){
				flagsList.add(FlagCol.extractFlagColFromSentence(sentence));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTCOLFLAG.get(), sentence);
			}

			else if(matcherString.contains("/NODE")){
				flagsList.add(extractFlagNodeFromSentence(sentence));
				sentence = Utils.removeRegexFromContent(EnumRegexList.SELECTNODEFLAG.get(), sentence);
			}

			matcher.find();
		}

		return flagsList;
	}

	private FlagNode		extractFlagNodeFromSentence(String sentence) throws ClassNotFoundInOntologyException {

		String nodeTypeString = Utils.matchRegexOnString(EnumRegexList.SELECTNODEFLAG.get(), sentence).group(1);

		return new FlagNode(ontologyHelper.getClass(nodeTypeString));

	}

	private Flag 			extractDataFromFlagDataTypeFromSentence(String sentence, String regex) throws Exception {
		String contentFromQuotationMark = null;
		try{
			contentFromQuotationMark = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(sentence, regex);
			return new FlagDataType(contentFromQuotationMark);
		}catch (Exception e){
			throw new Exception("Not found XSD datatype for '" + contentFromQuotationMark + "'");
		}

	}

	private Flag 			extractDataFromFlagCustomIDFromSentence(String sentence, String regex) {
		String contentFromQuotationMark = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(sentence, regex);

		return new FlagCustomID(contentFromQuotationMark);
	}

	private Flag 			extractDataFromFlagBaseIRIFromSentence(String sentence, String regex) {
		sentence = Utils.matchRegexOnString(regex, sentence).group();

		String iri = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(sentence, regex);
		String namespace = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(sentence, EnumRegexList.SELECTNAMESPACEBASEIRIFLAG.get());


		return new FlagBaseIRI(iri, namespace);
	}

	private Flag 			extractDataFromFlagFixedContentFromSentence(String sentence, String regex) {
		String contentFromQuotationMark = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(sentence, regex);

		return new FlagFixedContent(contentFromQuotationMark);
	}

	private Flag 			extractDataFromFlagConditionFromSentence(String sentence, String regex) {

		String cbFlagTerm = Utils.matchRegexOnString(regex, sentence).group();
		String matchedConditionSelected = Utils.matchRegexOnString("\\((\\w+)\\)", cbFlagTerm).group(1);

		String id = matchedConditionSelected;

		return new FlagConditionBlock(id);
	}

	private Flag 			extractDataFromFlagSearchBlockFromSentence(String sentence, String regex) throws Exception {

		String id = Utils.matchRegexOnString(regex, sentence).group(1);
		String variable = Utils.matchRegexOnString(regex, sentence).group(2);

		if(variable.equals("") || !variable.startsWith("?"))
			throw new Exception("Variable of the search element flag is missing or is invalid. The variable must begin with '?'. Ocorrence: " + sentence);

		return new FlagSearchBlock(id, variable);
	}

	private Flag 			extractDataFromFlagSeparatorFromSentence(String sentence, String regex) throws CustomExceptions {

		sentence = Utils.matchRegexOnString(regex, sentence).group();
		String contentFromQuotationMark = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(sentence, regex);
		String contentWithoutQuotationMark = Utils.removeDataFromFirstQuotationMarkBlockInsideRegex(sentence, regex);


		List<Integer> columnsSelected = new ArrayList<Integer>();

		//Extract range
		if(contentWithoutQuotationMark.contains(",")) {
			String[] rangeString = contentWithoutQuotationMark.split(",");

			for(int i = 1; i < rangeString.length; i++) {

				rangeString[i] = rangeString[i].replaceAll("\\)", "").trim();

				if(rangeString[i].contains(":")) {
					//Columns specified are in a interval
					int start, end = 0;
					String[] rangeNumbers = rangeString[i].split(":");
					try {
						start = Integer.parseInt(rangeNumbers[0]);
						end = Integer.parseInt(rangeNumbers[1]);
					}catch (Exception e){
						throw new SeparatorFlagException("Value specified as column number is not a number. Instruction: " + contentWithoutQuotationMark);
					}

					for(int ii = start; ii <= end; ii++)
						columnsSelected.add(ii - 1);

				} else {
					//The columns specified are not in an interval
					try {
						columnsSelected.add(Integer.parseInt(rangeString[i]) - 1);
					}catch (Exception e){
						e.printStackTrace();
						throw new SeparatorFlagException("Value specified as column number is not a number. Instruction: " + contentWithoutQuotationMark);

					}
				}
			}
		}else {
			//Case there are no columns specified
			if(columnsSelected.size() < 1){
				columnsSelected.add(Integer.MAX_VALUE);
			}
		}

		return new FlagSeparator(contentFromQuotationMark, columnsSelected);
	}

	private String 			extractRuleIDFromSentence(String blockRulesAsText) {
		String data = "";

		Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTRULEID.get(), blockRulesAsText);
		//data = matcher.group().replace("matrix_rule[", "").replace("simple_rule[", "");
		data = matcher.group(2);
		return data;
	}

	private OWLClass 		extractSubjectFromSentence(String blockRulesAsText) throws Exception {

		String subjectString = Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(blockRulesAsText, EnumRegexList.SELECTSUBJECTCLASSNAME.get());

		OWLClass subject = ontologyHelper.getClass(subjectString);

		if(subject == null)
			throw new ClassNotFoundInOntologyException("Not found any ontology class with label '" + subjectString + "'");

		return subject;
	}

	private List<String> 	identifyRuleBlocksFromString(String fileContent) throws Exception {
		List<String> ruleBlocks = new ArrayList<>();
		try {
			List<String> identifiedBlocks = identifyBlocksFromString(fileContent);
			for(String block : identifiedBlocks){
				if(block.startsWith("column_based_rule") || block.startsWith("row_based_rule"))
					ruleBlocks.add(block);
			}
		}catch(Exception e){
			throw new Exception("No rule block identified in your file of rules. Please check your file.");
		}

		return ruleBlocks;
	}

	public static List<String> identifyConfigBlocksFromString(String fileContent) throws Exception {
		List<String> configBlocks = new ArrayList<>();

		List<String> identifiedBlocks = identifyBlocksFromString(fileContent);
		if(identifiedBlocks.size() == 0)
			throw new Exception("No rule_config, simple_rule or matrix_rule blocks identified in your file of rules. Please check your file.");

		for(String block : identifiedBlocks){
			if(block.startsWith("config_element"))
				configBlocks.add(block);
		}

		if(configBlocks.size() == 0)
			throw new Exception("No config rule block identified in your file of rules. Please check your file.");
		return configBlocks;
	}

	public static List<String> 	identifyBlocksFromString(String fileContent) {
		Pattern patternToFind = Pattern.compile(EnumRegexList.SELECTELEMENTSBLOCKS.get());
		Matcher matcher = patternToFind.matcher(fileContent);
		//matcher.find();

		List<String> identifiedRules = new ArrayList<String>();
		while(matcher.find()){
			identifiedRules.add(matcher.group());
		}

		return identifiedRules;
	}

	public 	void 			writeRDF(TriplesProcessing triplesProcessing, String transformationId) throws Exception {
		System.out.println("#####################################\nWriting RDF...");
		Date startTime = Calendar.getInstance().getTime();
		try{

			Lang lang = triplesProcessing.defaultRuleConfig.getSyntax();

			TransformationManagerImpl manager = new TransformationManagerImpl();
			File triplesetFolderPath = new File(manager.localStorage + "/" + transformationId,
							  manager.properties.getProperty("transformations.triplesetSubpath"));
			if(!triplesetFolderPath.exists())
				triplesetFolderPath.mkdir();

			File tripleset = new File(triplesetFolderPath, "RDFTriples." + lang.getFileExtensions().get(0));

			FileOutputStream fos = new FileOutputStream(tripleset);
			//RDFDataMgr.write(fos, model, Lang.TRIG);
			//RDFDataMgr.write(fos, model, Lang.TURTLE);
			//RDFDataMgr.write(fos, triplesProcessing.getModel(), Lang.RDFXML);
			RDFDataMgr.write(fos, triplesProcessing.getModel(), lang);
			//RDFDataMgr.write(fos, triplesProcessing.getModel(), lang);
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}

		Date stopTime = Calendar.getInstance().getTime();
		long elapsedTime = stopTime.getTime() - startTime.getTime();
		System.out.println("Wrote RDF in " + elapsedTime / 1000 + " secs");
		System.out.println("Processing Finished time:" + stopTime);
		elapsedTime = stopTime.getTime() - triplesProcessing.startTime.getTime();
		System.out.println("Processed in " + elapsedTime / 1000 + " secs");
	}

	public void runQuery(String transformationId, String queryId){
		fileSystemManager = new TransformationManagerImpl();

		System.out.println("Query ID: " + queryId);
		SPARQLQueryProcessing queryProcessing = new SPARQLQueryProcessing();
		try {
			queryProcessing.QuerySelect(transformationId, queryId);
			fileSystemManager.updateQueryStatus(transformationId, queryId, EnumActivityState.SUCCEEDED);
		} catch (Exception e) {
			try {
				fileSystemManager.updateQueryStatus(transformationId, queryId, EnumActivityState.FAILED);
				fileSystemManager.logQueryError(transformationId, queryId, e);

			} catch (StatusFileException | ErrorFileException w) {
				System.out.println(w.getMessage());
			}
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
