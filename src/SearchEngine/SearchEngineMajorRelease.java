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
import SearchEngine.index.CitationIndex;
import SearchEngine.index.CitationIndexer;
import SearchEngine.index.ContentIndex;
import SearchEngine.index.ContentIndexer;
import SearchEngine.index.parse.ParsedEventListener;
import SearchEngine.search.CitationSearch;
import SearchEngine.search.SearchFactory;
import SearchEngine.utils.SpellingCorrector;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class SearchEngineMajorRelease extends SearchEngine implements ParsedEventListener { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
    private ContentIndex contentIndex = new ContentIndex();
    private CitationIndex citationIndex = new CitationIndex();
    private List<String> files = new LinkedList<>();
    private int maxThreads = 2;
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

        indexContent();
        indexCitations();
        setupSpellchecking();
    }

    private void indexContent() {
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        Set<Future<Integer>> result = new HashSet<>();
        int curFileNum = 0;

        while (!executor.isShutdown() && curFileNum < files.size()) {
            Callable<Integer> fileIndexer = new ContentIndexer(files.get(curFileNum), curFileNum, this);
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
        contentIndex.mergePartialIndices(files, numPatents);
    }

    private void indexCitations() {
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        int curFileNum = 0;

        while (!executor.isShutdown() && curFileNum < files.size()) {
            Runnable fileIndexer = new CitationIndexer(files.get(curFileNum), this);
            executor.execute(fileIndexer);
            ++curFileNum;
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        citationIndex.mergePartialIndices(files);
    }

    private void setupSpellchecking() {
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
        contentIndex.compressIndex();
        searchFactory = new SearchFactory();
        searchFactory.setContentIndex(contentIndex);
    }

    @Override
    boolean loadCompressedIndex() {
        try {
            contentIndex.loadFromFile(FilePaths.COMPRESSED_INDEX_PATH, FilePaths.DOCINDEX_FILE);
            citationIndex.load(FilePaths.INDEX_CITATION_PATH);
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
        searchFactory.setContentIndex(contentIndex);
        return true;
    }

    @Override
    ArrayList<String> search(String query, int topK) {
        ArrayList<String> results;

        if (query.startsWith("LinkTo:")) {
            List<Integer> docIds = new CitationSearch(query, citationIndex).execute();
            results = new ArrayList<>();

            try {
                int i = 0;
                for(Integer docId: docIds) {
                    if (i >= topK) break;
                    ++i;

                    Posting posting = new Posting();
                    posting.setDocId(docId);
                    Document doc = contentIndex.buildDocument(posting);
                    results.add("0" + doc.getDocId() + "\t" + doc.getInventionTitle());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            results = executeSearch(query, topK);

            if (results.size() == 0 && Configuration.ENABLE_SPELLING_CORRECTION) {
                StringBuilder correctedQuery = new StringBuilder();
                for (String queryWord: query.split(" ")) {
                    correctedQuery.append(SpellingCorrector.getInstance().correctSpelling(queryWord));
                    correctedQuery.append(" ");
                }

                results = executeSearch(correctedQuery.toString(), topK);
            }
        }

        return results;
    }

    private ArrayList<String> executeSearch(String query, int topK) {
        ArrayList<Posting> postings;
        ArrayList<Document> documents = new ArrayList<>();

        try {
            postings = searchFactory.getSearchFromQuery(query, topK).execute();

            for (int i = 0; i < topK && i < postings.size(); ++i) {
                documents.add(contentIndex.buildDocument(postings.get(i)));
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
