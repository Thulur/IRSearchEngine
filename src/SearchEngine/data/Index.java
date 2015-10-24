package SearchEngine.data;

import SearchEngine.utils.WordParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

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
}
