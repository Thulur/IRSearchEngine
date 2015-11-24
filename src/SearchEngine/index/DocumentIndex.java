package SearchEngine.index;

import SearchEngine.data.CustomFileReader;
import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.data.Posting;
import SearchEngine.utils.NumberParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sebastian on 23.11.2015.
 */
public class DocumentIndex {
    Map<Integer, DocumentIndexEntry> values = new HashMap<>();

    public void load() throws IOException {
        CustomFileReader docIndex = new CustomFileReader(FilePaths.DOCINDEX_FILE);
        DocumentIndexEntry docIndexEntry = new DocumentIndexEntry();
        List<Byte[]> line = docIndex.readLineOfSpaceSeparatedValues();

        int valueCount = 0;
        Iterator<Byte[]> iterator = line.iterator();
        while (iterator.hasNext()) {
            switch (valueCount % 5) {
                case 0:
                    docIndexEntry = new DocumentIndexEntry();
                    docIndexEntry.docId = NumberParser.parseDecimalInt(iterator.next());
                    break;
                case 1:
                    docIndexEntry.titlePos = NumberParser.parseDecimalLong(iterator.next());
                    break;
                case 2:
                    docIndexEntry.abstractPos = NumberParser.parseDecimalLong(iterator.next());
                    break;
                case 3:
                    docIndexEntry.titleLength = NumberParser.parseDecimalLong(iterator.next());
                    break;
                case 4:
                    docIndexEntry.abstractLength = NumberParser.parseDecimalLong(iterator.next());
                    values.put(docIndexEntry.docId, docIndexEntry);
                    break;
            }

            ++valueCount;
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
        public int docId;
        public long titlePos;
        public long abstractPos;
        public long titleLength;
        public long abstractLength;
    }
}
