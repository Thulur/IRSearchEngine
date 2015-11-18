package SearchEngine.search;

import SearchEngine.data.Document;
import SearchEngine.index.Index;

import java.util.ArrayList;

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
    public ArrayList<Document> execute() {
        return null;
    }
}
