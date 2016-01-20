package SearchEngine.search;

import SearchEngine.data.Configuration;
import SearchEngine.data.Posting;
import SearchEngine.index.Index;
import SearchEngine.utils.WordParser;

import java.io.IOException;
import java.util.*;

/**
 * Created by sebastian on 10.11.2015.
 */
public class VectorSpaceSearch implements Search {
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
    public ArrayList<Posting> execute() throws IOException {
        if (searchTerm.startsWith("\"") && searchTerm.endsWith("\"")) {
            return processPhraseQuery();
        } else {
            return processSimpleQuery();
        }
    }

    private ArrayList<Posting> processSimpleQuery() throws IOException {
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

        computeQueryVector(searchWords.toArray(new String[searchWords.size()]));

        ArrayList<Posting> postings = new ArrayList<>();
        double docWeightSum = 0;
        for (String searchWord: queryVector.keySet()) {
            List<Posting> tmpDocList = index.lookUpPostingInFile(searchWord);
            // Index does not contain the word
            if (tmpDocList.size() == 0) continue;

            postings.addAll(tmpDocList);
            int numDocs = index.getNumDocuments();

            Double docVector = (1 + Math.log10(queryVector.get(searchWord)) * Math.log10(numDocs / tmpDocList.size()));
            queryVector.put(searchWord, docVector);
            docWeightSum += docVector*docVector;
        }

        // Normalize query weights
        docWeightSum = Math.sqrt(docWeightSum);
        for (String key: queryVector.keySet()) {
            queryVector.put(key, queryVector.get(key)/docWeightSum);
        }

        postings = rankResults(postings);

        return postings;
    }

    private ArrayList<Posting> processPhraseQuery() throws IOException {
        Map<Integer, List<Posting>> docs = new HashMap<>();
        String removedQuotationMarks = searchTerm.substring(1, searchTerm.length() - 1);
        String[] stemmedQuery = WordParser.getInstance().stemToString(removedQuotationMarks, Configuration.FILTER_STOPWORDS_IN_PHRASES).split(" ");

        computeQueryVector(stemmedQuery);
        int numDocs = index.getNumDocuments();
        double docWeightSum = 0;
        // Initialize HashSet with first documents
        String searchWord = stemmedQuery[0];
        List<Posting> tmpPostingList = index.lookUpPostingInFile(searchWord);
        for (Posting posting: tmpPostingList) {
            LinkedList<Posting> tmpList = new LinkedList<>();
            tmpList.add(posting);
            docs.put(posting.getDocId(), tmpList);

            Double docVector = (1 + Math.log10(queryVector.get(searchWord)) * Math.log10(numDocs / tmpPostingList.size()));
            queryVector.put(searchWord, docVector);
            docWeightSum += docVector * docVector;
        }

        for (int i = 1; i < stemmedQuery.length; ++i) {
            searchWord = stemmedQuery[i];
            tmpPostingList = index.lookUpPostingInFile(searchWord);

            for (Posting doc: tmpPostingList) {
                if (docs.containsKey(doc.getDocId())) {
                    List<Posting> tmpList = docs.get(doc.getDocId());
                    tmpList.add(doc);
                    docs.put(doc.getDocId(), tmpList);
                }

                Double docVector = (1 + Math.log10(queryVector.get(searchWord)) * Math.log10(numDocs / tmpPostingList.size()));
                queryVector.put(searchWord, docVector);
                docWeightSum += docVector * docVector;
            }
        }

        // Normalize query weights
        docWeightSum = Math.sqrt(docWeightSum);
        for (String key: queryVector.keySet()) {
            queryVector.put(key, queryVector.get(key)/docWeightSum);
        }

        ArrayList<Posting> results = new ArrayList<>();
        for (Map.Entry<Integer, List<Posting>> postingsPerDocId: docs.entrySet()) {
            List<Posting> postings = postingsPerDocId.getValue();
            if (postings.size() == stemmedQuery.length) {
                for (long occurrence: postings.get(0).getOccurrences()) {
                    boolean isMatch = true;
                    for (int i = 1; i < postings.size(); ++i) {
                        if (!postings.get(i).getOccurrences().contains(occurrence + i)) {
                            isMatch = false;
                        }
                    }

                    if (isMatch) {
                        results.addAll(postings);
                        break;
                    }
                }
            }
        }

        return rankResults(results);
    }

    private void computeQueryVector(String[] searchWords) {
        for (String searchWord: searchWords) {
            double wordCount;
            if (queryVector.get(searchWord) == null) {
                wordCount = 0.0;
            } else {
                wordCount = queryVector.get(searchWord);
            }

            queryVector.put(searchWord.toLowerCase(),wordCount+1);
        }
    }

    private ArrayList<Posting> rankResults(ArrayList<Posting> postings) {
        Map<Integer, Posting> postingTable = new HashMap<>();

        // Initialize data structure
        for (Posting posting: postings) {
            int docId = posting.getDocId();
            postingTable.put(docId, posting);
        }

        // Compute cosine similarities
        Map<Integer, Double> rankings = new HashMap<>();
        for (int i = 0; i < postings.size(); ++i) {
            double weight = postings.get(i).getWeight();
            double ranking = weight * queryVector.get(postings.get(i).getToken());

            if (rankings.containsKey(postings.get(i).getDocId())) {
                rankings.put(postings.get(i).getDocId(), ranking + rankings.get(postings.get(i).getDocId()));
            } else {
                rankings.put(postings.get(i).getDocId(), ranking);
            }
        }

        List<HashMap.Entry<Integer, Double>> tmpList = new ArrayList<>(rankings.entrySet());
        Collections.sort(tmpList, (obj1, obj2) -> ((Comparable) ((obj2)).getValue()).compareTo(((obj1)).getValue()));
        ArrayList<Posting> result = new ArrayList<>();

        for (int i = 0; i < tmpList.size(); ++i) {
            int docId = tmpList.get(i).getKey();
            result.add(postingTable.get(docId));
        }

        return result;
    }
}
