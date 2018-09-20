package br.usp.ffclrp.dcm.lssb.transformation_software;
import br.usp.ffclrp.dcm.lssb.transformation_manager.EnumActivityState;
import br.usp.ffclrp.dcm.lssb.transformation_manager.TransformationManagerImpl;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;


public class SPARQLQueryProcessing {

    public ResultSet QuerySelect(File rdfTriples, String queryFilePath) {
        Model model = ModelFactory.createDefaultModel().read(rdfTriples.getAbsolutePath());

        String queryString = readFile(queryFilePath);
        Query query = QueryFactory.create(queryString) ;

        ResultSet results;
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            results = qexec.execSelect();

            results = ResultSetFactory.copyResults(results);
        }
        return results;    // Passes the result set out of the try-resources
        //ResultSetFormatter.out(System.out, results, query) ;
    }

    public void QuerySelect(String transformationId, String queryId) throws Exception {
        TransformationManagerImpl transformationManager = new TransformationManagerImpl();

        String queryFilePath = transformationManager.getQueryFilePath(transformationId, queryId);
        File rdfTriples = transformationManager.getTransformedData(transformationId);

        transformationManager.updateQueryStatus(transformationId, queryId, EnumActivityState.RUNNING);

        ResultSet resultSet = QuerySelect(rdfTriples, queryFilePath);

        File queryFolderPath = transformationManager.getSpecificQueryFolderPath(transformationId, queryId);
        File resultFile = new File(queryFolderPath, "/queryResult.tsv");
        FileOutputStream fos = new FileOutputStream(resultFile);

        ResultSetFormatter.outputAsTSV(fos, resultSet);
    }

    public String 			readFile(String pathfile){
        String fileContent = "";
        try(Stream<String> stream = Files.lines(Paths.get(pathfile))){

            for(String line : stream.toArray(String[]::new)){
                fileContent += " " + line;
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        return fileContent;
    }
}
