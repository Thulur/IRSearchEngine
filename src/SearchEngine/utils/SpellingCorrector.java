package SearchEngine.utils;

import SearchEngine.data.FilePaths;
import SearchEngine.index.Index;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Dennis on 22.11.2015.
 */
public class SpellingCorrector {
    private static SpellingCorrector instance;
    private HashMap<String, Integer> languageModel;

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

    public String correctSpelling(String word) {
        if(languageModel.containsKey(word)) return word;
        ArrayList<String> list = edits(word);
        HashMap<Integer, String> candidates = new HashMap<Integer, String>();
        for(String s : list) if(languageModel.containsKey(s)) candidates.put(languageModel.get(s),s);
        if(candidates.size() > 0) return candidates.get(Collections.max(candidates.keySet()));
        for(String s : list) for(String w : edits(s)) if(languageModel.containsKey(w)) candidates.put(languageModel.get(w),w);
        String result = candidates.get(Collections.max(candidates.keySet()));
        System.out.println(result);
        return candidates.size() > 0 ? result : word;

    }

    public static void setup(Index index) {
       if (SpellingCorrector.instance == null) {
           SpellingCorrector.instance = new SpellingCorrector();
           SpellingCorrector.instance.buildLanguageModel(index);
       }
    }

    private void buildLanguageModel(Index index) {
        try {
            FileReader reader = new FileReader("data/big.txt");

            languageModel = new HashMap<>();

            Matcher whiteSpace;
            Matcher wordChar;

            String curString = new String();
            int temp;
            String curChar = new String();

            while ((temp = reader.read()) != -1) {
                curChar += (char) temp;

                wordChar = Pattern.compile("\\w").matcher(curChar);

                if (wordChar.find() && !curChar.equals("_")) {
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
                curChar = "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> edits(String word) {
        ArrayList<String> result = new ArrayList<String>();
        for(int i=0; i < word.length(); ++i) result.add(word.substring(0, i) + word.substring(i+1));
        for(int i=0; i < word.length()-1; ++i) result.add(word.substring(0, i) + word.substring(i+1, i+2) + word.substring(i, i+1) + word.substring(i+2));
        for(int i=0; i < word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i+1));
        for(int i=0; i <= word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i));
        return result;
    }
}
