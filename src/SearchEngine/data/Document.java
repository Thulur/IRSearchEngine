package SearchEngine.data;

import SearchEngine.SearchEngine;

/**
 * Created by sebastian on 22.10.2015.
 */
public class Document {
    private int docId;
    private String inventionTitle;
    private long inventionTitlePos;
    private int inventionTitleLength;
    private String patentAbstract;
    private long patentAbstractPos;
    private int patentAbstractLength;

    public Document() {

    }

    public Document(int docId, String inventionTitle, String patentAbstract) {
        this.docId = docId;
        this.inventionTitle = inventionTitle;
        this.patentAbstract = patentAbstract;
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

    public int getInventionTitleLength() {
        return inventionTitleLength;
    }

    public void setInventionTitleLength(int inventionTitleLength) {
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

    public int getPatentAbstractLength() {
        return patentAbstractLength;
    }

    public void setPatentAbstractLength(int patentAbstractLength) {
        this.patentAbstractLength = patentAbstractLength;
    }
}
