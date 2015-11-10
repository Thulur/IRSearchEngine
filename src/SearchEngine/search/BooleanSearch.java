package SearchEngine.search;

import SearchEngine.data.Document;
import SearchEngine.index.Index;
import SearchEngine.utils.WordParser;

import java.util.*;

/**
 * Created by Dennis on 06.11.2015.
 */
public class BooleanSearch {
    private String searchTerm;
    private List<String> booleanTokens;
    private HashMap<String, List<Document>> searchResults;
    private String booleanOperator;
    private Index index;
    private WordParser wordParser;
    private ArrayList<Document> results;

    public BooleanSearch(String searchTerm, Index index) {
        this.searchTerm = searchTerm;
        this.index = index;

        wordParser = WordParser.getInstance();

        booleanTokens = new LinkedList<>();
        booleanTokens.add("AND");
        booleanTokens.add("OR");
        booleanTokens.add("NOT");

        searchResults = new LinkedHashMap<>();
        results = new ArrayList<>();
    }

    public ArrayList<Document> execute() {
        defineSubqueries();
        processQuery();
        executeBooleanOperation();

        return results;
    }

    /**
     * Parses the query and tries to find subqueries.
     * This method is only needed as soon as we have to process complex boolean queries.
     */
    private void defineSubqueries() {
    }

    private void processQuery() {
        for (String searchToken : searchTerm.split(" ")) {
            if (booleanTokens.contains(searchToken)) {
                booleanOperator = searchToken;
            } else if (searchToken.contains("*")) {
                searchResults.put(searchToken, index.lookUpPostingInFileWithCompression(searchToken.toLowerCase()));
            } else {
                searchResults.put(searchToken, index.lookUpPostingInFileWithCompression(wordParser.stemSingleWord(searchToken)));
            }
        }
    }

    private void executeBooleanOperation() {
        Set<Document> firstSet = new HashSet<>(searchResults.values().iterator().next());
        Set<Document> secondSet = new HashSet<>(searchResults.values().iterator().next());

        switch (booleanOperator) {
            case "AND":  firstSet.retainAll(secondSet); break;
            case "OR": firstSet.addAll(secondSet); break;
            case "NOT": firstSet.removeAll(secondSet); break;
            default: break;
        }

        results.addAll(firstSet);
    }
}
