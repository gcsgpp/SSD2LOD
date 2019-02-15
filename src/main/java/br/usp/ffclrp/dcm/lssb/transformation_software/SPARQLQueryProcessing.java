package br.usp.ffclrp.dcm.lssb.transformation_software;
import br.usp.ffclrp.dcm.lssb.transformation_manager.EnumActivityState;
import br.usp.ffclrp.dcm.lssb.transformation_manager.TransformationManagerImpl;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.lang.LangNTriples;
import org.apache.jena.sparql.mgt.Explain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SPARQLQueryProcessing {

    private List<String> languages = new ArrayList<>();

    public SPARQLQueryProcessing(){

    }

    public ResultSet QuerySelect(File rdfTriples, String queryFilePath) throws Exception {

        Model model = ModelFactory.createDefaultModel();

        try {
            model.read(rdfTriples.getAbsolutePath());
        }catch (RiotException e){
            throw new Exception("ERROR: Error on importing the Triple Set to query. - " + e.getMessage() );
        }

        String queryString = null;
        try {
            queryString = readFile(queryFilePath);
        }catch (Exception e){
            throw new Exception("ERROR: Error on importing query file. - " + e.getMessage() );
        }
        Query query = QueryFactory.create(queryString) ;


        ResultSet results;
        ARQ.setExecutionLogging(Explain.InfoLevel.ALL) ;
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            results = qexec.execSelect();

            results = ResultSetFactory.copyResults(results);
        }
        return results;    // Passes the result set out of the try-resources
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

    public String 			readFile(String pathfile) throws IOException {
        String fileContent = "";
        try(Stream<String> stream = Files.lines(Paths.get(pathfile))){

            for(String line : stream.toArray(String[]::new)){
                fileContent += " " + line;
            }
        }catch(IOException e){
            e.printStackTrace();
            throw e;
        }

        return fileContent;
    }
}
