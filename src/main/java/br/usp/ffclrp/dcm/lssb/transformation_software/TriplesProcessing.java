package br.usp.ffclrp.dcm.lssb.transformation_software;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.*;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.*;
import openllet.jena.PelletReasonerFactory;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.ValidityReport.Report;
import org.semanticweb.owlapi.model.OWLProperty;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class TriplesProcessing {

	private SemistructuredFileReader fileReader;
	private Model model = null;
	private Map<String, Rule> allRules = new HashMap<>();
	private List<Rule> regularRuleList;
	private List<Rule> dependencyList = new ArrayList<Rule>();
	private Map<String, ConditionBlock> conditionBlocks;
	private Map<String, SearchBlock> searchBlocks;
	public 	RuleConfig defaultRuleConfig;
	private MatrixLineNumberTracking currentLineNumberMatrixRules = new MatrixLineNumberTracking();
	private Map<String, List<Resource>> rulesAlreadyProcessed = new HashMap<>();
	public 	Date startTime;
	private List<String> ontologiesList = new ArrayList<>();

	public 					TriplesProcessing() {
		startTime = Calendar.getInstance().getTime();
		System.out.println("Processing Started time:" + Calendar.getInstance().getTime().toString());
		this.model = ModelFactory.createDefaultModel();
		//model.read(relativePathOntologyFile); //load ontology and add its axioms to the linked graph
		fileReader = new SemistructuredFileReader();
	}

	public void 			addDatasetToBeProcessed(String relativePathDataFile) throws IOException {
		fileReader.addFilesToBeProcessed(relativePathDataFile);
	}
	@SuppressWarnings("unchecked")
	public void 			createTriplesFromRules(List<Rule> listRules,
												  Map<String, ConditionBlock> conditionBlocks,
												  Map<String, SearchBlock> searchBlocks,
												  RuleConfig defaultRuleConfig,
												  List<String> ontologiesList) throws Exception{

		if(fileReader.getFilesAdded() <= 0)
			throw new NoFilesAddedException("No files were added to be processed.");

		this.regularRuleList 	= listRules;
		this.conditionBlocks 	= conditionBlocks;
		this.searchBlocks 		= searchBlocks;
		this.defaultRuleConfig 	= defaultRuleConfig;
		this.ontologiesList		= ontologiesList;

		for(Rule rule : regularRuleList){	
			allRules.put(rule.getId(), rule);
		}

		//POPULATE dependencyList
		for(Rule rule : regularRuleList){
			MultiValuedMap<OWLProperty, TripleObject> predicateObjectMAP = rule.getPredicateObjects();
			for(Map.Entry<OWLProperty, TripleObject> predicateMapEntry : predicateObjectMAP.entries()){
				if(predicateMapEntry.getValue() instanceof TripleObjectAsRule){
					List<ObjectAsRule> listOfRulesAndFlagsOfAPredicate = (List<ObjectAsRule>) predicateMapEntry.getValue().getObject();
					for(ObjectAsRule objectRulesWithFlags : listOfRulesAndFlagsOfAPredicate) {
						Rule objectRule = allRules.get(objectRulesWithFlags.getRuleId());
						if(objectRule == null)
							throw new RuleNotFound("Rule " + objectRulesWithFlags.getRuleId() + " was not found/created check your file.");
						dependencyList.add(objectRule);
					}
				}
			}
		}

		for(Rule rule : dependencyList){	
			regularRuleList.remove(rule);
		}

		System.out.println("Processing rules...");
 		for(Rule rule : regularRuleList){
			//System.out.println("Processing rule: " + rule.getId());
			processRule(rule);
		}

		loadNamespacesFromOntology();
	}

	private List<Resource> 	processRule(Rule rule) throws Exception {
//		System.out.println("Processing rule: " + rule.getId());
		List<Resource> subjectListToBeReturned = new LinkedList<>();
		if(rule.isMatrix()){
			//if(currentLineNumberMatrixRules.isEmpty()){
			if(rulesAlreadyProcessed.get(rule.getId()) == null) {
				for (Integer tsvLineNumber = rule.getStartLineNumber(); tsvLineNumber < fileReader.getLinesCount(); tsvLineNumber++) {
					//currentLineNumberMatrixRules = new MatrixLineNumberTracking(tsvLineNumber, rule.getId());

					Boolean conditionBlock = true;
					for (TSVColumn ruleColumns : rule.getSubjectTSVColumns()) {
						if (!assertConditionBlock(ruleColumns.getFlags(), tsvLineNumber))
							conditionBlock = false;
					}

					if (conditionBlock) {
						if (rule.processedLines.get(tsvLineNumber) == null) {

							// *** SUBJECT ***
							List<Resource> subjectList = getSubject(rule, tsvLineNumber); //ONE SUBJECT FOR EACH ITEM IN THE CELL
							List<Resource> tripleSubjects = processPredicateAndObject(rule, tsvLineNumber, subjectList);
							rule.processedLines.put(tsvLineNumber, tripleSubjects);
							subjectListToBeReturned.addAll(tripleSubjects);
						} else {
							subjectListToBeReturned.addAll(rule.processedLines.get(tsvLineNumber));
						}
					}
				}
			}else{
				subjectListToBeReturned = rulesAlreadyProcessed.get(rule.getId());
			}

//				currentLineNumberMatrixRules = new MatrixLineNumberTracking();
//
//			}else{
//				for(TSVColumn ruleColumns : rule.getSubjectTSVColumns()){
//					if(!assertConditionBlock(ruleColumns.getFlags(), currentLineNumberMatrixRules.getLineNumber()))
//						return null;
//				}
//
//				// *** SUBJECT ***
//				List<Resource> subjectList = getSubject(rule, currentLineNumberMatrixRules.getLineNumber()); //ONE SUBJECT FOR EACH ITEM IN THE CELL
//				subjectListToBeReturned.addAll(processPredicateAndObject(rule, currentLineNumberMatrixRules.getLineNumber(), subjectList));
//			}


		}else{

			if(rulesAlreadyProcessed.get(rule.getId()) == null){
				// *** SUBJECT ***
				List<Resource> subjectList = getSubject(rule, rule.getStartLineNumber()); //ONE SUBJECT FOR EACH ITEM IN THE CELL

				for(Integer tsvLineNumber = rule.getStartLineNumber(); tsvLineNumber < fileReader.getLinesCount(); tsvLineNumber++) {
					for (TSVColumn ruleColumns : rule.getSubjectTSVColumns()) {
						if (!assertConditionBlock(ruleColumns.getFlags(), tsvLineNumber))
							return null;
					}

					List<Resource> subjectsProcessed = processPredicateAndObject(rule, tsvLineNumber, subjectList);

					subjectListToBeReturned = subjectsProcessed;
				}
			}else{
				subjectListToBeReturned = rulesAlreadyProcessed.get(rule.getId());
			}

		}
		rulesAlreadyProcessed.put(rule.getId(), subjectListToBeReturned);
		return subjectListToBeReturned;
	}

	private List<Resource> 	processPredicateAndObject(Rule rule, Integer tsvLineNumber, List<Resource> subjectList) throws Exception {

		// *** PREDICATE AND OBJECT ***
		Property predicate;

		for(Resource subject : subjectList){

			MultiValuedMap<OWLProperty, TripleObject> predicateObjectMAP = rule.getPredicateObjects();
			for(Map.Entry<OWLProperty, TripleObject> predicateMapEntry : predicateObjectMAP.entries()){
				//GET PREDICATE IRI
				predicate = model.createProperty(predicateMapEntry.getKey().getIRI().toString());

				//TRIPLEOBJECTS POINTING TO A RULE PROCESS FLOW
				if(predicateMapEntry.getValue() instanceof TripleObjectAsRule){
					TripleObjectAsRule tripleObjectAsRule = (TripleObjectAsRule) predicateMapEntry.getValue();
					for(ObjectAsRule objectAsRule : tripleObjectAsRule.getObject()){
						if(assertConditionBlock(objectAsRule.getFlags(), tsvLineNumber)){
							List<Resource> subjectsFromDependentRule = processRule(allRules.get(objectAsRule.getRuleId()));
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
					List<String> listOfContent = extractData(dataColumns, tsvLineNumber);


					Object 	 datatype = null;
					FlagNode flagNodeObjectType = null;
					for(TSVColumn column : dataColumns){
						datatype = getDataTypeContentFlag(column); //get the first element
						flagNodeObjectType = getNodeFlagFromTSVColumn(column); //get the first element
					}

					if(datatype != null && flagNodeObjectType != null)
						throw new Exception("The triple object cannot have both Datatype Flag and Node Flag at the same time. Rule: " + rule.getId());

					if(flagNodeObjectType != null){
						Resource nodeType = model.createResource(flagNodeObjectType.getFlagNode().getIRI().toString());
						List<Resource> objectList = getObjects(rule, listOfContent, dataColumns, nodeType);
						for (Resource object : objectList) {
							addTripleToModel(subject, predicate, object);
						}

					}else {
						for (String content : listOfContent) {
							if (content != null && !content.equals(""))
								addTripleToModel(subject, predicate, content, datatype);
						}
					}
				}
			}
		}

		return subjectList;
	}

	private boolean 		assertConditionBlock(List<Flag> flags, Integer tsvLineNumber) throws CustomExceptions, CustomWarnings {
		for(Flag flag : flags){
			if(flag instanceof FlagConditionBlock){

				if(conditionBlocks.isEmpty())
					throw new ConditionBlockException("No condition block created");

				ConditionBlock conditionBlock = conditionBlocks.get(((FlagConditionBlock) flag).getId());
				if(conditionBlock == null)
					throw new ConditionBlockException("Condition block not found: " + ((FlagConditionBlock) flag).getId());

				for(Condition condition : conditionBlock.getConditions()){
					String contentTSVColumn = fileReader.getData(condition, tsvLineNumber);
					if(contentTSVColumn == null)
						return false;


					Boolean result = true;
					if(condition.getOperation() == EnumOperationsConditionBlock.EQUAL){
						result = contentTSVColumn.equals(condition.getConditionValue());
					}

					if(condition.getOperation() == EnumOperationsConditionBlock.DIFFERENT){
						if(contentTSVColumn.equals(condition.getConditionValue()))
							result = false;
					}

					if(condition.getOperation() == EnumOperationsConditionBlock.LESSTHAN){

						try {

							Double value = condition.parsedValues.get(contentTSVColumn);
							if(value == null) {
								value = Double.parseDouble(contentTSVColumn);
								condition.parsedValues.put(contentTSVColumn, value);
							}

							result = value < ((double) condition.getConditionValue());

						}catch (Exception e ){
							result = false;
						}

					}

					if(condition.getOperation() == EnumOperationsConditionBlock.LESSTHANEQUALTO){
						try {

							Double value = condition.parsedValues.get(contentTSVColumn);

							if(value == null) {
								value = Double.parseDouble(contentTSVColumn);
								condition.parsedValues.put(contentTSVColumn, value);
							}

							result = value <= ((double) condition.getConditionValue());

						}catch (Exception e ) {
							result = false;
						}
					}


					if(condition.getOperation() == EnumOperationsConditionBlock.GREATERTHAN){
						try {

							Double value = condition.parsedValues.get(contentTSVColumn);

							if(value == null) {
								value = Double.parseDouble(contentTSVColumn);
								condition.parsedValues.put(contentTSVColumn, value);
							}

							result = value > ((double) condition.getConditionValue());

						}catch (Exception e ) {
							result = false;
						}
					}

					if(condition.getOperation() == EnumOperationsConditionBlock.GREATERTHANEQUALTO){
						try {

							Double value = condition.parsedValues.get(contentTSVColumn);

							if(value == null) {
								value = Double.parseDouble(contentTSVColumn);
								condition.parsedValues.put(contentTSVColumn, value);
							}

							result = value >= ((double) condition.getConditionValue());

						}catch (Exception e ) {
							result = false;
						}
					}


					if(!result)
						return false;
				}
			}
		}		

		return true;
	}

	private void 			addTripleToModel(Resource subject, Property predicate, Resource object) {
		subject.addProperty(predicate, object);		
	}

	private void 			addTripleToModel(Resource subject, Property predicate, String contentElement, Object datatype) {
		//System.out.println("S: " + subject.getURI() + " P: " + predicate.getURI() + " O: " + contentElement);

		if(datatype instanceof String && datatype.equals("Literal")) //defined in the FlagDataType
			datatype = new BaseDatatype("http://www.w3.org/2000/01/rdf-schema#Literal");
		else {
			if(	model.contains(subject, predicate, contentElement))
				return;
			else{
				if(datatype != null){
					subject.addProperty(predicate, contentElement, (RDFDatatype) datatype);
					return;
				}else {
					subject.addProperty(predicate, contentElement);
					return;
				}
			}
		}

		NodeIterator it = model.listObjectsOfProperty(subject, predicate);
		while (it.hasNext()) {
			Node rdfnode = it.next().asNode();
			Boolean datatypeComparisson = rdfnode.getLiteralDatatype().getURI().equals(((RDFDatatype) datatype).getURI());
			Boolean labelComparisson = rdfnode.getLiteralLexicalForm().toString().equals(contentElement);
			if(datatypeComparisson && labelComparisson)
				return;
		}

		subject.addProperty(predicate, contentElement, (RDFDatatype) datatype);
	}

	private List<String> extractData(List<TSVColumn> listTSVColumn, Integer lineNumber) throws Exception {
		List<String> objectContent = new ArrayList<String>();


		List<String[]> dataColumnsSeparated = new ArrayList<String[]>();
		for(TSVColumn column : listTSVColumn){
			String[] columnData = new String[1];
			Boolean extractedData = false;

			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagSeparator){
					String[] separatedData = separateDataFromTSVColumn((FlagSeparator) flag, column, lineNumber);
					if(separatedData == null)
						continue;
					columnData = separatedData;
					extractedData = true;
				}else if(flag instanceof FlagNotMetadata) {

					columnData[0] = fileReader.getData(column, 0);

					if(columnData[0] == null)
						continue;

					extractedData = true;
				}else if(flag instanceof FlagFixedContent) {
					columnData[0] = ((FlagFixedContent) flag).getContent();

					if(columnData[0] == null)
						continue;

					extractedData = true;
				}
			}

			//IF ANY OF THE SPECIAL CASES (FLAGS ABOVE) WERE NOT MET, EXTRACT THE CONTENT NORMALLY.
			if(!extractedData){
				columnData[0] = fileReader.getData(column, lineNumber);

				if(columnData[0] == null)
					continue;
			}

			//IF THERE IS A SEARCH BLOCK SET
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagSearchBlock) {
					String flagId = ((FlagSearchBlock) flag).getId();
					SearchBlock searchBlock = this.searchBlocks.get(flagId);

					//System.out.print("line " + lineNumber + " - ");

					try{
						List<String> externalIRIs = null;
						 while(externalIRIs == null){
						 	//String data = fileReader.getData(column, lineNumber);
						 	externalIRIs = searchBlock.getExternalNode(Arrays.asList(columnData), ((FlagSearchBlock) flag).getVariable());
						 }

						if(!externalIRIs.isEmpty()) {
							columnData = externalIRIs.toArray(new String[0]);
						}else{
						 	columnData = new String[0];
						}

					}catch (NullPointerException e){
						e.printStackTrace();
					}
				}

			}


			if(columnData.length > 0)
				dataColumnsSeparated.add(columnData);
		}

		if(dataColumnsSeparated.isEmpty())
			return objectContent;


		objectContent = mergeContentOfMultipleTSVColumns(dataColumnsSeparated);


		return objectContent;
	}


	private List<String> mergeContentOfMultipleTSVColumns (List<String[]> dataColumnsSeparated){

		//FINDS THE BIGGER ARRAY OF DATA EXTRACTED
		Integer biggerColumn = Integer.MIN_VALUE;
		for(String[] array : dataColumnsSeparated){
			if(array.length > biggerColumn)
				biggerColumn = array.length;
		}

		List<String> objectContent = new ArrayList<String>();

		for(int i = 0; i < biggerColumn; i++)
		{
			String content = "";
			for(String[] dataColumn : dataColumnsSeparated)
			{
				if(dataColumn.length > i)
					content += " " + dataColumn[i].trim();
			}
			//System.out.println("Content: " + content.trim());
			objectContent.add(content.trim());
		}

		return objectContent;
	}

	private String[] 		separateDataFromTSVColumn(FlagSeparator flag, TSVColumn column, Integer lineNumber) throws Exception {
		String rawData = fileReader.getData(column, lineNumber);

		if(rawData == null)
			return null;

		String flagTerm = Pattern.quote(flag.getTerm());

		String[] splitData = null;
		if(rawData.contains(flag.getTerm()))
			splitData = rawData.split(flagTerm);
		else if(!rawData.isEmpty()) {
			splitData = new String[1];
			splitData[0] = rawData;
		}else{
				throw new SeparatorFlagException("There is no caractere '" + flag.getTerm() +
						"' in the field '" + column.getTitle() + "' to be used as separator");
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

	private List<Resource> 	getSubject(Rule rule, Integer lineNumber) throws Exception {
		String baseIRI = rule.getDefaultBaseIRI();
		List<Resource> subjectList = new ArrayList<Resource>();

		Resource subjectType = model.createResource(rule.getSubject().getIRI().toString());

		FlagSearchBlock searchBlockFlag = getSearchBlockFlag(rule.getSubjectTSVColumns());
		if(searchBlockFlag != null){
			SearchBlock searchBlock = this.searchBlocks.get(searchBlockFlag.getId());

			List<String> subjects = extractData(rule.getSubjectTSVColumns(), lineNumber);

			for (String node : subjects) {
				subjectList.add(model.createResource(node, subjectType));
			}

			return subjectList;
		}

		String baseIRIFlag = getBASEIRIFlag(rule.getSubjectTSVColumns());
		if(baseIRIFlag != null)
			baseIRI = baseIRIFlag;

		String customIDFlag = getCustomIDFlag(rule.getSubjectTSVColumns());
		if (customIDFlag != null) {
			subjectList.add(model.createResource(baseIRI + customIDFlag, subjectType));


		} else {
			String fixedContentFlag = getFixedContentFlag(rule.getSubjectTSVColumns());
			if (fixedContentFlag != null) {
				subjectList.add(model.createResource(baseIRI + fixedContentFlag, subjectType));

			} else {
				List<String> subjectContentRaw = extractData(rule.getSubjectTSVColumns(), lineNumber);

				List<String> subjectContent = new ArrayList<String>();
				for (String content : subjectContentRaw) {
					subjectContent.add(content.replaceAll(" ", "_"));
				}

				for (String individualContent : subjectContent) {
					subjectList.add(model.createResource(baseIRI + individualContent, subjectType));
				}
			}
		}
		return subjectList;
	}

	private List<Resource> 	getObjects(Rule rule, List<String> listOfContent, List<TSVColumn> columns, Resource objectType) throws Exception {
		String baseIRI = rule.getDefaultBaseIRI();
		List<Resource> objectList = new ArrayList<Resource>();

		FlagSearchBlock searchBlockFlag = getSearchBlockFlag(columns);
		if(searchBlockFlag != null){
			SearchBlock searchBlock = this.searchBlocks.get(searchBlockFlag.getId());
			for (String node : listOfContent) {
				objectList.add(model.createResource(node, objectType));
			}

			return objectList;
		}

		String baseIRIFlag = getBASEIRIFlag(columns);
		if(baseIRIFlag != null)
			baseIRI = baseIRIFlag;

		String customIDFlag = getCustomIDFlag(columns);
		if (customIDFlag != null) {
			objectList.add(model.createResource(baseIRI + customIDFlag, objectType));


		} else {
			String fixedContentFlag = getFixedContentFlag(columns);
			if (fixedContentFlag != null) {
				fixedContentFlag = fixedContentFlag.replaceAll(" ", "_");
				objectList.add(model.createResource(baseIRI + fixedContentFlag, objectType));

			} else {
				List<String> subjectContent = new ArrayList<String>();
				for (String content : listOfContent) {
					subjectContent.add(content.replaceAll(" ", "_"));
				}

				for (String individualContent : subjectContent) {
					objectList.add(model.createResource(baseIRI + individualContent, objectType));
				}
			}
		}
		return objectList;
	}

	private String 			getCustomIDFlag(List<TSVColumn> listTSVColumn) {
		for(TSVColumn column : listTSVColumn){
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagCustomID){
					return ((FlagCustomID) flag).getContent();
				}
			}
		}
		return null;
	}

	private String 			getFixedContentFlag(List<TSVColumn> listTSVColumn) {
		for(TSVColumn column : listTSVColumn){
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagFixedContent){
					return ((FlagFixedContent) flag).getContent();
				}
			}
		}
		return null;
	}

	private Object 			getDataTypeContentFlag(TSVColumn column) {
		for(Flag flag : column.getFlags()){
			if(flag instanceof FlagDataType){
				return ((FlagDataType) flag).getDatatype();
			}
		}
		return null;
	}

	private FlagNode 			getNodeFlagFromTSVColumn(TSVColumn column) {
		for(Flag flag : column.getFlags()){
			if(flag instanceof FlagNode){
				return (FlagNode) flag;
			}
		}
		return null;
	}

	private String 			getBASEIRIFlag(List<TSVColumn> listTSVColumn) throws BaseIRIException {
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

	private FlagSearchBlock 	getSearchBlockFlag(List<TSVColumn> listTSVColumn) {
		for(TSVColumn column : listTSVColumn){
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagSearchBlock){
					return (FlagSearchBlock) flag;
				}
			}
		}
		return null;
	}

	private void 			loadNamespacesFromOntology() {
		Set<String> namespacesUsed = new HashSet<String>();

		model.listStatements().forEachRemaining(s -> { 
			Triple triple = s.asTriple();
			namespacesUsed.add(triple.getSubject().getNameSpace());
			namespacesUsed.add(triple.getPredicate().getNameSpace());
			if(triple.getObject().isURI())
				namespacesUsed.add(triple.getObject().getNameSpace());
		});

		//namespacesUsed.forEach(n -> System.out.println(n));

		for(String ontology : ontologiesList) {
			Model tempModel = ModelFactory.createDefaultModel();
			tempModel.read(ontology);

			tempModel.getNsPrefixMap().forEach((k, v) -> {
				if (namespacesUsed.contains(v))
					model.setNsPrefix(k, v);
			});
		}
	}

	public Boolean 			checkConsistency() throws Exception {
		System.out.println("#####################################\nChecking consistency...");
		for(String ontologyPath : ontologiesList) {
			System.out.println("# Checking consistency of ontology: " + ontologyPath);
			Model ontology = ModelFactory.createOntologyModel().read(ontologyPath);
			Reasoner openPellet = PelletReasonerFactory.theInstance().create().bindSchema(ontology);
			InfModel inf = ModelFactory.createInfModel(openPellet, model);

			ValidityReport report = inf.validate();

			System.out.println("Is Clean?:" + report.isClean());

			System.out.println("Is valid?:" + report.isValid());

			if (!report.isValid()) {
				String conflicts = "Ontology conflicts:\n";

				File file = new File(ontologyPath);
				conflicts += file.getName() + "\n";

				for (Iterator<Report> i = report.getReports(); i.hasNext(); ) {
					conflicts += "-> " + i.next() + "\n";
				}
				throw new Exception(conflicts);
			}
		}
		return true;
	}

	public 	Model 			getModel() {
		return this.model;
	}

}
