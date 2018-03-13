package br.usp.ffclrp.dcm.lssb.transformation_manager;

import java.io.File;
import java.io.InputStream;

public interface TransformationManagerDao {

    public String newTransformation() throws Exception;

    public File getTransformedData(String transformationId) throws Exception;

    public void addOntologyToTransformation(String transformationId, InputStream ontology, String filename ) throws Exception;

    public void addDataSetToTransformation(String transformationId, InputStream dataset, String filename ) throws Exception;

    public void addRulesToTransformation(String transformationId, InputStream rules) throws Exception;

    public void deleteTransformation(String transformationId) throws Exception;
}
