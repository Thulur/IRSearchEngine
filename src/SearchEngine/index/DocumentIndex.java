package SearchEngine.index;

import SearchEngine.data.CustomFileReader;
import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.data.Posting;
import SearchEngine.utils.NumberParser;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sebastian on 23.11.2015.
 */
public class DocumentIndex {
    Map<Integer, DocumentIndexEntry> values = new HashMap<>();

    public void load() throws IOException {
        CustomFileReader docIndex = new CustomFileReader(FilePaths.DOCINDEX_FILE);
        DocumentIndexEntry docIndexEntry;
        List<Byte[]> line;

        while ((line = docIndex.readLineOfSpaceSeparatedValues()) != null) {
            docIndexEntry = new DocumentIndexEntry();
            docIndexEntry.docId = NumberParser.parseDecimalInt(line.get(0));
            docIndexEntry.titlePos = NumberParser.parseDecimalLong(line.get(1));
            docIndexEntry.abstractPos = NumberParser.parseDecimalLong(line.get(2));
            docIndexEntry.titleLength = NumberParser.parseDecimalLong(line.get(3));
            docIndexEntry.abstractLength = NumberParser.parseDecimalLong(line.get(4));
            values.put(docIndexEntry.docId, docIndexEntry);
        }
    }

    public Document buildDocument(Posting posting, RandomAccessFile file) throws IOException {
        Document doc = new Document(posting);
        DocumentIndexEntry docIndexEntry = values.get(posting.getDocId());

        doc.setInventionTitlePos(docIndexEntry.titlePos);
        doc.setPatentAbstractPos(docIndexEntry.abstractPos);
        doc.setInventionTitleLength(docIndexEntry.titleLength);
        doc.setPatentAbstractLength(docIndexEntry.abstractLength);
        doc.loadPatentData(file);

        return doc;
    }

    private class DocumentIndexEntry {
        public int docId;
        public long titlePos;
        public long abstractPos;
        public long titleLength;
        public long abstractLength;
    }
}
