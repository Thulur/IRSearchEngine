package SearchEngine.data;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sebastian on 24.10.2015.
 */
public class Posting {
    public static int POSTING_WEIGHT_POS = 0;
    public static int POSTING_FILE_ID_POS = 1;
    public static int POSTING_DOC_ID_POS = 2;
    public static int POSTING_TITLE_L_POS = 3;
    public static int POSTING_ABSTRACT_L_POS = 4;
    public static int POSTING_TITLE_P_POS = 5;
    public static int POSTING_ABSTRACT_P_POS = 6;
    public static int POSTING_NUM_OCC_POS = 7;
    private int fileId;
    private int docId;
    private long abstractPos;
    private long abstractLength;
    private long inventionTitlePos;
    private long inventionTitleLength;
    private String cacheFile;
    private Double weight;
    private List<Long> occurrences = new LinkedList<>();
    private String token;

    public Posting() {

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


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getCacheFile() {
        return cacheFile;
    }

    public void setCacheFile(String cacheFile) {
        this.cacheFile = cacheFile;
    }
}
