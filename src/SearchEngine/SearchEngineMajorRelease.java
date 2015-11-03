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

    public SearchEngineMajorRelease() { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
        // This should stay as is! Don't add anything here!
        super();
    }

    @Override
    void index(String directory) {
        start = System.nanoTime();

        try {
            BufferedReader reader = new BufferedReader(new FileReader("data/xmlfiles.txt"));
            String filesString = reader.readLine();
            files = Arrays.asList(filesString.split("[,]"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        numRemainingFiles = files.size();
        WordParser.getInstance().disableErrorOutput();

        for (int i = 0; i < maxThreads && i < files.size(); ++i) {
            ++curFileNum;
            new Thread(new FileIndexer(files.get(i), this)).start();
        }
    }

    @Override
    boolean loadIndex(String directory) {
        try {
            index.loadFromFile(new BufferedReader(new FileReader("data/index.txt")));
//            index.printIndex();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    void compressIndex(String directory) {
        index.compressIndex();
        // for testing purposes
        index.decompressLine();
    }

    @Override
    boolean loadCompressedIndex(String directory) {

        return false;
    }
    
    @Override
    ArrayList<String> search(String query, int topK, int prf) {
        List<String> searchWords = WordParser.getInstance().stem(query);
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


    //Observer methods
    @Override
    public void documentParsed(Document document) {
        return;
    }

    @Override
    public void finishedParsing() {
        --numRemainingFiles;

        if (curFileNum < files.size()) {
            ++curFileNum;
            new Thread(new FileIndexer(files.get(curFileNum), this)).start();
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
