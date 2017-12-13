package br.usp.ffclrp.dcm.lssb.transformation_software;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.FileAccessException;
import br.usp.ffclrp.dcm.lssb.transformation_software.OntologyHelper;
import br.usp.ffclrp.dcm.lssb.transformation_software.TriplesProcessing;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumContentDirectionTSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumOperationsConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Flag;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagBaseIRI;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagContentDirectionTSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagCustomID;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagFixedContent;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagNotMetadata;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ObjectAsRule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Rule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagSeparator;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObject;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObjectAsColumns;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObjectAsRule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Condition;

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
		participantColumn.setTitle("Term");

		List<Flag> participantFlags = new ArrayList<Flag>();
		participantFlags.add(new FlagContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		participantColumn.setFlags(participantFlags);

		predicateObjects.put(ontologyHelper.getProperty("has participant"), new TripleObjectAsRule(new ObjectAsRule(3, participantFlags)));

		return new Rule(id, subjectClass, subjectTSVColumns, predicateObjects);
	}

	private Rule createRuleTwo() {
		String id = "2";
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

		return new Rule(id, subjectClass, subjectTSVColumns, predicateObjects);
	}

	private Rule createRuleThree() {
		String id = "3";
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

		return new Rule(id, subjectClass, subjectTSVColumns, predicateObjects);
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
	public void processRuleWithConditionblockflagBaseiriflagSeparatorflag()
	{
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleOne());
		listRules.add(createRuleThree());
		createConditionBlocks();

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "enrichedDataGOTerm.tsv", testFolderPath + "ontology.owl");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
		}catch(Exception e) {
			fail();
		}

		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

		for(Statement statement : statements) {
			Triple triple = statement.asTriple();

			if( triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 	||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")			||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#range")				) //is not interesting to check these predicates because there are other classes in the ontology if tested will get away from the objective of this method
				continue;

			if(triple.getSubject().getURI().equals("http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=GO:0030001")){
				assert(	triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_pvalue") 		&& triple.getObject().getLiteralValue().equals("0.000397262") ||
						triple.getPredicate().getURI().equals("http://schema.org/name") 				&& triple.getObject().getLiteralValue().equals("metal ion transport") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A5") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=JPH3") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A4") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A12"));
			}else {
				assert(false);
			}
		}
	}

	@Test
	public void processRuleWithColonAsSeparator()
	{
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleTwo());
		listRules.add(createRuleThree());
		createConditionBlocks();

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "enrichedDataKeggTerm.tsv", testFolderPath + "ontology.owl");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
		}catch(Exception e) {
			fail();
		}

		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();

		for(Statement statement : statements) {
			Triple triple = statement.asTriple();

			if( triple.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 	||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")			||
					triple.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#range")				) //is not interesting to check these predicates because there are other classes in the ontology if tested will get away from the objective of this method
				continue;

			if(triple.getSubject().getURI().equals("http://www.kegg.jp/entry/hsa00190")){
				assert(	triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_pvalue") 		&& triple.getObject().getLiteralValue().equals("0.020404871") ||
						triple.getPredicate().getURI().equals("http://schema.org/name") 				&& triple.getObject().getLiteralValue().equals("Oxidative phosphorylation") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=UQCRC2") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=UQCRC1") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=ATP5J"));
			}else {
				assert(false);
			}
		}
	}

	private Rule createRuleWithNotMetadataFlag() {
		String id = "1";
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("GSM1243183");

		List<Flag> subjectFlags = new ArrayList<Flag>();
		subjectFlags.add(new FlagNotMetadata(true));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();

		return new Rule(id, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithNotMetadataFlag() {		
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithNotMetadataFlag());

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "NormalizedData.txt", testFolderPath + "ontology.owl");
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

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "enrichedDataGOTerm2.tsv", testFolderPath + "ontology.owl");

		processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");

	}

	@Test
	public void assertingConditionBlockThatDontExist() throws Exception {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleOne());

		thrown.expect(NullPointerException.class);
		thrown.expectMessage("No condition block created");
		
		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "enrichedDataGOTerm2.tsv", testFolderPath + "ontology.owl");

		processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
	}

	private Rule createRuleWithFixedContentFlag() {
		String id = "1";
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();

		subject.setTitle("");

		List<Flag> subjectFlags = new ArrayList<Flag>();
		subjectFlags.add(new FlagFixedContent("NormalizedData"));

		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();

		return new Rule(id, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithFixedContentFlag() {		
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithFixedContentFlag());

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "NormalizedData.txt", testFolderPath + "ontology.owl");
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

	private Rule createRuleWithCustomIDFlag() {
		String id = "1";
		OWLClass subjectClass = ontologyHelper.getClass("geo sample");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		
		TSVColumn subject = new TSVColumn();
		subject.setTitle("");
		List<Flag> subjectFlags = new ArrayList<Flag>();
		subjectFlags.add(new FlagCustomID("NormalizedData"));
		subject.setFlags(subjectFlags);
		
		subjectTSVColumns.add(subject);

		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();
		return new Rule(id, subjectClass, subjectTSVColumns, predicateObjects);
	}

	@Test
	public void processRuleWithCustomIDFlag() {
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/normalizedFiles/";
		String ontologyPath = testFolderPath + "ontology.owl";

		ontologyHelper.loadingOntologyFromFile(ontologyPath);
		listRules.add(createRuleWithCustomIDFlag());

		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "NormalizedData.txt", testFolderPath + "ontology.owl");
		try{
			processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http://example.org/onto/individual#");
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

}
