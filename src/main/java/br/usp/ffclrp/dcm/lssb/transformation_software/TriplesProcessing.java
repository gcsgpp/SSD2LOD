package br.usp.ffclrp.dcm.lssb.transformation_software;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.*;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.*;
import openllet.jena.PelletReasonerFactory;
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
	private Map<Integer, Rule> allRules = new HashMap<Integer, Rule>();
	private List<Rule> regularRuleList;
	private List<Rule> dependencyList = new ArrayList<Rule>();
	private Map<Integer, ConditionBlock> conditionBlocks;
	private Map<Integer, SearchBlock> searchBlocks;
	public 	RuleConfig defaultRuleConfig;
	private MatrixLineNumberTracking currentLineNumberMatrixRules = new MatrixLineNumberTracking();
	private Map<Integer, List<Resource>> simpleRuleAlreadyProcessed = new HashMap<>();
	public 	long startTime;
	private List<String> ontologiesList = new ArrayList<>();

	public 					TriplesProcessing() {
		startTime = new Date().getTime();
		this.model = ModelFactory.createDefaultModel();
		//model.read(relativePathOntologyFile); //load ontology and add its axioms to the linked graph
		fileReader = new SemistructuredFileReader();
	}

	public void 			addDatasetToBeProcessed(String relativePathDataFile) throws IOException {
		fileReader.addFilesToBeProcessed(relativePathDataFile);
	}
	@SuppressWarnings("unchecked")
	public void 			createTriplesFromRules(List<Rule> listRules,
												  Map<Integer, ConditionBlock> conditionBlocks,
												  Map<Integer, SearchBlock> searchBlocks,
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
			allRules.put(Integer.parseInt(rule.getId()), rule);
		}

		//POPULATE dependencyList
		for(Rule rule : regularRuleList){	
			Map<OWLProperty, TripleObject> predicateObjectMAP = rule.getPredicateObjects();
			for(Map.Entry<OWLProperty, TripleObject> predicateMapEntry : predicateObjectMAP.entrySet()){
				if(predicateMapEntry.getValue() instanceof TripleObjectAsRule){
					List<ObjectAsRule> listOfRulesAndFlagsOfAPredicate = (List<ObjectAsRule>) predicateMapEntry.getValue().getObject();
					for(ObjectAsRule objectRulesWithFlags : listOfRulesAndFlagsOfAPredicate) {
						Rule objectRule = allRules.get(objectRulesWithFlags.getRuleNumber());
						if(objectRule == null)
							throw new RuleNotFound("Rule number " + objectRulesWithFlags.getRuleNumber() + " was not found/created check your file.");
						dependencyList.add(objectRule);
					}
				}
			}
		}

		for(Rule rule : dependencyList){	
			regularRuleList.remove(rule);
		}

 		for(Rule rule : regularRuleList){
			//System.out.println("Processing rules...");
			processRule(rule);
		}

		loadNamespacesFromOntology();
	}

	private List<Resource> 	processRule(Rule rule) throws Exception {
		System.out.println("Processing rules...");
		List<Resource> subjectListToBeReturned = new LinkedList<>();
		if(rule.isMatrix()){
			if(currentLineNumberMatrixRules.isEmpty()){
				for(Integer tsvLineNumber = rule.getStartLineNumber(); tsvLineNumber < fileReader.getLinesCount(); tsvLineNumber++) {
					try {
						currentLineNumberMatrixRules = new MatrixLineNumberTracking(tsvLineNumber, Integer.parseInt(rule.getId()));
						for(TSVColumn ruleColumns : rule.getSubjectTSVColumns()){
							if(!assertConditionBlock(ruleColumns.getFlags(), tsvLineNumber))
								return null;
						}

						// *** SUBJECT ***
						List<Resource> subjectList = getSubject(rule, tsvLineNumber); //ONE SUBJECT FOR EACH ITEM IN THE CELL
						subjectListToBeReturned.addAll(processPredicateAndObject(rule, tsvLineNumber, subjectList));
					} catch (CustomWarnings w) {
						//System.out.println(w.getMessage());
						continue;
					} catch (ArrayIndexOutOfBoundsException e) {
						continue;
					}
				}

				currentLineNumberMatrixRules = new MatrixLineNumberTracking();
			}else{
				try {
					for(TSVColumn ruleColumns : rule.getSubjectTSVColumns()){
						if(!assertConditionBlock(ruleColumns.getFlags(), currentLineNumberMatrixRules.getLineNumber()))
							return null;
					}

					// *** SUBJECT ***
					List<Resource> subjectList = getSubject(rule, currentLineNumberMatrixRules.getLineNumber()); //ONE SUBJECT FOR EACH ITEM IN THE CELL
					subjectListToBeReturned.addAll(processPredicateAndObject(rule, currentLineNumberMatrixRules.getLineNumber(), subjectList));
				} catch (CustomWarnings w) {
					//System.out.println(w.getMessage());
					return null;
				} catch (ArrayIndexOutOfBoundsException e) {
					return null;
				}
			}


		}else{
			List<Resource> t = simpleRuleAlreadyProcessed.get(Integer.parseInt(rule.getId()));

			if(simpleRuleAlreadyProcessed.get(Integer.parseInt(rule.getId())) == null){
				// *** SUBJECT ***
				List<Resource> subjectList = getSubject(rule, rule.getStartLineNumber()); //ONE SUBJECT FOR EACH ITEM IN THE CELL

				for(Integer tsvLineNumber = rule.getStartLineNumber(); tsvLineNumber < fileReader.getLinesCount(); tsvLineNumber++) {
					try {
						for (TSVColumn ruleColumns : rule.getSubjectTSVColumns()) {
							if (!assertConditionBlock(ruleColumns.getFlags(), tsvLineNumber))
								return null;
						}

						List<Resource> subjectsProcessed = processPredicateAndObject(rule, tsvLineNumber, subjectList);
						simpleRuleAlreadyProcessed.put(Integer.parseInt(rule.getId()), subjectsProcessed);
						subjectListToBeReturned = subjectsProcessed;

					} catch (CustomWarnings w) {
						//System.out.println(w.getMessage());
						return null;
					} catch (ArrayIndexOutOfBoundsException e) {
						return null;
					}
				}
			}else{
				subjectListToBeReturned = simpleRuleAlreadyProcessed.get(Integer.parseInt(rule.getId()));
			}
		}
		return subjectListToBeReturned;
	}

	private List<Resource> 	processPredicateAndObject(Rule rule, Integer tsvLineNumber, List<Resource> subjectList) throws Exception {

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
							List<Resource> subjectsFromDependentRule = processRule(allRules.get(objectAsRule.getRuleNumber()));
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
					List<String> listOfContent = null;

					for(TSVColumn dataColumn : (List<TSVColumn>) predicateMapEntry.getValue().getObject()) {
						try {
							listOfContent = extractDataFromTSVColumn(dataColumns, tsvLineNumber);
						} catch (ColumnNotFoundWarning e) {
							//Skipping to next rule line
							continue;
						}

						Object datatype = getDataTypeContentFlag(dataColumn); //get the first element, not necessarily the index 0

						for(String content : listOfContent){
							if(content != null && !content.equals(""))
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

				for(Condition condition : conditionBlock.getConditions()){
					String contentTSVColumn = fileReader.getData(condition, tsvLineNumber);
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

	private void 			addTripleToModel(Resource subject, Property predicate, Resource object) {
		//System.out.println("S: " + subject.getURI() + " P: " + predicate.getURI() + " O: " + object.getURI());
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
				subject.addProperty(predicate, contentElement);
				return;
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

	private List<String> 	extractDataFromTSVColumn(List<TSVColumn> listTSVColumn, Integer lineNumber) throws Exception {
		List<String> objectContent = new ArrayList<String>();


		List<String[]> dataColumnsSeparated = new ArrayList<String[]>();
		for(TSVColumn column : listTSVColumn){
			Boolean extractedData = false;

			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagSeparator){
					dataColumnsSeparated.add(separateDataFromTSVColumn((FlagSeparator) flag, column, lineNumber));
					extractedData = true;
				}else if(flag instanceof FlagNotMetadata) {
					String[] columnData = new String[1];
					columnData[0] = fileReader.getData(column, 0);
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
				columnData[0] = fileReader.getData(column, lineNumber);
				dataColumnsSeparated.add(columnData);
			}
		}

		//FINDS THE BIGGER ARRAY OF DATA EXTRACTED
		Integer biggerColumn = Integer.MIN_VALUE;
		for(String[] array : dataColumnsSeparated){
			if(array.length > biggerColumn)
				biggerColumn = array.length;
		}

		//MAKES ALL ARRAYS BE THE SAME SIZE.
		//THE ARRAYS THAT IS SMALLER THAN THE BIGGEST IS COMPLETED WITH 
		//THE DATA AT THE BEGGINING OF THE ARRAY ITSELF.
		List<List<String>> dataColumns = new ArrayList<List<String>>();
		for(String[] array : dataColumnsSeparated){
			List<String> list = new ArrayList<String>();
			int oldPosition = -1;
			while(list.size() != biggerColumn.intValue()) {
				try {
					list.add(array[oldPosition + 1]);
					oldPosition++;

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

	private String[] 		separateDataFromTSVColumn(FlagSeparator flag, TSVColumn column, Integer lineNumber) throws Exception {
		String rawData = fileReader.getData(column, lineNumber);

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

		SearchBlock searchBlock = getSearchBlockFlag(rule.getSubjectTSVColumns());
		if(searchBlock != null){

			List<String> subjectContent = extractDataFromTSVColumn(rule.getSubjectTSVColumns(), lineNumber);

			for (String individualContent : subjectContent) {
				subjectList.add(model.createResource(searchBlock.getExternalNode(individualContent), subjectType));
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
				List<String> subjectContentRaw = extractDataFromTSVColumn(rule.getSubjectTSVColumns(), lineNumber);

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

	private SearchBlock 	getSearchBlockFlag(List<TSVColumn> listTSVColumn) {
		for(TSVColumn column : listTSVColumn){
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagSearchBlock){
					Integer searchBlockId = ((FlagSearchBlock) flag).getId();

					return this.searchBlocks.get(searchBlockId);
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
