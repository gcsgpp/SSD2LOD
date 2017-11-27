package br.usp.ffclrp.dcm.lssb.transformation_software;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.semanticweb.owlapi.model.OWLProperty;

import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Condition;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumOperationsConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Flag;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagBaseIRI;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagCustomID;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagFixedContent;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagNotMetadata;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ObjectAsRule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Rule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagSeparator;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObject;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObjectAsRule;

public class TriplesProcessing {

	private SemistructuredFileReader fileReader;
	private Model model = null;
	private Map<Integer, Rule> allRules = new HashMap<Integer, Rule>();
	private List<Rule> regularRuleList;
	private List<Rule> dependencyList = new ArrayList<Rule>();
	private Map<Integer, ConditionBlock> conditionBlocks;


	public TriplesProcessing(String relativePathDataFile, String relativePathOntologyFile) {
		model = ModelFactory.createDefaultModel();
		loadNamespacesFromOntology(relativePathOntologyFile);
		//model.read(relativePathOntologyFile); //load ontology and add its axioms to the linked graph
		fileReader = new SemistructuredFileReader(relativePathDataFile);
	}

	private void loadNamespacesFromOntology(String relativePathOntologyFile) {
		Model tempModel = ModelFactory.createDefaultModel();
		tempModel.read(relativePathOntologyFile);
		tempModel.getNsPrefixMap().forEach((k, v) -> model.setNsPrefix(k, v));

		for(Entry<String, String> e : model.getNsPrefixMap().entrySet()) {
			System.out.println(e.getKey() + "-->" + e.getValue());
		}
	}

	@SuppressWarnings("unchecked")
	public void createTriplesFromRules(List<Rule> listRules, Map<Integer, ConditionBlock> conditionBlocks,  String defaultNs){	

		this.regularRuleList = listRules;
		this.conditionBlocks = conditionBlocks;

		for(Rule rule : regularRuleList){	
			allRules.put(Integer.parseInt(rule.getId()), rule);
		}

		//POPULATE dependencyList
		for(Rule rule : regularRuleList){	
			Map<OWLProperty, TripleObject> predicateObjectMAP = rule.getPredicateObjects();
			for(Map.Entry<OWLProperty, TripleObject> predicateMapEntry : predicateObjectMAP.entrySet()){
				if(predicateMapEntry.getValue() instanceof TripleObjectAsRule){
					List<ObjectAsRule> listOfRulesAndFlagsOfAPredicate = (List<ObjectAsRule>) predicateMapEntry.getValue().getObject();
					for(ObjectAsRule objectRulesWithFlags : listOfRulesAndFlagsOfAPredicate)
						dependencyList.add(allRules.get(objectRulesWithFlags.getRuleNumber()));
				}
			}
		}

		for(Rule rule : dependencyList){	
			regularRuleList.remove(rule);
		}

		for(Rule rule : regularRuleList){
			for(Integer tsvLineNumber = 1; tsvLineNumber < fileReader.getAllDataRows().size(); tsvLineNumber++){
				processRule(rule, tsvLineNumber, defaultNs);
			}
		}

		writeRDF();
	}

	private List<Resource> processRule(Rule rule, Integer tsvLineNumber, String defaultNs) {

		for(TSVColumn ruleColumns : rule.getSubjectTSVColumns()){
			if(!assertConditionBlock(ruleColumns.getFlags(), tsvLineNumber))
				return null;
		}

		// *** SUBJECT ***
		List<Resource> subjectList = getSubject(rule, defaultNs, tsvLineNumber); //ONE SUBJECT FOR EACH ITEM IN THE CELL

		// *** PREDICATE AND OBJECT ***
		Property predicate;

		for(Resource subject : subjectList){

			Map<OWLProperty, TripleObject> predicateObjectMAP = rule.getPredicateObjects();
			for(Map.Entry<OWLProperty, TripleObject> predicateMapEntry : predicateObjectMAP.entrySet()){
				//GET PREDICATE IRI
				predicate = model.createProperty(predicateMapEntry.getKey().getIRI().toString());

				//TRIPLEOBJECTS POINTING TO A RULE PROCESS FLOW
				if(predicateMapEntry.getValue() instanceof TripleObjectAsRule){
					TripleObjectAsRule tripleObjectAsRule = (TripleObjectAsRule) predicateMapEntry.getValue();
					for(ObjectAsRule objectAsRule : tripleObjectAsRule.getObject()){
						if(assertConditionBlock(objectAsRule.getFlags(), tsvLineNumber)){
							List<Resource> subjectsFromDependentRule = processRule(allRules.get(objectAsRule.getRuleNumber()), tsvLineNumber, defaultNs);
							if(subjectsFromDependentRule != null){
								for(Resource singleSubjectFromDependentRule : subjectsFromDependentRule){
									addTripleToModel(subject, predicate, singleSubjectFromDependentRule);
								}
							}
						}
					}
					//TRIPLEOBJECTS POINTING TO LIST OF TSV COLUMNS PROCESS FLOW
				}else{
					@SuppressWarnings("unchecked")
					List<String> content = extractDataFromTSVColumn((List<TSVColumn>) predicateMapEntry.getValue().getObject(), tsvLineNumber);

					/*if(content == null)	{
						content = new ArrayList<String>();
						Random random = new Random(999999);
						Integer randomNumber = random.nextInt();
						content.add(randomNumber.toString());
					}*/

					for(String contentElement : content){
						if(contentElement != null && contentElement != "")
							addTripleToModel(subject, predicate, contentElement);
					}
				}
			}
		}

		return subjectList;

	}

	private boolean assertConditionBlock(List<Flag> flags, Integer tsvLineNumber) {
		for(Flag flag : flags){
			if(flag instanceof FlagConditionBlock){

				if(conditionBlocks.isEmpty())
					throw new NullPointerException("No condition block created");

				ConditionBlock conditionBlock = conditionBlocks.get(((FlagConditionBlock) flag).getId());

				for(Condition condition : conditionBlock.getConditions()){
					String contentTSVColumn = fileReader.getData(condition.getColumn(), tsvLineNumber);
					Boolean result = true;
					if(condition.getOperation() == EnumOperationsConditionBlock.EQUAL){
						result = contentTSVColumn.equals(condition.getConditionValue());
					}

					if(condition.getOperation() == EnumOperationsConditionBlock.DIFFERENT){
						result = compareDifferent(contentTSVColumn, condition.getConditionValue());
					}

					if(condition.getOperation() == EnumOperationsConditionBlock.LESSTHAN){
						result = Double.parseDouble(contentTSVColumn) < Double.parseDouble(condition.getConditionValue());
					}

					if(condition.getOperation() == EnumOperationsConditionBlock.GREATERTHAN){
						result = Double.parseDouble(contentTSVColumn) > Double.parseDouble(condition.getConditionValue());
					}

					if(!result)
						return false;
				}
			}
		}		

		return true;
	}

	private Boolean compareDifferent(String str1, String str2){
		char[] chr1 = str1.toCharArray();
		char[] chr2 = str2.toCharArray();

		if(chr1.length != chr2.length)
			return true;

		for(int i = 0; i < chr1.length; i++){
			if(chr1[i] != chr2[i]) return true;
		}

		return false;
	}

	private void addTripleToModel(Resource subject, Property predicate, Resource object) {
		//System.out.println("S: " + subject.getURI() + " P: " + predicate.getURI() + " O: " + object.getURI());
		subject.addProperty(predicate, object);		
	}

	private void addTripleToModel(Resource subject, Property predicate, String contentElement) {
		//System.out.println("S: " + subject.getURI() + " P: " + predicate.getURI() + " O: " + contentElement);
		subject.addProperty(predicate, contentElement);
	}

	private List<String> extractDataFromTSVColumn(List<TSVColumn> listTSVColumn, Integer lineNumber) {
		List<String> objectContent = new ArrayList<String>();


		List<String[]> dataColumnsSeparated = new ArrayList<String[]>();
		for(TSVColumn column : listTSVColumn){
			Boolean extractedData = false;

			//PROCESS THE SEPARATOR FLAG
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagSeparator){
					dataColumnsSeparated.add(separateDataFromTSVColumn((FlagSeparator) flag, column.getTitle(), lineNumber));
					extractedData = true;
				}else if(flag instanceof FlagNotMetadata) {
					String[] columnData = new String[1];
					columnData[0] = fileReader.getData(column.getTitle(), 0);
					dataColumnsSeparated.add(columnData);
					extractedData = true;
				}else if(flag instanceof FlagFixedContent) {
					String[] columnData = new String[1];
					columnData[0] = ((FlagFixedContent) flag).getContent();
					dataColumnsSeparated.add(columnData);
					extractedData = true;
				}
			}


			if(!extractedData){
				String[] columnData = new String[1];
				columnData[0] = fileReader.getData(column.getTitle(), lineNumber);
				dataColumnsSeparated.add(columnData);
			}
		}

		//FINDS THE BIGGER ARRAY OF DATA EXTRACTED
		Integer biggerColumn = Integer.MIN_VALUE;
		for(String[] array : dataColumnsSeparated){
			if(array.length > biggerColumn)
				biggerColumn = array.length;
		}

		//MAKES ALL ARRAYS TO BE THE SAME SIZE. 
		//THE ARRAYS THAT IS SMALLER THAN THE BIGGEST IS COMPLETED WITH 
		//THE DATA AT THE BEGGINING OF THE ARRAY IT SELF.
		List<List<String>> dataColumns = new ArrayList<List<String>>();
		for(String[] array : dataColumnsSeparated){
			List<String> list = new ArrayList<String>();
			int oldPosition = 0;
			while(list.size() != biggerColumn.intValue()) {
				try {
					list.add(array[oldPosition]);
				}catch(ArrayIndexOutOfBoundsException e){
					oldPosition = 0;
				}
			}
			dataColumns.add(list);
		}

		//MERGE BETWEEN THE DATA ARRAYS EXTRACTED
		//ONE ITEM FROM EACH ARRAY
		for(int i = 0; i < biggerColumn; i++){
			String content = "";
			for(List<String> list : dataColumns){
				content += " " + list.get(i);
			}
			content = content.trim();
			objectContent.add(content);
		}

		return objectContent;
	}

	private String[] separateDataFromTSVColumn(FlagSeparator flag, String columnTitle, Integer lineNumber) {
		String rawData = fileReader.getData(columnTitle, lineNumber);

		String[] splitData = null;
		try{
			splitData = rawData.split(flag.getTerm());
		}catch (Exception e) {
			System.out.println(	"There is no caractere '" + flag.getTerm() +
					"' on the field '" + columnTitle + "' to be used as separator");
			e.printStackTrace();
		}

		if(flag.getColumns().get(0).equals(Integer.MAX_VALUE))
			return splitData; //If MAX_VALUE all columns were selected

		String[] resultData = new String[flag.getColumns().size()];

		for(int i = 0; i < flag.getColumns().size(); i++){
			int colNumber = flag.getColumns().get(i);
			if(splitData.length - 1 >= colNumber)
				resultData[i] = splitData[colNumber];
		}

		return resultData;
	}

	private List<Resource> getSubject(Rule rule, String defaultNs, Integer lineNumber) {
		List<Resource> subjectList = new ArrayList<Resource>();

		Resource subjectType = model.createResource(rule.getSubject().getIRI().toString());

		String baseIRIFlag = getBASEIRIFlag(rule.getSubjectTSVColumns());
		if(baseIRIFlag != null){
			defaultNs = baseIRIFlag;
		}

		String customIDFlag = getCustomIDFlag(rule.getSubjectTSVColumns());
		if(customIDFlag != null) {
			subjectList.add(model.createResource(defaultNs + customIDFlag, subjectType));
			
		
		}else{
			String fixedContentFlag = getFixedContentFlag(rule.getSubjectTSVColumns());
			if(fixedContentFlag != null){
				subjectList.add(model.createResource(defaultNs + fixedContentFlag, subjectType));

			}else {
				List<String> subjectContentRaw = extractDataFromTSVColumn(rule.getSubjectTSVColumns(), lineNumber);

				List<String> subjectContent = new ArrayList<String>();
				for(String content : subjectContentRaw){
					subjectContent.add(content.replaceAll(" ", "_"));
				}

				for(String individualContent : subjectContent){
					subjectList.add(model.createResource(defaultNs + individualContent, subjectType));
				}
			}
		}

		return subjectList;
	}

	/* private List<TSVColumn> getCustomIDFlag(List<TSVColumn> listTSVColumn) {
		TSVColumn customID = new TSVColumn();
		for(TSVColumn column : listTSVColumn){
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagCustomID){
					customID.setTitle(((FlagCustomID) flag).getContent());
				}
			}
		}

		List<TSVColumn> temp = new ArrayList<TSVColumn>();
		temp.add(customID);

		return temp;
	}

	private boolean hasCustomIDFlag(List<TSVColumn> listTSVColumn) {
		for(TSVColumn column : listTSVColumn){
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagCustomID){
					return true;
				}
			}
		}
		return false;
	}
	 */

	private String getCustomIDFlag(List<TSVColumn> listTSVColumn) {
		for(TSVColumn column : listTSVColumn){
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagCustomID){
					return ((FlagCustomID) flag).getContent();
				}
			}
		}
		return null;
	}

	private String getFixedContentFlag(List<TSVColumn> listTSVColumn) {
		for(TSVColumn column : listTSVColumn){
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagFixedContent){
					return ((FlagFixedContent) flag).getContent();
				}
			}
		}
		return null;
	}

	private String getBASEIRIFlag(List<TSVColumn> listTSVColumn) {
		for(TSVColumn column : listTSVColumn){
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagBaseIRI){
					model.setNsPrefix(((FlagBaseIRI) flag).getNamespace(), ((FlagBaseIRI) flag).getIRI());
					return ((FlagBaseIRI) flag).getIRI();
				}
			}
		}
		return null;
	}

	public void writeRDF(){
		System.out.println("#####################################\n=== Writing RDF... \n");
		long startTime = System.currentTimeMillis();
		try{
			File f = new File("teste.rdf");
			FileOutputStream fos;
			fos = new FileOutputStream(f);
			//RDFDataMgr.write(fos, model, Lang.TRIG);
			//RDFDataMgr.write(fos, model, Lang.TURTLE);
			//RDFDataMgr.write(fos, model, Lang.RDFXML);
			//RDFDataMgr.write(fos, model, Lang.NTRIPLES);
			fos.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("=== Wrote RDF in " + elapsedTime / 1000 + " secs ===\n#####################################\n\n");
	}

	public Model getModel() {
		return this.model;
	}

}
