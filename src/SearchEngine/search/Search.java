package SearchEngine.search;

import SearchEngine.data.Posting;
import SearchEngine.index.ContentIndex;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by sebastian on 10.11.2015.
 */
public interface Search {
    void setupSearch(String searchTerm, ContentIndex contentIndex, int topK, int prf);

    ArrayList<Posting> execute() throws IOException;
}
