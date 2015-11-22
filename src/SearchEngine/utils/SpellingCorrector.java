package SearchEngine.utils;

import SearchEngine.data.FilePaths;
import SearchEngine.index.Index;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
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

    public String correctSpelling(String query) {
        return "";
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
}
