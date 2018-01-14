package br.usp.ffclrp.dcm.lssb.transformation_software;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.BaseIRIException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.ConditionBlockException;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.*;
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
	private Map<Integer, ConditionBlock> conditionsBlocks = new HashMap<Integer, ConditionBlock>();
	private List<Rule> listRules = new ArrayList<Rule>();


	@org.junit.Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void initializer() {
		ontologyHelper = null;
		conditionsBlocks = new HashMap<Integer, ConditionBlock>();
		listRules = new ArrayList<Rule>();
	}

	private Rule createRuleOne() {
		String id = "1";
		RuleConfig ruleConfig = new RuleConfig("default");
		ruleConfig.setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("Term");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("Term");

		List<Flag> subjectFlags = new ArrayList<Flag>();
		List<Integer> separatorCols = new ArrayList<Integer>();
		separatorCols.add(0);
		subjectFlags.add(new FlagSeparator("~", separatorCols));
		subjectFlags.add(new FlagBaseIRI("http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=", "go"));
		subjectFlags.add(new FlagConditionBlock(1));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();

		/* has_pvalue predicate */
		TSVColumn pvalueColumn = new TSVColumn();
		pvalueColumn.setTitle("PValue");

		List<Flag> pvalueFlags = new ArrayList<Flag>();
		pvalueColumn.setFlags(pvalueFlags);

		predicateObjects.put(ontologyHelper.getProperty("has_pvalue"), new TripleObjectAsColumns(pvalueColumn));

		/* name predicate */
		TSVColumn nameColumn = new TSVColumn();
		nameColumn.setTitle("Term");

		List<Flag> nameFlags = new ArrayList<Flag>();
		separatorCols = new ArrayList<Integer>();
		separatorCols.add(1);
		nameFlags.add(new FlagSeparator("~", separatorCols));
		nameColumn.setFlags(nameFlags);

		predicateObjects.put(ontologyHelper.getProperty("name"), new TripleObjectAsColumns(nameColumn));

		/* 'has participant' predicate */
		TSVColumn participantColumn = new TSVColumn();
		participantColumn.setTitle("Genes");

		List<Flag> participantFlags = new ArrayList<Flag>();
		participantColumn.setFlags(participantFlags);

		predicateObjects.put(ontologyHelper.getProperty("has participant"), new TripleObjectAsRule(new ObjectAsRule(3, participantFlags)));

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	private Rule createRuleTwo() {
		String id = "2";
		RuleConfig ruleConfig = new RuleConfig("default");
		ruleConfig.setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("Term");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("Term");

		List<Flag> subjectFlags = new ArrayList<Flag>();
		List<Integer> separatorCols = new ArrayList<Integer>();
		separatorCols.add(0);
		subjectFlags.add(new FlagSeparator(":", separatorCols));
		subjectFlags.add(new FlagBaseIRI("http://www.kegg.jp/entry/", "kegg"));
		subjectFlags.add(new FlagConditionBlock(2));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();

		/* has_pvalue predicate */
		TSVColumn pvalueColumn = new TSVColumn();
		pvalueColumn.setTitle("PValue");

		List<Flag> pvalueFlags = new ArrayList<Flag>();
		pvalueColumn.setFlags(pvalueFlags);

		predicateObjects.put(ontologyHelper.getProperty("has_pvalue"), new TripleObjectAsColumns(pvalueColumn));

		/* name predicate */
		TSVColumn nameColumn = new TSVColumn();
		nameColumn.setTitle("Term");

		List<Flag> nameFlags = new ArrayList<Flag>();
		separatorCols = new ArrayList<Integer>();
		separatorCols.add(1);
		nameFlags.add(new FlagSeparator(":", separatorCols));
		nameColumn.setFlags(nameFlags);

		predicateObjects.put(ontologyHelper.getProperty("name"), new TripleObjectAsColumns(nameColumn));

		/* 'has participant' predicate */
		TSVColumn participantColumn = new TSVColumn();
		participantColumn.setTitle("Term");

		List<Flag> participantFlags = new ArrayList<Flag>();
		participantColumn.setFlags(participantFlags);

		predicateObjects.put(ontologyHelper.getProperty("has participant"), new TripleObjectAsRule(new ObjectAsRule(3, participantFlags)));

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	private Rule createRuleThree() {
		String id = "3";
		RuleConfig ruleConfig = new RuleConfig("default");
		ruleConfig.setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("Gene");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("Genes");

		List<Flag> subjectFlags = new ArrayList<Flag>();
		List<Integer> separatorCols = new ArrayList<Integer>();
		separatorCols.add(Integer.MAX_VALUE);
		subjectFlags.add(new FlagSeparator(", ", separatorCols));
		subjectFlags.add(new FlagBaseIRI("http://www.genecards.org/cgi-bin/carddisp.pl?gene=", "genecard"));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	private void createConditionBlocks() {		
		List<Condition> conditions = new ArrayList<Condition>();
		conditions.add(new Condition("Category", EnumOperationsConditionBlock.DIFFERENT, "KEGG_PATHWAY"));
		conditions.add(new Condition("PValue", EnumOperationsConditionBlock.LESSTHAN, "0.01"));

		conditionsBlocks.put(1, new ConditionBlock("1", conditions));

		conditions = new ArrayList<Condition>();
		conditions.add(new Condition("Category", EnumOperationsConditionBlock.EQUAL, "KEGG_PATHWAY"));
		conditions.add(new Condition("PValue", EnumOperationsConditionBlock.LESSTHAN, "0.03"));

		conditionsBlocks.put(2, new ConditionBlock("2", conditions));
	}

	@Test
	public void processRuleWithConditionblockflagBaseiriflagSeparatorflag()	{
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleOne());
		listRules.add(createRuleThree());
		createConditionBlocks();

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "enrichedDataGOTerm.tsv");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
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
	public void processRuleWithColonAsSeparator()	{
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleTwo());
		listRules.add(createRuleThree());
		createConditionBlocks();

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "enrichedDataKeggTerm.tsv");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
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
		String id = "1";
		RuleConfig ruleConfig = new RuleConfig("default", "http://example.org/onto/individual#");
		ruleConfig.setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("GSM1243183");

		List<Flag> subjectFlags = new ArrayList<Flag>();
		subjectFlags.add(new FlagNotMetadata(true));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithNotMetadataFlag() {		
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithNotMetadataFlag());

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "NormalizedData.txt");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
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


	@Test
	public void assertingConditionBlockThatDontExist() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleOne());

		thrown.expect(ConditionBlockException.class);
		thrown.expectMessage("No condition block created");
		
		TriplesProcessing processingClass = new TriplesProcessing( testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "enrichedDataGOTerm2.tsv");

		processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
	}


    // ##################


	private Rule createRuleWithFixedContentFlagOnSubjectLine() {
		String id = "1";
		RuleConfig ruleConfig = new RuleConfig("default", "http://example.org/onto/individual#");
		ruleConfig.setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("");

		List<Flag> subjectFlags = new ArrayList<Flag>();
		subjectFlags.add(new FlagFixedContent("NormalizedData"));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithFixedContentFlagOnSubjectLine() {		
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithFixedContentFlagOnSubjectLine());

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "NormalizedData.txt");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
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
		String id = "1";
		RuleConfig ruleConfig = new RuleConfig("default");
		ruleConfig.setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("GSM1243183");
		List<Flag> subjectFlags = new ArrayList<Flag>();
		subjectFlags.add(new FlagNotMetadata(true));
		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();
		
		OWLProperty enrichmentProperty = ontologyHelper.getProperty("has enrichement");
		
		List<Flag> objectFlags = new ArrayList<Flag>();
		objectFlags.add(new FlagFixedContent("test fixed content"));
		TripleObject object = new TripleObjectAsColumns(new TSVColumn("GSM1243183", objectFlags));
		
		predicateObjects.put(enrichmentProperty, object);

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithFixedContentFlagOnObjectLine() {		
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithFixedContentFlagOnObjectLine());

		TriplesProcessing processingClass = new TriplesProcessing( testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "NormalizedData.txt");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}
		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

		assertEquals(2, statements.size()); // the triple with rdfs:type and with the "test fixed content";
		for(Statement statement : statements) {
			Triple triple = statement.asTriple();
			if(triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				assertEquals("http://www.purl.org/g/classes#geo_sample", triple.getObject().getURI());
			} else if(triple.getPredicate().getURI().equals("http://www.purl.org/g/properties#enrichment")) {
				assertEquals("test fixed content", triple.getObject().getLiteralValue());
			} else {
				fail();
			}			
		}
	}


    // ##################


	private Rule createRuleWithCustomIDFlag() {
		String id = "1";
		RuleConfig ruleConfig = new RuleConfig("default", "http://example.org/onto/individual#");
		ruleConfig.setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		
		TSVColumn subject = new TSVColumn();
		subject.setTitle("");
		List<Flag> subjectFlags = new ArrayList<Flag>();
		subjectFlags.add(new FlagCustomID("NormalizedData"));
		subject.setFlags(subjectFlags);
		
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();
		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithCustomIDFlag() {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithCustomIDFlag());

		TriplesProcessing processingClass = new TriplesProcessing( testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "NormalizedData.txt");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks,  "http://example.org/onto/individual#");
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
		String id = "1";
		RuleConfig ruleConfig = new RuleConfig("default");
		ruleConfig.setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();

		TSVColumn subject = new TSVColumn();
		subject.setTitle("");
		List<Flag> subjectFlags = new ArrayList<Flag>();
		subjectFlags.add(new FlagBaseIRI("http://purl.org/g/", null));
		subject.setFlags(subjectFlags);

		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();
		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithBaseIRIWithNoNamespace() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithBaseIRIWithNoNamespace());

		TriplesProcessing processingClass = new TriplesProcessing( testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "NormalizedData.txt");

        thrown.expect(BaseIRIException.class);
        thrown.expectMessage("Some BaseIRI flag has an empty namespace field.");

        processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
	}


    // ##################


	private Rule createRuleWithBaseIRIWithNoIRI() {
		String id = "1";
		RuleConfig ruleConfig = new RuleConfig("default");
		ruleConfig.setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();

		TSVColumn subject = new TSVColumn();
		subject.setTitle("");
		List<Flag> subjectFlags = new ArrayList<Flag>();
		subjectFlags.add(new FlagBaseIRI(null, "purl"));
		subject.setFlags(subjectFlags);

		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();
		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithBaseIRIWithNoIRI() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithBaseIRIWithNoIRI());

		TriplesProcessing processingClass = new TriplesProcessing( testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "NormalizedData.txt");

		thrown.expect(BaseIRIException.class);
		thrown.expectMessage("Some BaseIRI flag has an empty IRI field.");

		processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
	}


    // ##################


	@Test
	public void processRuleWithConditionBlockNotMet()	{
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleOne());
		listRules.add(createRuleThree());
		createConditionBlocks();
		
		//Changing the type of operation to manage not met the data inside the dataset
		ConditionBlock cb = conditionsBlocks.get(1);
		for(Iterator<Condition> iteratorCB = cb.getConditions().iterator(); iteratorCB.hasNext(); ) {
			Condition condition = iteratorCB.next();
			if(condition.getColumn().equals("Category")) {
				condition.setOperation(EnumOperationsConditionBlock.EQUAL);
			}
		}

		TriplesProcessing processingClass = new TriplesProcessing( testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "enrichedDataGOTerm.tsv");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
		}catch(Exception e) {
			fail();
		}
	}


    // ##################


	private Rule createRuleSeparatorElementInSeparatorFlagDoesNotExist() {
		String id = "1";
		RuleConfig ruleConfig = new RuleConfig("default");
		ruleConfig.setMatrix(true);
		OWLClass subjectClass = ontologyHelper.getClass("Gene");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		
		TSVColumn subject = new TSVColumn();
		subject.setTitle("Genes");
		List<Flag> subjectFlags = new ArrayList<Flag>();
		subjectFlags.add(new FlagSeparator("-", new ArrayList<Integer>()));
		subject.setFlags(subjectFlags);
		
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();
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
		processingClass.addFilesToBeProcessed(testFolderPath + "enrichedDataGOTerm2.tsv");

		processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
	}
*/

	// ##################


	private void createConditionBlockWithGreaterThanCondition() {
		List<Condition> conditions = new ArrayList<Condition>();
		conditions.add(new Condition("Pop Hits", EnumOperationsConditionBlock.GREATERTHAN, "800"));

		conditionsBlocks.put(1, new ConditionBlock("1", conditions));
	}

	@Test
	public void processRuleWithConditionblockWithGreaterThanCondition()	{
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleOne());
		listRules.add(createRuleThree());
		createConditionBlockWithGreaterThanCondition();

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "enrichedDataGOTerm3.tsv");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
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
    public void multipleFiles()	{
        ontologyHelper = new OntologyHelper();
        String testFolderPath = "testFiles/unitTestsFiles/";
        String ontologyPath = testFolderPath + "ontology.owl";

        ontologyHelper.loadingOntologyFromFile(ontologyPath);
        listRules.add(createRuleOne());
        listRules.add(createRuleThree());
        createConditionBlocks();

        TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "ontology.owl");
        processingClass.addFilesToBeProcessed(testFolderPath + "enrichedDataGOTerm.tsv");
        processingClass.addFilesToBeProcessed(testFolderPath + "enrichedDataGOTerm3.tsv");
        try{
            processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
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
		String id = "1";
		RuleConfig ruleConfig = new RuleConfig(id, "http://example.org/onto/individual#");
		OWLClass subjectClass = ontologyHelper.getClass("investigation");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();

		TSVColumn subject = new TSVColumn();
		subject.setTitle("GSE67111_SERIES");
		List<Flag> subjectFlags = new ArrayList<Flag>();
		subject.setFlags(subjectFlags);

		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();

		OWLProperty statusProperty = ontologyHelper.getProperty("status");
		List<Flag> contentDirection = new ArrayList<Flag>();
		TripleObject statusTripleObject = new TripleObjectAsColumns(new TSVColumn("GSE67111_status",contentDirection));

		OWLProperty summaryProperty = ontologyHelper.getProperty("summary");
		TripleObject summaryTripleObject = new TripleObjectAsColumns(new TSVColumn("GSE67111_summary",contentDirection));


		predicateObjects.put(statusProperty, statusTripleObject);
		predicateObjects.put(summaryProperty, summaryTripleObject);

		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processSimpleRule()	{
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/geo_preprocessed/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createSimpleRule());
		createConditionBlockWithGreaterThanCondition();

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "GSE67111.tsv");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
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
		String id = "1";
		RuleConfig ruleConfig = new RuleConfig(id, "http://example.org/onto/individual#");
		OWLClass subjectClass = ontologyHelper.getClass("investigation");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();

		TSVColumn subject = new TSVColumn();
		subject.setTitle("GSE67111_SERIES");
		List<Flag> subjectFlags = new ArrayList<Flag>();
		subject.setFlags(subjectFlags);

		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();

		TripleObject hasParticipantTripleObject = new TripleObjectAsRule(new ObjectAsRule(2, new ArrayList<Flag>()));

		OWLProperty hasParticipantProperty = ontologyHelper.getProperty("has participant");

		predicateObjects.put(hasParticipantProperty, hasParticipantTripleObject);

		rules.add(new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects));

		//RULE 2
		id = "2";
		ruleConfig = new RuleConfig(id, "http://example.org/onto/individual#");
		subjectClass = ontologyHelper.getClass("microarray platform");
		subjectTSVColumns = new ArrayList<TSVColumn>();

		subject = new TSVColumn();
		subject.setTitle("GPL19921_geo_accession");
		subject.setFlags(new ArrayList<Flag>());
		subjectTSVColumns.add(subject);

		predicateObjects = new HashMap<>();

		OWLProperty titleProperty = ontologyHelper.getProperty("Title");
		TripleObject titleTripleObject = new TripleObjectAsColumns(new TSVColumn("GPL19921_title", new ArrayList<Flag>()));
		predicateObjects.put(titleProperty, titleTripleObject);

		rules.add(new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects));


		return rules;
	}

	@Test
	public void processSimpleRuleInsideSimpleRuleAndOtherFile()	{
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/geo_preprocessed/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.addAll(createSimpleRuleInsideSimpleRuleAndOtherFile());
		createConditionBlockWithGreaterThanCondition();

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "GSE67111.tsv");
		processingClass.addFilesToBeProcessed(testFolderPath + "GPL19921.tsv");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
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
		String id = "1";
		RuleConfig ruleConfig = new RuleConfig(id, "http://example.org/onto/individual/");
		OWLClass subjectClass = ontologyHelper.getClass("investigation");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();

		TSVColumn subject = new TSVColumn();
		subject.setTitle("GSE67111_SERIES");
		subject.setFlags(new ArrayList<Flag>());

		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();

		OWLProperty hasParticipantProperty = ontologyHelper.getProperty("has participant");
		TripleObject hasParticipantTripleObject = new TripleObjectAsRule(new ObjectAsRule(2, new ArrayList<Flag>()));
		predicateObjects.put(hasParticipantProperty, hasParticipantTripleObject);

		rules.add(new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects));

		//RULE 2
		id = "2";
		ruleConfig = new RuleConfig(id, "http://example.org/onto/individual/");
		ruleConfig.setMatrix(true);
		subjectClass = ontologyHelper.getClass("Platform Data");
		subjectTSVColumns = new ArrayList<TSVColumn>();

		subject = new TSVColumn();
		subject.setTitle("GPL19921_ID");
		subject.setFlags(new ArrayList<Flag>());

		TSVColumn subject2 = new TSVColumn();
		subject2.setTitle("GPL19921_ORF");
		subject2.setFlags(new ArrayList<Flag>());

		TSVColumn subject3 = new TSVColumn();
		subject3.setTitle("GPL19921_Assay ID");
		subject3.setFlags(new ArrayList<Flag>());

		subjectTSVColumns.add(subject);
		subjectTSVColumns.add(subject2);
		subjectTSVColumns.add(subject3);

		predicateObjects = new HashMap<>();

		OWLProperty idProperty = ontologyHelper.getProperty("ID");
		TripleObject idTripleObject = new TripleObjectAsColumns(new TSVColumn("GPL19921_ID",new ArrayList<Flag>()));

		OWLProperty orfProperty = ontologyHelper.getProperty("ORF");
		TripleObject orfTripleObject = new TripleObjectAsColumns(new TSVColumn("GPL19921_ORF",new ArrayList<Flag>()));

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
	public void processSimpleRuleInsideMatrixRule()	{
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/geo_preprocessed/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.addAll(createSimpleRuleInsideMatrixRule());
		createConditionBlockWithGreaterThanCondition();

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "ontology.owl");
		processingClass.addFilesToBeProcessed(testFolderPath + "GSE67111.tsv");
		processingClass.addFilesToBeProcessed(testFolderPath + "GPL19921.tsv");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual/");
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
}
