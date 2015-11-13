package SearchEngine.utils;

import SearchEngine.data.Configuration;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.tartarus.snowball.ext.EnglishStemmer;

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
    List<String> punctuation = new LinkedList<>();

    private WordParser() {
        try {
            Scanner scanner = new Scanner(new FileInputStream("data/stopWords.txt"));

            while (scanner.hasNext()) {
                stopWords.add(scanner.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        punctuation.add(".");
        punctuation.add(",");
        punctuation.add(":");
        punctuation.add(";");
        punctuation.add("-lrb-");
        punctuation.add("-rrb-");
    }

    public static WordParser getInstance () {
        if (WordParser.instance == null) {
            WordParser.instance = new WordParser();
        }
        return WordParser.instance;
    }

    public void disableErrorOutput() {
        if (Configuration.DISABLE_ERROR_OUTPUT) {
            System.setErr(new PrintStream(new OutputStream() {
                public void write(int b) {
                }
            }));
        }
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
        StringTokenizer test = new StringTokenizer(word);
        EnglishStemmer stemmer = new EnglishStemmer();
        stemmer.setCurrent(test.nextToken());

        if (stemmer.stem()) {
            word = stemmer.getCurrent();
        } else {
            System.out.println("Stemmer error");
        }

        return word;
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

    public String stemToString(String text, Boolean filterStopwords) {
        EnglishStemmer stemmer = new EnglishStemmer();
        String result = "";
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);
        Annotation document = pipeline.process(text);

        for(CoreMap sentence: document.get(CoreAnnotations.SentencesAnnotation.class))
        {
            for(CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class))
            {
                String word = token.get(CoreAnnotations.TextAnnotation.class).toLowerCase();
                stemmer.setCurrent(word);

                if (stemmer.stem()) {
                    word = stemmer.getCurrent();
                } else {
                    System.out.println("Stemmer error");
                }

                // Do not save stopwords if the user does not want them as a part of the result
                if (filterStopwords && stopWords.contains(word)) {
                    continue;
                }

                if (!punctuation.contains(word)) {
                    result += word + " ";
                }
            }
        }

        return result;
    }

    private Map<String, List<Long>> genericStem(String text, Boolean filterStopwords, Long pos) {
        // Negative values should not be passed as position
        assert pos >= 0;
        String[] inputWords = text.split("[\\s+,.?!()]");

        EnglishStemmer stemmer = new EnglishStemmer();
        Map<String, List<Long>> words = new HashMap<>();
        long numWord = 0;
        String word;

        for (String tmpWord: inputWords) {
            word = tmpWord.toLowerCase();
            if ((filterStopwords && stopWords.contains(word)) | word.length() == 0) {
                continue;
            }

            stemmer.setCurrent(word);

            if (stemmer.stem()) {
                word = stemmer.getCurrent();
            } else {
                System.out.println("Stemmer error");
            }

            if (words.get(word) == null) {
                List<Long> tmpList = new LinkedList<>();
                tmpList.add(numWord);
                words.put(word, tmpList);
            } else {
                List<Long> tmpList = words.get(word);
                tmpList.add(numWord);
            }

            ++numWord;
        }

        return words;
    }

    public Map<String, List<Long>> snowballStem (String text, Boolean filterStopwords, Long pos) {
        EnglishStemmer stemmer = new EnglishStemmer();
        Map<String, List<Long>> words = new HashMap<>();

        Long posOffset = Long.valueOf(0);

        for (String word: text.toLowerCase().split(" ")) {
            stemmer.setCurrent(word);

            if (stemmer.stem()) {
                if (filterStopwords && stopWords.contains(word)) {
                    continue;
                }
//                words.put(stemmer.getCurrent(), null);
                if (words.get(word) == null) {
                    List<Long> tmpList = new LinkedList<>();
                    tmpList.add(pos + posOffset);
                    words.put(word, tmpList);
                } else {
                    words.get(word).add(pos + posOffset);
                }

            } else {
                System.out.println("ERROR");
            }

            posOffset += word.length() + 1;
        }

        punctuation.forEach(words::remove);

        return words;
    }
}
