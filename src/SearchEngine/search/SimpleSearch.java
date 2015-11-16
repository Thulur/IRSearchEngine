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
    private Map<String, Double> queryVector;
    private int topK;

    @Override
    public void setupSearch(String searchTerm, Index index, int topK, int prf) {
        this.searchTerm = searchTerm;
        this.index = index;
        this.queryVector = new HashMap<>();
        this.topK = topK;
    }

    @Override
    public ArrayList<Document> execute() {
        ArrayList<Document> documents;
        ArrayList<Document> result = new ArrayList<>();

        if (searchTerm.startsWith("\"") && searchTerm.endsWith("\"")) {
            documents = processPhraseQuery();
        } else {
            documents = processSimpleQuery();
        }

        Document doc;
        for (int i = 0; i < topK && i < documents.size(); ++i) {
            doc = documents.get(i);
            doc.loadPatentData();
            result.add(doc);
        }

        return result;
    }

    private ArrayList<Document> processSimpleQuery() {
        List<String> wildcardTokens = new LinkedList<>();

        List<String> searchWords = new LinkedList<>();
        // TODO: According to the exercise sheet 5 wildcards are part of boolean queries
        for (String queryToken: searchTerm.split("[\\s.,!?:;]")) {
            if (queryToken.contains("*")) wildcardTokens.add(queryToken);
            else searchWords.add(WordParser.getInstance().stemSingleWord(queryToken));
        }

        for (String wildcardToken: wildcardTokens) {
            searchWords.add(wildcardToken);
        }

        computeQueryVector(searchWords);

        ArrayList<Document> documents = new ArrayList<>();
        double docWeightSum = 0;
        for (String searchWord: queryVector.keySet()) {
            List<Document> tmpDocList = index.lookUpPostingInFileWithCompression(searchWord);
            documents.addAll(tmpDocList);

            Double docVector = (1 + Math.log10(queryVector.get(searchWord)) * Math.log10(index.getNumDocuments() / tmpDocList.size()));
            queryVector.put(searchWord, docVector);
            docWeightSum += docVector*docVector;
        }

        // Normalize query weights
        docWeightSum = Math.sqrt(docWeightSum);
        for (String key: queryVector.keySet()) {
            queryVector.put(key, queryVector.get(key)/docWeightSum);
        }

        documents = rankResults(documents, searchWords);

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

    private void computeQueryVector(List<String> searchWords) {
        for (String searchWord: searchWords) {
            double wordCount;
            if (queryVector.get(searchWord) == null) {
                wordCount = 0.0;
            } else {
                wordCount = queryVector.get(searchWord);
            }

            queryVector.put(searchWord,wordCount+1);
        }
    }

    private ArrayList<Document> rankResults(ArrayList<Document> documents, List<String> searchWords) {
        Map<Integer, Document> documentTable = new HashMap<>();

        // Initialize data structure
        for (Document document: documents) {
            int docId = document.getDocId();
            documentTable.put(docId, document);
        }

        // Compute cosine similarities
        Map<Integer, Double> rankings = new HashMap<>();
        for (int i = 0; i < documents.size(); ++i) {
            double weight = documents.get(i).getWeight();
            double ranking = weight * queryVector.get(documents.get(i).getToken());

            if (rankings.containsKey(documents.get(i).getDocId())) {
                rankings.put(documents.get(i).getDocId(), ranking + rankings.get(documents.get(i).getDocId()));
            } else {
                rankings.put(documents.get(i).getDocId(), ranking);
            }
        }

        List<HashMap.Entry<Integer, Double>> tmpList = new ArrayList<>(rankings.entrySet());
        Collections.sort(tmpList, (obj1, obj2) -> ((Comparable) ((obj1)).getValue()).compareTo(((obj2)).getValue()));
        Collections.reverse(tmpList);
        ArrayList<Document> result = new ArrayList<>();

        for (int i = 0; i < topK && i < tmpList.size(); ++i) {
            int docId = tmpList.get(i).getKey();
            result.add(documentTable.get(docId));
        }

        return result;
    }

}
