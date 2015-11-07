package SearchEngine.utils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by Sebastian on 25.10.2015.
 */
public class WordParser {
    private static WordParser instance;
    private List<String> stopWords = new LinkedList<>();
    private PrintStream err = System.err;

    private WordParser() {
        try {
            Scanner scanner = new Scanner(new FileInputStream("data/stopWords.txt"));

            while (scanner.hasNext()) {
                stopWords.add(scanner.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static WordParser getInstance () {
        if (WordParser.instance == null) {
            WordParser.instance = new WordParser();
        }
        return WordParser.instance;
    }

    public void disableErrorOutput() {
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));
    }

    public void enableErrorOutput() {
        System.setErr(err);
    }

    /**
     * Stems a given string.
     * @param text A string which will be tokenized.
     * @return  Returns tokens with their position in the given string.
     */
    public Map<String, List<Long>> stem(String text, Boolean filterStopwords) {
        return genericStem(text, filterStopwords, 0l);
    }

    public String stemSingleWord(String word) {
        Map<String, List<Long>> result = genericStem(word, false, 0l);

        return result.keySet().iterator().next();
    }

    /**
     * Stems a given string.
     * @param text A string which will be tokenized.
     * @param position A position which the result positions will be relative to.
     * @return Returns tokens with a list of positions relative to a given position.
     */
    public Map<String, List<Long>> stem(String text, Boolean filterStopwords, Long position) {
        return genericStem(text, filterStopwords, position);
    }

    private Map<String, List<Long>> genericStem(String text, Boolean filterStopwords, Long pos) {
        // Negative values should not be passed as position
        assert pos >= 0;

        Map<String, List<Long>> words = new HashMap<>();
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);
        Annotation document = pipeline.process(text);

        for(CoreMap sentence: document.get(CoreAnnotations.SentencesAnnotation.class))
        {
            for(CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class))
            {
                //String word = token.get(CoreAnnotations.TextAnnotation.class);
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase();
                Long tmpInt = token.beginPosition() + pos;

                // Do not save stopwords if the user does not want them as a part of the result
                if (filterStopwords && stopWords.contains(lemma)) {
                    continue;
                }

                if (words.get(lemma) == null) {
                    List<Long> tmpList = new LinkedList<>();
                    tmpList.add(tmpInt);
                    words.put(lemma, tmpList);
                } else {
                    List<Long> tmpList = words.get(lemma);
                    tmpList.add(tmpInt);
                }
            }
        }

        List<String> punctuation = new LinkedList<>();
        punctuation.add(".");
        punctuation.add(",");
        punctuation.add(":");
        punctuation.add(";");
        punctuation.add("-lrb-");
        punctuation.add("-rrb-");

        punctuation.forEach(words::remove);

        return words;
    }
}
