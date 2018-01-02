package br.usp.ffclrp.dcm.lssb.transformation_software;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.BaseIRIException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.ConditionBlockException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.FileAccessException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.SeparatorFlagException;
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
		subjectFlags.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();

		/* has_pvalue predicate */
		TSVColumn pvalueColumn = new TSVColumn();
		pvalueColumn.setTitle("PValue");

		List<Flag> pvalueFlags = new ArrayList<Flag>();
		pvalueFlags.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		pvalueColumn.setFlags(pvalueFlags);

		predicateObjects.put(ontologyHelper.getProperty("has_pvalue"), new TripleObjectAsColumns(pvalueColumn));

		/* name predicate */
		TSVColumn nameColumn = new TSVColumn();
		nameColumn.setTitle("Term");

		List<Flag> nameFlags = new ArrayList<Flag>();
		nameFlags.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		separatorCols = new ArrayList<Integer>();
		separatorCols.add(1);
		nameFlags.add(new FlagSeparator("~", separatorCols));
		nameColumn.setFlags(nameFlags);

		predicateObjects.put(ontologyHelper.getProperty("name"), new TripleObjectAsColumns(nameColumn));

		/* 'has participant' predicate */
		TSVColumn participantColumn = new TSVColumn();
		participantColumn.setTitle("Genes");

		List<Flag> participantFlags = new ArrayList<Flag>();
		participantFlags.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
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
		subjectFlags.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();

		/* has_pvalue predicate */
		TSVColumn pvalueColumn = new TSVColumn();
		pvalueColumn.setTitle("PValue");

		List<Flag> pvalueFlags = new ArrayList<Flag>();
		pvalueFlags.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		pvalueColumn.setFlags(pvalueFlags);

		predicateObjects.put(ontologyHelper.getProperty("has_pvalue"), new TripleObjectAsColumns(pvalueColumn));

		/* name predicate */
		TSVColumn nameColumn = new TSVColumn();
		nameColumn.setTitle("Term");

		List<Flag> nameFlags = new ArrayList<Flag>();
		nameFlags.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		separatorCols = new ArrayList<Integer>();
		separatorCols.add(1);
		nameFlags.add(new FlagSeparator(":", separatorCols));
		nameColumn.setFlags(nameFlags);

		predicateObjects.put(ontologyHelper.getProperty("name"), new TripleObjectAsColumns(nameColumn));

		/* 'has participant' predicate */
		TSVColumn participantColumn = new TSVColumn();
		participantColumn.setTitle("Term");

		List<Flag> participantFlags = new ArrayList<Flag>();
		participantFlags.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
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
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "enrichedDataGOTerm.tsv", "http://example.org/onto/individual#");
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
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "enrichedDataKeggTerm.tsv", "http://example.org/onto/individual#");
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
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "NormalizedData.txt", "http://example.org/onto/individual#");
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
	public void accessingColumnThatDontExist() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleOne());
		createConditionBlocks();

		thrown.expect(FileAccessException.class);
		thrown.expectMessage("Not possible to access the file or the content in the file. Column tried to access: Term. This column may not exist or the file is not accessible.");

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "ontology.owl");

		processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "enrichedDataGOTerm2.tsv", "http://example.org/onto/individual#");

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

		processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "enrichedDataGOTerm2.tsv", "http://example.org/onto/individual#");
	}


    // ##################


	private Rule createRuleWithFixedContentFlagOnSubjectLine() {
		String id = "1";
		RuleConfig ruleConfig = new RuleConfig("default");
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
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "NormalizedData.txt", "http://example.org/onto/individual#");
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
		objectFlags.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
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
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "NormalizedData.txt", "http://example.org/onto/individual#");
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
		RuleConfig ruleConfig = new RuleConfig("default");
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
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "NormalizedData.txt", "http://example.org/onto/individual#");
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

        thrown.expect(BaseIRIException.class);
        thrown.expectMessage("Some BaseIRI flag has an empty namespace field.");

        processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "NormalizedData.txt", "http://example.org/onto/individual#");
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

		thrown.expect(BaseIRIException.class);
		thrown.expectMessage("Some BaseIRI flag has an empty IRI field.");

		processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "NormalizedData.txt", "http://example.org/onto/individual#");
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
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "enrichedDataGOTerm.tsv", "http://example.org/onto/individual#");
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
		subjectFlags.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		subjectFlags.add(new FlagSeparator("-", new ArrayList<Integer>()));
		subject.setFlags(subjectFlags);
		
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();
		return new Rule(id, ruleConfig, subjectClass, subjectTSVColumns, predicateObjects);
	}
	
	@Test
	public void separatorElementInSeparatorFlagDoesNotExist() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleSeparatorElementInSeparatorFlagDoesNotExist());

		thrown.expect(SeparatorFlagException.class);
		thrown.expectMessage("There is no caractere '-' in the field 'Genes' to be used as separator");
		
		TriplesProcessing processingClass = new TriplesProcessing( testFolderPath + "ontology.owl");

		processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "enrichedDataGOTerm2.tsv", "http://example.org/onto/individual#");
	}


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
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "enrichedDataGOTerm3.tsv", "http://example.org/onto/individual#");
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
        try{
            processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "enrichedDataGOTerm.tsv", "http://example.org/onto/individual#");
            processingClass.createTriplesFromRules(listRules, conditionsBlocks, testFolderPath + "enrichedDataGOTerm3.tsv", "http://example.org/onto/individual#");
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

}
