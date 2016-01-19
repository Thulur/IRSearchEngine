package SearchEngine.search;

import SearchEngine.data.Configuration;
import SearchEngine.data.Document;
import SearchEngine.data.Posting;
import SearchEngine.index.Index;

import java.io.IOException;
import java.util.*;

/**
 * Created by sebastian on 18.11.2015.
 */
public class PRFSearch implements Search {
    private String searchTerm;
    private Index index;
    private int topK;
    private int prf;

    @Override
    public void setupSearch(String searchTerm, Index index, int topK, int prf) {
        this.searchTerm = searchTerm;
        this.index = index;
        this.topK = topK;
        this.prf = prf;
    }

    @Override
    public ArrayList<Posting> execute() throws IOException {
        SearchFactory searchFactory = new SearchFactory();
        searchFactory.setIndex(index);

        // Execute search with original query
        Search firstSearch = searchFactory.getSearchFromQuery(searchTerm, topK, 0);
        ArrayList<Posting> firstSearchResults = firstSearch.execute();
        Map<String, List<Long>> words = getWordOccurrencesFromResults(firstSearchResults);

        // Sort words by their number of occurrences
        List<HashMap.Entry<String, List<Long>>> tmpList = new ArrayList<>(words.entrySet());
        Collections.sort(tmpList, (obj1, obj2) -> ((Comparable) ((obj2)).getValue().size()).compareTo(((obj1)).getValue().size()));

        String modifiedSearchTerm = getModifiedQuery(searchTerm, tmpList.iterator());

        // Execute search with modified query
        Search secondSearch = searchFactory.getSearchFromQuery(modifiedSearchTerm, topK, 0);
        ArrayList<Posting> secondSearchResults = secondSearch.execute();

        ArrayList<Document> result = new ArrayList<>();
        Document curDoc;
        /*for (int i = 0; i < topK && i < secondSearchResults.size(); ++i) {
            curDoc = secondSearchResults.get(i);
            curDoc.loadPatentData(index.getCacheFile(curDoc.getFileId()));
            result.add(curDoc);
        }*/

        return secondSearchResults;
    }

    private Map<String, List<Long>> getWordOccurrencesFromResults(ArrayList<Posting> results) throws IOException {
        Map<String, List<Long>> words = new HashMap<>();

        /*for (int i = 0; i < prf && i < results.size(); ++i) {
            Posting curDoc = results.get(i);
            curDoc.loadPatentData(index.getCacheFile(curDoc.getFileId()));

            for (Map.Entry<String, List<Long>> entry: WordParser.getInstance().stem(curDoc.generateSnippet(searchTerm), true).entrySet()) {
                if (words.containsKey(entry.getKey())) {
                    words.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    words.put(entry.getKey(), entry.getValue());
                }
            }
        }*/

        return words;
    }

    private String getModifiedQuery(String searchTerm, Iterator<Map.Entry<String, List<Long>>> iterator) {
        StringBuilder modifiedSearchTerm = new StringBuilder(searchTerm);
        HashMap.Entry<String, List<Long>> curEntry;

        for (int i = 0; i < Configuration.PRF_WORD_COUNT && iterator.hasNext(); ++i) {
            curEntry = iterator.next();
            modifiedSearchTerm.append(" ");
            modifiedSearchTerm.append(curEntry.getKey());
        }

        return modifiedSearchTerm.toString();
    }
}
