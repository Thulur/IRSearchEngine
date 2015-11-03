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

import java.io.*;
import java.util.*;

public class SearchEngineMajorRelease extends SearchEngine implements ParsedEventListener { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
    private Index index = new Index();
    private int numFiles;

    public SearchEngineMajorRelease() { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
        // This should stay as is! Don't add anything here!
        super();
    }

    @Override
    void index(String directory) {
        numFiles = 1;
        new FileIndexer("data/testData.xml", this);
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
        --numFiles;

        if (numFiles == 0) {
            System.out.println("Finished parsing!");
        }
    }
}
