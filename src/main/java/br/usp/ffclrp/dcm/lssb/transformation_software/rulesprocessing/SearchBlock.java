package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.NotFoundExternalNodeException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.SearchBlockException;
import br.usp.ffclrp.dcm.lssb.transformation_software.RuleInterpretor;
import br.usp.ffclrp.dcm.lssb.transformation_software.Utils;
import org.apache.jena.base.Sys;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.*;
import java.util.regex.Matcher;

public class SearchBlock {

    private String id;
    private String endpointIRI;
    private String predicateToSearch;
    private Map<String, String> externalNodesAlreadyFound = new HashMap<>();
    private int qtd = 0;
    private int qtdSearched = 0;
    private Set<String> reused = new HashSet<>();

    public SearchBlock(String id, String endpointIRI, String predicateToSearch){
        this.id                 = id;
        this.endpointIRI        = endpointIRI;
        this.predicateToSearch  = predicateToSearch;
    }

    public String getId() {
        return id;
    }

    public String getEndpointIRI() {    return endpointIRI;    }

    public String getPredicateToSearch() {  return predicateToSearch;   }

    public List<String> getExternalNode(List<String> dataList) throws NotFoundExternalNodeException {

        List<String> externalNodeIRI = new ArrayList<>();

        String dataString = "";

        for(String item : dataList){
            String existed = externalNodesAlreadyFound.get(item);
            if( existed == null) {
                dataString += "\"" + item + "\"";
                this.qtdSearched++;
            }else{
                externalNodeIRI.add(existed);
                reused.add(existed);
                //System.out.println(" Reusing: " + existed + ";");
            }
            //System.out.print(item + " ");
        }

        //There's nothing to search for
        if(dataString.length() == 0) {
            System.out.println("Terms reused: " + reused.size());
            return externalNodeIRI;
        }


        //System.out.println(" Searching: " + dataString + ";");
        String query = "SELECT DISTINCT ?s ?o2 WHERE { ?s <" + this.predicateToSearch + "> ?o . BIND(str(?o) as ?o2) . VALUES ?o2 { " + dataString + " } }";
        QueryExecution q = QueryExecutionFactory.sparqlService(this.endpointIRI, query);

        ResultSet results;
        try {
            results = q.execSelect();
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }


        while(results.hasNext()){
            QuerySolution soln = results.nextSolution();
            externalNodeIRI.add(soln.get("s").toString());
            externalNodesAlreadyFound.put(soln.get("o2").toString(), soln.get("s").toString());
            this.qtd++;
        }
        System.out.println("Terms found: " + qtd + " of " + qtdSearched + " (" + reused.size() + " reused)");


        //throw new NotFoundExternalNodeException("Not found node with predicate <" + this.predicateToSearch + "> and object: \"" + object + "\"");
        //System.out.println("Not found node with predicate <" + this.predicateToSearch + "> and object: \"" + object + "\"");
        if(externalNodeIRI.isEmpty())
            return null;

        return externalNodeIRI;
    }

    static public List<SearchBlock> extractSearchBlockFromString(String fileContent) throws Exception {
        List<String> searchBlockListAsText = identifySearchBlocksFromString(fileContent);
        List<SearchBlock> sbList = new ArrayList<>();

        for(String sb : searchBlockListAsText){
            sbList.add(SearchBlock.createSearchBlockFromString(sb));
        }

        return sbList;
    }

    public static List<String> identifySearchBlocksFromString(String fileContent) throws Exception {
        List<String> searchBlocks = new ArrayList<>();

        List<String> identifiedBlocks = RuleInterpretor.identifyBlocksFromString(fileContent);

        for(String block : identifiedBlocks){
            if(block.startsWith("search_element"))
                searchBlocks.add(block);
        }
        return searchBlocks;
    }

    static private SearchBlock createSearchBlockFromString(String sbAsText) throws Exception {
        Matcher matcher 				=	Utils.matchRegexOnString(EnumRegexList.SELECTSEARCHID.get(), sbAsText);

        String searchBlockId = matcher.group(2);
                matcher = Utils.matchRegexOnString(EnumRegexList.SELECTSEARCHBODY.get(), sbAsText);;
        String predicatesLinesOneBlock = matcher.group(2);


        matcher = Utils.matchRegexOnString(EnumRegexList.SELECTSEARCHPREDICATESDIVISIONS.get(), predicatesLinesOneBlock);
        List<Integer> initialOfEachMatch = new ArrayList<Integer>();
        while(!matcher.hitEnd()){
            initialOfEachMatch.add(matcher.start());
            matcher.find();
        }

        String endpoint = null;
        String predicate = null;
        for(int i = 0; i <= initialOfEachMatch.size()-1; i++){
            int finalChar;
            if(i == initialOfEachMatch.size()-1) //IF LAST MATCH, GET THE END OF THE SENTENCE
                finalChar = predicatesLinesOneBlock.length();
            else
                finalChar = initialOfEachMatch.get(i+1);

            String lineFromBlock = predicatesLinesOneBlock.substring(initialOfEachMatch.get(i) + 1, // +1 exists to not include the first character, a comma
                    finalChar);

            String column 	= Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTSEARCHPREDICATESDIVISIONS.get());
            lineFromBlock 	= Utils.removeRegexFromContent(EnumRegexList.SELECTSEARCHPREDICATESDIVISIONS.get(), lineFromBlock);
            String value 	= Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTALL.get());

            if(column.toLowerCase().equals("endpoint")) {
                endpoint = value;
            }else if(column.toLowerCase().equals("predicate")){
                predicate = value;
            }else{
                throw new SearchBlockException("It was not possible to identify the term field +\'" + column + "\'");
            }


        }

        if(endpoint == null)
            throw new SearchBlockException("The field 'endpoint' is mandatory if you are using search_block but unfortunately this field was not identified in all search blocks. Please check your file.");

        if(predicate == null)
            throw new SearchBlockException("The field 'predicate' is mandatory if you are using search_block but unfortunately this field was not identified in all search blocks. Please check your file.");

        return new SearchBlock(searchBlockId, endpoint, predicate);
    }

    static private String extractSearchBlockIDFromSentence(String blockRulesAsText) {
        String data = "";

        Matcher matcher = Utils.matchRegexOnString(EnumRegexList.SELECTSEARCHBLOCKID.get(), blockRulesAsText);
        String test = matcher.group();
        data = matcher.group().replace("search_block[", "").trim();
        return data;
    }
}
