package SearchEngine.data;

import SearchEngine.SearchEngine;

/**
 * Created by sebastian on 22.10.2015.
 */
public class Document {
    int docId;
    String inventionTitle;
    String patentAbstract;

    public String getInventionTitle() {
        return inventionTitle;
    }

    public void setInventionTitle(String inventionTitle) {
        this.inventionTitle = inventionTitle;
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
}
