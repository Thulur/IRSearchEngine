package SearchEngine.data;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by sebastian on 22.10.2015.
 */
public class Document {
    private int docId;
    private int fileId;
    private String inventionTitle = "";
    private long inventionTitlePos;
    private long inventionTitleLength;
    private String patentAbstract = "";
    private long patentAbstractPos;
    private long patentAbstractLength;

    public Document() {

    }

    public Document(int docId, String cacheFile) {
        this.docId = docId;
    }

    public Document(Posting posting) {
        this.docId = posting.getDocId();
        this.fileId = posting.getFileId();
    }

    public Document(int docId, String inventionTitle, String patentAbstract) {
        this.docId = docId;
        this.inventionTitle = inventionTitle;
        this.patentAbstract = patentAbstract;
    }

    public void loadPatentData(RandomAccessFile cacheReader) throws IOException {
        inventionTitle = readTillTag(cacheReader, inventionTitlePos, inventionTitleLength, "</invent");
        patentAbstract = readTillTag(cacheReader, patentAbstractPos, patentAbstractLength, "</abstract");
    }

    public String getDocIndexEntry() {
        return docId + " " + inventionTitlePos + " " + patentAbstractPos + " " + inventionTitleLength + " " + patentAbstractLength;
    }

    private String readTillTag(RandomAccessFile file, long pos, long length, String tag) throws IOException {
        int tmpLength = 2 * Math.toIntExact(length);
        byte[] readData = new byte[tmpLength];

        file.seek(pos);
        file.read(readData);

        int tagPos = 0;
        int bufferPos = 0;
        while (bufferPos < tmpLength && tagPos < tag.length()) {
            if (readData[bufferPos] == tag.charAt(tagPos)) {
                ++tagPos;
            } else {
                tagPos = 0;
            }

            ++bufferPos;
        }

        return new String(readData, 0, bufferPos - tag.length());
    }

    private String readLineFromFile(RandomAccessFile file, long pos, long length) throws IOException {
        int tmpLength = Math.toIntExact(length);
        byte[] readData = new byte[tmpLength];

        file.seek(pos);
        file.read(readData);

        return new String(readData, 0, tmpLength);
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

    public int getFileId() {
        return fileId;
    }
}
