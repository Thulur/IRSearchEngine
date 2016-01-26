package SearchEngine.search;

import SearchEngine.index.CitationIndex;
import SearchEngine.utils.NumberParser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sebastian on 26.01.2016.
 */
public class CitationSearch {
    private CitationIndex citationIndex;
    private String query;

    public CitationSearch(String query, CitationIndex citationIndex) {
        this.citationIndex = citationIndex;
        this.query = query;
    }

    public List<Integer> execute() {
        List<Integer> results = new LinkedList<>();
        query = query.replace("LinkTo:", "");

        try {
            if (query.contains(" AND ")) {
                String[] docIds = query.split(" AND ");

                List<Integer> firstId = citationIndex.lookUpCitationsInFile(NumberParser.parseDecimalInt(docIds[0]));
                List<Integer> secondId = citationIndex.lookUpCitationsInFile(NumberParser.parseDecimalInt(docIds[1]));

                for (Integer docId: firstId) {
                    if (secondId.contains(docId)) {
                        results.add(docId);
                    }
                }
            } else {
                results = citationIndex.lookUpCitationsInFile(NumberParser.parseDecimalInt(query));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }
}
