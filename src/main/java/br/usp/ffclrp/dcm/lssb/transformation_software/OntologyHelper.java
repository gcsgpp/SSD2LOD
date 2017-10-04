package br.usp.ffclrp.dcm.lssb.transformation_software;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.jena.vocabulary.OWL;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing.EnumTypeOfProperty;
import uk.ac.manchester.cs.owl.owlapi.OWLAnnotationAssertionAxiomImpl;

public class OntologyHelper {
	public OWLOntology o;
	OWLOntologyManager m;
	OWLDataFactory factory;
	Map<String, OWLClass> mappingClassesAnnotations;
	Map<String, OWLProperty> mappingPredicatesAnnotations;
	
	public OntologyHelper(){
		this.m = OWLManager.createOWLOntologyManager();
		this.factory = m.getOWLDataFactory();
		
	}
	
	public void loadingOntologyFromFile(String path){
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();

		InputStream is = OntologyHelper.class.getResourceAsStream(path);
		try {
			o = m.loadOntologyFromOntologyDocument(is);
			addAllClassesLabelsToMap();			
			addAllPredicadesToMap();

		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

	private void addAllPredicadesToMap() {
		mappingPredicatesAnnotations = new HashMap<String, OWLProperty>();
		
		Stream<OWLDataProperty> dataProperties = o.dataPropertiesInSignature();
		dataProperties.forEach(p -> mappingPredicatesAnnotations.put(labelFor(p), p));
	}

	private void addAllDataProperties(OWLClass value) {
		value.dataPropertiesInSignature().forEach(o -> mappingPredicatesAnnotations.put(labelFor(o), o));
	}

	private void addAllObjectProperties(OWLClass value) {
		value.objectPropertiesInSignature().forEach(o -> mappingPredicatesAnnotations.put(labelFor(o), o));
	}

	private void addAllClassesLabelsToMap() {
		
		mappingClassesAnnotations = new HashMap<String, OWLClass>();
		
		Stream<OWLClass> stream = o.classesInSignature();
		stream.forEach(c -> mappingClassesAnnotations.put(labelFor(c), c));
		
	}
	
	private String labelFor(@Nonnull OWLProperty property) {
        /*
         * Use a visitor to extract label annotations
         */
        LabelExtractor le = new LabelExtractor();
        EntitySearcher.getAnnotationObjects(property, o).forEach( anno -> anno.accept(le));
        
        /* Print out the label if there is one. If not, just return null */
        if (le.getResult() != null)
            return le.getResult();
        else
        	return null;
	}
    
	
	
	// @author Ignazio Palmisano (https://github.com/ignazio1977)
	// Modified by Gabriel Gusmao (https://github.com/gcsgpp)
	private String labelFor(@Nonnull OWLClass clazz) {
        /*
         * Use a visitor to extract label annotations
         */
        LabelExtractor le = new LabelExtractor();
        EntitySearcher.getAnnotationObjects(clazz, o).forEach( anno -> anno.accept(le));
        
        /* Print out the label if there is one. If not, just return null */
        if (le.getResult() != null)
            return le.getResult();
        else
        	return null;
    }

	public void printClasses(){
		Stream<OWLClass> stream = o.classesInSignature();
		
		stream.forEach(
				e -> System.out.println(e + " -> " + e.getIRI()));
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
		Stream<OWLNamedIndividual> stream = o.individualsInSignature();

		stream.forEach(
				e -> System.out.println(e));
	}
	
	public void createClasses(){		
		PrefixManager pm = new DefaultPrefixManager("http://sw.palmas.br/teste/ontologia#");
		
		OWLClass classA = factory.getOWLClass(":A", pm);
		
		OWLDeclarationAxiom axiom = factory.getOWLDeclarationAxiom(classA);
		
		if(o.add(axiom) == ChangeApplied.SUCCESSFULLY)
			System.out.println("Class added sucessfully");
		else
			System.out.println("Class not added");
	}
	
	public void saveOntology(){
		
		File file = new File("newOntology.owl");
	
		
		RDFXMLDocumentFormat rdf = new RDFXMLDocumentFormat();
		
		try {
			m.saveOntology(o, rdf, IRI.create(file.toURI()));
			System.out.println("Ontology saved.");
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error on trying to save ontology.");
		}
	}
	
	public void deleteEntity(String entityName){
		
		OWLEntityRemover remover = new OWLEntityRemover(o);
		
		OWLClass classToBeRemoved = getClass(entityName);
		
		classToBeRemoved.accept(remover);
		
		try{
			o.applyChanges(remover.getChanges());
			System.out.println("Class deleted.");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}