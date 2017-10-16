package br.usp.ffclrp.dcm.lssb.transformation_software;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
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
		dataProperties.forEach(p -> {//System.out.println(labelFor(p) + " -> " + p);
									mappingPredicatesAnnotations.put(labelFor(p), p);
									});
		
		Stream<OWLObjectProperty> objectProperties = o.objectPropertiesInSignature();
		objectProperties.forEach(p -> {//System.out.println(labelFor(p) + " -> " + p);
									  mappingPredicatesAnnotations.put(labelFor(p), p);
									  });
		
		/*for(int i=0; i<mappingPredicatesAnnotations.size(); i++){
			System.out.println( mappingPredicatesAnnotations.keySet().toArray()[i] + " -> " +
								mappingPredicatesAnnotations.values().toArray()[i]);
		}
		
		System.out.println("End");*/
	}

	private void addAllClassesLabelsToMap() {
		
		mappingClassesAnnotations = new HashMap<String, OWLClass>();
		
		Stream<OWLClass> stream = o.classesInSignature();
		stream.forEach(c -> mappingClassesAnnotations.put(labelFor(c), c));
		
	}
	
	// @author Ignazio Palmisano (https://github.com/ignazio1977)
	// Modified by Gabriel Gusmao (https://github.com/gcsgpp)
	private String labelFor(@Nonnull OWLEntity entity) {
        /*
         * Use a visitor to extract label annotations
         */
        LabelExtractor le = new LabelExtractor();
        EntitySearcher.getAnnotationObjects(entity, o).forEach( anno -> anno.accept(le));
        
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