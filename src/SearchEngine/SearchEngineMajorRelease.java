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
import SearchEngine.data.FilePaths;
import SearchEngine.index.FileIndexer;
import SearchEngine.index.Index;
import SearchEngine.index.ParsedEventListener;
import SearchEngine.search.SearchFactory;
import SearchEngine.utils.WordParser;

import java.io.*;
import java.util.*;

public class SearchEngineMajorRelease extends SearchEngine implements ParsedEventListener { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
    private Index index = new Index();
    private int numRemainingFiles;
    private List<String> files = new LinkedList<>();
    private Long start;
    private int maxThreads = 1;
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
        start = System.nanoTime();
        BufferedWriter docIdFile;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("data/xmlfiles.txt"));
            String filesString = reader.readLine();
            files = Arrays.asList(filesString.split("[,]"));
            numRemainingFiles = files.size();
            fileIndexers = new FileIndexer[files.size()];
            fileThreads = new Thread[files.size()];

            docIdFile = new BufferedWriter(new FileWriter(FilePaths.DOC_IDS_FILE));

            for (int i = 0; i < files.size(); ++i) {
                fileIndexers[i] = new FileIndexer(files.get(i), i, this);
                fileThreads[i] = new Thread(fileIndexers[i]);
                String fileId = "";
                String filename = files.get(i);

                if (filename.indexOf("ipg") >= 0) {
                    fileId = filename.substring(filename.indexOf("ipg") + 3, filename.indexOf("ipg") + 9);
                }
                docIdFile.write(i + " cache" + fileId + ".txt" + "\n");
            }

            docIdFile.close();
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
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Join all indices at the end
        index.mergePartialIndices(files, numPatents);
    }

    @Override
    boolean loadIndex(String directory) {
        index.loadFromFile(FilePaths.INDEX_PATH);
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
        index.loadFromFile(FilePaths.COMPRESSED_INDEX_PATH);
        searchFactory = new SearchFactory();
        searchFactory.setIndex(index);
        return true;
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
        // TODO: Update the code to use current implementations
        Map<String, List<Long>> searchWords = WordParser.getInstance().stem(query, true);
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
        ArrayList<Document> documents = searchFactory.getSearchFromQuery(query, topK, prf).execute();
        ArrayList<String> results = new ArrayList<>();

        for (Document document: documents) {
            results.add(document.getDocId() + "------" + document.getInventionTitle());
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
            System.out.println("Finished parsing!");
            Long end = System.nanoTime();
            System.out.println("Indexing took " + ((end - start)/1000000000) + " seconds.");
        }
    }
}
