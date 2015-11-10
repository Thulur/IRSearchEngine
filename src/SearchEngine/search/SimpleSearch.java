package SearchEngine.search;

import SearchEngine.data.Configuration;
import SearchEngine.data.Document;
import SearchEngine.index.Index;
import SearchEngine.utils.WordParser;

import java.util.*;

/**
 * Created by sebastian on 10.11.2015.
 */
public class SimpleSearch implements Search {
    private String searchTerm;
    private Index index;

    @Override
    public void setupSearch(String searchTerm, Index index, int topK, int prf) {
        this.searchTerm = searchTerm;
        this.index = index;
    }

    @Override
    public ArrayList<Document> execute() {
        WordParser.getInstance().disableErrorOutput();

        ArrayList<Document> documents;

        if (searchTerm.startsWith("\"") && searchTerm.endsWith("\"")) {
            documents = processPhraseQuery();
        } else {
            documents = processSimpleQuery();
        }

        return documents;
    }

    private ArrayList<Document> processSimpleQuery() {
        String strippedQuery = new String();
        List<String> wildcardTokens = new LinkedList<>();

        // TODO: According to the exercise sheet 5 wildcards are part of boolean queries
        for (String queryToken: searchTerm.split(" ")) {
            if (queryToken.contains("*")) wildcardTokens.add(queryToken);
            else strippedQuery += queryToken + " ";
        }

        Map<String, List<Long>> searchWords = WordParser.getInstance().stem(strippedQuery, true);

        for (String wildcardToken: wildcardTokens) {
            searchWords.put(wildcardToken, null);
        }

        ArrayList<Document> documents = new ArrayList<>();

        for (Map.Entry<String, List<Long>> entry : searchWords.entrySet()) {
            documents.addAll(index.lookUpPostingInFileWithCompression(entry.getKey()));
        }

        return documents;
    }

    private ArrayList<Document> processPhraseQuery() {
        Map<Integer, Document> docs = new HashMap<>();
        HashSet<Integer> docIds = new HashSet<>();
        String removedQuotationMarks = searchTerm.substring(1, searchTerm.length() - 1);
        Set<String> tokens = WordParser.getInstance().stem(removedQuotationMarks, Configuration.FILTER_STOPWORDS_IN_PHRASES).keySet();
        String stemmedQuery = WordParser.getInstance().stemToString(removedQuotationMarks, Configuration.FILTER_STOPWORDS_IN_PHRASES);

        Iterator<String> tokenIterator = tokens.iterator();
        // Initialize HashSet with first documents
        for (Document doc: index.lookUpPostingInFileWithCompression(tokenIterator.next())) {
            docs.put(doc.getDocId(), doc);
            docIds.add(doc.getDocId());
        }

        while (tokenIterator.hasNext()) {
            HashSet<Integer> newDocIds = new HashSet<>();

            for (Document doc: index.lookUpPostingInFileWithCompression(tokenIterator.next())) {
                docs.put(doc.getDocId(), doc);
                newDocIds.add(doc.getDocId());
            }

            docIds.retainAll(newDocIds);
        }

        ArrayList<Document> documents = new ArrayList<>();

        // TODO: Introduce all possible results for phrase queries (the exercise sheet one is quite inaccurate,
        //      if a particular query is needed in a document it should not be stemmed at all, also stop words should stay in the query)
        Iterator<Integer> docIdIterator = docIds.iterator();
        while (docIdIterator.hasNext()) {
            int curDocId = docIdIterator.next();
            Document curDoc = docs.get(curDocId);
            String stemmedTitle = WordParser.getInstance().stemToString(curDoc.getInventionTitle(), Configuration.FILTER_STOPWORDS_IN_PHRASES);
            String stemmedAbstract = WordParser.getInstance().stemToString(curDoc.getPatentAbstract(), Configuration.FILTER_STOPWORDS_IN_PHRASES);

            if (stemmedTitle.indexOf(stemmedQuery) >= 0 ||
                    stemmedAbstract.indexOf(stemmedQuery) >= 0) {
                documents.add(curDoc);
            }
        }

        return documents;
    }
}
