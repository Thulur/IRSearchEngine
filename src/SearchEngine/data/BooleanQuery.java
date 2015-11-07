package SearchEngine.data;

import SearchEngine.indexing.Index;
import SearchEngine.utils.WordParser;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Dennis on 06.11.2015.
 */
public class BooleanQuery {

    private String searchTerm;
    private List<String> booleanTokens;
    private HashMap<String, List<Document>> searchResults;
    private String booleanOperator;
    private Index index;
    private WordParser wordParser;
    private ArrayList<Document> results;
    private String[] subQueries;



    public BooleanQuery(String searchTerm, Index index) {
        this.searchTerm = searchTerm;

        this.index = index;

        wordParser = WordParser.getInstance();

        booleanTokens = new LinkedList<>();
        booleanTokens.add("AND");
        booleanTokens.add("OR");
        booleanTokens.add("NOT");

        searchResults = new LinkedHashMap<>();
        results = new ArrayList<>();
        subQueries = new String[2];
    }

    public ArrayList<Document> executeQuery() {
        if (searchTerm.split(" ").length == 1) {
            processQuery();
        } else {
            defineSubqueries();
        }

        executeBooleanOperation();

        return results;
    }

    /**
     * Parses the query and tries to find subqueries.
     * This method is only needed as soon as we have to process complex boolean queries.
     */
    private void defineSubqueries() {

        int bracketCount = 0;
        int index = 0;

        for (int i = 0; i < searchTerm.length(); i++) {
            switch (searchTerm.charAt(i)) {
                case '(': ++bracketCount; index = bracketCount == 1 ? 1 : index; break;
                case ')': --bracketCount; index = bracketCount == 0 ? 0 : index; break;
                default: subQueries[index] += searchTerm.charAt(i);
            }
        }

        if (searchTerm.charAt(0) == '(') {
            String temp = subQueries[0];
            subQueries[0] = subQueries[1];
            subQueries[1] = temp;
        }


    }

    private void processQuery() {

        for (String searchToken : searchTerm.split(" ")) {
            if (booleanTokens.contains(searchToken)) {
                booleanOperator = searchToken;
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
