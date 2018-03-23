package br.usp.ffclrp.dcm.lssb.transformation_software;

import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumTypeOfProperty;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class OntologyHelper {
	public List<OWLOntology> ontologies = new ArrayList<OWLOntology>();
	OWLOntologyManager m = OWLManager.createOWLOntologyManager();
	OWLDataFactory factory;
	Map<String, OWLClass> mappingClassesAnnotations;
	Map<String, OWLProperty> mappingPredicatesAnnotations;

	public OntologyHelper(){
		this.m = OWLManager.createOWLOntologyManager();
		this.factory = m.getOWLDataFactory();

	}

	public void loadingOntologyFromFile(String path) throws OWLOntologyCreationException, IOException {
		loadOntology(path);
		addClassesAndPredicatesToMap();
	}

	public void loadingOntologyFromFile(List<String> ontologiesPaths) throws OWLOntologyCreationException, IOException {
		for(String path : ontologiesPaths) {
			loadOntology(path);
		}
		addClassesAndPredicatesToMap();
	}

	private void loadOntology(String path) throws OWLOntologyCreationException, IOException {
		try {
			File file = new File(path);

			OWLOntology ontology = m.loadOntologyFromOntologyDocument(file);
			
			m.directImports(ontology).forEach(o -> ontologies.add(o));
			ontologies.add(ontology);

		} catch (OWLOntologyCreationException e) {
			throw e;
		}
	}

	private void addClassesAndPredicatesToMap() {
		addAllClassesLabelsToMap();			
		addAllPredicadesToMap();
	}

	private void addAllPredicadesToMap() {
		mappingPredicatesAnnotations = new HashMap<String, OWLProperty>();

		for(OWLOntology onto : ontologies) {

			try(Stream<OWLDataProperty> dataProperties = onto.dataPropertiesInSignature()){
				dataProperties.forEach(p -> {//System.out.println(labelFor(p) + " -> " + p);
					mappingPredicatesAnnotations.put(labelFor(p), p);
				});
			}

			try(Stream<OWLObjectProperty> objectProperties = onto.objectPropertiesInSignature()) {
				objectProperties.forEach(p -> {//System.out.println(labelFor(p) + " -> " + p);
					mappingPredicatesAnnotations.put(labelFor(p), p);
				});
			}
		}
	}

	private void addAllClassesLabelsToMap() {

		mappingClassesAnnotations = new HashMap<String, OWLClass>();
		for(OWLOntology onto : ontologies) {
			try(Stream<OWLClass> stream = onto.classesInSignature()) {
				stream.forEach(c -> mappingClassesAnnotations.put(labelFor(c), c));
			}
		}
	}

	// @author Ignazio Palmisano (https://github.com/ignazio1977)
	// Modified by Gabriel Gusmao (https://github.com/gcsgpp)
	private String labelFor(@Nonnull OWLEntity entity) {
		/*
		 * Use a visitor to extract label annotations
		 */
		LabelExtractor le = new LabelExtractor();
		for(OWLOntology onto : ontologies) {
			EntitySearcher.getAnnotationObjects(entity, onto).forEach( anno -> anno.accept(le));

			/* Print out the label if there is one. If not, just return null */
			if (le.getResult() != null)
				return le.getResult();
		}

		return null;
	}

	public void printClasses(){
		for(OWLOntology onto : ontologies) {
			try(Stream<OWLClass> stream = onto.classesInSignature()) {
				stream.forEach(
						e -> System.out.println(e + " -> " + e.getIRI()));
			}
		}
	}

	public OWLClass getClass(String classLabel){

		return mappingClassesAnnotations.get(classLabel);
	}

	public OWLProperty getProperty(String predicateLabel){

		return mappingPredicatesAnnotations.get(predicateLabel);
	}

	//Prerequisite: baseIri must **not** end with "#"
	public OWLProperty getProperty(String propertyName, String baseIri, EnumTypeOfProperty type){

		OWLProperty property;

		if(type == EnumTypeOfProperty.DATATYPEPROPERTY)
			property = factory.getOWLObjectProperty(IRI.create(baseIri + "#" + propertyName));
		else
			property = factory.getOWLDataProperty(IRI.create(baseIri + "#" + propertyName));


		return property;
	}

	public void printIndividuals(){
		for(OWLOntology onto : ontologies) {
			try(Stream<OWLNamedIndividual> stream = onto.individualsInSignature()) {
				stream.forEach(
						e -> System.out.println(e));
			}
		}
	}

	//	public void createClasses(){		
	//		PrefixManager pm = new DefaultPrefixManager("http://sw.palmas.br/teste/ontologia#");
	//
	//		OWLClass classA = factory.getOWLClass(":A", pm);
	//
	//		OWLDeclarationAxiom axiom = factory.getOWLDeclarationAxiom(classA);
	//
	//		if(ontologies.add(axiom) == ChangeApplied.SUCCESSFULLY)
	//			System.out.println("Class added sucessfully");
	//		else
	//			System.out.println("Class not added");
	//	}

	//	public void saveOntology(){
	//
	//		File file = new File("newOntology.owl");
	//
	//
	//		RDFXMLDocumentFormat rdf = new RDFXMLDocumentFormat();
	//
	//		try {
	//			m.saveOntology(ontologies, rdf, IRI.create(file.toURI()));
	//			System.out.println("Ontology saved.");
	//		} catch (OWLOntologyStorageException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//			System.out.println("Error on trying to save ontology.");
	//		}
	//	}
	//
	//	public void deleteEntity(String entityName){
	//
	//		OWLEntityRemover remover = new OWLEntityRemover(ontologies);
	//
	//		OWLClass classToBeRemoved = getClass(entityName);
	//
	//		classToBeRemoved.accept(remover);
	//
	//		try{
	//			ontologies.applyChanges(remover.getChanges());
	//			System.out.println("Class deleted.");
	//		}catch(Exception e){
	//			e.printStackTrace();
	//		}
	//	}
}