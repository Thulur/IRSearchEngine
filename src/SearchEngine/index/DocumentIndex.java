package SearchEngine.index;

import SearchEngine.data.CustomFileReader;
import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.data.Posting;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sebastian on 23.11.2015.
 */
public class DocumentIndex {
    Map<Integer, DocumentIndexEntry> values = new HashMap<>();

    public void load() throws IOException {
        CustomFileReader docIndex = new CustomFileReader(FilePaths.DOCINDEX_FILE);
        String line;
        String[] lineValues;
        DocumentIndexEntry docIndexEntry;

        while ((line = docIndex.readLine()) != null) {
            lineValues = line.split("[ ]");
            docIndexEntry = new DocumentIndexEntry();
            docIndexEntry.titlePos = Long.parseLong(lineValues[1]);
            docIndexEntry.abstractPos = Long.parseLong(lineValues[2]);
            docIndexEntry.titleLength = Long.parseLong(lineValues[3]);
            docIndexEntry.abstractLength = Long.parseLong(lineValues[4]);
            values.put(Integer.parseInt(lineValues[0]), docIndexEntry);
        }
    }

    public Document buildDocument(Posting posting) {
        Document doc = new Document(posting);
        DocumentIndexEntry docIndexEntry = values.get(posting.getDocId());

        doc.setInventionTitlePos(docIndexEntry.titlePos);
        doc.setPatentAbstractPos(docIndexEntry.abstractPos);
        doc.setInventionTitleLength(docIndexEntry.titleLength);
        doc.setPatentAbstractLength(docIndexEntry.abstractLength);
        doc.loadPatentData();

        return doc;
    }

    private class DocumentIndexEntry {
        public long titlePos;
        public long abstractPos;
        public long titleLength;
        public long abstractLength;
    }
}
