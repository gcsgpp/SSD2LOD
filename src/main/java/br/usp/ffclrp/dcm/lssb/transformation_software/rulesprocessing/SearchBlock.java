package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.NotFoundExternalNodeException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.SearchBlockException;
import br.usp.ffclrp.dcm.lssb.transformation_software.RuleInterpretor;
import br.usp.ffclrp.dcm.lssb.transformation_software.Utils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class SearchBlock {

    private int id;
    private String endpointIRI;
    private String predicateToSearch;
    private Map<String, String> externalNodesAlredyFound = new HashMap<>();

    public SearchBlock(int id, String endpointIRI, String predicateToSearch){
        this.id                 = id;
        this.endpointIRI        = endpointIRI;
        this.predicateToSearch  = predicateToSearch;
    }

    public int getId() {
        return id;
    }

    public String getEndpointIRI() {    return endpointIRI;    }

    public String getPredicateToSearch() {  return predicateToSearch;   }

    public String getExternalNode(String object) throws NotFoundExternalNodeException {

        String externalNodeIRI;
        externalNodeIRI = externalNodesAlredyFound.get(object);
        if(externalNodeIRI != null)
            return externalNodeIRI;

        //this.endpointIRI = "http://bio2rdf.org/sparql";
        String query = "SELECT DISTINCT ?s WHERE { ?s <" + this.predicateToSearch + "> ?o . BIND(str(?o) as ?o2) . VALUES ?o2 { \"" + object + "\" } } LIMIT 1";

        //query = "select distinct * where { ?s <http://bio2rdf.org/taxonomy_vocabulary:scientific-name> ?o. BIND(str(?o) as ?o2) . VALUES ?o2 { \"Phaseolus vulgaris\" }	}";
        QueryExecution q = QueryExecutionFactory.sparqlService(this.endpointIRI, query);

        ResultSet results = q.execSelect();

        if(results.hasNext()){
            QuerySolution soln = results.nextSolution();
            externalNodeIRI = soln.get("s").toString();
            externalNodesAlredyFound.put(object, externalNodeIRI);
            return externalNodeIRI;
        }

        throw new NotFoundExternalNodeException("Not found node with predicate <" + this.predicateToSearch + "> and object: \"" + object + "\"");
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
            if(block.startsWith("search_block"))
                searchBlocks.add(block);
        }
        return searchBlocks;
    }

    static private SearchBlock createSearchBlockFromString(String sbAsText) throws Exception {
        Matcher matcher 				=	Utils.matchRegexOnString(EnumRegexList.SELECTSUBJECTLINE.get(), sbAsText);
        String subjectLine 				=	Utils.splitByIndex(sbAsText, matcher.start())[0];
        String predicatesLinesOneBlock 	= 	Utils.splitByIndex(sbAsText, matcher.start())[1];

        Integer searchBlockId;
        try{
            searchBlockId =	Integer.parseInt(extractSearchBlockIDFromSentence(subjectLine));
        }catch (NumberFormatException e ){
            throw new SearchBlockException("One of the identifiers of Search Blocks is not a number. Check your transformation rule file.");
        }


        matcher = Utils.matchRegexOnString(EnumRegexList.SELECTPREDICATESDIVISIONS.get(), predicatesLinesOneBlock);
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

            String column 	= Utils.extractDataFromFirstQuotationMarkBlockInsideRegex(lineFromBlock, EnumRegexList.SELECTPREDICATE.get());
            lineFromBlock 	= Utils.removeRegexFromContent(EnumRegexList.SELECTPREDICATE.get(), lineFromBlock);
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
        data = matcher.group().replace("search_block[", "").trim();
        return data;
    }
}
