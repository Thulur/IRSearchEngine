package SearchEngine.data;

import SearchEngine.utils.WordParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Sebastian on 24.10.2015.
 */
public class Index {
    HashMap<String, Integer> index = new HashMap<>();

    public void addToIndex(String word) {
        if (index.get(word) == null) {
            index.put(word, null);
        }
    }

    /*
    * method printIndex() for testing purposes only
    * also to get an understanding of how to work with HashMaps
    * FYI: (Work in progress)We really should introduce junit soon (tests are also made for a better understanding of code :D)
    */

    // index should still be savable and loadable, so we dont have to index every time we start the program

    public void loadFromFile(FileReader fileReader) {
        //TODO: implement
    }

    public void saveToFile(FileWriter fileWriter) {
        try {
            //TODO: implement

            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Document> search(String word) {
        List<Document> results = new LinkedList<>();

        return results;
    }

    public void printIndex() {
        //TODO: implement
    }
}
