package SearchEngine.search;

import SearchEngine.data.Configuration;
import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.data.Posting;
import SearchEngine.index.Index;
import SearchEngine.utils.WordParser;

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

        ArrayList<Posting> postings = new ArrayList<>();
        double docWeightSum = 0;
        for (String searchWord: queryVector.keySet()) {
            List<Posting> tmpDocList = index.lookUpPostingInFileWithCompression(searchWord);
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

        postings = rankResults(postings, searchWords);

        ArrayList<Document> result = new ArrayList<>();
        for (int i = 0; i < topK; ++i) {
            result.add(new Document(postings.get(i)));
        }

        return result;
    }

    private ArrayList<Document> processPhraseQuery() {
        Map<Integer, List<Posting>> docs = new HashMap<>();
        String removedQuotationMarks = searchTerm.substring(1, searchTerm.length() - 1);
        Set<String> tokens = WordParser.getInstance().stem(removedQuotationMarks, Configuration.FILTER_STOPWORDS_IN_PHRASES).keySet();
        String stemmedQuery = WordParser.getInstance().stemToString(removedQuotationMarks, Configuration.FILTER_STOPWORDS_IN_PHRASES);

        computeQueryVector(new LinkedList<>(tokens));
        int numDocs = index.getNumDocuments();
        double docWeightSum = 0;
        Iterator<String> tokenIterator = tokens.iterator();
        // Initialize HashSet with first documents
        String searchWord = tokenIterator.next();
        List<Posting> tmpDocList = index.lookUpPostingInFileWithCompression(searchWord);
        for (Posting doc: tmpDocList) {
            LinkedList<Posting> tmpList = new LinkedList<>();
            tmpList.add(doc);
            docs.put(doc.getDocId(), tmpList);

            Double docVector = (1 + Math.log10(queryVector.get(searchWord)) * Math.log10(numDocs / tmpDocList.size()));
            queryVector.put(searchWord, docVector);
            docWeightSum += docVector * docVector;
        }

        while (tokenIterator.hasNext()) {
            searchWord = tokenIterator.next();
            tmpDocList = index.lookUpPostingInFileWithCompression(searchWord);

            for (Posting doc: tmpDocList) {
                if (docs.containsKey(doc.getDocId())) {
                    List<Posting> tmpList = docs.get(doc.getDocId());
                    tmpList.add(doc);
                    docs.put(doc.getDocId(), tmpList);
                }

                Double docVector = (1 + Math.log10(queryVector.get(searchWord)) * Math.log10(numDocs / tmpDocList.size()));
                queryVector.put(searchWord, docVector);
                docWeightSum += docVector * docVector;
            }
        }

        // Normalize query weights
        docWeightSum = Math.sqrt(docWeightSum);
        for (String key: queryVector.keySet()) {
            queryVector.put(key, queryVector.get(key)/docWeightSum);
        }


        ArrayList<Document> result = new ArrayList<>();
        ArrayList<Posting> postings = new ArrayList<>();
        docs.values().forEach(postings::addAll);
        postings = rankResults(postings, new LinkedList<>(tokens));
        Document doc;

        for (Posting posting: postings) {
            doc = new Document(posting);
            String stemmedTitle = WordParser.getInstance().stemToString(doc.getInventionTitle(), Configuration.FILTER_STOPWORDS_IN_PHRASES);
            String stemmedAbstract = WordParser.getInstance().stemToString(doc.getPatentAbstract(), Configuration.FILTER_STOPWORDS_IN_PHRASES);

            if (stemmedTitle.indexOf(stemmedQuery) >= 0 ||
                    stemmedAbstract.indexOf(stemmedQuery) >= 0) {
                result.add(doc);
            }
        }

        return result;
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

    private ArrayList<Posting> rankResults(ArrayList<Posting> postings, List<String> searchWords) {
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
        Collections.sort(tmpList, (obj1, obj2) -> ((Comparable) ((obj1)).getValue()).compareTo(((obj2)).getValue()));
        Collections.reverse(tmpList);
        ArrayList<Posting> result = new ArrayList<>();

        for (int i = 0; i < topK && i < tmpList.size(); ++i) {
            int docId = tmpList.get(i).getKey();
            result.add(postingTable.get(docId));
        }

        return result;
    }

}
