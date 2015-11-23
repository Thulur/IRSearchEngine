package SearchEngine.utils;

import SearchEngine.index.Index;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Dennis on 22.11.2015.
 */
public class SpellingCorrector {
    private static SpellingCorrector instance;
    private HashMap<String, Integer> languageModel;
    private int BUFFER_SIZE = 16384;

//    private SpellingCorrector(){
//        // setup
//        buildLanguageModel();
//    }

    public static SpellingCorrector getInstance() {
        if (SpellingCorrector.instance == null) {
            SpellingCorrector.instance = new SpellingCorrector();
        }
        return SpellingCorrector.instance;
    }

    public static void setup() {
        if (SpellingCorrector.instance == null) {
            SpellingCorrector.instance = new SpellingCorrector();
            SpellingCorrector.instance.buildLanguageModel();
        }
    }

    public String correctSpelling(String word) {
        if(languageModel.containsKey(word)) return word;

        ArrayList<String> edits = computeEdits(word);
        Map<Integer, String> candidates = new HashMap<>();
        String result = new String();

        for(String s : edits) {
            if(languageModel.containsKey(s)) candidates.put(languageModel.get(s),s);
        }

        if (candidates.size() > 0) {
            result = candidates.get(Collections.max(candidates.keySet()));
            System.out.println(result);
            return result;
        }


        for(String edit : edits) {
            for(String w : computeEdits(edit)) {
                if(languageModel.containsKey(w)) candidates.put(languageModel.get(w),w);
            }
        }

        if (candidates.size() > 0) result = candidates.get(Collections.max(candidates.keySet()));
        System.out.println(result);
        return candidates.size() > 0 ? result : word;

    }

    private void buildLanguageModel() {
        try {
            languageModel = new HashMap<>();

            String curString = new String();
            Matcher whiteSpace;
            Matcher wordChar;

            BufferedReader reader = new BufferedReader(new FileReader("data/big.txt"));
            char[] buffer = new char[BUFFER_SIZE];

            while (reader.read(buffer) != -1) {
                for (char curChar: buffer) {
                    wordChar = Pattern.compile("\\w").matcher(curChar+"");

                    if (wordChar.find() && curChar != '_') {
                        curString += curChar;
                    } else {
                        if (!curString.equals("")) {
                            curString = curString.toLowerCase();
                            if (languageModel.get(curString) == null) {
                                languageModel.put(curString, 1);
                            } else {
                                languageModel.put(curString, languageModel.get(curString) + 1);
                            }
                            curString = "";
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> computeEdits(String word) {
        ArrayList<String> result = new ArrayList<>();
        for(int i=0; i < word.length(); ++i) result.add(word.substring(0, i) + word.substring(i+1));
        for(int i=0; i < word.length()-1; ++i) result.add(word.substring(0, i) + word.substring(i+1, i+2) + word.substring(i, i+1) + word.substring(i+2));
        for(int i=0; i < word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i+1));
        for(int i=0; i <= word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i));
        return result;
    }
}
