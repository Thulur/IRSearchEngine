package SearchEngine.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by sebastian on 22.10.2015.
 */
public class Document {
    private String token;
    private int docId;
    private String inventionTitle = "";
    private long inventionTitlePos;
    private long inventionTitleLength;
    private String patentAbstract = "";
    private long patentAbstractPos;
    private long patentAbstractLength;
    private String cacheFile;
    private Double weight;

    public Document() {

    }

    public Document(int docId, String cacheFile) {
        this.docId = docId;
        this.cacheFile = cacheFile;
    }

    public Document(int docId, String inventionTitle, String patentAbstract) {
        this.docId = docId;
        this.inventionTitle = inventionTitle;
        this.patentAbstract = patentAbstract;
    }

    public void loadPatentData() {
        try {
            RandomAccessFile cacheReader = new RandomAccessFile(cacheFile, "r");

            patentAbstract = readLineFromFile(cacheReader, patentAbstractPos, patentAbstractLength);
            inventionTitle = readLineFromFile(cacheReader, inventionTitlePos, inventionTitleLength);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String readLineFromFile(RandomAccessFile file, long pos, long length) {
        int tmpLength = Math.toIntExact(length);
        byte[] readData = new byte[tmpLength];

        try {
            file.seek(pos);
            file.read(readData);
            return new String(readData, 0, tmpLength);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public String getInventionTitle() {
        return inventionTitle;
    }

    public void setInventionTitle(String inventionTitle) {
        this.inventionTitle = inventionTitle;
    }

    public long getInventionTitlePos() {
        return inventionTitlePos;
    }

    public void setInventionTitlePos(long inventionTitlePos) {
        this.inventionTitlePos = inventionTitlePos;
    }

    public long getInventionTitleLength() {
        return inventionTitleLength;
    }

    public void setInventionTitleLength(long inventionTitleLength) {
        this.inventionTitleLength = inventionTitleLength;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public String getPatentAbstract() {
        return patentAbstract;
    }

    public void setPatentAbstract(String patentAbstract) {
        this.patentAbstract = patentAbstract;
    }

    public long getPatentAbstractPos() {
        return patentAbstractPos;
    }

    public void setPatentAbstractPos(long patentAbstractPos) {
        this.patentAbstractPos = patentAbstractPos;
    }

    public long getPatentAbstractLength() {
        return patentAbstractLength;
    }

    public void setPatentAbstractLength(long patentAbstractLength) {
        this.patentAbstractLength = patentAbstractLength;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCacheFile() {
        return cacheFile;
    }

    public void setCacheFile(String cacheFile) {
        this.cacheFile = cacheFile;
    }
}
