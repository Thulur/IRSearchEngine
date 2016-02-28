package SearchEngine.search;

import SearchEngine.data.Configuration;
import SearchEngine.data.Document;
import SearchEngine.data.Posting;
import SearchEngine.index.ContentIndex;
import SearchEngine.utils.WordParser;

import java.io.IOException;
import java.util.*;

/**
 * Created by sebastian on 18.11.2015.
 */
public class PRFSearch implements Search {
    private String searchTerm;
    private ContentIndex contentIndex;
    private int topK;
    private int prf;

    @Override
    public void setupSearch(String searchTerm, ContentIndex contentIndex, int topK, int prf) {
        this.searchTerm = searchTerm;
        this.contentIndex = contentIndex;
        this.topK = topK;
        this.prf = prf;
    }

    @Override
    public ArrayList<Posting> execute() throws IOException {
        SearchFactory searchFactory = new SearchFactory();
        searchFactory.setContentIndex(contentIndex);

        // Execute search with original query
        Search firstSearch = searchFactory.getSearchFromQuery(searchTerm, topK);
        ArrayList<Posting> firstSearchResults = firstSearch.execute();
        Map<String, List<Long>> words = getWordOccurrencesFromResults(firstSearchResults);

        // Sort words by their number of occurrences
        List<HashMap.Entry<String, List<Long>>> tmpList = new ArrayList<>(words.entrySet());
        Collections.sort(tmpList, (obj1, obj2) -> ((Comparable) ((obj2)).getValue().size()).compareTo(((obj1)).getValue().size()));

        String modifiedSearchTerm = getModifiedQuery(searchTerm, tmpList.iterator());

        // Execute search with modified query
        Search secondSearch = searchFactory.getSearchFromQuery(modifiedSearchTerm, topK);
        ArrayList<Posting> secondSearchResults = secondSearch.execute();

        ArrayList<Posting> result = new ArrayList<>();
        Posting posting;
        for (int i = 0; i < topK && i < secondSearchResults.size(); ++i) {
            posting = secondSearchResults.get(i);
            result.add(posting);
        }

        return secondSearchResults;
    }

    private Map<String, List<Long>> getWordOccurrencesFromResults(ArrayList<Posting> results) throws IOException {
        Map<String, List<Long>> words = new HashMap<>();

        for (int i = 0; i < prf && i < results.size(); ++i) {
            Document curDoc = new Document(results.get(i));;
            curDoc.loadPatentData(contentIndex.getCacheFile(curDoc.getFileId()));

            for (Map.Entry<String, List<Long>> entry: WordParser.getInstance().stem(curDoc.generateSnippet(searchTerm), true).entrySet()) {
                if (words.containsKey(entry.getKey())) {
                    words.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    words.put(entry.getKey(), entry.getValue());
                }
            }
        }

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
