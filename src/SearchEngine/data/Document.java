package SearchEngine.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by sebastian on 22.10.2015.
 */
public class Document {
    public static int patentIdPos = 1;
    public static int numOccurrencePos = 4;
    private int docId;
    private String inventionTitle = "";
    private long inventionTitlePos;
    private String patentAbstract = "";
    private long patentAbstractPos;
    private String cacheFile;

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

            patentAbstract = readLineFromFile(cacheReader, patentAbstractPos);
            inventionTitle = readLineFromFile(cacheReader, inventionTitlePos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String readLineFromFile(RandomAccessFile file, long pos) {
        String result = "";

        try {
            file.seek(pos);
            result = file.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
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
}
