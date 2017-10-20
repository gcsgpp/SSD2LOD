package br.usp.ffclrp.dcm.lssb.transformation_software;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;

import br.usp.ffclrp.dcm.lssb.transformation_software.OntologyHelper;
import br.usp.ffclrp.dcm.lssb.transformation_software.TriplesProcessing;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ContentDirectionTSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumContentDirectionTSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumOperationsConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Flag;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagBaseIRI;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagConditionBlock;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.ObjectAsRule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Rule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Separator;
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

	public Rule createRuleOne() {
		String id = "1";
		OWLClass subjectClass = ontologyHelper.getClass("Term");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();
		
		subject.setTitle("Term");
		
		List<Flag> subjectFlags = new ArrayList<Flag>();
		List<Integer> separatorCols = new ArrayList<Integer>();
		separatorCols.add(0);
		subjectFlags.add(new Separator("~", separatorCols));
		subjectFlags.add(new FlagBaseIRI("http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=", "go"));
		subjectFlags.add(new FlagConditionBlock(1));
		subjectFlags.add(new ContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		
		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);
		
		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();
		
		/* has_pvalue predicate */
		TSVColumn pvalueColumn = new TSVColumn();
		pvalueColumn.setTitle("PValue");
		
		List<Flag> pvalueFlags = new ArrayList<Flag>();
		pvalueFlags.add(new ContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		pvalueColumn.setFlags(pvalueFlags);
		
		predicateObjects.put(ontologyHelper.getProperty("has_pvalue"), new TripleObjectAsColumns(pvalueColumn));
		
		/* name predicate */
		TSVColumn nameColumn = new TSVColumn();
		nameColumn.setTitle("Term");
		
		List<Flag> nameFlags = new ArrayList<Flag>();
		nameFlags.add(new ContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		separatorCols = new ArrayList<Integer>();
		separatorCols.add(1);
		nameFlags.add(new Separator("~", separatorCols));
		nameColumn.setFlags(nameFlags);
		
		predicateObjects.put(ontologyHelper.getProperty("name"), new TripleObjectAsColumns(nameColumn));
		
		/* 'has participant' predicate */
		TSVColumn participantColumn = new TSVColumn();
		participantColumn.setTitle("Term");
		
		List<Flag> participantFlags = new ArrayList<Flag>();
		participantFlags.add(new ContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		participantColumn.setFlags(participantFlags);
		
		predicateObjects.put(ontologyHelper.getProperty("has participant"), new TripleObjectAsRule(new ObjectAsRule(3, participantFlags)));
		
		return new Rule(id, subjectClass, subjectTSVColumns, predicateObjects);
	}

	public Rule createRuleTwo() {
		String id = "2";
		OWLClass subjectClass = ontologyHelper.getClass("Term");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();
		
		subject.setTitle("Term");
		
		List<Flag> subjectFlags = new ArrayList<Flag>();
		List<Integer> separatorCols = new ArrayList<Integer>();
		separatorCols.add(0);
		subjectFlags.add(new Separator(":", separatorCols));
		subjectFlags.add(new FlagBaseIRI("http://www.kegg.jp/entry/", "kegg"));
		subjectFlags.add(new FlagConditionBlock(2));
		subjectFlags.add(new ContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		
		subject.setFlags(subjectFlags);
		subjectTSVColumns.add(subject);
		
		Map<OWLProperty, TripleObject> predicateObjects = new HashMap<OWLProperty, TripleObject>();
		
		/* has_pvalue predicate */
		TSVColumn pvalueColumn = new TSVColumn();
		pvalueColumn.setTitle("PValue");
		
		List<Flag> pvalueFlags = new ArrayList<Flag>();
		pvalueFlags.add(new ContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		pvalueColumn.setFlags(pvalueFlags);
		
		predicateObjects.put(ontologyHelper.getProperty("has_pvalue"), new TripleObjectAsColumns(pvalueColumn));
		
		/* name predicate */
		TSVColumn nameColumn = new TSVColumn();
		nameColumn.setTitle("Term");
		
		List<Flag> nameFlags = new ArrayList<Flag>();
		nameFlags.add(new ContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		separatorCols = new ArrayList<Integer>();
		separatorCols.add(1);
		nameFlags.add(new Separator(":", separatorCols));
		nameColumn.setFlags(nameFlags);
		
		predicateObjects.put(ontologyHelper.getProperty("name"), new TripleObjectAsColumns(nameColumn));
		
		/* 'has participant' predicate */
		TSVColumn participantColumn = new TSVColumn();
		participantColumn.setTitle("Term");
		
		List<Flag> participantFlags = new ArrayList<Flag>();
		participantFlags.add(new ContentDirectionTSVColumn(EnumContentDirectionTSVColumn.DOWN));
		participantColumn.setFlags(participantFlags);
		
		predicateObjects.put(ontologyHelper.getProperty("has participant"), new TripleObjectAsRule(new ObjectAsRule(3, participantFlags)));
		
		return new Rule(id, subjectClass, subjectTSVColumns, predicateObjects);
	}
	
	public Rule createRuleThree() {
		String id = "3";
		OWLClass subjectClass = ontologyHelper.getClass("Gene");
		List<TSVColumn> subjectTSVColumns = new ArrayList<TSVColumn>();
		TSVColumn subject = new TSVColumn();
		
		subject.setTitle("Genes");
		
		List<Flag> subjectFlags = new ArrayList<Flag>();
		List<Integer> separatorCols = new ArrayList<Integer>();
		separatorCols.add(Integer.MAX_VALUE);
		subjectFlags.add(new Separator(", ", separatorCols));
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
	public void processRule() throws Exception
	{
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		
		ontologyHelper.loadingOntologyFromFile(testFolderPath + "ontology.owl");
		listRules.add(createRuleOne());
		listRules.add(createRuleThree());
		createConditionBlocks();
		
		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "enrichedDataGOTerm.tsv", testFolderPath + "ontology.owl");
		processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http:\\example.org/onto/individual#");
		
		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();
		
		for(Statement statement : statements) {
			Triple triple = statement.asTriple();
			if(triple.getSubject().getURI().equals("http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=GO:0030001")){
				assert(	triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_pvalue") 		&& triple.getObject().getLiteralValue().equals("0.000397262") ||
						triple.getPredicate().getURI().equals("http://schema.org/name") 				&& triple.getObject().getLiteralValue().equals("metal ion transport") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=\"SLC5A5") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=JPH3") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A4") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=SLC5A12\""));
			}else {
				assert(false);
			}
		}
	}
	
	@Test
	public void processRuleRuleTwoTest() throws Exception
	{
		ontologyHelper = new OntologyHelper();
		String testFolderPath = "testFiles/unitTestsFiles/";
		
		ontologyHelper.loadingOntologyFromFile(testFolderPath + "ontology.owl");
		listRules.add(createRuleTwo());
		listRules.add(createRuleThree());
		createConditionBlocks();
		
		TriplesProcessing processingClass = new TriplesProcessing(testFolderPath + "enrichedDataKeggTerm.tsv", testFolderPath + "ontology.owl");
		processingClass.createTriplesFromRules(listRules, conditionsBlocks, "http:\\example.org/onto/individual#");
		
		Model model = processingClass.getModel();
		List<Statement> statements = model.listStatements().toList();
		
		for(Statement statement : statements) {
			Triple triple = statement.asTriple();
			if(triple.getSubject().getURI().equals("http://www.kegg.jp/entry/hsa00190")){
				assert(	triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_pvalue") 		&& triple.getObject().getLiteralValue().equals("0.020404871") ||
						triple.getPredicate().getURI().equals("http://schema.org/name") 				&& triple.getObject().getLiteralValue().equals("Oxidative phosphorylation") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=\"UQCRC2") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=UQCRC1") ||
						triple.getPredicate().getURI().equals("http://purl.org/g/onto/has_participant")	&& triple.getObject().getURI().equals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=ATP5J\""));
			}else {
				assert(false);
			}
		}
	}
	
	
}
