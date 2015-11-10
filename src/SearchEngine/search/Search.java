package SearchEngine.search;

import SearchEngine.data.Document;

import java.util.ArrayList;

/**
 * Created by sebastian on 10.11.2015.
 */
public interface Search {
    ArrayList<Document> execute();
}
