package SearchEngine.search;

import SearchEngine.data.Document;
import SearchEngine.index.Index;
import SearchEngine.utils.WordParser;

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
    public ArrayList<Document> execute() throws IOException {
        SearchFactory searchFactory = new SearchFactory();
        searchFactory.setIndex(index);

        Search firstSearch = searchFactory.getSearchFromQuery(searchTerm, topK, 0);
        ArrayList<Document> firstSearchResults = firstSearch.execute();

        Map<String, List<Long>> words = new HashMap<>();
        for (int i = 0; i < prf && i < firstSearchResults.size(); ++i) {
            Document curDoc = firstSearchResults.get(i);
            curDoc.loadPatentData(index.getCacheFile(curDoc.getFileId()));
            for (Map.Entry<String, List<Long>> entry: WordParser.getInstance().stem(curDoc.generateSnippet(searchTerm, false), true).entrySet()) {
                if (words.containsKey(entry.getKey())) {
                    words.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    words.put(entry.getKey(), entry.getValue());
                }
            }
        }

        List<HashMap.Entry<String, List<Long>>> tmpList = new ArrayList<>(words.entrySet());
        Collections.sort(tmpList, (obj1, obj2) -> ((Comparable) ((obj1)).getValue().size()).compareTo(((obj2)).getValue().size()));
        Collections.reverse(tmpList);

        StringBuilder modifiedSearchTerm = new StringBuilder(searchTerm);
        Iterator<HashMap.Entry<String, List<Long>>> iterator = tmpList.iterator();
        HashMap.Entry<String, List<Long>> curEntry;
        for (int i = 0; i < 3 && iterator.hasNext(); ++i) {
            curEntry = iterator.next();
            modifiedSearchTerm.append(" ");
            modifiedSearchTerm.append(curEntry.getKey());
        }

        Search secondSearch = searchFactory.getSearchFromQuery(modifiedSearchTerm.toString(), topK, 0);
        ArrayList<Document> secondSearchResults = secondSearch.execute();

        ArrayList<Document> result = new ArrayList<>();
        Document curDoc;
        for (int i = 0; i < topK && i < secondSearchResults.size(); ++i) {
            curDoc = secondSearchResults.get(i);
            curDoc.loadPatentData(index.getCacheFile(curDoc.getFileId()));
            result.add(curDoc);
        }

        return result;
    }
}
