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
import SearchEngine.data.Posting;
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
    void index(){
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
    boolean loadIndex() {
        return loadCompressedIndex();
    }
    
    @Override
    void compressIndex() {
        index.compressIndex();
        searchFactory = new SearchFactory();
        searchFactory.setIndex(index);
    }

    @Override
    boolean loadCompressedIndex() {
        try {
            index.loadFromFile(FilePaths.COMPRESSED_INDEX_PATH, FilePaths.DOCINDEX_FILE);
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
    ArrayList<String> search(String query, int topK) {
        ArrayList<String> results = executeSearch(query, topK);

        if (results.size() == 0 && Configuration.ENABLE_SPELLING_CORRECTION) {
            StringBuilder correctedQuery = new StringBuilder();
            for (String queryWord: query.split(" ")) {
                correctedQuery.append(SpellingCorrector.getInstance().correctSpelling(queryWord));
                correctedQuery.append(" ");
            }

            results = executeSearch(correctedQuery.toString(), topK);
        }

        return results;
    }

    private ArrayList<String> executeSearch(String query, int topK) {
        ArrayList<Posting> postings;
        ArrayList<Document> documents = new ArrayList<>();

        try {
            postings = searchFactory.getSearchFromQuery(query, topK).execute();

            for (int i = 0; i < topK && i < postings.size(); ++i) {
                documents.add(index.buildDocument(postings.get(i)));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> tmpResults = new ArrayList<>();

        for (Document document: documents) {
            tmpResults.add(String.valueOf(document.getDocId()));
        }

        WebFile webFile = new WebFile();

        ArrayList<String> goldRanking = webFile.getGoogleRanking(query);
        computeNdcgList(goldRanking, tmpResults);

        ArrayList<String> results = new ArrayList<>();

        // topK should be used in the Search class not here
        for (int i = 0; i < topK && i < documents.size(); ++i) {
            if (documents.get(i) != null) {
                results.add(documents.get(i).generateSnippet(query, ndcg.get(i), outputFormat));
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
        ArrayList<Double> goldRelevances = new ArrayList<>();

        for (int i = 0; i < goldRanking.size(); ++i) {
            goldRelevances.add(1 + Math.floor(10 * Math.pow(0.5, i * 0.1)));
        }

        for (int i = 0; i < results.size(); ++i) {
            if (i == 0) {
                idealDcg.add(goldRelevances.get(i));
            } else {
                double ranking = i < goldRelevances.size() ? goldRelevances.get(i) : 0;
                idealDcg.add(idealDcg.get(i-1) + (ranking/(Math.log(i+1)/Math.log(2))));
            }


            double summand = 0.0;
            if (goldRanking.contains(results.get(i))) {
                summand = goldRelevances.get(goldRanking.indexOf(results.get(i)));
            }
            if (i == 0) {
                actualDcg.add(summand);
            } else {
                actualDcg.add(actualDcg.get(i-1) + (summand/(Math.log(i+1)/Math.log(2))));
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
