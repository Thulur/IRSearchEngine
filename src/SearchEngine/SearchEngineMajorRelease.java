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
import SearchEngine.data.output.AnsiEscapeFormat;
import SearchEngine.data.output.HTMLFormat;
import SearchEngine.data.output.OutputFormat;
import SearchEngine.index.FileIndexer;
import SearchEngine.index.Index;
import SearchEngine.index.parse.ParsedEventListener;
import SearchEngine.search.SearchFactory;
import SearchEngine.utils.SpellingCorrector;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class SearchEngineMajorRelease extends SearchEngine implements ParsedEventListener { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
    private Index index = new Index();
    private List<String> files = new LinkedList<>();
    private int maxThreads = 4;
    private int curFileNum = 0;
    private SearchFactory searchFactory;
    private int numPatents = 0;
    private OutputFormat outputFormat;
    private ArrayList<Double> ndcg;

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

            fileIdFile = new BufferedWriter(new FileWriter(FilePaths.FILE_IDS_FILE));

            for (int i = 0; i < files.size(); ++i) {
                fileIdFile.write(i + " " + files.get(i) + "\n");
            }

            fileIdFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        Set<Future<Integer>> result = new HashSet<>();
        while (!executor.isShutdown() && curFileNum < files.size()) {
            Callable<Integer> fileIndexer = new FileIndexer(files.get(curFileNum), curFileNum, this);
            result.add(executor.submit(fileIndexer));
            ++curFileNum;
        }


        try {
            for (Future<Integer> future : result) {
                numPatents += future.get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        executor.shutdown();

        // Join all indices at the end
        index.mergePartialIndices(files, numPatents);

        if (Configuration.ENABLE_SPELLING_CORRECTION) {
            SpellingCorrector.setup();
            try {
                SpellingCorrector.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    boolean loadIndex(String directory) {
        return loadCompressedIndex(directory);
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
            try {
                SpellingCorrector.load();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (Configuration.EXPORT_FORMAT) {
            outputFormat = new HTMLFormat();
        } else {
            outputFormat = new AnsiEscapeFormat();
        }

        searchFactory = new SearchFactory();
        searchFactory.setIndex(index);
        return true;
    }

    @Override
    ArrayList<String> search(String query, int topK, int prf) {
        ArrayList<String> results = executeSearch(query, topK, prf);

        if (results.size() == 0 && Configuration.ENABLE_SPELLING_CORRECTION) {
            StringBuilder correctedQuery = new StringBuilder();
            for (String queryWord: query.split(" ")) {
                correctedQuery.append(SpellingCorrector.getInstance().correctSpelling(queryWord));
                correctedQuery.append(" ");
            }

            results = executeSearch(correctedQuery.toString(), topK, prf);
        }

        return results;
    }

    private ArrayList<String> executeSearch(String query, int topK, int prf) {
        ArrayList<Document> documents = null;
        try {
            documents = searchFactory.getSearchFromQuery(query, topK, prf).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*ArrayList<String> tmpResults = new ArrayList<>();

        for (Document document: documents) {
            tmpResults.add(document.getInventionTitle());
        }

        WebFile webFile = new WebFile();

        ArrayList<String> goldRanking = webFile.getGoogleRanking(query);
        computeNdcgList(goldRanking, tmpResults);*/

        ArrayList<String> results = new ArrayList<>();

        // topK should be used in the Search class not here
        for (int i = 0; i < topK && i < documents.size(); ++i) {
            if (documents.get(i) != null) {
                results.add(documents.get(i).generateSnippet(query, outputFormat)/* + "\nNDCG Value: " + ndcg.get(i)*/ + "\n");
            }
        }

        return results;
    }

    @Override
    Double computeNdcg(ArrayList<String> goldRanking, ArrayList<String> ranking, int p) {
        return ndcg.get(p);
    }

    private void computeNdcgList(ArrayList<String> goldRanking, ArrayList<String> results) {
        ArrayList<Double> actualDcg = new ArrayList<>();
        ArrayList<Double> idealDcg = new ArrayList<>();

        for (int i = 0; i < results.size(); ++i) {
            if (i == 0) {
                idealDcg.add((1 + Math.floor(10 * Math.pow(0.5, i))));
            } else {
                idealDcg.add(idealDcg.get(i-1) + (1 + Math.floor(10 * Math.pow(0.5, i))));
            }


            double summand = 0.0;
            if (goldRanking.contains(results.get(i))) {
                System.out.println("Containment found");
                summand = 1 + Math.floor(10 * Math.pow(0.5, i * 0.1));
            }
            if (i == 0) {
                actualDcg.add(summand);
            } else {
                actualDcg.add(actualDcg.get(i-1) + summand);
            }

        }

        ndcg = new ArrayList<>();

        for (int i = 0; i < actualDcg.size(); ++i) {
            ndcg.add(actualDcg.get(i) / idealDcg.get(i));
        }
    }



    //Observer methods
    @Override
    public void documentParsed(Document document) {
        return;
    }

    @Override
    public void finishedParsing() {

    }
}
