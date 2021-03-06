package br.usp.ffclrp.dcm.lssb.transformation_software;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.ClassNotFoundInOntologyException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.PropertyNotExistException;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumTypeOfProperty;
import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.RuleConfig;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import javax.annotation.Nonnull;
import java.io.*;
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
	Map<String, String> namespaces;

	public OntologyHelper(){
		this.m = OWLManager.createOWLOntologyManager();
		this.factory = m.getOWLDataFactory();

	}

	public void setNamespaces(Map<String, String> namespaces)
	{
		this.namespaces = namespaces;
	}

	public Map<String, String> getNamespaces()
	{
		return this.namespaces;
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
				dataProperties.forEach(p -> {

					String ns = getKnownNamespaceFromIri(p.getIRI().toString());

					String label = labelFor(p);

					if(label != null)
						mappingPredicatesAnnotations.put(ns.toLowerCase() + label.toLowerCase(), p);
				});
			}

			try(Stream<OWLObjectProperty> objectProperties = onto.objectPropertiesInSignature()) {
				objectProperties.forEach(p -> {

					String ns = getKnownNamespaceFromIri(p.getIRI().toString());

					String label = labelFor(p);

					if(label != null)
						mappingPredicatesAnnotations.put(ns.toLowerCase() + label.toLowerCase(), p);
				});
			}

			try(Stream<OWLAnnotationProperty> annotationProperties = onto.annotationPropertiesInSignature()) {
				annotationProperties.forEach(p -> {

					String ns = getKnownNamespaceFromIri(p.getIRI().toString());

					String label = labelFor(p);

					if(label != null)
						mappingPredicatesAnnotations.put(ns.toLowerCase() + label.toLowerCase(), p);
				});
			}
		}
	}

	private void addAllClassesLabelsToMap() {

		mappingClassesAnnotations = new HashMap<String, OWLClass>();
		for (OWLOntology onto : ontologies) {

			try (Stream<OWLClass> stream = onto.classesInSignature()) {

				stream.forEach(c -> {

					String ns = getKnownNamespaceFromIri(c.getIRI().toString());
					String label = labelFor(c);

					if(label != null)
						mappingClassesAnnotations.put(ns.toLowerCase() + label.toLowerCase(), c);

				});
			}
		}
	}

	private String getKnownNamespaceFromIri(String iri)
	{
		String ns = "";

		for(Map.Entry<String, String> entry : this.namespaces.entrySet())
		{
			if(iri.toLowerCase().trim().contains(entry.getValue().toLowerCase().trim())) {
				ns = entry.getKey() + ":";
				break;
			}
		}

		return ns;
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

	public OWLClass getClass(String classLabel) throws ClassNotFoundInOntologyException {

		OWLClass clazz = mappingClassesAnnotations.get(classLabel.toLowerCase());

		if(clazz == null)
			throw new ClassNotFoundInOntologyException("Not found any ontology class with label '" + classLabel + "'");

		return clazz;
	}

	public OWLProperty getProperty(String predicateLabel) throws PropertyNotExistException {

		OWLProperty prop =  mappingPredicatesAnnotations.get(predicateLabel.toLowerCase());

		if(prop == null)
			throw new PropertyNotExistException("Not found any ontology property with label '" + predicateLabel + "'");

		return prop;
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