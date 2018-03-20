package br.usp.ffclrp.dcm.lssb.transformation_manager;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.DirectoryCreationFailedException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.ErrorFileException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.StatusFileException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.TransformationActivityNotFoundException;
import br.usp.ffclrp.dcm.lssb.transformation_software.TriplesProcessing;
import br.usp.ffclrp.dcm.lssb.transformation_software.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.jena.riot.RDFDataMgr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;


public class TransformationManagerImpl implements TransformationManagerDao {

    public File localStorage = null;
    Properties properties = null;

    public TransformationManagerImpl(){
        properties = new Properties();

        try {

            InputStream propertiesFile = TransformationManagerImpl.class
                    .getResourceAsStream("/FileSystemDAO.properties");
            properties.load(propertiesFile);
            propertiesFile.close();

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

            // create input rules directory
            File inputRulesSubdirectory =
                    new File(transformationRoot, properties.getProperty(
                            "transformations.inputRulesSubpath"));
            if (!inputRulesSubdirectory.exists()) {
                inputRulesSubdirectory.mkdirs();
            }


            // create state file
            File transformationStateFile = new File(transformationRoot, "metadata/state");
            if (!transformationStateFile.exists()) {
                FileUtils.write(transformationStateFile,
                        EnumActivityState.CREATED.toString(), "utf-8");
            }

            // create error file
            File transformationErrorFile = new File(transformationRoot, "metadata/error");
            if (!transformationErrorFile.exists()) {
                transformationErrorFile.createNewFile();
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
    public void addRulesToTransformation(String transformationId, InputStream rules) throws TransformationActivityNotFoundException, StatusFileException {
        File transformationRoot = new File(localStorage, transformationId);

        if (transformationRoot.exists()){
            File inputRulesSubdirectory =
                    new File(transformationRoot, properties.getProperty(
                            "transformations.inputRulesSubpath"));


            saveFile(rules, new File(inputRulesSubdirectory, "rules").toPath());

            isReady(transformationId);

        } else {
            throw new TransformationActivityNotFoundException(transformationId);
        }
    }

    @Override
    public void addOntologyToTransformation(String transformationId, InputStream ontology, String filename )
            throws TransformationActivityNotFoundException, StatusFileException {
        File transformationRoot = new File(localStorage, transformationId);

        if (transformationRoot.exists()){
            File inputOntologiesSubdirectory =
                    new File(transformationRoot, properties.getProperty(
                            "transformations.inputOntologiesSubpath"));


            saveFile(ontology, new File(inputOntologiesSubdirectory, filename).toPath());
            isReady(transformationId);

        } else {
            throw new TransformationActivityNotFoundException(transformationId);
        }
    }

    @Override
    public void addDataSetToTransformation(String transformationId, InputStream dataset, String filename)
            throws TransformationActivityNotFoundException, StatusFileException  {
        File transformationRoot = new File(localStorage, transformationId);

        if (transformationRoot.exists()){
            File inputsDataSetSubdirectory =
                    new File(transformationRoot, properties.getProperty(
                            "transformations.inputDataSetsSubpath"));

            saveFile(dataset, new File(inputsDataSetSubdirectory, filename).toPath());
            isReady(transformationId);

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

    @Override
    public Boolean isReady(String transformationId) throws StatusFileException {
        Boolean ready;

        File transformationRoot = new File(localStorage, transformationId);

        File transformationStateFile = new File(transformationRoot, "metadata/state");
        if(getStatus(transformationId).equals(EnumActivityState.READY.toString()))
            return true;

        File datasetsDirectory = new File(transformationRoot, properties.getProperty("transformations.inputDataSetsSubpath"));
        File ontologiesDirectory = new File(transformationRoot, properties.getProperty("transformations.inputOntologiesSubpath"));
        File rulesDirectory = new File(transformationRoot, properties.getProperty("transformations.inputRulesSubpath"));

        ready = datasetsDirectory.listFiles().length > 0;
        ready = ready && ontologiesDirectory.listFiles().length > 0;
        ready = ready && rulesDirectory.listFiles().length == 1;

        if(ready){
            updateStatus(transformationId, EnumActivityState.READY);
        }

        return ready;
    }

    public void updateStatus(String transformationId, EnumActivityState newState) throws StatusFileException{
        File transformationRoot = new File(localStorage, transformationId);

        File transformationStateFile = new File(transformationRoot, "metadata/state");

        // update state file
        try{
            FileUtils.write(transformationStateFile, newState.toString(), "utf-8");
        }catch (Exception e){
            throw new StatusFileException(e.getMessage());
        }
    }

    public void log(String transformationId, Exception exception) throws ErrorFileException {

        if(exception == null)
            exception = new Exception("");

        File transformationRoot = new File(localStorage, transformationId);

        File transformationErrorFile = new File(transformationRoot, "metadata/error");

        // update error file
        try{
            FileUtils.write(transformationErrorFile, exception.getMessage(), "utf-8");
        }catch (Exception e){
            throw new ErrorFileException(e.getMessage());
        }
    }

    public String getStatus(String transformationId) throws StatusFileException {
        File transformationRoot = new File(localStorage, transformationId);
        File transformationStateFile = new File(transformationRoot, "metadata/state");

        String fileContent;
        try {
             fileContent = Utils.readFile(transformationStateFile.getAbsolutePath());
        }catch (Exception e){
            throw new StatusFileException(e.getMessage());
        }

        if(fileContent.equals(EnumActivityState.CREATED.toString()))
            return EnumActivityState.CREATED.toString();
        if(fileContent.equals(EnumActivityState.READY.toString()))
            return EnumActivityState.READY.toString();
        if(fileContent.equals(EnumActivityState.RUNNING.toString()))
            return EnumActivityState.RUNNING.toString();
        if(fileContent.equals(EnumActivityState.SUCCEEDED.toString()))
            return EnumActivityState.SUCCEEDED.toString();
        if(fileContent.equals(EnumActivityState.FAILED.toString()))
            return EnumActivityState.FAILED.toString();
        if(fileContent.equals(EnumActivityState.REMOVED.toString()))
            return EnumActivityState.REMOVED.toString();

        throw new StatusFileException("Unable to recovery the state.");
    }

    public List<String> getAllOntologies(String transformationId) throws Exception {
        List<String> ontologiesList = new ArrayList<>();
        File transformationRoot = new File(localStorage, transformationId);

        try {
            if (transformationRoot.exists()) {
                File inputOntologiesSubdirectory =
                        new File(transformationRoot, properties.getProperty(
                                "transformations.inputOntologiesSubpath"));

                File[] files = inputOntologiesSubdirectory.listFiles();

                for(File f : files){
                    ontologiesList.add(f.getAbsolutePath());
                }
            }else
                throw new Exception("Transformation ID not exists.");
        } catch (Exception e) {
            throw e;
        }

        return ontologiesList;
    }


    public List<String> getAllDatasets(String transformationId) throws Exception {
        List<String> datasetsList = new ArrayList<>();
        File transformationRoot = new File(localStorage, transformationId);

        try {
            if (transformationRoot.exists()) {
                File inputDatasetsSubdirectory =
                        new File(transformationRoot, properties.getProperty(
                                "transformations.inputDataSetsSubpath"));

                File[] files = inputDatasetsSubdirectory.listFiles();

                for(File f : files){
                    datasetsList.add(f.getAbsolutePath());
                }
            }else
                throw new Exception("Transformation ID not exists.");
        } catch (Exception e) {
            throw e;
        }

        return datasetsList;
    }

    public String getRulesFile(String transformationId) throws Exception {
        String rulesFile = "";

        File transformationRoot = new File(localStorage, transformationId);

        if(transformationRoot.exists()){
            File inputRulesSubdirectory =
                    new File(transformationRoot, properties.getProperty(
                            "transformations.inputRulesSubpath"));

            File[] files = inputRulesSubdirectory.listFiles();
            for(File f : files){
                if(f.getName().equals("rules"))
                    rulesFile = f.getAbsolutePath();
            }
        }else{
            throw new Exception("Transformation ID not exists");
        }

        return rulesFile;
    }

    private void saveFile(InputStream file, Path path){
        try{
            Files.copy(file, path);
            file.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public 	void 			writeRDF(TriplesProcessing triplesProcessing, String transformationId) throws Exception {
        System.out.println("#####################################\nWriting RDF...");
        long startTime = System.currentTimeMillis();
        try{
            File f = new File(localStorage, transformationId + "/RDFTriples.rdf");
            FileOutputStream fos = new FileOutputStream(f);
            //RDFDataMgr.write(fos, model, Lang.TRIG);
            //RDFDataMgr.write(fos, model, Lang.TURTLE);
            //RDFDataMgr.write(fos, model, Lang.RDFXML);
            //RDFDataMgr.write(fos, model, Lang.NTRIPLES);
            RDFDataMgr.write(fos, triplesProcessing.getModel(), triplesProcessing.defaultRuleConfig.getSyntax());
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("Wrote RDF in " + elapsedTime / 1000 + " secs");
        elapsedTime = stopTime - triplesProcessing.startTime;
        System.out.println("Processed in " + elapsedTime / 1000 + " secs");
    }

    public String getErrorFileContent(String transformationId) throws ErrorFileException{
        File transformationRoot = new File(localStorage, transformationId);
        File transformationStateFile = new File(transformationRoot, "metadata/error");

        String fileContent = "";
        try {
            fileContent = Utils.readFile(transformationStateFile.getAbsolutePath());
        }catch (Exception e){
            throw new ErrorFileException(e.getMessage());
        }

        return fileContent;
    }
}
