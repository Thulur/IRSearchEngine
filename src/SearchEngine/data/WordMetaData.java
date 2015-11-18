package SearchEngine.data;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sebastian on 24.10.2015.
 */
public class WordMetaData {
    private int fileId;
    private int docId;
    private long abstractPos;
    private long abstractLength;
    private long inventionTitlePos;
    private long inventionTitleLength;
    private List<Long> occurrences = new LinkedList<>();
    private String word;

    public WordMetaData() {

    }

    public void addWordOccurrence(long pos) {
        occurrences.add(pos);
    }

    public void sortOccurrences() {
        Collections.sort(occurrences);
    }

    @Override
    public String toString() {
        StringBuilder metaDataStringBuilder = new StringBuilder();

        metaDataStringBuilder.append(fileId + "," + docId + "," + inventionTitlePos + "," +
                                    abstractPos + "," + inventionTitleLength + "," +
                                    abstractLength + "," + occurrences.size());

        for (long occurrence: occurrences) {
            metaDataStringBuilder.append("," + occurrence);
        }

        metaDataStringBuilder.append(";");

        return metaDataStringBuilder.toString();
    }

    //GETTER & SETTER

    public long getAbstractPos() {
        return abstractPos;
    }

    public void setAbstractPos(long pos) {
        this.abstractPos = pos;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public long getAbstractLength() {
        return abstractLength;
    }

    public void setAbstractLength(long abstractLength) {
        this.abstractLength = abstractLength;
    }

    public long getInventionTitleLength() {
        return inventionTitleLength;
    }

    public void setInventionTitleLength(long inventionTitleLength) {
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
