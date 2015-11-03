package SearchEngine.data;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sebastian on 24.10.2015.
 */
public class WordMetaData {
    private int docId;
    private int patentDocId;
    private long abstractPos;
    private int abstractLength;
    private long inventionTitlePos;
    private int inventionTitleLength;
    private List<Long> occurrences = new LinkedList<>();
    private String word;

    public WordMetaData() {

    }

    public WordMetaData(String metaDataString) {

    }

    public void addWordOccurrence(long pos) {
        occurrences.add(pos);
    }

    public void sortOccurences() {
        Collections.sort(occurrences);
    }

    @Override
    public String toString() {
        String metaDataString = new String();

        metaDataString += docId + "," + patentDocId + ",";
        metaDataString += inventionTitlePos + "," + abstractPos + "," + abstractLength + "," + inventionTitleLength + ",";
        metaDataString += occurrences.size();

        for (long occurrence: occurrences) {
            metaDataString += "," + occurrence ;
        }

        metaDataString += ";";

        return metaDataString;
    }

    //GETTER & SETTER

    public long getAbstractPos() {
        return abstractPos;
    }

    public void setAbstractPos(long pos) {
        this.abstractPos = pos;
    }

    public int getPatentDocId() {
        return patentDocId;
    }

    public void setPatentDocId(int patentDocId) {
        this.patentDocId = patentDocId;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public int getAbstractLength() {
        return abstractLength;
    }

    public void setAbstractLength(int abstractLength) {
        this.abstractLength = abstractLength;
    }

    public int getInventionTitleLength() {
        return inventionTitleLength;
    }

    public void setInventionTitleLength(int inventionTitleLength) {
        this.inventionTitleLength = inventionTitleLength;
    }

    public long getInventionTitlePos() {
        return inventionTitlePos;
    }

    public void setInventionTitlePos(long inventionTitlePos) {
        this.inventionTitlePos = inventionTitlePos;
    }


    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
