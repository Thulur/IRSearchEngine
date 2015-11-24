package SearchEngine.search;

import SearchEngine.data.Document;
import SearchEngine.index.Index;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by sebastian on 10.11.2015.
 */
public interface Search {
    void setupSearch(String searchTerm, Index index, int topK, int prf);

    ArrayList<Document> execute() throws IOException;
}
