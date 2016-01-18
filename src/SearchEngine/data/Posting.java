package SearchEngine.data;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sebastian on 24.10.2015.
 */
public class Posting implements Comparable<Posting> {
    public static int POSTING_WEIGHT_POS = 0;
    public static int POSTING_DOC_ID_POS = 1;
    public static int POSTING_NUM_OCC_POS = 2;
    private int docId;
    private String cacheFile;
    private Double weight;
    private List<Long> occurrences = new LinkedList<>();
    private String token;

    public Posting() {

    }

    public Posting fromStringWithoutWeight(String postingString) {
        String[] values = postingString.split(",");

        docId = Integer.parseInt(values[POSTING_DOC_ID_POS - 1]);

        for (int i = POSTING_NUM_OCC_POS; i < POSTING_NUM_OCC_POS  + Integer.parseInt(values[POSTING_NUM_OCC_POS - 1]); ++i) {
            occurrences.add(Long.parseLong(values[i]));
        }

        return this;
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

        metaDataStringBuilder.append(docId + "," + occurrences.size());

        for (long occurrence: occurrences) {
            metaDataStringBuilder.append("," + occurrence);
        }

        metaDataStringBuilder.append(";");

        return metaDataStringBuilder.toString();
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
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

    public List<Long> getOccurrences() {
        return occurrences;
    }

    @Override
    public int compareTo(Posting posting) {
        if (weight < posting.getWeight()) {
            return -1;
        } else if (weight > posting.getWeight()) {
            return 1;
        } else {
            return 0;
        }
    }
}
