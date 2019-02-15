package br.usp.ffclrp.dcm.lssb.transformation_software.rulesprocessing;

import br.usp.ffclrp.dcm.lssb.custom_exceptions.NotFoundExternalNodeException;
import br.usp.ffclrp.dcm.lssb.custom_exceptions.SearchBlockException;
import br.usp.ffclrp.dcm.lssb.transformation_software.RuleInterpretor;
import br.usp.ffclrp.dcm.lssb.transformation_software.Utils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
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
    private String query;
    private MultiValuedMap<String, String> externalNodesAlreadyFound = new ArrayListValuedHashMap<>();
    private int qtd = 0;
    private int qtdSearched = 0;
    private Set<String> reused = new HashSet<>();

    public SearchBlock(String id, String endpointIRI, String query){
        this.id                 = id;
        this.endpointIRI        = endpointIRI;
        this.query              = query;
    }

    public String getId() {
        return id;
    }

    public String getEndpointIRI() {    return endpointIRI;    }

    public String getQueryToSearch() {  return query;   }

    public List<String> getExternalNode(List<String> dataList, String variable) throws NotFoundExternalNodeException {

        List<String> externalNodeIRI = new ArrayList<>();

        for(String item : dataList){
            Collection<String> existed = externalNodesAlreadyFound.get(item);
            if( existed.size() > 0) {
                externalNodeIRI.addAll(existed);
                reused.addAll(existed);
                continue;
            }else{

                String query = this.query;
                query = query.replaceAll("\\?tsvData", "\"" + item + "\"");

                QueryExecution q = QueryExecutionFactory.sparqlService(this.endpointIRI, query);

                ResultSet results;
                try {
                    results = q.execSelect();
                }catch (Exception e){
                    System.out.println(e.getMessage());
                    return null;
                }

                this.qtdSearched++;
                while(results.hasNext()){
                    QuerySolution soln = results.nextSolution();
                    externalNodeIRI.add(soln.get(variable).toString());
                    externalNodesAlreadyFound.put(item, soln.get(variable).toString());
                    this.qtd++;
                }
            }
        }
        System.out.println("IRIs founds: " + qtd + " of " + qtdSearched + " (" + reused.size() + " reused)");
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
                matcher = Utils.matchRegexOnString(EnumRegexList.SELECTSEARCHBODY.get(), sbAsText);

        String endpoint = matcher.group(1).trim();
        String query = matcher.group(2).trim();

        if(endpoint == null)
            throw new SearchBlockException("The field 'endpoint' is mandatory if you are using search_block but unfortunately this field was not identified in all search blocks. Please check your file.");

//        if(predicate == null)
//            throw new SearchBlockException("The field 'predicate' is mandatory if you are using search_block but unfortunately this field was not identified in all search blocks. Please check your file.");

        return new SearchBlock(searchBlockId, endpoint, query);
    }
}
