package SearchEngine.search;

import SearchEngine.index.ContentIndex;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sebastian on 10.11.2015.
 */
public class SearchFactory {
    private ContentIndex contentIndex;

    public void setContentIndex(ContentIndex contentIndex) {
        this.contentIndex = contentIndex;
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

        int prf = 0;
        Pattern prfPattern = Pattern.compile(".*[#]\\d+");
        Matcher prfMatcher = prfPattern.matcher(query);
        if (prfMatcher.matches()) {
            prf = Integer.parseInt(query.substring(query.indexOf("#") + 1, query.length()));
        }

        if (queryIsBoolean) {
            search = new BooleanSearch();
        } else if (prf > 0) {
            search = new PRFSearch();
        } else {
            search = new VectorSpaceSearch();
        }

        search.setupSearch(query, contentIndex, topK, 0);

        return search;
    }
}
