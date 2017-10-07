package br.usp.ffclrp.dcm.lssb.transformation_software;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.semanticweb.owlapi.model.HasOntologyChangeListeners;
import org.semanticweb.owlapi.model.OWLProperty;

import com.fasterxml.jackson.databind.ser.std.MapProperty;

import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Flag;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagBaseIRI;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.FlagOWNID;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Rule;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.Separator;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TSVColumn;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObject;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObjectAsColumns;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.TripleObjectAsRule;

public class TriplesProcessing {

	SemistructuredFileReader fileReader;
	Model model = null;
	List<TriplePool> triplePoolList = new ArrayList<TriplePool>();
	List<ResourcesMapping> resourcesMapping = new ArrayList<ResourcesMapping>();

	public TriplesProcessing(String relativePathDataFile) {
		model = ModelFactory.createDefaultModel();
		fileReader = readFile(relativePathDataFile);
	}

	@SuppressWarnings("unchecked")
	public void createTriplesFromRules(List<Rule> listRules, String defaultNs) throws Exception{	
		for(Rule rule : listRules){

			for(Integer tsvLineNumber = 1; tsvLineNumber < fileReader.getAllDataRows().size(); tsvLineNumber++){

				// *** SUBJECT ***
				List<Resource> subjectList = getSubject(rule, defaultNs, tsvLineNumber);


				// *** PREDICATE AND OBJECT ***
				Property predicate;
				String objectContent = "";

				for(Resource subject : subjectList){

					resourcesMapping.add(new ResourcesMapping(Integer.parseInt(rule.getId()), tsvLineNumber, subject));

					Map<OWLProperty, TripleObject> predicateObjectMAP = rule.getPredicateObjects();
					for(Map.Entry<OWLProperty, TripleObject> predicateMapEntry : predicateObjectMAP.entrySet()){
						//GET PREDICATE IRI
						predicate = model.createProperty(predicateMapEntry.getKey().getIRI().toString());

						//TRIPLEOBJECTS POINTING TO A RULE PROCESS FLOW
						if(predicateMapEntry.getValue() instanceof TripleObjectAsRule){
							for(Integer ruleNumber : (List<Integer>) predicateMapEntry.getValue().getObject()){
								triplePoolList.add(new TriplePool(subject, predicate, ruleNumber, tsvLineNumber));
							}
							//TRIPLEOBJECTS POINTING TO LIST OF TSV COLUMNS PROCESS FLOW
						}else{
							List<String> content = extractDataFromTSVColumn((List<TSVColumn>) predicateMapEntry.getValue().getObject(), tsvLineNumber);

							if(content == null)	{
								content = new ArrayList<String>();
								Random random = new Random(999999);
								Integer randomNumber = random.nextInt();
								content.add(randomNumber.toString());
							}

							for(String contentElement : content){
								addTripleToModel(subject, predicate, contentElement);
							}
						}
					}
				}
			}
		}

		for(TriplePool poolElement : triplePoolList){
			@Nonnull Resource object = retrieveResouce(poolElement.getRuleNumber(), poolElement.getLineNumber());
			addTripleToModel(poolElement.getSubject(), poolElement.getPredicate(), object);
		}

		writeRDF();
	}

	private Resource retrieveResouce(Integer ruleNumber, Integer lineNumber) {
		for(ResourcesMapping mappingElement : resourcesMapping){
			if(mappingElement.getRuleNumber() == ruleNumber && mappingElement.getLineNumber() == lineNumber)
				return mappingElement.getTripleSubject();
		}
		return null;
	}

	private void addTripleToModel(Resource subject, Property predicate, Resource object) {
		System.out.println("S: " + subject.getURI() + " P: " + predicate.getURI() + " O: " + object.getURI());
		subject.addProperty(predicate, subject);		
	}

	private void addTripleToModel(Resource subject, Property predicate, String contentElement) {
		System.out.println("S: " + subject.getURI() + " P: " + predicate.getURI() + " O: " + contentElement);
		subject.addProperty(predicate, contentElement, "en");
	}

	private List<String> extractDataFromTSVColumn(List<TSVColumn> listTSVColumn, Integer lineNumber) {
		List<String> objectContent = new ArrayList<String>();

		//PROCESS THE SEPARATOR FLAG
		List<String[]> dataColumnsSeparated = new ArrayList<String[]>();
		for(TSVColumn column : listTSVColumn){
			Boolean extractedData = false;
			for(Flag flag : column.getFlags()){
				if(flag instanceof Separator){
					dataColumnsSeparated.add(separateDataFromTSVColumn((Separator) flag, column.getTitle(), lineNumber));
					extractedData = true;
				}
			}
			if(!extractedData){
					String[] columnData = new String[1];
					columnData[0] = fileReader.getData(column.getTitle(), lineNumber);
					dataColumnsSeparated.add(columnData);
			}
		}

		//FINDS THE BIGGER ARRAY OF DATA EXTRACTED AND MAKE THE MERGE BETWEEN THE DATA ARRAYS EXTRACTED
		//ONE ITEM FROM EACH ARRAY
		Integer biggerColumn = Integer.MIN_VALUE;
		for(String[] array : dataColumnsSeparated){
			if(array.length > biggerColumn)
				biggerColumn = array.length;
		}

		for(int i = 0; i < biggerColumn; i++){
			String content = "";
			for(String[] array : dataColumnsSeparated){
				try {
					content += " " + array[i];
				} catch (ArrayIndexOutOfBoundsException e) {
					content += "";
				}
			}
			content = content.trim();
			objectContent.add(content);
		}

		return objectContent;
	}

	private String[] separateDataFromTSVColumn(Separator flag, String columnTitle, Integer lineNumber) {
		String rawData = fileReader.getData(columnTitle, lineNumber);

		String[] splitData = rawData.split(flag.getTerm());

		if(flag.getColumns().get(0).equals(Integer.MAX_VALUE))
			return splitData; //If MAX_VALUE all columns were selected

		String[] resultData = new String[flag.getColumns().size()];
		for(int i = 0; i < flag.getColumns().size(); i++){
			int colNumber = flag.getColumns().get(i);
			resultData[i] = splitData[colNumber];
		}

		return resultData;
	}

	private List<Resource> getSubject(Rule rule, String defaultNs, Integer lineNumber) throws Exception {
		Boolean hasOWNIDTag = false;
		Resource subjectType = null;

		if(hasOWNIDTag(rule.getSubjectTSVColumns())){
			hasOWNIDTag = true;
			defaultNs = getBASEIRITag(rule.getSubjectTSVColumns());
			if(defaultNs == null){
				throw new Exception("OWN ID Tag must have a BASEIRI Tag accompanied");
			}
		}else{
			subjectType = model.createResource(rule.getSubject().getIRI().toString());
		}


		List<String> subjectContent = extractDataFromTSVColumn(rule.getSubjectTSVColumns(), lineNumber);

		/*//SUBJECT THAT DO NOT HAVE DATA EXTRACTED FROM TSVCOLUMN MUST HAVE SOME NAME GENERATED
		if(subjectContent == null)	{
			subjectContent = new ArrayList<String>();
			Random random = new Random(999999);
			Integer randomNumber = random.nextInt();
			subjectContent.add(randomNumber.toString());
		}*/

		List<Resource> subjectList = new ArrayList<Resource>();
		if(hasOWNIDTag){
			for(String individualContent : subjectContent){
				subjectList.add(model.createResource(defaultNs + individualContent));			
			}
		}else{
			for(String individualContent : subjectContent){
				subjectList.add(model.createResource(defaultNs + individualContent, subjectType));
			}
		}

		return subjectList;
	}

	private String getBASEIRITag(List<TSVColumn> listTSVColumn) {
		for(TSVColumn column : listTSVColumn){
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagBaseIRI){
					return ((FlagBaseIRI) flag).getIRI();
				}
			}
		}
		return null;
	}

	private boolean hasOWNIDTag(List<TSVColumn> listTSVColumn) {
		for(TSVColumn column : listTSVColumn){
			for(Flag flag : column.getFlags()){
				if(flag instanceof FlagOWNID){
					return ((FlagOWNID) flag).isOwnId();
				}
			}
		}
		return false;
	}

	private SemistructuredFileReader readFile(String relativePath) {
		return new SemistructuredFileReader(relativePath);
	}

	public void writeRDF(){
		System.out.println("#####################################\n=== Writing RDF... \n");
		long startTime = System.currentTimeMillis();
		try{
			File f = new File("teste.rdf");
			FileOutputStream fos;
			fos = new FileOutputStream(f);
			//System.out.println("Printing...");
			model.write(fos, "N-TRIPLES");
		} catch (Exception e) {
			e.printStackTrace();
		}

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("=== Wrote RDF in " + elapsedTime / 1000 + " secs ===\n#####################################\n\n");
	}

}
