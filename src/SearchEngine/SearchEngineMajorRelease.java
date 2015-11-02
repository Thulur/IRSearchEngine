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
import SearchEngine.data.Index;
import SearchEngine.utils.WordParser;

import java.io.*;
import java.util.*;


public class SearchEngineMajorRelease extends SearchEngine implements ParsedEventListener { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
    private XMLParser saxApp = new XMLParser();
    private Index index = new Index();
    private LineNumberReader lnr;

    public SearchEngineMajorRelease() { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
        // This should stay as is! Don't add anything here!
        super();
    }

    @Override
    void index(String directory) {
        saxApp.addDocumentParsedListener(this);
        
        List<String> files = new LinkedList<>();
        files.add("data/testData.xml");
        
        try {
            saxApp.parseFiles(files);
        } catch (Exception e) {
            e.printStackTrace();
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
        processDocument(document);
    }

    public void finishedParsing() {
        System.out.println("Finished parsing!");
        index.save();
    }

    // title?

    private void processDocument(Document document) {
        index.addToIndex(document);
    }
}
