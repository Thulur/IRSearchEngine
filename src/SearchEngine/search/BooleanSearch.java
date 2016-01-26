package SearchEngine.search;

import SearchEngine.data.Posting;
import SearchEngine.index.ContentIndex;
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
    private ContentIndex contentIndex;
    private WordParser wordParser;
    private ArrayList<Posting> results;
    private int topK;

    @Override
    public void setupSearch(String searchTerm, ContentIndex contentIndex, int topK, int prf) {
        this.searchTerm = searchTerm;
        this.contentIndex = contentIndex;
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
        searchFactory.setContentIndex(contentIndex);

        for (String searchToken : searchTerm.split(" ")) {
            if (booleanTokens.contains(searchToken)) {
                booleanOperator = searchToken;
            } else if (searchToken.contains("*")) {
                searchResults.put(searchToken, searchFactory.getSearchFromQuery(searchToken.toLowerCase(), topK).execute());
            } else {
                searchResults.put(searchToken, searchFactory.getSearchFromQuery(wordParser.stemSingleWord(searchToken), topK).execute());
            }
        }
    }

    private void executeBooleanOperation() throws IOException {
        Iterator<List<Posting>> searchIterator = searchResults.values().iterator();
        Map<Integer, Posting> postings = new HashMap<>();

        for (Posting posting: searchIterator.next()) {
            postings.put(posting.getDocId(), posting);
        }

        for (Posting posting: searchIterator.next()) {
            if (postings.get(posting.getDocId()) != null) {
                if (booleanOperator.equals("NOT")) {
                    postings.remove(posting.getDocId());
                } else {
                    Posting tmpPosting = postings.get(posting.getDocId());
                    double curWeight = tmpPosting.getWeight();
                    tmpPosting.setWeight(curWeight + posting.getWeight());

                    if (booleanOperator.equals("AND")) {
                        results.add(tmpPosting);
                    }
                }
            } else {
                if (booleanOperator.equals("OR")) {
                    postings.put(posting.getDocId(), posting);
                }
            }
        }

        if (booleanOperator.equals("NOT") || booleanOperator.equals("OR")) {
            results.addAll(postings.values());
        }

        Collections.sort(results, (obj1, obj2) -> ((Comparable) ((obj2)).getWeight()).compareTo(((obj1)).getWeight()));
    }
}
