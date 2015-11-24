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

// To activate the FUN MODE set VM options: -Djava.compiler=NONE

import SearchEngine.data.Configuration;
import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.index.FileIndexer;
import SearchEngine.index.Index;
import SearchEngine.index.ParsedEventListener;
import SearchEngine.search.SearchFactory;
import SearchEngine.utils.SpellingCorrector;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SearchEngineMajorRelease extends SearchEngine implements ParsedEventListener { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
    private Index index = new Index();
    private List<String> files = new LinkedList<>();
    private int maxThreads = 2;
    private int curFileNum = -1;
    private Thread[] fileThreads;
    private FileIndexer[] fileIndexers;
    private SearchFactory searchFactory;
    private int numPatents = 0;

    public SearchEngineMajorRelease() { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
        // This should stay as is! Don't add anything here!
        super();
    }

    @Override
    void index(String directory){
        BufferedWriter fileIdFile;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("data/xmlfiles.txt"));
            String filesString = reader.readLine();
            files = Arrays.asList(filesString.split("[,]"));
            fileIndexers = new FileIndexer[files.size()];
            fileThreads = new Thread[files.size()];

            fileIdFile = new BufferedWriter(new FileWriter(FilePaths.FILE_IDS_FILE));

            for (int i = 0; i < files.size(); ++i) {
                fileIndexers[i] = new FileIndexer(files.get(i), i, this);
                fileThreads[i] = new Thread(fileIndexers[i]);

                fileIdFile.write(i + " " + files.get(i) + "\n");
            }

            fileIdFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < maxThreads && i < files.size(); ++i) {
            ++curFileNum;
            fileThreads[curFileNum].start();
        }

        try {
            for (int i = 0; i < files.size(); ++i) {
                fileThreads[i].join();
                numPatents += fileIndexers[i].getNumPatents();
                fileThreads[i] = null;
                fileIndexers[i] = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Join all indices at the end
        index.mergePartialIndices(files, numPatents);
    }

    @Override
    boolean loadIndex(String directory) {
        try {
            index.loadFromFile(FilePaths.INDEX_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        searchFactory = new SearchFactory();
        searchFactory.setIndex(index);

        return true;
    }
    
    @Override
    void compressIndex(String directory) {
        index.compressIndex();
        searchFactory = new SearchFactory();
        searchFactory.setIndex(index);
    }

    @Override
    boolean loadCompressedIndex(String directory) {
        try {
            index.loadFromFile(FilePaths.COMPRESSED_INDEX_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Configuration.ENABLE_SPELLING_CORRECTION) {
            SpellingCorrector.setup();
        }

        searchFactory = new SearchFactory();
        searchFactory.setIndex(index);
        return true;
    }

    @Override
    ArrayList<String> search(String query, int topK, int prf) {
        ArrayList<String> results = searchWithCompression(query, topK, prf);

        if (results.size() == 0 && Configuration.ENABLE_SPELLING_CORRECTION) {
            StringBuilder correctedQuery = new StringBuilder();
            for (String queryWord: query.split(" ")) {
                correctedQuery.append(SpellingCorrector.getInstance().correctSpelling(queryWord));
                correctedQuery.append(" ");
            }

            results = searchWithCompression(correctedQuery.toString(), topK, prf);
        }

        return results;
    }

    private ArrayList<String> searchWithCompression(String query, int topK, int prf) {
        ArrayList<Document> documents = searchFactory.getSearchFromQuery(query, topK, prf).execute();
        ArrayList<String> results = new ArrayList<>();

        // topK should be used in the Search class not here
        for (int i = 0; i < topK && i < documents.size(); ++i) {
            if (documents.get(i) != null) {
                results.add("0" + documents.get(i).getDocId() + " " + documents.get(i).getInventionTitle());
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
        ++curFileNum;

        if (curFileNum < files.size()) {
            fileThreads[curFileNum].start();
        }
    }
}
