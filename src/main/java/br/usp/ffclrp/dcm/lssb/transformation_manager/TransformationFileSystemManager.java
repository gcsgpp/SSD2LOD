package br.usp.ffclrp.dcm.lssb.transformation_manager;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.DirectoryCreationFailedException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.TransformationActivityNotFoundException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;


public class TransformationFileSystemManager implements TransformationManagerDao {

    public File localStorage = null;
    Properties properties = null;

    public TransformationFileSystemManager(){
        properties = new Properties();

        try {
            properties.load(
                    TransformationFileSystemManager.class
                            .getResourceAsStream("/FileSystemDAO.properties"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String newDirectoryPath =
                properties.getProperty("FSManager.localStorage");

        localStorage = new File(newDirectoryPath);

        if (!localStorage.exists()) {
            localStorage.mkdirs();
        }
    }

    /**
    *    Method inspired on @cawal`s code.
    *    @cawal: http://www.github.com/cawal
    */
    @Override
    public String newTransformation() throws DirectoryCreationFailedException{
        UUID newTransformationId = UUID.randomUUID();
        File transformationRoot = new File(localStorage, newTransformationId.toString());

        try {
            transformationRoot.mkdirs();

            // create input ontologies directory
            File inputOntologiesSubdirectory =
                    new File(transformationRoot, properties.getProperty(
                            "transformations.inputOntologiesSubpath"));
            if (!inputOntologiesSubdirectory.exists()) {
                inputOntologiesSubdirectory.mkdirs();
            }

            // create input datasets directory
            File inputDataSetsSubdirectory =
                    new File(transformationRoot, properties.getProperty(
                            "transformations.inputDataSetsSubpath"));
            if (!inputDataSetsSubdirectory.exists()) {
                inputDataSetsSubdirectory.mkdirs();
            }

            // create state file
            File transformationStateFile = new File(transformationRoot, "metadata/state");
            if (!transformationStateFile.exists()) {
                FileUtils.write(transformationStateFile,
                        EnumActivityState.CREATED.toString(), "utf-8");
            }

            return newTransformationId.toString();

        } catch (IOException e) {
            transformationRoot.delete();
            DirectoryCreationFailedException dcfe =
                    new DirectoryCreationFailedException();
            dcfe.initCause(e);
            throw dcfe;
        }

    }

    /**
     * Retrieves a transformation by its ID.
     *
     * @param transformationId
     * @return
     * @throws TransformationActivityNotFoundException
     */
    @Override
    public File getTransformedData(String transformationId) throws TransformationActivityNotFoundException {

        File transformedDocument = new File(properties.getProperty("succeededTransformations.localStorage"), transformationId + ".rdf");

        if (transformedDocument.exists()) {
                return transformedDocument;
        } else {
            throw new TransformationActivityNotFoundException(transformationId);
        }
    }

    @Override
    public void addOntologyToTransformation(String transformationId, InputStream ontology, String filename ) throws TransformationActivityNotFoundException {
        File transformationRoot = new File(localStorage, transformationId);

        if (transformationRoot.exists()){
            File inputOntologiesSubdirectory =
                    new File(transformationRoot, properties.getProperty(
                            "transformations.inputOntologiesSubpath"));


            saveFile(ontology, new File(inputOntologiesSubdirectory, filename).toPath());

        } else {
            throw new TransformationActivityNotFoundException(transformationId);
        }
    }

    @Override
    public void addDataSetToTransformation(String transformationId, InputStream dataset, String filename) throws TransformationActivityNotFoundException  {
        File transformationRoot = new File(localStorage, transformationId);

        if (transformationRoot.exists()){
            File inputsDataSetSubdirectory =
                    new File(transformationRoot, properties.getProperty(
                            "transformations.inputDataSetsSubpath"));

            saveFile(dataset, new File(inputsDataSetSubdirectory, filename).toPath());

        } else {
            throw new TransformationActivityNotFoundException(transformationId);
        }
    }

    @Override
    public void deleteTransformation(String transformationId) throws TransformationActivityNotFoundException {
        File transformationRoot = new File(localStorage, transformationId);
        try {
            FileUtils.deleteDirectory(transformationRoot);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TransformationActivityNotFoundException(transformationId);
        }
    }

    private void saveFile(InputStream file, Path path){
        try{
            Files.copy(file, path);

        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
