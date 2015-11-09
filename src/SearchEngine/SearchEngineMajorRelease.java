package SearchEngine;

/**
 *
 * @author: Your team name
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 *
 * This is your file! implement your search engine here!
 * 
 * Describe your search engine briefly:
 *  - multi-threaded?
 *  - stemming?
 *  - stopword removal?
 *  - index algorithm?
 *  - etc.  
 * 
 * Keep in mind to include your implementation decisions also in the pdf file of each assignment
 */

import SearchEngine.data.BooleanQuery;
import SearchEngine.data.Configuration;
import SearchEngine.data.Document;
import SearchEngine.indexing.Index;
import SearchEngine.indexing.FileIndexer;
import SearchEngine.indexing.ParsedEventListener;
import SearchEngine.utils.WordParser;

import java.io.*;
import java.util.*;

public class SearchEngineMajorRelease extends SearchEngine implements ParsedEventListener { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
    private Index index = new Index();
    private int numRemainingFiles;
    private List<String> files = new LinkedList<>();
    private Long start;
    private int maxThreads = 4;
    private int curFileNum = -1;
    private Thread[] fileThreads;

    public SearchEngineMajorRelease() { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
        // This should stay as is! Don't add anything here!
        super();
    }

    @Override
    void index(String directory){
        start = System.nanoTime();

        try {
            BufferedReader reader = new BufferedReader(new FileReader("data/xmlfiles.txt"));
            String filesString = reader.readLine();
            files = Arrays.asList(filesString.split("[,]"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        numRemainingFiles = files.size();
        fileThreads = new Thread[files.size()];
        WordParser.getInstance().disableErrorOutput();

        for (int i = 0; i < files.size(); ++i) {
            fileThreads[i] = new Thread(new FileIndexer(files.get(i), this));
        }

        for (int i = 0; i < maxThreads && i < files.size(); ++i) {
            ++curFileNum;
            fileThreads[curFileNum].start();
        }

        try {
            for (int i = 0; i < files.size(); ++i) {
                    fileThreads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Join all indices at the end
        index.mergePartialIndices(files);
    }

    @Override
    boolean loadIndex(String directory) {
        // TODO: loadFromFile should throw an IOException via signature
        //index.loadFromFile();

        return true;
    }
    
    @Override
    void compressIndex(String directory) {
        index.compressIndex();
    }

    @Override
    boolean loadCompressedIndex(String directory) {

        return false;
    }

    @Override
    ArrayList<String> search(String query, int topK, int prf) {
        if (Configuration.COMPRESSED) {
            return searchWithCompression(query, topK, prf);
        } else {
            return searchWithoutCompression(query, topK, prf);
        }
    }

    private ArrayList<String> searchWithoutCompression(String query, int topK, int prf) {
        Map<String, List<Long>> searchWords = WordParser.getInstance().stem(query, true);
        WordParser.getInstance().disableErrorOutput();
        ArrayList<String> results = new ArrayList<>();

        for (Map.Entry<String, List<Long>> entry : searchWords.entrySet()) {
            List<Document> documents = index.lookUpPostingInFile(entry.getKey());

            for (Document document: documents) {
                results.add(document.getInventionTitle());
            }
        }

        return results;
    }

    private ArrayList<String> searchWithCompression(String query, int topK, int prf) {
        List<Document> documents;
        ArrayList<String> results;

        List<String> booleanTokens = new LinkedList<>();
        booleanTokens.add("OR");
        booleanTokens.add("NOT");
        booleanTokens.add("AND");

        boolean queryIsBoolean = false;

        for (String queryToken: query.split(" ")) {
            if (booleanTokens.contains(queryToken)) queryIsBoolean = true;
        }

        if (queryIsBoolean) {
            return performBooleanQuery(query);
        } else {
            return performNormalQuery(query);
        }
    }

    private ArrayList<String> performBooleanQuery(String query) {
        BooleanQuery booleanQuery = new BooleanQuery(query, index);

        List<Document> documents = booleanQuery.executeQuery();

        ArrayList<String> results = new ArrayList<>();

        for (Document document: documents) {
            results.add(document.getInventionTitle());
        }
        return results;
    }

    private ArrayList<String> performNormalQuery(String query) {
        WordParser.getInstance().disableErrorOutput();

        if (query.startsWith("\"") && query.endsWith("\"")) {
            return processPhraseQuery(query);
        }

        String strippedQuery = new String();
        List<String> wildcardTokens = new LinkedList<>();

        for (String queryToken: query.split(" ")) {
            if (queryToken.contains("*")) wildcardTokens.add(queryToken);
            else strippedQuery += queryToken + " ";
        }

        Map<String, List<Long>> searchWords = WordParser.getInstance().stem(strippedQuery, true);

        for (String wildcardToken: wildcardTokens) {
            searchWords.put(wildcardToken, null);
        }

        ArrayList<String> results = new ArrayList<>();
        List<Document> documents;

        for (Map.Entry<String, List<Long>> entry : searchWords.entrySet()) {
            documents = index.lookUpPostingInFileWithCompression(entry.getKey());

            for (Document document: documents) {
                results.add(document.getInventionTitle());
            }
        }

        return results;
    }

    private ArrayList<String> processPhraseQuery(String query) {
        Map<Integer, Document> docs = new HashMap<>();
        HashSet<Integer> docIds = new HashSet<>();
        String removedQuotationMarks = query.substring(1, query.length() - 1);
        Set<String> tokens = WordParser.getInstance().stem(removedQuotationMarks, false).keySet();

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

        ArrayList<String> results = new ArrayList<>();

        Iterator<Integer> docIdIterator = docIds.iterator();
        while (docIdIterator.hasNext()) {
            int curDocId = docIdIterator.next();
            Document curDoc = docs.get(curDocId);

            if (curDoc.getInventionTitle().indexOf(removedQuotationMarks) >= 0 ||
                    curDoc.getPatentAbstract().indexOf(removedQuotationMarks) >= 0) {
                results.add(curDoc.getInventionTitle());
            }
        }

        return results;
    }

    //Observer methods
    @Override
    public void documentParsed(Document document) {
        return;
    }

    @Override
    public void finishedParsing() {
        --numRemainingFiles;
        ++curFileNum;

        if (curFileNum < files.size()) {
            fileThreads[curFileNum].start();
        }

        if (numRemainingFiles == 0) {
            WordParser.getInstance().enableErrorOutput();
            System.out.println("Finished parsing!");
            Long end = System.nanoTime();
            System.out.println("Indexing took " + ((end - start)/1000000000) + " seconds.");

        }
    }
}
