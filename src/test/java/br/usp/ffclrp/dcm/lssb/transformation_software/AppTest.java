package br.usp.ffclrp.dcm.lssb.transformation_software;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.ClassNotFoundInOntologyException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.ConditionBlockException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.PropertyNotExistException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.SeparatorFlagException;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.*;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.semanticweb.owlapi.model.OWLProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static org.junit.Assert.*;

public class AppTest
{
	@org.junit.Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void exctractConditionsFromString(){
		String content = "condition_block[1: \"Category\" != \"KEGG_PATHWAY\", \"PValue\" < \"0.01\" ]";
		content += "condition_block[2: \"Category\" == \"KEGG_PATHWAY\",	\"PValue\" < \"0.03\" ]";

		List<ConditionBlock> conditionsExtracted = null;
		try {
			conditionsExtracted = ConditionBlock.extractConditionsBlocksFromString(content);
		} catch (ConditionBlockException e) {
			e.printStackTrace();
			fail();
		}

		assertEquals(2, conditionsExtracted.size());

		for(ConditionBlock conditionBlock : conditionsExtracted){
			for(Condition condition : conditionBlock.getConditions())
				if(conditionBlock.getId() == 1){

					assertEquals(2, conditionBlock.getConditions().size());

					if(condition.getColumn().equals("Category")){
						assertEquals(EnumOperationsConditionBlock.DIFFERENT, condition.getOperation());
						assertEquals("KEGG_PATHWAY", condition.getConditionValue());

					}else if(condition.getColumn().equals("PValue")){
						assertEquals(EnumOperationsConditionBlock.LESSTHAN, condition.getOperation());
						assertEquals("0.01", condition.getConditionValue());
					}else{
						fail();
					}

				}else if(conditionBlock.getId() == 2){

					assertEquals(2, conditionBlock.getConditions().size());

					if(condition.getColumn().equals("Category")){
						assertEquals(EnumOperationsConditionBlock.EQUAL, condition.getOperation());
						assertEquals("KEGG_PATHWAY", condition.getConditionValue());

					}else if(condition.getColumn().equals("PValue")){
						assertEquals(EnumOperationsConditionBlock.LESSTHAN, condition.getOperation());
						assertEquals("0.03", condition.getConditionValue());
					}else{
						fail();
					}

				}else
					fail();
		}
	}

	@Test
	public void creatingRuleFromStringWithBaseIRISeparatorConditionBlock(){

		String 	ruleString = "matrix_rule[1, \"Term\" = \"Term\" /SP(\"~\", 1) /BASEIRI(\"http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=\", \"go\") /CB(1) :" +
				" \"has_pvalue\" = \"PValue\", " +
				" \"name\" = \"Term\" /SP(\"~\", 2), " +
				" \"has participant\" = 3	] ";

		ruleString = ruleString.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		app.ontologyHelper.loadingOntologyFromFile("testFiles/unitTestsFiles/ontology.owl");

		Rule rule1Extracted = null;
		try {
			rule1Extracted = app.createRulesFromBlock(ruleString);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		assertEquals("1", rule1Extracted.getId());
		assertEquals("http://purl.obolibrary.org/obo/NCIT_P382", rule1Extracted.getSubject().getIRI().toString());
		for(TSVColumn column : rule1Extracted.getSubjectTSVColumns()){
			if(column.getTitle().equals("Term")){

				assertEquals(3, column.getFlags().size()); //all from the rule line plus the ContentDirection

				for(Flag flag : column.getFlags()){
					if(flag instanceof FlagSeparator){
						FlagSeparator separator = (FlagSeparator) flag;
						assertEquals("~", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(0, separator.getColumns().get(0).intValue());

					}else if(flag instanceof FlagBaseIRI){
						FlagBaseIRI baseiri = (FlagBaseIRI) flag;
						assertEquals("http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=", baseiri.getIRI());
						assertEquals("go", baseiri.getNamespace());

					}else if(flag instanceof FlagConditionBlock){
						assertEquals(1, ((FlagConditionBlock) flag).getId().intValue());

					}else{
						fail();
					}
				}
			}else{
				fail();
			}
		}

		assertEquals(3, rule1Extracted.getPredicateObjects().size());

		for(Entry<OWLProperty, TripleObject> entry : rule1Extracted.getPredicateObjects().entrySet()){

			if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_pvalue")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals(1, object.getObject().size());
				assertEquals("PValue", object.getObject().get(0).getTitle());
				assertEquals(0, object.getObject().get(0).getFlags().size());

			}else if(entry.getKey().getIRI().toString().equals("http://schema.org/name")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals("Term", object.getObject().get(0).getTitle());

				List<Flag> flagsList = object.getObject().get(0).getFlags();
				assertEquals(1, flagsList.size());
				for(Flag flag : flagsList){
					if(flag instanceof FlagSeparator){
						FlagSeparator separator = (FlagSeparator) flag;
						assertEquals("~", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(1, separator.getColumns().get(0).intValue());
					}else{
						fail();
					}
				}

			}else if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_participant")){
				TripleObjectAsRule object = (TripleObjectAsRule) entry.getValue();

				assertEquals(1, object.getObject().size());

				for(ObjectAsRule objectAsRule : object.getObject()){
					assertEquals(3, objectAsRule.getRuleNumber().intValue());
					assertEquals(0, objectAsRule.getFlags().size());

				}

			}else
				fail();
		}
	}

	@Test
	public void creatingRuleFromStringUsingColonAsSeparator(){

		String 	ruleString = " matrix_rule[2, \"Term\" = \"Term\" /SP(\":\", 1) /BASEIRI(\"http://www.kegg.jp/entry/\", \"kegg\") /CB(2) : " +
				" \"has_pvalue\" = \"PValue\", " +
				" \"name\" = \"Term\" /SP(\":\", 2), " +
				" \"has participant\" = 3 " +
				"] ";


		ruleString = ruleString.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		app.ontologyHelper.loadingOntologyFromFile("testFiles/unitTestsFiles/ontology.owl");

		Rule rule2Extracted = null;
		try {
			rule2Extracted = app.createRulesFromBlock(ruleString);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		assertEquals("2", rule2Extracted.getId());
		assertEquals("http://purl.obolibrary.org/obo/NCIT_P382", rule2Extracted.getSubject().getIRI().toString());
		for(TSVColumn column : rule2Extracted.getSubjectTSVColumns()){
			if(column.getTitle().equals("Term")){

				assertEquals(3, column.getFlags().size()); //all from the rule line plus the ContentDirection

				for(Flag flag : column.getFlags()){
					if(flag instanceof FlagSeparator){
						FlagSeparator separator = (FlagSeparator) flag;
						assertEquals(":", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(0, separator.getColumns().get(0).intValue());
					}else if(flag instanceof FlagBaseIRI){
						FlagBaseIRI baseiri = (FlagBaseIRI) flag;
						assertEquals("http://www.kegg.jp/entry/", baseiri.getIRI());
						assertEquals("kegg", baseiri.getNamespace());
					}else if(flag instanceof FlagConditionBlock){
						assertEquals(2, ((FlagConditionBlock) flag).getId().intValue());

					}else{
						fail();
					}
				}
			}else{
				fail();
			}
		}

		assertEquals(3, rule2Extracted.getPredicateObjects().size());

		for(Entry<OWLProperty, TripleObject> entry : rule2Extracted.getPredicateObjects().entrySet()){

			if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_pvalue")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals(1, object.getObject().size());
				assertEquals("PValue", object.getObject().get(0).getTitle());
				assertEquals(0, object.getObject().get(0).getFlags().size());


			}else if(entry.getKey().getIRI().toString().equals("http://schema.org/name")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals("Term", object.getObject().get(0).getTitle());

				List<Flag> flagsList = object.getObject().get(0).getFlags();
				assertEquals(1, flagsList.size());
				for(Flag flag : flagsList){
					if(flag instanceof FlagSeparator){
						FlagSeparator separator = (FlagSeparator) flag;
						assertEquals(":", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(1, separator.getColumns().get(0).intValue());
					}else{
						fail();
					}
				}


			}else if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_participant")){
				TripleObjectAsRule object = (TripleObjectAsRule) entry.getValue();

				assertEquals(1, object.getObject().size());

				for(ObjectAsRule objectAsRule : object.getObject()){
					assertEquals(3, objectAsRule.getRuleNumber().intValue());
					assertEquals(0, objectAsRule.getFlags().size());
				}


			}else
				fail();
		}

	}

	@Test
	public void creatingRuleFromStringUsingCommaAsSeparator(){

		String 	ruleString = " matrix_rule[2, \"Term\" = \"Term\" /SP(\",\", 1) /BASEIRI(\"http://www.kegg.jp/entry/\", \"kegg\") /CB(2) : " +
				" \"has_pvalue\" = \"PValue\", " +
				" \"name\" = \"Term\" /SP(\",\", 2), " +
				" \"has participant\" = 3 " +
				"] ";


		ruleString = ruleString.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		app.ontologyHelper.loadingOntologyFromFile("testFiles/unitTestsFiles/ontology.owl");

		Rule rule2Extracted = null;
		try {
			rule2Extracted = app.createRulesFromBlock(ruleString);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		assertEquals("2", rule2Extracted.getId());
		assertEquals("http://purl.obolibrary.org/obo/NCIT_P382", rule2Extracted.getSubject().getIRI().toString());
		for(TSVColumn column : rule2Extracted.getSubjectTSVColumns()){
			if(column.getTitle().equals("Term")){

				assertEquals(3, column.getFlags().size()); //all from the rule line plus the ContentDirection

				for(Flag flag : column.getFlags()){
					if(flag instanceof FlagSeparator){
						FlagSeparator separator = (FlagSeparator) flag;
						assertEquals(",", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(0, separator.getColumns().get(0).intValue());
					}else if(flag instanceof FlagBaseIRI){
						FlagBaseIRI baseiri = (FlagBaseIRI) flag;
						assertEquals("http://www.kegg.jp/entry/", baseiri.getIRI());
						assertEquals("kegg", baseiri.getNamespace());
					}else if(flag instanceof FlagConditionBlock){
						assertEquals(2, ((FlagConditionBlock) flag).getId().intValue());

					}else{
						fail();
					}
				}
			}else{
				fail();
			}
		}

		assertEquals(3, rule2Extracted.getPredicateObjects().size());

		for(Entry<OWLProperty, TripleObject> entry : rule2Extracted.getPredicateObjects().entrySet()){

			if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_pvalue")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals(1, object.getObject().size());
				assertEquals("PValue", object.getObject().get(0).getTitle());
				assertEquals(0, object.getObject().get(0).getFlags().size());



			}else if(entry.getKey().getIRI().toString().equals("http://schema.org/name")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals("Term", object.getObject().get(0).getTitle());

				List<Flag> flagsList = object.getObject().get(0).getFlags();
				assertEquals(1, flagsList.size());
				for(Flag flag : flagsList){
					if(flag instanceof FlagSeparator){
						FlagSeparator separator = (FlagSeparator) flag;
						assertEquals(",", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(1, separator.getColumns().get(0).intValue());
					}else{
						fail();
					}
				}




			}else if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_participant")){
				TripleObjectAsRule object = (TripleObjectAsRule) entry.getValue();

				assertEquals(1, object.getObject().size());

				for(ObjectAsRule objectAsRule : object.getObject()){
					assertEquals(3, objectAsRule.getRuleNumber().intValue());
					assertEquals(0, objectAsRule.getFlags().size());
				}


			}else
				fail();
		}

	}

	@Test
	public void creatingRuleFromStringWithNoPredicatesAndObjects(){

		String 	ruleString = " matrix_rule[3, \"Gene\" = \"Genes\" /SP(\", \") /BASEIRI(\"http://www.genecards.org/cgi-bin/carddisp.pl?gene=\", \"genecard\") ] ";

		ruleString = ruleString.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		app.ontologyHelper.loadingOntologyFromFile("testFiles/unitTestsFiles/ontology.owl");

		Rule rule3Extracted = null;
		try {
			rule3Extracted = app.createRulesFromBlock(ruleString);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		assertEquals("3", rule3Extracted.getId());
		assertEquals("http://purl.org/g/onto/Gene", rule3Extracted.getSubject().getIRI().toString());
		for(TSVColumn column : rule3Extracted.getSubjectTSVColumns()){
			if(column.getTitle().equals("Genes")){

				assertEquals(2, column.getFlags().size()); //all from the rule line plus the ContentDirection

				for(Flag flag : column.getFlags()){
					if(flag instanceof FlagSeparator){
						FlagSeparator separator = (FlagSeparator) flag;
						assertEquals(", ", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(Integer.MAX_VALUE, separator.getColumns().get(0).intValue());
					}else if(flag instanceof FlagBaseIRI){
						FlagBaseIRI baseiri = (FlagBaseIRI) flag;
						assertEquals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=", baseiri.getIRI().toString());
						assertEquals("genecard", baseiri.getNamespace());
					}else{
						fail();
					}
				}
			}else{
				fail();
			}
		}

		assertEquals(0, rule3Extracted.getPredicateObjects().size());

	}

	@Test
	public void creatingRuleFromStringWithTwoEqualPredicatesPointingToDifferentRules() {
		String 	ruleString = " matrix_rule[2, \"Term\" = \"Term\" /SP(\":\", 1) /BASEIRI(\"http://www.kegg.jp/entry/\", \"kegg\") /CB(2) : " +
				" \"has_pvalue\" = \"PValue\", " +
				" \"name\" = \"Term\" /SP(\":\", 2), " +
				" \"has participant\" = 3, " +
				" \"has participant\" = 4, " +
				"] ";


		ruleString = ruleString.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		app.ontologyHelper.loadingOntologyFromFile("testFiles/unitTestsFiles/ontology.owl");

		Rule rule2Extracted = null;
		try {
			rule2Extracted = app.extractRulesFromString(ruleString).get(0);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		assertEquals("2", rule2Extracted.getId());
		assertEquals("http://purl.obolibrary.org/obo/NCIT_P382", rule2Extracted.getSubject().getIRI().toString());
		for(TSVColumn column : rule2Extracted.getSubjectTSVColumns()){
			if(column.getTitle().equals("Term")){

				assertEquals(3, column.getFlags().size()); //all from the rule line plus the ContentDirection

				for(Flag flag : column.getFlags()){
					if(flag instanceof FlagSeparator){
						FlagSeparator separator = (FlagSeparator) flag;
						assertEquals(":", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(0, separator.getColumns().get(0).intValue());
					}else if(flag instanceof FlagBaseIRI){
						FlagBaseIRI baseiri = (FlagBaseIRI) flag;
						assertEquals("http://www.kegg.jp/entry/", baseiri.getIRI());
						assertEquals("kegg", baseiri.getNamespace());
					}else if(flag instanceof FlagConditionBlock){
						assertEquals(2, ((FlagConditionBlock) flag).getId().intValue());

					}else{
						fail();
					}
				}
			}else{
				fail();
			}
		}

		assertEquals(3, rule2Extracted.getPredicateObjects().size());

		for(Entry<OWLProperty, TripleObject> entry : rule2Extracted.getPredicateObjects().entrySet()){

			if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_pvalue")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals(1, object.getObject().size());
				assertEquals("PValue", object.getObject().get(0).getTitle());
				assertEquals(0, object.getObject().get(0).getFlags().size());




			}else if(entry.getKey().getIRI().toString().equals("http://schema.org/name")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals("Term", object.getObject().get(0).getTitle());

				List<Flag> flagsList = object.getObject().get(0).getFlags();
				assertEquals(1, flagsList.size());
				for(Flag flag : flagsList){
					if(flag instanceof FlagSeparator){
						FlagSeparator separator = (FlagSeparator) flag;
						assertEquals(":", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(1, separator.getColumns().get(0).intValue());
					}else{
						fail();
					}
				}

			}else if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_participant")){
				TripleObjectAsRule object = (TripleObjectAsRule) entry.getValue();

				assertEquals(2, object.getObject().size());

				for(ObjectAsRule objectAsRule : object.getObject()){
					assert((objectAsRule.getRuleNumber().intValue() == 3) ||
							(objectAsRule.getRuleNumber().intValue() == 4));
					assertEquals(0, objectAsRule.getFlags().size());

				}

			}else
				fail();
		}
	}

	@Test
	public void creatingRuleFromStringWithTwoEqualPredicatesPointingToDifferentTSVColumns() {
		String 	ruleString = "matrix_rule[1, \"Term\" = \"Term\" /SP(\"~\", 1) /BASEIRI(\"http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=\", \"go\") /CB(1) :" +
				" \"has_pvalue\" = \"PValue\", " +
				" \"name\" = \"Term\" /SP(\"~\", 2), " +
				" \"has_pvalue\" = \"List Total\", " +
				" \"has participant\" = 3	] ";


		ruleString = ruleString.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		app.ontologyHelper.loadingOntologyFromFile("testFiles/unitTestsFiles/ontology.owl");

		Rule ruleExtracted = null;
		try {
			ruleExtracted = app.extractRulesFromString(ruleString).get(0);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		assertEquals("1", ruleExtracted.getId());
		assertEquals("http://purl.obolibrary.org/obo/NCIT_P382", ruleExtracted.getSubject().getIRI().toString());
		for(TSVColumn column : ruleExtracted.getSubjectTSVColumns()){
			if(column.getTitle().equals("Term")){

				assertEquals(3, column.getFlags().size()); //all from the rule line plus the ContentDirection

				for(Flag flag : column.getFlags()){
					if(flag instanceof FlagSeparator){
						FlagSeparator separator = (FlagSeparator) flag;
						assertEquals("~", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(0, separator.getColumns().get(0).intValue());
					}else if(flag instanceof FlagBaseIRI){
						FlagBaseIRI baseiri = (FlagBaseIRI) flag;
						assertEquals("http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=", baseiri.getIRI());
						assertEquals("go", baseiri.getNamespace());
					}else if(flag instanceof FlagConditionBlock){
						assertEquals(1, ((FlagConditionBlock) flag).getId().intValue());

					}else{
						fail();
					}
				}
			}else{
				fail();
			}
		}

		assertEquals(3, ruleExtracted.getPredicateObjects().size());

		for(Entry<OWLProperty, TripleObject> entry : ruleExtracted.getPredicateObjects().entrySet()){

			if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_pvalue")){
				TripleObjectAsColumns objects = (TripleObjectAsColumns) entry.getValue();

				assertEquals(2, objects.getObject().size());

				for(TSVColumn object : objects.getObject()) {
					if(object.getTitle().equals("PValue")) {
						assertEquals(0, object.getFlags().size());
					}else if(object.getTitle().equals("List Total")) {
						assertEquals(0, object.getFlags().size());
					}else {
						fail();
					}
				}

			}else if(entry.getKey().getIRI().toString().equals("http://schema.org/name")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals("Term", object.getObject().get(0).getTitle());

				List<Flag> flagsList = object.getObject().get(0).getFlags();
				assertEquals(1, flagsList.size());
				for(Flag flag : flagsList){
					if(flag instanceof FlagSeparator){
						FlagSeparator separator = (FlagSeparator) flag;
						assertEquals("~", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(1, separator.getColumns().get(0).intValue());
					}else{
						fail();
					}
				}

			}else if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_participant")){
				TripleObjectAsRule object = (TripleObjectAsRule) entry.getValue();

				assertEquals(1, object.getObject().size());

				for(ObjectAsRule objectAsRule : object.getObject()){
					assert(objectAsRule.getRuleNumber().intValue() == 3);
					assertEquals(0, objectAsRule.getFlags().size());
				}

			}else
				fail();
		}
	}

	@Test
	public void extractFixedContentFlag() {	
		String sentence = "\\\"name\\\" = \\\"Term\\\" /FX(\"fixed content test\"), ";

		App app = new App();
		List<Flag> flagsExtracted = null;
		try {
			flagsExtracted = app.extractFlagsFromSentence(sentence);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(1, flagsExtracted.size());
		for(Flag flagExtracted : flagsExtracted) {
			if(flagExtracted instanceof FlagFixedContent) {
				FlagFixedContent flag = (FlagFixedContent) flagExtracted;

				assertEquals("fixed content test", flag.getContent());
			}else{
				fail();
			}
		}
	}

	@Test
	public void extractNotMetadataFlag() {	
		String sentence = "\\\"name\\\" = \\\"Term\\\" /NM, ";

		App app = new App();
		List<Flag> flagsExtracted = null;
		try {
			flagsExtracted = app.extractFlagsFromSentence(sentence);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(1, flagsExtracted.size());
		for(Flag flagExtracted : flagsExtracted) {
			if(flagExtracted instanceof FlagNotMetadata) {
				FlagNotMetadata flag = (FlagNotMetadata) flagExtracted;

				assert(flag.isMetadata());
			}else{
				fail();
			}
		}
	}
	
	@Test
	public void extractDatatypeContentFlag() {	
		String sentence = "\\\"name\\\" = \\\"Term\\\" /DT(\"string\"), ";

		App app = new App();
		List<Flag> flagsExtracted = null;
		try {
			flagsExtracted = app.extractFlagsFromSentence(sentence);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(1, flagsExtracted.size());
		for(Flag flagExtracted : flagsExtracted) {
			if(flagExtracted instanceof FlagDataType) {
				FlagDataType flag = (FlagDataType) flagExtracted;

				assertEquals(XSDDatatype.XSDstring.getURI(), ((XSDDatatype) flag.getDatatype()).getURI());
			}else{
				fail();
			}
		}
	}

	@Test
	public void operationNotIdentifiedAtConditionBlock() throws ConditionBlockException {
		String content = "condition_block[1: \"Category\" = \"KEGG_PATHWAY\", \"PValue\" < \"0.01\" ]";
		thrown.expect(ConditionBlockException.class);
		thrown.expectMessage("No valid condition operator identified in a condition block.");
		@SuppressWarnings("unused")
		List<ConditionBlock> conditionsExtracted = ConditionBlock.extractConditionsBlocksFromString(content);
	}

	@Test
	public void creatingRuleFromMultipleOntologies() {
		String 	ruleString = "matrix_rule[1, \"Term2\" = \"Term\" /SP(\"~\", 1) /BASEIRI(\"http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=\", \"go\") /CB(1) :" +
				" \"has_pvalue\" = \"PValue\", " +
				" \"name2\" = \"Term\" /SP(\"~\", 2), " +
				" \"has participant\" = 3	] ";

		ruleString = ruleString.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		List<String> ontologiesPaths = new ArrayList<String>();
		ontologiesPaths.add("testFiles/unitTestsFiles/ontology.owl");
		ontologiesPaths.add("testFiles/unitTestsFiles/ontology2.owl");
		app.ontologyHelper.loadingOntologyFromFile(ontologiesPaths);

		Rule rule1Extracted = null;
		try {
			rule1Extracted = app.createRulesFromBlock(ruleString);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		assertEquals("1", rule1Extracted.getId());
		assertEquals("http://purl.obolibrary.org/obo/NCIT_P382_onto2", rule1Extracted.getSubject().getIRI().toString());
		for(TSVColumn column : rule1Extracted.getSubjectTSVColumns()){
			if(column.getTitle().equals("Term")){

				assertEquals(3, column.getFlags().size());

				for(Flag flag : column.getFlags()){
					if(flag instanceof FlagSeparator){
						FlagSeparator separator = (FlagSeparator) flag;
						assertEquals("~", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(0, separator.getColumns().get(0).intValue());

					}else if(flag instanceof FlagBaseIRI){
						FlagBaseIRI baseiri = (FlagBaseIRI) flag;
						assertEquals("http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=", baseiri.getIRI());
						assertEquals("go", baseiri.getNamespace());

					}else if(flag instanceof FlagConditionBlock){
						assertEquals(1, ((FlagConditionBlock) flag).getId().intValue());

					}else{
						fail();
					}
				}
			}else{
				fail();
			}
		}

		assertEquals(3, rule1Extracted.getPredicateObjects().size());

		for(Entry<OWLProperty, TripleObject> entry : rule1Extracted.getPredicateObjects().entrySet()){

			if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_pvalue")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals(1, object.getObject().size());
				assertEquals("PValue", object.getObject().get(0).getTitle());

			}else if(entry.getKey().getIRI().toString().equals("http://schema.org/name2")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals("Term", object.getObject().get(0).getTitle());

				List<Flag> flagsList = object.getObject().get(0).getFlags();
				assertEquals(1, flagsList.size());
				for(Flag flag : flagsList){
					if(flag instanceof FlagSeparator){
						FlagSeparator separator = (FlagSeparator) flag;
						assertEquals("~", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(1, separator.getColumns().get(0).intValue());

					}else{
						fail();
					}
				}

			}else if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_participant")){
				TripleObjectAsRule object = (TripleObjectAsRule) entry.getValue();

				assertEquals(1, object.getObject().size());

				for(ObjectAsRule objectAsRule : object.getObject()){
					assertEquals(3, objectAsRule.getRuleNumber().intValue());
					assertEquals(0, objectAsRule.getFlags().size());
				}

			}else
				fail();
		}
	}

	@Test
	public void extractMultipleTSVColumnsFromSentence() {
		String sentence = " \"column1\" ; \"column2\" /R ; \"column3\" /BASEIRI(\"http://teste.com\", \"test\") ";

		App app = new App();
		List<TSVColumn> tsvColumns = null;
		try {
			tsvColumns = app.extractTSVColumnsFromSentence(sentence);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(3, tsvColumns.size());
		for(TSVColumn column : tsvColumns) {

			if(column.getTitle().equals("column1")) {
				assertEquals(0, column.getFlags().size());

			}else if(column.getTitle().equals("column2")){
				assertEquals(0, column.getFlags().size());

			}else if(column.getTitle().equals("column3")){
				assertEquals(1, column.getFlags().size());
				for(Flag flag : column.getFlags()) {
					assertTrue(flag instanceof FlagBaseIRI);
				}
			}else {
				fail();
			}
		}
	}

	@Test
	public void separatorFlagColumnsAsRange() {
		String sentence = "\\\"name\\\" = \\\"Term\\\" /SP(\"teste\", 1:3)";

		App app = new App();
		List<Flag> flagsExtracted = null;
		try {
			flagsExtracted = app.extractFlagsFromSentence(sentence);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(1, flagsExtracted.size()); //Separator Flag
		for(Flag flagExtracted : flagsExtracted) {
			if(flagExtracted instanceof FlagSeparator) {
				FlagSeparator flag = (FlagSeparator) flagExtracted;

				assertEquals(3, flag.getColumns().size());
				for(int columnNumber : flag.getColumns()) {
					assert(columnNumber == 0 | columnNumber == 1 | columnNumber == 2);
				}
			}else{
				fail();
			}
		}
	}

	@Test
	public void classNotExist() throws Exception {
		String 	ruleString = "matrix_rule[1, \"Pudim\" = \"Term\" /SP(\"~\", 1) /BASEIRI(\"http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=\", \"go\") /CB(1) :" +
				" \"has_pvalue\" = \"PValue\" ]";

		ruleString = ruleString.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		app.ontologyHelper.loadingOntologyFromFile("testFiles/unitTestsFiles/ontology.owl");

		thrown.expect(ClassNotFoundInOntologyException.class);
		thrown.expectMessage("Not found any ontology class with label 'Pudim'");
		try {
			app.createRulesFromBlock(ruleString);
		} catch (Exception e) {
			throw e;
		}
	}

	@Test
	public void propertyNotExist() throws Exception {
		String 	ruleString = "matrix_rule[1, \"Term\" = \"Term\" /SP(\"~\", 1) /BASEIRI(\"http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=\", \"go\") /CB(1) :" +
				" \"any property\" = \"PValue\" ]";

		ruleString = ruleString.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		app.ontologyHelper.loadingOntologyFromFile("testFiles/unitTestsFiles/ontology.owl");

		thrown.expect(PropertyNotExistException.class);
		String message = " \"any property\" = \"PValue\" ]";
		thrown.expectMessage("Property does not exist in ontology. Instruction: " + message);
		try {
			app.createRulesFromBlock(ruleString);
		} catch (Exception e) {
			throw e;
		}
	}

	@Test
	public void extractRangeFromSeparatorFlagFailure() throws Exception {
		String 	ruleString = "matrix_rule[1, \"Term\" = \"Term\" /SP(\"~\", 1:p) /BASEIRI(\"http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=\", \"go\") /CB(1) :" +
				" \"has_pvalue\" = \"PValue\" ]";

		ruleString = ruleString.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		app.ontologyHelper.loadingOntologyFromFile("testFiles/unitTestsFiles/ontology.owl");

		thrown.expect(SeparatorFlagException.class);
		String message = "/SP(, 1:p)";
		thrown.expectMessage("Value specified as column number is not a number. Instruction: " + message);
		try {
			app.createRulesFromBlock(ruleString);
		} catch (Exception e) {
			throw e;
		}
	}
}
