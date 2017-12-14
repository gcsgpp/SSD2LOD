package br.usp.ffclrp.dcm.lssb.transformation_software;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.ValidityReport.Report;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.semanticweb.owlapi.model.OWLProperty;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.BaseIRIException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.ConditionBlockException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.CustomExceptions;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.SeparatorFlagException;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Condition;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumOperationsConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Flag;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagBaseIRI;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagCustomID;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagDataType;
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
	private Model ontology = null;
	String relativePathOntologyFile = null;

	public TriplesProcessing(String relativePathDataFile, String relativePathOntologyFile) {
		this.relativePathOntologyFile = relativePathOntologyFile;

		this.model = ModelFactory.createDefaultModel();
		//model.read(relativePathOntologyFile); //load ontology and add its axioms to the linked graph
		fileReader = new SemistructuredFileReader(relativePathDataFile);

		this.ontology = ModelFactory.createDefaultModel().read(relativePathOntologyFile);
	}

	@SuppressWarnings("unchecked")
	public void createTriplesFromRules(List<Rule> listRules, Map<Integer, ConditionBlock> conditionBlocks,  String defaultNs) throws Exception{	

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

		loadNamespacesFromOntology();
		writeRDF(model, "RDFtriples.rdf");
		checkConsistency();
	}

	private List<Resource> processRule(Rule rule, Integer tsvLineNumber, String defaultNs) throws Exception {

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
					List<TSVColumn> dataColumns = (List<TSVColumn>) predicateMapEntry.getValue().getObject();
					List<String> content = extractDataFromTSVColumn(dataColumns, tsvLineNumber);

					if(content.size() > 1) {
						for(String contentElement : content){
							if(contentElement != null && contentElement != "")
								addTripleToModel(subject, predicate, contentElement, XSDDatatype.XSDstring);
						}

					} else {
						TSVColumn firstDataColumn = dataColumns.iterator().next(); //iterator to get the first element of the list
						XSDDatatype datatype = getDataTypeContentFlag(firstDataColumn); //get the first element, not necessarily the index 0
						
						String firstContent = content.iterator().next();
						addTripleToModel(subject, predicate, firstContent, datatype);
					}
				}
			}
		}

		return subjectList;
	}

	private boolean assertConditionBlock(List<Flag> flags, Integer tsvLineNumber) throws CustomExceptions {
		for(Flag flag : flags){
			if(flag instanceof FlagConditionBlock){

				if(conditionBlocks.isEmpty())
					throw new ConditionBlockException("No condition block created");

				ConditionBlock conditionBlock = conditionBlocks.get(((FlagConditionBlock) flag).getId());

				for(Condition condition : conditionBlock.getConditions()){
					String contentTSVColumn = fileReader.getData(condition.getColumn(), tsvLineNumber);
					Boolean result = true;
					if(condition.getOperation() == EnumOperationsConditionBlock.EQUAL){
						result = contentTSVColumn.equals(condition.getConditionValue());
					}

					if(condition.getOperation() == EnumOperationsConditionBlock.DIFFERENT){
						if(contentTSVColumn.equals(condition.getConditionValue()))
							result = false;
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
	
	private void addTripleToModel(Resource subject, Property predicate, Resource object) {
		//System.out.println("S: " + subject.getURI() + " P: " + predicate.getURI() + " O: " + object.getURI());
		subject.addProperty(predicate, object);		
	}

	private void addTripleToModel(Resource subject, Property predicate, String contentElement, XSDDatatype datatype) {
		//System.out.println("S: " + subject.getURI() + " P: " + predicate.getURI() + " O: " + contentElement);
		subject.addProperty(predicate, contentElement, datatype);
	}

	private List<String> extractDataFromTSVColumn(List<TSVColumn> listTSVColumn, Integer lineNumber) throws Exception {
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

			//IF ANY OF THE SPECIAL CASES (FLAGS ABOVE) WERE MET, EXTRACT THE CONTENT NORMALLY. 
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
		//THE DATA AT THE BEGGINING OF THE ARRAY ITSELF.
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

	private String[] separateDataFromTSVColumn(FlagSeparator flag, String columnTitle, Integer lineNumber) throws Exception {
		String rawData = fileReader.getData(columnTitle, lineNumber);

		String[] splitData = null;
		if(rawData.contains(flag.getTerm()))
			splitData = rawData.split(flag.getTerm());
		else {
			throw new SeparatorFlagException("There is no caractere '" + flag.getTerm() +
					"' in the field '" + columnTitle + "' to be used as separator");
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

	private List<Resource> getSubject(Rule rule, String defaultNs, Integer lineNumber) throws Exception {
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

	private XSDDatatype getDataTypeContentFlag(TSVColumn column) {
		for(Flag flag : column.getFlags()){
			if(flag instanceof FlagDataType){
				return ((FlagDataType) flag).getDatatype();
			}
		}
		return null;
	}

	private String getBASEIRIFlag(List<TSVColumn> listTSVColumn) throws BaseIRIException {
		for(TSVColumn column : listTSVColumn){
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagBaseIRI){
					FlagBaseIRI flagBase = (FlagBaseIRI) flag;

					if(flagBase.getNamespace() == null)
						throw new BaseIRIException("Some BaseIRI flag has an empty namespace field.");
					if(flagBase.getIRI() == null)
						throw new BaseIRIException("Some BaseIRI flag has an empty IRI field.");

					return ((FlagBaseIRI) flag).getIRI();
				}
			}
		}
		return null;
	}

	private void loadNamespacesFromOntology() {
		Set<String> namespacesUsed = new HashSet<String>();

		model.listStatements().forEachRemaining(s -> { 
			Triple triple = s.asTriple();
			namespacesUsed.add(triple.getSubject().getNameSpace());
			namespacesUsed.add(triple.getPredicate().getNameSpace());
			if(triple.getObject().isURI())
				namespacesUsed.add(triple.getObject().getNameSpace());
		});

		namespacesUsed.forEach(n -> System.out.println(n));



		Model tempModel = ModelFactory.createDefaultModel();
		tempModel.read(relativePathOntologyFile);

		tempModel.getNsPrefixMap().forEach((k, v) -> {
			if(namespacesUsed.contains(v))
				model.setNsPrefix(k, v);
		});
	}

	public void writeRDF(Model modelToPrint, String filename){
		System.out.println("#####################################\nWriting RDF...");
		long startTime = System.currentTimeMillis();
		try{
			File f = new File(filename);
			FileOutputStream fos = new FileOutputStream(f);
			//RDFDataMgr.write(fos, modelToPrint, Lang.TRIG);
			//RDFDataMgr.write(fos, modelToPrint, Lang.TURTLE);
			RDFDataMgr.write(fos, modelToPrint, Lang.RDFXML);
			//RDFDataMgr.write(fos, modelToPrint, Lang.NTRIPLES);
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Wrote RDF in " + elapsedTime / 1000 + " secs");
	}

	private void checkConsistency() {
		System.out.println("#####################################\nChecking consistency...");
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner().bindSchema(ontology);
		InfModel inf = ModelFactory.createInfModel(reasoner, model);

		ValidityReport report = inf.validate();
		System.out.println("Is valid?:" + report.isValid());

		if(!report.isValid()) {
			System.out.println("--------> Conflicts:");
			for(Iterator<Report> i = report.getReports(); i.hasNext(); ) {
				System.out.println("-> "+ i.next());
			}
		}
		System.out.println("Is Clean?:" + report.isClean());

		System.out.println("\n ----- \n");
	}

	public Model getModel() {
		return this.model;
	}

}
