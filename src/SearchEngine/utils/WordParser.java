package SearchEngine.utils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by Sebastian on 25.10.2015.
 */
public class WordParser {
    private static WordParser instance;
    private List<String> stopWords = new LinkedList<>();

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

    public List<String> stem(String text) {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
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

    public void removeStopwords(List<String> words) {
        words.removeAll(stopWords);
    }
}
