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

import SearchEngine.data.Configuration;
import SearchEngine.data.Document;
import SearchEngine.indexing.Index;
import SearchEngine.indexing.FileIndexer;
import SearchEngine.indexing.ParsedEventListener;
import SearchEngine.utils.WordParser;
import edu.stanford.nlp.ling.Word;

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
    }

    @Override
    boolean loadIndex(String directory) {
        // TODO: loadFromFile should throw an IOException via signature
//        index.loadFromFile();

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

        List<String> searchWords = WordParser.getInstance().stem(query);
        WordParser.getInstance().disableErrorOutput();
        WordParser.getInstance().removeStopwords(searchWords);
        ArrayList<String> results = new ArrayList<>();

        for (String word: searchWords) {
            List<Document> documents = index.lookUpPostingInFile(word, "data/postinglist.txt");

            for (Document document: documents) {
                results.add(document.getInventionTitle());
            }
        }

        return results;
    }

    private ArrayList<String> searchWithCompression(String query, int topK, int prf) {

        List<String> searchWords = WordParser.getInstance().stem(query);
        WordParser.getInstance().disableErrorOutput();
        WordParser.getInstance().removeStopwords(searchWords);
        ArrayList<String> results = new ArrayList<>();

        for (String word: searchWords) {
            List<Document> documents = index.lookUpPostingInFileWithCompression(word, "data/compressed_postinglist.txt");

            for (Document document: documents) {
                results.add(document.getInventionTitle());
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
            index.mergePartialIndices(files);
            WordParser.getInstance().enableErrorOutput();
            System.out.println("Finished parsing!");
            Long end = System.nanoTime();
            System.out.println("Indexing took " + ((end - start)/1000000000) + " seconds.");

        }
    }
}
