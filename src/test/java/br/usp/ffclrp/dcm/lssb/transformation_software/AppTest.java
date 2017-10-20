package br.usp.ffclrp.dcm.lssb.transformation_software;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLProperty;

import br.usp.ffclrp.dcm.lssb.transformation_software.App;
import br.usp.ffclrp.dcm.lssb.transformation_software.OntologyHelper;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Condition;
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

/**
 * Unit test for simple App.
 */
public class AppTest
{
	@Test
	public void exctractConditionsFromString()
	{
		String content = "condition_block[1: \"Category\" != \"KEGG_PATHWAY\", \"PValue\" < \"0.01\" ]";
		content += "condition_block[2: \"Category\" == \"KEGG_PATHWAY\",	\"PValue\" < \"0.03\" ]";

		List<ConditionBlock> conditionsExtracted = new App().extractConditionsBlocksFromString(content);

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
						assert(false);
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
						assert(false);
					}

				}else
					assert(false);
		}
	}

	@Test
	public void creatingRuleFromStringWithBaseIRISeparatorConditionBlock(){

		String 	rule1String = "transformation_rule[1, \"Term\" = \"Term\" /SP(\"~\", 0) /BASEIRI(\"http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=\", \"go\") /CB(1) :" +
				" \"has_pvalue\" = \"PValue\", " +
				" \"name\" = \"Term\" /SP(\"~\", 1), " +
				" \"has participant\" = 3	] ";

		rule1String = rule1String.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		app.ontologyHelper.loadingOntologyFromFile("testFiles/unitTestsFiles/ontology.owl");

		Rule rule1Extracted = app.createRulesFromBlock(rule1String);


		//Assert rule 1
		assertEquals("1", rule1Extracted.getId());
		assertEquals("http://purl.obolibrary.org/obo/NCIT_P382", rule1Extracted.getSubject().getIRI().toString());
		for(TSVColumn column : rule1Extracted.getSubjectTSVColumns()){
			if(column.getTitle().equals("Term")){

				assertEquals(4, column.getFlags().size()); //all from the rule line plus the ContentDirection

				for(Flag flag : column.getFlags()){
					if(flag instanceof Separator){
						Separator separator = (Separator) flag;
						assertEquals("~", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(0, separator.getColumns().get(0).intValue());

					}else if(flag instanceof FlagBaseIRI){
						FlagBaseIRI baseiri = (FlagBaseIRI) flag;
						assertEquals("http://amigo1.geneontology.org/cgi-bin/amigo/term_details?term=", baseiri.getIRI());
						assertEquals("go", baseiri.getNamespace());

					}else if(flag instanceof FlagConditionBlock){
						assertEquals(1, ((FlagConditionBlock) flag).getId().intValue());

					}else if(flag instanceof ContentDirectionTSVColumn){
						assertEquals(EnumContentDirectionTSVColumn.DOWN, ((ContentDirectionTSVColumn) flag).getDirection());
					}else{
						assert(false);
					}
				}
			}else{
				assert(false);
			}
		}

		assertEquals(3, rule1Extracted.getPredicateObjects().size());

		for(Entry<OWLProperty, TripleObject> entry : rule1Extracted.getPredicateObjects().entrySet()){

			if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_pvalue")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals(1, object.getObject().size());
				assertEquals("PValue", object.getObject().get(0).getTitle());
				assertEquals(1, object.getObject().get(0).getFlags().size());
				Flag flag = object.getObject().get(0).getFlags().get(0);
				assertEquals(EnumContentDirectionTSVColumn.DOWN, ((ContentDirectionTSVColumn) flag).getDirection());



			}else if(entry.getKey().getIRI().toString().equals("http://schema.org/name")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals("Term", object.getObject().get(0).getTitle());

				List<Flag> flagsList = object.getObject().get(0).getFlags();
				assertEquals(2, flagsList.size());
				for(Flag flag : flagsList){
					if(flag instanceof Separator){
						Separator separator = (Separator) flag;
						assertEquals("~", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(1, separator.getColumns().get(0).intValue());
					}else if(flag instanceof ContentDirectionTSVColumn){
						assertEquals(EnumContentDirectionTSVColumn.DOWN, ((ContentDirectionTSVColumn) flag).getDirection());

					}else{
						assert(false);
					}
				}

			}else if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_participant")){
				TripleObjectAsRule object = (TripleObjectAsRule) entry.getValue();

				assertEquals(1, object.getObject().size());

				for(ObjectAsRule objectAsRule : object.getObject()){
					assertEquals(3, objectAsRule.getRuleNumber().intValue());
					assertEquals(1, objectAsRule.getFlags().size());
					Flag flag = objectAsRule.getFlags().get(0);
					assertEquals(EnumContentDirectionTSVColumn.DOWN, ((ContentDirectionTSVColumn) flag).getDirection());

				}

			}else
				assert(false);
		}

	}

	@Test
	public void creatingRuleFromStringUsingColonAsSeparator(){

		String 	rule2String = " transformation_rule[2, \"Term\" = \"Term\" /SP(\":\", 0) /BASEIRI(\"http://www.kegg.jp/entry/\", \"kegg\") /CB(2) : " +
				" \"has_pvalue\" = \"PValue\", " +
				" \"name\" = \"Term\" /SP(\":\", 1), " +
				" \"has participant\" = 3 " +
				"] ";


		rule2String = rule2String.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		app.ontologyHelper.loadingOntologyFromFile("testFiles/unitTestsFiles/ontology.owl");

		Rule rule2Extracted = app.createRulesFromBlock(rule2String);

		//Assert rule 2
		assertEquals("2", rule2Extracted.getId());
		assertEquals("http://purl.obolibrary.org/obo/NCIT_P382", rule2Extracted.getSubject().getIRI().toString());
		for(TSVColumn column : rule2Extracted.getSubjectTSVColumns()){
			if(column.getTitle().equals("Term")){

				assertEquals(4, column.getFlags().size()); //all from the rule line plus the ContentDirection

				for(Flag flag : column.getFlags()){
					if(flag instanceof Separator){
						Separator separator = (Separator) flag;
						assertEquals(":", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(0, separator.getColumns().get(0).intValue());
					}else if(flag instanceof FlagBaseIRI){
						FlagBaseIRI baseiri = (FlagBaseIRI) flag;
						assertEquals("http://www.kegg.jp/entry/", baseiri.getIRI());
						assertEquals("kegg", baseiri.getNamespace());
					}else if(flag instanceof FlagConditionBlock){
						assertEquals(2, ((FlagConditionBlock) flag).getId().intValue());

					}else if(flag instanceof ContentDirectionTSVColumn){
						assertEquals(EnumContentDirectionTSVColumn.DOWN, ((ContentDirectionTSVColumn) flag).getDirection());
					}else{
						assert(false);
					}
				}
			}else{
				assert(false);
			}
		}

		assertEquals(3, rule2Extracted.getPredicateObjects().size());

		for(Entry<OWLProperty, TripleObject> entry : rule2Extracted.getPredicateObjects().entrySet()){

			if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_pvalue")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals(1, object.getObject().size());
				assertEquals("PValue", object.getObject().get(0).getTitle());
				assertEquals(1, object.getObject().get(0).getFlags().size());
				Flag flag = object.getObject().get(0).getFlags().get(0);
				assertEquals(EnumContentDirectionTSVColumn.DOWN, ((ContentDirectionTSVColumn) flag).getDirection());




			}else if(entry.getKey().getIRI().toString().equals("http://schema.org/name")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals("Term", object.getObject().get(0).getTitle());

				List<Flag> flagsList = object.getObject().get(0).getFlags();
				assertEquals(2, flagsList.size());
				for(Flag flag : flagsList){
					if(flag instanceof Separator){
						Separator separator = (Separator) flag;
						assertEquals(":", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(1, separator.getColumns().get(0).intValue());
					}else if(flag instanceof ContentDirectionTSVColumn){
						assertEquals(EnumContentDirectionTSVColumn.DOWN, ((ContentDirectionTSVColumn) flag).getDirection());
					}else{
						assert(false);
					}
				}




			}else if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_participant")){
				TripleObjectAsRule object = (TripleObjectAsRule) entry.getValue();

				assertEquals(1, object.getObject().size());

				for(ObjectAsRule objectAsRule : object.getObject()){
					assertEquals(3, objectAsRule.getRuleNumber().intValue());
					assertEquals(1, objectAsRule.getFlags().size());
					for(Flag flag : objectAsRule.getFlags()){
						if(flag instanceof ContentDirectionTSVColumn){
							assertEquals(EnumContentDirectionTSVColumn.DOWN, ((ContentDirectionTSVColumn) flag).getDirection());
						}else{
							assert(false);
						}
					}
				}


			}else
				assert(false);
		}

	}

	@Test
	public void creatingRuleFromStringWithNoPredicatesAndObjects(){

		String 	rule3String = " transformation_rule[3, \"Gene\" = \"Genes\" /SP(\", \") /BASEIRI(\"http://www.genecards.org/cgi-bin/carddisp.pl?gene=\", \"genecard\") ] ";

		rule3String = rule3String.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		app.ontologyHelper.loadingOntologyFromFile("testFiles/unitTestsFiles/ontology.owl");

		Rule rule3Extracted = app.createRulesFromBlock(rule3String);

		//Assert rule 3
		assertEquals("3", rule3Extracted.getId());
		assertEquals("http://purl.org/g/onto/Gene", rule3Extracted.getSubject().getIRI().toString());
		for(TSVColumn column : rule3Extracted.getSubjectTSVColumns()){
			if(column.getTitle().equals("Genes")){

				assertEquals(3, column.getFlags().size()); //all from the rule line plus the ContentDirection

				for(Flag flag : column.getFlags()){
					if(flag instanceof Separator){
						Separator separator = (Separator) flag;
						assertEquals(", ", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(Integer.MAX_VALUE, separator.getColumns().get(0).intValue());
					}else if(flag instanceof FlagBaseIRI){
						FlagBaseIRI baseiri = (FlagBaseIRI) flag;
						assertEquals("http://www.genecards.org/cgi-bin/carddisp.pl?gene=", baseiri.getIRI().toString());
						assertEquals("genecard", baseiri.getNamespace());
					}else if(flag instanceof ContentDirectionTSVColumn){
						assertEquals(EnumContentDirectionTSVColumn.DOWN, ((ContentDirectionTSVColumn) flag).getDirection());
					}else{
						fail();
					}
				}
			}else{
				assert(false);
			}
		}

		assertEquals(0, rule3Extracted.getPredicateObjects().size());

	}

	@Test
	public void creatingRuleFromStringWithTwoRulesOnSamePredicate() {
		String 	rule2String = " transformation_rule[2, \"Term\" = \"Term\" /SP(\":\", 0) /BASEIRI(\"http://www.kegg.jp/entry/\", \"kegg\") /CB(2) : " +
				" \"has_pvalue\" = \"PValue\", " +
				" \"name\" = \"Term\" /SP(\":\", 1), " +
				" \"has participant\" = 3, " +
				" \"has participant\" = 4, " +
				"] ";


		rule2String = rule2String.replace("\t", "").replaceAll("\n", "");

		App app = new App();
		app.ontologyHelper = new OntologyHelper();
		app.ontologyHelper.loadingOntologyFromFile("testFiles/unitTestsFiles/ontology.owl");

		Rule rule2Extracted = app.createRulesFromBlock(rule2String);

		//Assert rule 2
		assertEquals("2", rule2Extracted.getId());
		assertEquals("http://purl.obolibrary.org/obo/NCIT_P382", rule2Extracted.getSubject().getIRI().toString());
		for(TSVColumn column : rule2Extracted.getSubjectTSVColumns()){
			if(column.getTitle().equals("Term")){

				assertEquals(4, column.getFlags().size()); //all from the rule line plus the ContentDirection

				for(Flag flag : column.getFlags()){
					if(flag instanceof Separator){
						Separator separator = (Separator) flag;
						assertEquals(":", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(0, separator.getColumns().get(0).intValue());
					}else if(flag instanceof FlagBaseIRI){
						FlagBaseIRI baseiri = (FlagBaseIRI) flag;
						assertEquals("http://www.kegg.jp/entry/", baseiri.getIRI());
						assertEquals("kegg", baseiri.getNamespace());
					}else if(flag instanceof FlagConditionBlock){
						assertEquals(2, ((FlagConditionBlock) flag).getId().intValue());

					}else if(flag instanceof ContentDirectionTSVColumn){
						assertEquals(EnumContentDirectionTSVColumn.DOWN, ((ContentDirectionTSVColumn) flag).getDirection());
					}else{
						assert(false);
					}
				}
			}else{
				assert(false);
			}
		}

		assertEquals(3, rule2Extracted.getPredicateObjects().size());

		for(Entry<OWLProperty, TripleObject> entry : rule2Extracted.getPredicateObjects().entrySet()){

			if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_pvalue")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals(1, object.getObject().size());
				assertEquals("PValue", object.getObject().get(0).getTitle());
				assertEquals(1, object.getObject().get(0).getFlags().size());
				Flag flag = object.getObject().get(0).getFlags().get(0);
				assertEquals(EnumContentDirectionTSVColumn.DOWN, ((ContentDirectionTSVColumn) flag).getDirection());




			}else if(entry.getKey().getIRI().toString().equals("http://schema.org/name")){
				TripleObjectAsColumns object = (TripleObjectAsColumns) entry.getValue();

				assertEquals("Term", object.getObject().get(0).getTitle());

				List<Flag> flagsList = object.getObject().get(0).getFlags();
				assertEquals(2, flagsList.size());
				for(Flag flag : flagsList){
					if(flag instanceof Separator){
						Separator separator = (Separator) flag;
						assertEquals(":", separator.getTerm());
						assertEquals(1, separator.getColumns().size());
						assertEquals(1, separator.getColumns().get(0).intValue());
					}else if(flag instanceof ContentDirectionTSVColumn){
						assertEquals(EnumContentDirectionTSVColumn.DOWN, ((ContentDirectionTSVColumn) flag).getDirection());
					}else{
						assert(false);
					}
				}

			}else if(entry.getKey().getIRI().toString().equals("http://purl.org/g/onto/has_participant")){
				TripleObjectAsRule object = (TripleObjectAsRule) entry.getValue();

				assertEquals(2, object.getObject().size());

				for(ObjectAsRule objectAsRule : object.getObject()){
					assert((objectAsRule.getRuleNumber().intValue() == 3) ||
						   (objectAsRule.getRuleNumber().intValue() == 4));
					assertEquals(1, objectAsRule.getFlags().size());
					for(Flag flag : objectAsRule.getFlags()){
						if(flag instanceof ContentDirectionTSVColumn){
							assertEquals(EnumContentDirectionTSVColumn.DOWN, ((ContentDirectionTSVColumn) flag).getDirection());
						}else{
							assert(false);
						}
					}
				}

			}else
				assert(false);
		}
	}
}
