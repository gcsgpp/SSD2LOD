package br.usp.ffclrp.dcm.lssb.transformation_software;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.BaseIRIException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.ConditionBlockException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.RuleNotFound;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.*;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TripleProcessingTest 
{
	private OntologyHelper ontologyHelper;
	private Map<String, ConditionBlock> conditionsBlocks;
	private List<Rule> listRules;
	private List<String> ontologiesList;


	@org.junit.Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void initializer() {
		ontologyHelper = null;
		conditionsBlocks = new HashMap<>();
		listRules = new ArrayList<>();
		ontologiesList = new ArrayList<>();
	}

	private Rule createRuleOne() {
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig("default").setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("Term");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("Term");

		List<Flag> subjectFlags = new ArrayList<>();
		List<Integer> separatorCols = new ArrayList<Integer>();
		separatorCols.add(0);
		subjectFlags.add(new FlagSeparator("~", separatorCols));
		subjectFlags.add(new FlagBaseIRI("http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=", "go"));
		subjectFlags.add(new FlagConditionBlock("condition1"));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();

		/* has_pvalue predicate */
		TSVColumn pvalueColumn = new TSVColumn();
		pvalueColumn.setTitle("PValue");

		List<Flag> pvalueFlags = new ArrayList<>();
		pvalueColumn.setFlags(pvalueFlags);

		predicateObjects.put(ontologyHelper.getProperty("has_pvalue"), new TripleObjectAsColumns(pvalueColumn));

		/* name predicate */
		TSVColumn nameColumn = new TSVColumn();
		nameColumn.setTitle("Term");

		List<Flag> nameFlags = new ArrayList<>();
		separatorCols = new ArrayList<Integer>();
		separatorCols.add(1);
		nameFlags.add(new FlagSeparator("~", separatorCols));
		nameColumn.setFlags(nameFlags);

		predicateObjects.put(ontologyHelper.getProperty("name"), new TripleObjectAsColumns(nameColumn));

		/* 'has participant' predicate */
		TSVColumn participantColumn = new TSVColumn();
		participantColumn.setTitle("Genes");

		List<Flag> participantFlags = new ArrayList<>();
		participantColumn.setFlags(participantFlags);

		predicateObjects.put(ontologyHelper.getProperty("has participant"), new TripleObjectAsRule(new ObjectAsRule("rule3", participantFlags)));

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	private Rule createRuleTwo() {
		String id = "rule2";
		RuleConfig ruleConfig = new RuleConfig("default").setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("Term");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("Term");

		List<Flag> subjectFlags = new ArrayList<>();
		List<Integer> separatorCols = new ArrayList<Integer>();
		separatorCols.add(0);
		subjectFlags.add(new FlagSeparator(":", separatorCols));
		subjectFlags.add(new FlagBaseIRI("http://www.kegg.jp/entry/", "kegg"));
		subjectFlags.add(new FlagConditionBlock("condition2"));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();

		/* has_pvalue predicate */
		TSVColumn pvalueColumn = new TSVColumn();
		pvalueColumn.setTitle("PValue");

		List<Flag> pvalueFlags = new ArrayList<>();
		pvalueColumn.setFlags(pvalueFlags);

		predicateObjects.put(ontologyHelper.getProperty("has_pvalue"), new TripleObjectAsColumns(pvalueColumn));

		/* name predicate */
		TSVColumn nameColumn = new TSVColumn();
		nameColumn.setTitle("Term");

		List<Flag> nameFlags = new ArrayList<>();
		separatorCols = new ArrayList<Integer>();
		separatorCols.add(1);
		nameFlags.add(new FlagSeparator(":", separatorCols));
		nameColumn.setFlags(nameFlags);

		predicateObjects.put(ontologyHelper.getProperty("name"), new TripleObjectAsColumns(nameColumn));

		/* 'has participant' predicate */
		TSVColumn participantColumn = new TSVColumn();
		participantColumn.setTitle("Term");

		List<Flag> participantFlags = new ArrayList<>();
		participantColumn.setFlags(participantFlags);

		predicateObjects.put(ontologyHelper.getProperty("has participant"), new TripleObjectAsRule(new ObjectAsRule("rule3", participantFlags)));

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	private Rule createRuleThree() {
		String id = "rule3";
		RuleConfig ruleConfig = new RuleConfig("default").setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("Gene");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("Genes");

		List<Flag> subjectFlags = new ArrayList<>();
		List<Integer> separatorCols = new ArrayList<Integer>();
		separatorCols.add(Integer.MAX_VALUE);
		subjectFlags.add(new FlagSeparator(", ", separatorCols));
		subjectFlags.add(new FlagBaseIRI("http://www.genecards.org/cgi-bin/carddisp.pl?gene=", "genecard"));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	private void createConditionBlocks() {		
		List<Condition> conditions = new ArrayList<>();
		TSVColumn 	column = new TSVColumn();
					column.setFlags(new ArrayList<>());
					column.setTitle("Category");
		conditions.add(new Condition(column, EnumOperationsConditionBlock.DIFFERENT, "KEGG_PATHWAY"));

					column = new TSVColumn();
					column.setFlags(new ArrayList<>());
					column.setTitle("PValue");
		conditions.add(new Condition(column, EnumOperationsConditionBlock.LESSTHAN, "0.01"));

		conditionsBlocks.put("condition1", new ConditionBlock("condition1", conditions));

		conditions = new ArrayList<>();

					column = new TSVColumn();
					column.setFlags(new ArrayList<>());
					column.setTitle("Category");
		conditions.add(new Condition(column, EnumOperationsConditionBlock.EQUAL, "KEGG_PATHWAY"));

					column = new TSVColumn();
					column.setFlags(new ArrayList<>());
					column.setTitle("PValue");
		conditions.add(new Condition(column, EnumOperationsConditionBlock.LESSTHAN, "0.03"));

		conditionsBlocks.put("condition2", new ConditionBlock("condition2", conditions));
	}

	@Test
	public void processRuleWithConditionblockflagBaseiriflagSeparatorflag() throws Exception	{
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleOne());
		listRules.add(createRuleThree());
		createConditionBlocks();
		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "enrichedDataGOTerm.tsv");
		try{
			processingClass.createTriplesFromRules(listRules,
													conditionsBlocks,
													searchBlocks,
													RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
													ontologiesList
													);
		}catch(Exception e) {
			fail();
		}

		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

        int numberOfStatementsPassed = 0;
		for(Statement statement : statements) {
			Triple triple = statement.asTriple();

			if( 	triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 	||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")			||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#range")				) //is not interesting to check these predicates because there are other classes in the ontology if tested will get away from the objective of this method
				continue;

			if(triple.getSubject().getURI().equals("http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=GO:0030001")){
				if(	triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_pvalue") 		&& triple.getObject().getLiteralValue().equals("0.000397262") ||
					triple.getPredicate().getURI().equals("http://schema.org/name") 				&& triple.getObject().getLiteralValue().equals("metal ion transport") ||
					triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A5") ||
					triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=JPH3") ||
					triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A4") ||
					triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A12"))
                {
                    numberOfStatementsPassed++;
                }
			}else {
				assert(false);
			}
		}

		assertEquals(6, numberOfStatementsPassed);
	}


    // ##################


	@Test
	public void processRuleWithColonAsSeparator() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleTwo());
		listRules.add(createRuleThree());
		createConditionBlocks();
		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "enrichedDataKeggTerm.tsv");
		try{
			processingClass.createTriplesFromRules(listRules,
													conditionsBlocks,
													searchBlocks,
													RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
													ontologiesList);
		}catch(Exception e) {
			fail();
		}

		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

        int numberOfStatementsPassed = 0;
		for(Statement statement : statements) {
			Triple triple = statement.asTriple();

			if( triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 	||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")			||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#range")				) //is not interesting to check these predicates because there are other classes in the ontology if tested will get away from the objective of this method
				continue;

			if(triple.getSubject().getURI().equals("http://www.kegg.jp/entry/hsa00190")){
				if(	triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_pvalue") 		&& triple.getObject().getLiteralValue().equals("0.020404871") ||
					triple.getPredicate().getURI().equals("http://schema.org/name") 				&& triple.getObject().getLiteralValue().equals("Oxidative phosphorylation") ||
					triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=UQCRC2") ||
					triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=UQCRC1") ||
					triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=ATP5J"))
                {
                    numberOfStatementsPassed++;
                }
			}else {
				assert(false);
			}
		}

		assertEquals(5, numberOfStatementsPassed);
	}


    // ##################


	private Rule createRuleWithNotMetadataFlag() {
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig("default", "http://example.org/onto/individual#").setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("GSM1243183");

		List<Flag> subjectFlags = new ArrayList<>();
		subjectFlags.add(new FlagNotMetadata(true));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithNotMetadataFlag() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithNotMetadataFlag());

		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "NormalizedData.txt");
		try{
			processingClass.createTriplesFromRules(listRules,
													conditionsBlocks,
													searchBlocks,
													RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
													ontologiesList);
		}catch(Exception e) {
			fail();
		}
		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

		for(Statement statement : statements) {
			Triple triple = statement.asTriple();

			assertEquals("http://example.org/onto/individual#GSM1243183", triple.getSubject().getURI());
		}
	}


    // ##################

	private Rule createRuleAssertingConditionBlockThatDontExist(){
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig("default").setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("Term");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("Category");

		List<Flag> subjectFlags = new ArrayList<>();
		subjectFlags.add(new FlagConditionBlock("condition2"));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);


		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();

		return new Rule(id,ruleConfig, subjectClass, subjectTSVColumns, predicateObjects );
	}

	@Test
	public void assertingConditionBlockThatDontExist() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleAssertingConditionBlockThatDontExist());

		thrown.expect(ConditionBlockException.class);
		thrown.expectMessage("No condition block created");

		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "enrichedDataGOTerm2.tsv");

		processingClass.createTriplesFromRules(listRules,
												conditionsBlocks,
												searchBlocks,
												RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
												ontologiesList);
	}


    // ##################


	private Rule createRuleWithFixedContentFlagOnSubjectLine() {
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig("default", "http://example.org/onto/individual#").setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("");

		List<Flag> subjectFlags = new ArrayList<>();
		subjectFlags.add(new FlagFixedContent("NormalizedData"));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithFixedContentFlagOnSubjectLine() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithFixedContentFlagOnSubjectLine());
		
		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "NormalizedData.txt");
		try{
			processingClass.createTriplesFromRules(listRules,
													conditionsBlocks,
													searchBlocks,
													RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
													ontologiesList);
		}catch(Exception e) {
			fail();
		}

		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

		for(Statement statement : statements) {
			Triple triple = statement.asTriple();

			assertEquals("http://example.org/onto/individual#NormalizedData", triple.getSubject().getURI());
		}
	}


    // ##################


	private Rule createRuleWithFixedContentFlagOnObjectLine() {
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig("default").setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("GSM1243183");
		List<Flag> subjectFlags = new ArrayList<>();
		subjectFlags.add(new FlagNotMetadata(true));
		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();
		
		OWLProperty enrichmentProperty = ontologyHelper.getProperty("has enrichement");
		
		List<Flag> objectFlags = new ArrayList<>();
		objectFlags.add(new FlagFixedContent("test fixed content"));
		TripleObject object = new TripleObjectAsColumns(new TSVColumn("GSM1243183", objectFlags));
		
		predicateObjects.put(enrichmentProperty, object);

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithFixedContentFlagOnObjectLine() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithFixedContentFlagOnObjectLine());

		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "NormalizedData.txt");
		try{
			processingClass.createTriplesFromRules(listRules,
													conditionsBlocks,
													searchBlocks, RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
													ontologiesList);
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}
		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

		assertEquals(2, statements.size()); // the triple with rdfs:type and with the "test fixed content";
		for(Statement statement : statements) {
			Triple triple = statement.asTriple();
			switch (triple.getPredicate().getURI()) {
				case "http://www.w3.org/1999/02/22-rdf-syntax-ns#type":
					assertEquals("http://www.purl.org/g/classes#geo_sample", triple.getObject().getURI());
					break;
				case "http://www.purl.org/g/properties#enrichment":
					assertEquals("test fixed content", triple.getObject().getLiteralValue());
					break;
				default:
					fail();
					break;
			}
		}
	}


    // ##################


	private Rule createRuleWithCustomIDFlag() {
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig("default", "http://example.org/onto/individual#").setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();
		
		TSVColumn subject = new TSVColumn();
		subject.setTitle("");
		List<Flag> subjectFlags = new ArrayList<>();
		subjectFlags.add(new FlagCustomID("NormalizedData"));
		subject.setFlags(subjectFlags);
		
		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();
		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithCustomIDFlag() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithCustomIDFlag());

		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "NormalizedData.txt");
		try{
			processingClass.createTriplesFromRules(listRules,
													conditionsBlocks,
													searchBlocks,
													RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
													ontologiesList);
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}

		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

		for(Statement statement : statements) {
			Triple triple = statement.asTriple();

			assertEquals("http://example.org/onto/individual#NormalizedData", triple.getSubject().getURI());
		}
	}


    // ##################


	private Rule createRuleWithBaseIRIWithNoNamespace() {
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig("default").setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();

		TSVColumn subject = new TSVColumn();
		subject.setTitle("");
		List<Flag> subjectFlags = new ArrayList<>();
		subjectFlags.add(new FlagBaseIRI("http://purl.org/g/", null));
		subject.setFlags(subjectFlags);

		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();
		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithBaseIRIWithNoNamespace() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithBaseIRIWithNoNamespace());
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "NormalizedData.txt");

        thrown.expect(BaseIRIException.class);
        thrown.expectMessage("Some BaseIRI flag has an empty namespace field.");

		Map<String, SearchBlock> searchBlocks = new HashMap<>();

        processingClass.createTriplesFromRules(listRules,
												conditionsBlocks,
												searchBlocks,
												RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
												ontologiesList);
	}


    // ##################


	private Rule createRuleWithBaseIRIWithNoIRI() {
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig("default").setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();

		TSVColumn subject = new TSVColumn();
		subject.setTitle("");
		List<Flag> subjectFlags = new ArrayList<>();
		subjectFlags.add(new FlagBaseIRI(null, "purl"));
		subject.setFlags(subjectFlags);

		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();
		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithBaseIRIWithNoIRI() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithBaseIRIWithNoIRI());
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "NormalizedData.txt");

		thrown.expect(BaseIRIException.class);
		thrown.expectMessage("Some BaseIRI flag has an empty IRI field.");

		Map<String, SearchBlock> searchBlocks = new HashMap<>();

		processingClass.createTriplesFromRules(listRules,
												conditionsBlocks,
												searchBlocks,
												RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
												ontologiesList);
	}


    // ##################


	@Test
	public void processRuleWithConditionBlockNotMet() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleOne());
		listRules.add(createRuleThree());
		createConditionBlocks();
		
		//Changing the type of operation to manage not met the data inside the dataset
		ConditionBlock cb = conditionsBlocks.get("condition1");
		for (Condition condition : cb.getConditions()) {
			if (condition.getColumn().equals("Category")) {
				condition.setOperation(EnumOperationsConditionBlock.EQUAL);
			}
		}

		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "enrichedDataGOTerm.tsv");
		try{
			processingClass.createTriplesFromRules(listRules,
													conditionsBlocks,
													searchBlocks,
													RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
													ontologiesList);
		}catch(Exception e) {
			fail();
		}
	}


    // ##################


	private Rule createRuleSeparatorElementInSeparatorFlagDoesNotExist() {
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig("default").setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("Gene");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();
		
		TSVColumn subject = new TSVColumn();
		subject.setTitle("Genes");
		List<Flag> subjectFlags = new ArrayList<>();
		subjectFlags.add(new FlagSeparator("-", new ArrayList<Integer>()));
		subject.setFlags(subjectFlags);
		
		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();
		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}
	
/*	@Test
	public void separatorElementInSeparatorFlagDoesNotExist() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleSeparatorElementInSeparatorFlagDoesNotExist());

		thrown.expect(SeparatorFlagException.class);
		thrown.expectMessage("There is no caractere '-' in the field 'Genes' to be used as separator");
		
		TriplesProcessing processingClass = new TriplesProcessing( testFolderPath + "ontology.owl");
		processingClass.addDatasetToBeProcessed(testFolderPath + "enrichedDataGOTerm2.tsv");

		processingClass.createTriplesFromRules(listRules, conditionsBlocks);
	}
*/

	// ##################


	private void createConditionBlockWithGreaterThanCondition() {
		List<Condition> conditions = new ArrayList<Condition>();
		TSVColumn 	column = new TSVColumn();
					column.setFlags(new ArrayList<>());
					column.setTitle("Pop Hits");
		conditions.add(new Condition(column, EnumOperationsConditionBlock.GREATERTHAN, "800"));

		conditionsBlocks.put("condition1", new ConditionBlock("condition1", conditions));
	}

	@Test
	public void processRuleWithConditionblockWithGreaterThanCondition() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleOne());
		listRules.add(createRuleThree());
		createConditionBlockWithGreaterThanCondition();

		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "enrichedDataGOTerm3.tsv");
		try{
			processingClass.createTriplesFromRules(listRules,
													conditionsBlocks,
													searchBlocks,
													RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
													ontologiesList);
		}catch(Exception e) {
			e.printStackTrace();
		    fail();
		}

		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

		int numberOfStatementsPassed = 0;
		for(Statement statement : statements) {
			Triple triple = statement.asTriple();

			if( 	triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 	||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")			||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#range")				) //is not interesting to check these predicates because there are other classes in the ontology if tested will get away from the objective of this method
				continue;

			if(triple.getSubject().getURI().equals("http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=GO:0030001")){
				if(	triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_pvalue") 		&& triple.getObject().getLiteralValue().equals("0.000397262") ||
					triple.getPredicate().getURI().equals("http://schema.org/name") 				&& triple.getObject().getLiteralValue().equals("metal ion transport") ||
					triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A5") ||
					triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=JPH3") ||
					triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A4") ||
					triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A12"))
                    {
                     numberOfStatementsPassed++;
                    }
			}else {
				assert(false);
			}
		}

		assertEquals(6, numberOfStatementsPassed);
	}


    // ##################

    @Test
    public void multipleFiles() throws Exception {
        ontologyHelper = new OntologyHelper();
        String testFolderPath = "testFiles/unitTestsFiles/";
        String ontologyPath = testFolderPath + "ontology.owl";

        ontologyHelper.loadingOntologyFromFile(ontologyPath);
        listRules.add(createRuleOne());
        listRules.add(createRuleThree());
        createConditionBlocks();

		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
        processingClass.addDatasetToBeProcessed(testFolderPath + "enrichedDataGOTerm.tsv");
        processingClass.addDatasetToBeProcessed(testFolderPath + "enrichedDataGOTerm3.tsv");
        try{
            processingClass.createTriplesFromRules(listRules,
													conditionsBlocks,
													searchBlocks,
													RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
													ontologiesList);
        }catch(Exception e) {
            e.printStackTrace();
            fail();
        }

        Model model = processingClass.getModel();
        List<Statement> statements = model.listStatements().toList();

        int numberOfStatementsPassedGO0030001 = 0;
        int numberOfStatementsPassedGO0098662 = 0;
        for(Statement statement : statements) {
            Triple triple = statement.asTriple();

            if( 	triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 	||
                    triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")			||
                    triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#range")				) //is not interesting to check these predicates because there are other classes in the ontology if tested will get away from the objective of this method
                continue;

            if(triple.getSubject().getURI().equals("http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=GO:0030001")){
                if(	    triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_pvalue") 		&& triple.getObject().getLiteralValue().equals("0.000397262") ||
                        triple.getPredicate().getURI().equals("http://schema.org/name") 				&& triple.getObject().getLiteralValue().equals("metal ion transport") ||
                        triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A5") ||
                        triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=JPH3") ||
                        triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A4") ||
                        triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A12"))
                {
                    numberOfStatementsPassedGO0030001++;
                }
            }else if(triple.getSubject().getURI().equals("http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=GO:0098662")){
                if(     triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_pvalue") 		&& triple.getObject().getLiteralValue().equals("0.001114307") ||
                        triple.getPredicate().getURI().equals("http://schema.org/name") 				&& triple.getObject().getLiteralValue().equals("inorganic cation transmembrane transport") ||
                        triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=CAV3") ||
                        triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=FXYD2") ||
                        triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=JPH3"))
                {
                    numberOfStatementsPassedGO0098662++;
                }
            }else {
                assert(false);
            }
        }

        assertEquals(6, numberOfStatementsPassedGO0030001);
        assertEquals(5, numberOfStatementsPassedGO0098662);
    }


	// ##################


    private Rule createSimpleRule(){
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig(id, "http://example.org/onto/individual#");
		OWLClass subjectClass = ontologyHelper.getClass("investigation");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();

		TSVColumn subject = new TSVColumn();
		subject.setTitle("GSE67111_SERIES");
		List<Flag> subjectFlags = new ArrayList<>();
		subject.setFlags(subjectFlags);

		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();

		OWLProperty statusProperty = ontologyHelper.getProperty("status");
		List<Flag> contentDirection = new ArrayList<>();
		TripleObject statusTripleObject = new TripleObjectAsColumns(new TSVColumn("GSE67111_status",contentDirection));

		OWLProperty summaryProperty = ontologyHelper.getProperty("summary");
		TripleObject summaryTripleObject = new TripleObjectAsColumns(new TSVColumn("GSE67111_summary",contentDirection));


		predicateObjects.put(statusProperty, statusTripleObject);
		predicateObjects.put(summaryProperty, summaryTripleObject);

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processSimpleRule() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/geo_preprocessed/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createSimpleRule());
		createConditionBlockWithGreaterThanCondition();

		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "GSE67111.tsv");
		try{
			processingClass.createTriplesFromRules(listRules,
													conditionsBlocks,
													searchBlocks,
													RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
													ontologiesList);
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}

		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

		String summary1 = "This study addressed involvement of fourteen 5-fluorouracil pathway genes in the prognosis of colorectal carcinoma patients. The major goal of our study was to investigate associations of gene expression of enzymes metabolizing 5-fluorouracil with therapy response and survival of colorectal carcinoma patients";
		String summary2 = "Downregulation of DPYD and upregulation of PPAT, UMPS, RRM2, and SLC29A1 transcripts were found in tumors compared to adjacent mucosas in testing and validation sets of patients. Low RRM2 transcript level significantly associated with poor response to the first-line palliative 5-FU-based chemotherapy in the testing set and with poor disease-free interval of patients in the validation set.";

		int numberOfStatementsPassed = 0;
		for(Statement statement : statements) {
			Triple triple = statement.asTriple();

			if( 	triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 	||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")			||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#range")				) //is not interesting to check these predicates because there are other classes in the ontology if tested will get away from the objective of this method
				continue;

			if(triple.getSubject().getURI().equals("http://example.org/onto/individual#GSE67111")){
				if(	triple.getPredicate().getURI().equals("http://example.com/geo_experiments/status") 		&& triple.getObject().getLiteralValue().equals("Public on Aug 31 2015") ||
						triple.getPredicate().getURI().equals("http://example.com/geo_experiments/summary") 				&& triple.getObject().getLiteralValue().equals(summary1) ||
						triple.getPredicate().getURI().equals("http://example.com/geo_experiments/summary") 				&& triple.getObject().getLiteralValue().equals(summary2))
				{
					numberOfStatementsPassed++;
				}
			}else {
				assert(false);
			}
		}

		assertEquals(3, numberOfStatementsPassed);
	}


	// ##################


	private List<Rule> createSimpleRuleInsideSimpleRuleAndOtherFile(){
		List<Rule> rules = new ArrayList<>();
		//RULE 1
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig(id, "http://example.org/onto/individual#");
		OWLClass subjectClass = ontologyHelper.getClass("investigation");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();

		TSVColumn subject = new TSVColumn();
		subject.setTitle("GSE67111_SERIES");
		List<Flag> subjectFlags = new ArrayList<>();
		subject.setFlags(subjectFlags);

		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();

		TripleObject hasParticipantTripleObject = new TripleObjectAsRule(new ObjectAsRule("rule2", new ArrayList<>()));

		OWLProperty hasParticipantProperty = ontologyHelper.getProperty("has participant");

		predicateObjects.put(hasParticipantProperty, hasParticipantTripleObject);

		rules.add(new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects));

		//RULE 2
		id = "rule2";
		ruleConfig = new RuleConfig(id, "http://example.org/onto/individual#");
		subjectClass = ontologyHelper.getClass("microarray platform");
		subjectTSVColumns = new ArrayList<>();

		subject = new TSVColumn();
		subject.setTitle("GPL19921_geo_accession");
		subject.setFlags(new ArrayList<>());
		subjectTSVColumns.add(subject);

		predicateObjects = new ArrayListValuedHashMap<>();

		OWLProperty titleProperty = ontologyHelper.getProperty("Title");
		TripleObject titleTripleObject = new TripleObjectAsColumns(new TSVColumn("GPL19921_title", new ArrayList<>()));
		predicateObjects.put(titleProperty, titleTripleObject);

		rules.add(new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects));


		return rules;
	}

	@Test
	public void processSimpleRuleInsideSimpleRuleAndOtherFile() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/geo_preprocessed/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.addAll(createSimpleRuleInsideSimpleRuleAndOtherFile());
		createConditionBlockWithGreaterThanCondition();

		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "GSE67111.tsv");
		processingClass.addDatasetToBeProcessed(testFolderPath + "GPL19921.tsv");
		try{
			processingClass.createTriplesFromRules(listRules,
													conditionsBlocks,
													searchBlocks,
													RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
													ontologiesList);
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}

		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

		int numberOfStatementsPassed = 0;
		for(Statement statement : statements) {
			Triple triple = statement.asTriple();

			if( 	triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 	||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")			||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#range")				) //is not interesting to check these predicates because there are other classes in the ontology if tested will get away from the objective of this method
				continue;

			if(triple.getSubject().getURI().equals("http://example.org/onto/individual#GSE67111")){
				if(	triple.getPredicate().getURI().equals("http://purl.obolibrary.org/obo/RO_0000057") 		&& triple.getObject().getURI().equals("http://example.org/onto/individual#GPL19921"))
					numberOfStatementsPassed++;
			}else if(triple.getSubject().getURI().equals("http://example.org/onto/individual#GPL19921")) {
				if (triple.getPredicate().getURI().equals("http://purl.org/dc/terms/title") && triple.getObject().getLiteralValue().equals("Custom TaqMan: qPCR ViiA7 real-time PCR system"))
					numberOfStatementsPassed++;
			}else{
				assert(false);
			}
		}

		assertEquals(2, numberOfStatementsPassed);
	}


	// ##################


	private List<Rule> createSimpleRuleInsideMatrixRule(){
		List<Rule> rules = new ArrayList<>();
		//RULE 1
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig(id, "http://example.org/onto/individual/");
		OWLClass subjectClass = ontologyHelper.getClass("investigation");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();

		TSVColumn subject = new TSVColumn();
		subject.setTitle("GSE67111_SERIES");
		subject.setFlags(new ArrayList<>());

		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();

		OWLProperty hasParticipantProperty = ontologyHelper.getProperty("has participant");
		TripleObject hasParticipantTripleObject = new TripleObjectAsRule(new ObjectAsRule("rule2", new ArrayList<>()));
		predicateObjects.put(hasParticipantProperty, hasParticipantTripleObject);

		rules.add(new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects));

		//RULE 2
		id = "rule2";
		ruleConfig = new RuleConfig(id, "http://example.org/onto/individual/").setMatrix(true);
		subjectClass = ontologyHelper.getClass("Platform Data");
		subjectTSVColumns = new ArrayList<>();

		subject = new TSVColumn();
		subject.setTitle("GPL19921_ID");
		subject.setFlags(new ArrayList<>());

		TSVColumn subject2 = new TSVColumn();
		subject2.setTitle("GPL19921_ORF");
		subject2.setFlags(new ArrayList<>());

		TSVColumn subject3 = new TSVColumn();
		subject3.setTitle("GPL19921_Assay ID");
		subject3.setFlags(new ArrayList<>());

		subjectTSVColumns.add(subject);
		subjectTSVColumns.add(subject2);
		subjectTSVColumns.add(subject3);

		predicateObjects = new ArrayListValuedHashMap<>();

		OWLProperty idProperty = ontologyHelper.getProperty("ID");
		TripleObject idTripleObject = new TripleObjectAsColumns(new TSVColumn("GPL19921_ID",new ArrayList<>()));

		OWLProperty orfProperty = ontologyHelper.getProperty("ORF");
		TripleObject orfTripleObject = new TripleObjectAsColumns(new TSVColumn("GPL19921_ORF",new ArrayList<>()));

		OWLProperty assayidProperty = ontologyHelper.getProperty("Assay ID");
		List<Flag> assayidFlags = new ArrayList<>();
		List<Integer> maxnumber = new ArrayList<>();
		maxnumber.add(Integer.MAX_VALUE);
		assayidFlags.add(new FlagSeparator(",", maxnumber));
		TripleObject assayidTripleObject = new TripleObjectAsColumns(new TSVColumn("GPL19921_Assay ID",assayidFlags));

		predicateObjects.put(idProperty, idTripleObject);
		predicateObjects.put(orfProperty, orfTripleObject);
		predicateObjects.put(assayidProperty, assayidTripleObject);

		rules.add(new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects));
		return rules;
	}

	@Test
	public void processSimpleRuleInsideMatrixRule() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/geo_preprocessed/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.addAll(createSimpleRuleInsideMatrixRule());
		createConditionBlockWithGreaterThanCondition();

		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "GSE67111.tsv");
		processingClass.addDatasetToBeProcessed(testFolderPath + "GPL19921.tsv");
		try{
			processingClass.createTriplesFromRules(listRules,
													conditionsBlocks,
													searchBlocks,
													RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
													ontologiesList);
		}catch(Exception e) {
			e.printStackTrace();
			fail();

		}

		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

		int numberOfStatementsRule1 = 0;
		int numberOfStatementsRule2 = 0;
		int numberOfStatementsRule3 = 0;
		int numberOfStatementsRule4 = 0;
		for(Statement statement : statements) {
			Triple triple = statement.asTriple();

			if( 	triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 	||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")			||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#range")				) //is not interesting to check these predicates because there are other classes in the ontology if tested will get away from the objective of this method
				continue;

			if(triple.getSubject().getURI().equals("http://example.org/onto/individual/GSE67111")){
				if(	triple.getPredicate().getURI().equals("http://purl.obolibrary.org/obo/RO_0000057") 		&& triple.getObject().getURI().equals("http://example.org/onto/individual/ABCA1_ABCA1_Hs00194045_m1") ||
					triple.getPredicate().getURI().equals("http://purl.obolibrary.org/obo/RO_0000057") 		&& triple.getObject().getURI().equals("http://example.org/onto/individual/ABCA10_ABCA10_Hs00365268_m1,Hs00739326_m1") ||
					triple.getPredicate().getURI().equals("http://purl.obolibrary.org/obo/RO_0000057") 		&& triple.getObject().getURI().equals("http://example.org/onto/individual/ABCA12_ABCA12_Hs00292421_m1,Hs00252524_m1"))
					numberOfStatementsRule1++;
			}else if(triple.getSubject().getURI().equals("http://example.org/onto/individual/ABCA1_ABCA1_Hs00194045_m1")) {
				  if (	triple.getPredicate().getURI().equals("http://example.com/geo_experiments/id") 			&& triple.getObject().getLiteralValue().equals("ABCA1") ||
						triple.getPredicate().getURI().equals("http://example.com/geo_experiments/ORF") 		&& triple.getObject().getLiteralValue().equals("ABCA1") ||
						triple.getPredicate().getURI().equals("http://example.com/geo_experiments/assay_id") 	&& triple.getObject().getLiteralValue().equals("Hs00194045_m1"))
					numberOfStatementsRule2++;
			}else if(triple.getSubject().getURI().equals("http://example.org/onto/individual/ABCA10_ABCA10_Hs00365268_m1,Hs00739326_m1")) {
				  if (	triple.getPredicate().getURI().equals("http://example.com/geo_experiments/id") 			&& triple.getObject().getLiteralValue().equals("ABCA10") ||
						triple.getPredicate().getURI().equals("http://example.com/geo_experiments/ORF") 		&& triple.getObject().getLiteralValue().equals("ABCA10") ||
						triple.getPredicate().getURI().equals("http://example.com/geo_experiments/assay_id") 	&& triple.getObject().getLiteralValue().equals("Hs00365268_m1") ||
						triple.getPredicate().getURI().equals("http://example.com/geo_experiments/assay_id") 	&& triple.getObject().getLiteralValue().equals("Hs00739326_m1"))
					  numberOfStatementsRule3++;
			}else if(triple.getSubject().getURI().equals("http://example.org/onto/individual/ABCA12_ABCA12_Hs00292421_m1,Hs00252524_m1")) {
				  if (	triple.getPredicate().getURI().equals("http://example.com/geo_experiments/id") 			&& triple.getObject().getLiteralValue().equals("ABCA12") ||
						triple.getPredicate().getURI().equals("http://example.com/geo_experiments/ORF") 		&& triple.getObject().getLiteralValue().equals("ABCA12") ||
						triple.getPredicate().getURI().equals("http://example.com/geo_experiments/assay_id") 	&& triple.getObject().getLiteralValue().equals("Hs00292421_m1") ||
						triple.getPredicate().getURI().equals("http://example.com/geo_experiments/assay_id") 	&& triple.getObject().getLiteralValue().equals("Hs00252524_m1"))
					numberOfStatementsRule4++;
			}else{
				assert(false);
			}
		}

		assertEquals(3, numberOfStatementsRule1);
		assertEquals(3, numberOfStatementsRule2);
		assertEquals(4, numberOfStatementsRule3);
		assertEquals(4, numberOfStatementsRule4);
	}


	@Test
	public void ruleDoesNotExist() throws Exception {
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";
		ontologyHelper = new OntologyHelper();
		ontologyHelper.loadingOntologyFromFile(ontologyPath);


		//RULE 1
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig(id, "http://example.org/onto/individual/");
		OWLClass subjectClass = ontologyHelper.getClass("investigation");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();

		TSVColumn subject = new TSVColumn();
		subject.setTitle("GSE67111_SERIES");
		subject.setFlags(new ArrayList<>());

		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();

		OWLProperty hasParticipantProperty = ontologyHelper.getProperty("has participant");
		TripleObject hasParticipantTripleObject = new TripleObjectAsRule(new ObjectAsRule("rule2", new ArrayList<>()));
		predicateObjects.put(hasParticipantProperty, hasParticipantTripleObject);

		Rule rule = new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);

		listRules.add(rule);
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "NormalizedData.txt");

		thrown.expect(RuleNotFound.class);
		thrown.expectMessage("Rule rule2 was not found/created check your file.");

		Map<String, SearchBlock> searchBlocks = new HashMap<>();

		processingClass.createTriplesFromRules(listRules,
												conditionsBlocks,
												searchBlocks,
												RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
												ontologiesList);

	}


	// ##################


	private List<Rule> createRuleWithSearchBlock(){
		List<Rule> rules = new ArrayList<>();
		//RULE 1
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig(id, "http://example.org/onto/individual/").setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("Platform Data");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();

		TSVColumn 	subject = new TSVColumn();
					subject.setTitle("GPL19921_ID");
					subject.setFlags(new ArrayList<>());

		TSVColumn 	subject2 = new TSVColumn();
					subject2.setTitle("GPL19921_ORF");
					subject2.setFlags(new ArrayList<>());

		TSVColumn 	subject3 = new TSVColumn();
					subject3.setTitle("GPL19921_Assay ID");
					subject3.setFlags(new ArrayList<>());

		subjectTSVColumns.add(subject);
		subjectTSVColumns.add(subject2);
		subjectTSVColumns.add(subject3);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();

		OWLProperty 	idProperty 		= ontologyHelper.getProperty("ID");
		TripleObject 	idTripleObject 	= new TripleObjectAsRule(new ObjectAsRule("rule2", new ArrayList<>()));

		predicateObjects.put(idProperty, idTripleObject);

		rules.add(new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects));


		//RULE 2
		id = "rule2";
		ruleConfig = new RuleConfig(id, "http://example.org/onto/individual/").setMatrix(true);
		subjectClass = ontologyHelper.getClass("genetic material");
		subjectTSVColumns = new ArrayList<>();

		subject = new TSVColumn();
		subject.setTitle("GPL19921_ID");
		List<Flag> subjectFlags = new ArrayList<>();
		subjectFlags.add(new FlagSearchBlock("search1", "?s"));
		subject.setFlags(subjectFlags);

		subjectTSVColumns.add(subject);

		predicateObjects = new ArrayListValuedHashMap<>();

		rules.add(new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects));

		return rules;
	}

	@Test
	public void processRuleWithSearchBlock() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/geo_preprocessed/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.addAll(createRuleWithSearchBlock());

		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		SearchBlock searchBlock = new SearchBlock("search1", "http://bio2rdf.org/sparql", "select distinct * where { ?s <http://bio2rdf.org/ctd_vocabulary:gene-symbol> ?o1. bind(str(?o1) as ?o). values ?o { ?tsvData } }");
		searchBlocks.put(searchBlock.getId(), searchBlock);
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "GSE67111.tsv");
		processingClass.addDatasetToBeProcessed(testFolderPath + "GPL19921.tsv");
		try{
			processingClass.createTriplesFromRules(listRules,
													conditionsBlocks,
													searchBlocks,
													RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
													ontologiesList);
		}catch(Exception e) {
			e.printStackTrace();
			fail();

		}

		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

		int numberOfStatementsRule1 = 0;
		int numberOfStatementsRule2 = 0;
		int numberOfStatementsRule3 = 0;
		for(Statement statement : statements) {
			Triple triple = statement.asTriple();

			if( 	triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 	||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")			||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#range")				) //is not interesting to check these predicates because there are other classes in the ontology if tested will get away from the objective of this method
				continue;

			if(triple.getSubject().getURI().equals("http://example.org/onto/individual/ABCA1_ABCA1_Hs00194045_m1")) {
				if (	triple.getPredicate().getURI().equals("http://example.com/geo_experiments/id") 			&& triple.getObject().getURI().equals("http://bio2rdf.org/ncbigene:19"))
					numberOfStatementsRule1++;
			}else if(triple.getSubject().getURI().equals("http://example.org/onto/individual/ABCA10_ABCA10_Hs00365268_m1,Hs00739326_m1")) {
				if (	triple.getPredicate().getURI().equals("http://example.com/geo_experiments/id") 			&& triple.getObject().getURI().equals("http://bio2rdf.org/ncbigene:10349"))
					numberOfStatementsRule2++;
			}else if(triple.getSubject().getURI().equals("http://example.org/onto/individual/ABCA12_ABCA12_Hs00292421_m1,Hs00252524_m1")) {
				if (	triple.getPredicate().getURI().equals("http://example.com/geo_experiments/id") 			&& triple.getObject().getURI().equals("http://bio2rdf.org/ncbigene:26154"))
					numberOfStatementsRule3++;
			}else{
				assert(false);
			}
		}

		assertEquals(1, numberOfStatementsRule1);
		assertEquals(1, numberOfStatementsRule2);
		assertEquals(1, numberOfStatementsRule3);
	}

	// ##################

	private Rule createRuleWithColFlag() {
		String id = "rule1";
		RuleConfig ruleConfig = new RuleConfig("default", "http://example.org/onto/individual/");
		OWLClass subjectClass = ontologyHelper.getClass("Gene");
		List<TSVColumn> subjectTSVColumns = new ArrayList<>();

		TSVColumn subject = new TSVColumn();
		subject.setTitle("");
		List<Flag> subjectFlags = new ArrayList<>();
		subjectFlags.add(new FlagCol("enrichedDataGOTerm.tsv", 5, null));
		subject.setFlags(subjectFlags);

		subjectTSVColumns.add(subject);

		MultiValuedMap<OWLProperty, TripleObject> predicateObjects = new ArrayListValuedHashMap<>();
		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processcreateRuleWithColFlag() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithColFlag());

		Map<String, SearchBlock> searchBlocks = new HashMap<>();
		ontologiesList.add(ontologyPath);

		TriplesProcessing processingClass = new TriplesProcessing();;
		processingClass.addDatasetToBeProcessed(testFolderPath + "enrichedDataGOTerm.tsv");
		processingClass.addDatasetToBeProcessed(testFolderPath + "enrichedDataGOTerm2.tsv");
		try{
			processingClass.createTriplesFromRules(listRules,
												conditionsBlocks,
												searchBlocks,
												RuleConfig.getDefaultRuleConfigFromRuleList(listRules),
												ontologiesList);
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}

		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

		for(Statement statement : statements) {
			Triple triple = statement.asTriple();

			assert(triple.getSubject().getURI().equals("http://example.org/onto/individual/0.000397262")); //means that the software was able to find the column
		}
	}

}
