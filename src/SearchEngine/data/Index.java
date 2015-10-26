package SearchEngine.data;

import SearchEngine.utils.WordParser;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Sebastian on 24.10.2015.
 */
public class Index {
    HashMap<String, HashMap<Integer, WordMetaData>> values = new HashMap<>();

    public void addToIndex(Document document) {
        List<String> words = WordParser.getInstance().stem(document.patentAbstract);
        WordParser.getInstance().removeStopwords(words);

        WordMetaData metaData = new WordMetaData();
        metaData.setPatentDocId(document.getDocId());
        // TODO: WordMetaData need to save the absolute position of the elements for parsing of single words
        // to calculate their positions
        //metaData.setAbstractPos();

        for (String word: words) {
            List<Integer> positions = new LinkedList<>(); // TODO: get all positions of a word in the abstract and title

            for (int pos: positions) {
                metaData.addWordOccurrence(pos);
            }

            HashMap<Integer, WordMetaData> metaDataList;

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
    */

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

    public void saveToFile(FileWriter fileWriter) {
        try {
            Set<String> keys = values.keySet();

            for (String key: keys) {
                HashMap<Integer, WordMetaData> value = values.get(key);

                Set<Integer> innerKeys = value.keySet();

                for (Integer innerKey: innerKeys) {

                    WordMetaData metaData = value.get(innerKey);

                    fileWriter.write(key + "," + innerKey + "," + metaData.getAbstractPos()
                            + "," + metaData.getDocId() + "," + metaData.getPatentDocId() + "\n");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
