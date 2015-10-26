package SearchEngine.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sebastian on 24.10.2015.
 */
public class WordMetaData {
    private int docId;
    private int patentDocId;
    private long abstractPos;
    private int abstractLenght;
    private List<Integer> occurences = new LinkedList<>();

    public WordMetaData() {

    }

    public WordMetaData(String metaDataString) {

    }

    public void addWordOccurrence(int pos) {
        occurences.add(pos);
    }

    @Override
    public String toString() {
        // TODO: Generate the stringified version of the data
        return super.toString();
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

    public int getAbstractLenght() {
        return abstractLenght;
    }

    public void setAbstractLenght(int abstractLenght) {
        this.abstractLenght = abstractLenght;
    }
}
