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

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.FileInputStream;
import java.util.*;


public class SearchEngineMajorRelease extends SearchEngine implements ParsedEventListener { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
    private MySAXApp saxApp = new MySAXApp();
    private List<String> stopWords = new LinkedList<>();

    public SearchEngineMajorRelease() { // Replace 'Template' with your search engine's name, i.e. SearchEngineMyTeamName
        // This should stay as is! Don't add anything here!
        super();
    }

    @Override
    void index(String directory) {
        initStopWords();
        saxApp.addDocumentParsedListener(this);
        
        List<String> files = new LinkedList<>();
        files.add("data/testData.xml");
        
        try {
            saxApp.parseFiles(files);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initStopWords() {
        try {
            Scanner scanner = new Scanner(new FileInputStream("data/stopWords.txt"));

            while (scanner.hasNext()) {
                stopWords.add(scanner.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> stem(String text) {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);
        Annotation document = pipeline.process(text);
        LinkedList<String> words = new LinkedList<>();

        for(CoreMap sentence: document.get(CoreAnnotations.SentencesAnnotation.class))
        {
            for(CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class))
            {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                words.add(word.toLowerCase());
            }
        }

        List<String> punctuation = new LinkedList<>();
        punctuation.add(".");
        punctuation.add(",");
        punctuation.add(":");
        punctuation.add(";");
        punctuation.add("-lrb-");
        punctuation.add("-rrb-");

        words.removeAll(punctuation);

        return words;
    }

    @Override
    boolean loadIndex(String directory) {
        return false;
    }
    
    @Override
    void compressIndex(String directory) {
    }

    @Override
    boolean loadCompressedIndex(String directory) {
        return false;
    }
    
    @Override
    ArrayList<String> search(String query, int topK, int prf) {
        return null;
    }

    @Override
    public void documentParsed(Document document) {
        processDocument(document);
    }

    private void processDocument(Document document) {
        List<String> words = stem(document.patentAbstract);

        words.removeAll(stopWords);

        System.out.println(document.docId);
        System.out.println(document.getPatentAbstract());
        System.out.println(String.join(", ", words));
    }
}
