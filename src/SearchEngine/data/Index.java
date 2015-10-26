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

    public void addToIndex(Document document) {
        List<String> words = WordParser.getInstance().stem(document.patentAbstract);
        WordParser.getInstance().removeStopwords(words);

        WordMetaData metaData = new WordMetaData();
        metaData.setPatentDocId(document.getDocId());
        // TODO: WordMetaData need to save the absolute position of the elements for parsing of single words
        // to calculate their positions
        //metaData.setAbstractPos();

        String patentAbstract = document.getPatentAbstract();
        for (String word: words) {
            HashMap<Integer, WordMetaData> metaDataList;

            for (int i = -1; (i = patentAbstract.indexOf(word, i + 1)) != -1; ) {
                metaData.addWordOccurrence(i);
            }

            if (values.get(word) == null) {
                metaDataList = new HashMap<>();

                values.put(word, metaDataList);
            } else {
                metaDataList = values.get(word);
            }

            metaDataList.put(document.docId, metaData);
        }
    }

    /*
    * method printIndex() for testing purposes only
    * also to get an understanding of how to work with HashMaps
    * FYI: (Work in progress)We really should introduce junit soon (tests are also made for a better understanding of code :D)
    */


    // index should still be savable and loadable, so we dont have to index every time we start the program
    public void printIndex() {
        Set<String> keys = values.keySet();

        for (String key: keys) {
            HashMap<Integer, WordMetaData> value = values.get(key);

            Set<Integer> innerKeys = value.keySet();

            for (Integer innerKey: innerKeys) {

                WordMetaData metaData = value.get(innerKey);

                System.out.println(key + " - " + innerKey + " - " + metaData.getAbstractPos()
                    + " - " + metaData.getDocId() + " - " + metaData.getPatentDocId());
            }
        }
    }


    public void loadFromFile(FileReader fileReader) {

    }

    public void saveToFile(FileWriter fileWriter) {
        try {

            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Document> search(String word) {
        List<Document> results = new LinkedList<>();

        return results;
    }

}
