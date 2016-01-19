package SearchEngine.search;

import SearchEngine.data.Posting;
import SearchEngine.index.Index;
import SearchEngine.utils.WordParser;

import java.io.IOException;
import java.util.*;

/**
 * Created by Dennis on 06.11.2015.
 */
public class BooleanSearch implements Search {
    private String searchTerm;
    private List<String> booleanTokens;
    private HashMap<String, List<Posting>> searchResults;
    private String booleanOperator;
    private Index index;
    private WordParser wordParser;
    private ArrayList<Posting> results;
    private int topK;

    @Override
    public void setupSearch(String searchTerm, Index index, int topK, int prf) {
        this.searchTerm = searchTerm;
        this.index = index;
        this.topK = topK;

        wordParser = WordParser.getInstance();

        booleanTokens = new LinkedList<>();
        booleanTokens.add("AND");
        booleanTokens.add("OR");
        booleanTokens.add("NOT");

        searchResults = new LinkedHashMap<>();
        results = new ArrayList<>();
    }

    public ArrayList<Posting> execute() throws IOException {
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

    private void processQuery() throws IOException {
        SearchFactory searchFactory = new SearchFactory();
        searchFactory.setIndex(index);

        for (String searchToken : searchTerm.split(" ")) {
            if (booleanTokens.contains(searchToken)) {
                booleanOperator = searchToken;
            } else if (searchToken.contains("*")) {
                searchResults.put(searchToken, searchFactory.getSearchFromQuery(searchToken.toLowerCase(), topK, 0).execute());
            } else {
                searchResults.put(searchToken, searchFactory.getSearchFromQuery(wordParser.stemSingleWord(searchToken), topK, 0).execute());
            }
        }
    }

    private void executeBooleanOperation() throws IOException {
        Iterator<List<Posting>> searchIterator = searchResults.values().iterator();
        Set<Integer> firstSet = new HashSet<>();
        Set<Integer> secondSet = new HashSet<>();
        Map<Integer, Posting> postings = new HashMap<>();

        for (Posting posting: searchIterator.next()) {
            firstSet.add(posting.getDocId());
            postings.put(posting.getDocId(), posting);
        }

        for (Posting posting: searchIterator.next()) {
            secondSet.add(posting.getDocId());

            postings.put(posting.getDocId(), posting);
        }

        switch (booleanOperator) {
            case "AND":  firstSet.retainAll(secondSet); break;
            case "OR": firstSet.addAll(secondSet); break;
            case "NOT": firstSet.removeAll(secondSet); break;
            default: break;
        }
        Iterator<Integer> docIdIterator = firstSet.iterator();
        int curDocId;
        int i = 0;
        while (docIdIterator.hasNext() && i < topK) {
            curDocId = docIdIterator.next();
            results.add(postings.get(curDocId));
            ++i;
        }
    }
}
