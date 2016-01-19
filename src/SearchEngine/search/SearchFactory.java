package SearchEngine.search;

import SearchEngine.index.Index;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sebastian on 10.11.2015.
 */
public class SearchFactory {
    private Index index;

    public void setIndex(Index index) {
        this.index = index;
    }

    public Search getSearchFromQuery(String query, int topK) {
        List<String> booleanTokens = new LinkedList<>();
        booleanTokens.add("OR");
        booleanTokens.add("NOT");
        booleanTokens.add("AND");

        boolean queryIsBoolean = false;

        for (String queryToken: query.split(" ")) {
            if (booleanTokens.contains(queryToken)) queryIsBoolean = true;
        }

        Search search;

        if (queryIsBoolean) {
            search = new BooleanSearch();
        //} else if (prf > 0) {
        //    search = new PRFSearch();
        } else {
            search = new VectorSpaceSearch();
        }

        search.setupSearch(query, index, topK, 0);

        return search;
    }
}
