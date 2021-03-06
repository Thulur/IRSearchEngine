package SearchEngine.utils;

import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.FileInputStream;
import java.util.*;

/**
 * Created by Sebastian on 25.10.2015.
 */
public class WordParser {
    private static WordParser instance;
    private List<String> stopWords = new LinkedList<>();
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

    /**
     * Stems a given string.
     * @param text A string which will be tokenized.
     * @return  Returns tokens with their position in the given string.
     */
    public Map<String, List<Long>> stem(String text, Boolean filterStopwords) {
        return genericStem(text, filterStopwords, 0l);
    }

    public String stemSingleWord(String word) {
        EnglishStemmer stemmer = new EnglishStemmer();
        stemmer.setCurrent(word);

        if (stemmer.stem()) {
            word = stemmer.getCurrent();
        } else {
            System.out.println("Stemmer error");
        }

        return word;
    }

    public String stemToString(String text, Boolean filterStopwords) {
        StringBuilder result = new StringBuilder();
        String[] inputWords = text.split("[\\s+,;:.?!()]");
        EnglishStemmer stemmer = new EnglishStemmer();
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

            result.append(word + " ");
        }

        return result.toString();
    }

    private Map<String, List<Long>> genericStem(String text, Boolean filterStopwords, Long pos) {
        // Negative values should not be passed as position
        assert pos >= 0;
        String[] inputWords = text.split("[\\s+,;:.?!()]");

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
