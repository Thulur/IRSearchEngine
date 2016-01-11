package SearchEngine.index;

import SearchEngine.data.CustomFileReader;
import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.data.Posting;
import SearchEngine.utils.NumberParser;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sebastian on 23.11.2015.
 */
public class DocumentIndex {
    Map<Integer, DocumentIndexEntry> values = new HashMap<>();
    Map<Integer, RandomAccessFile> cacheFiles = new HashMap<>();
    Map<Integer, String> fileIds = new HashMap<>();

    public void load() throws IOException {
        CustomFileReader docIndex = new CustomFileReader(FilePaths.DOCINDEX_FILE);
        DocumentIndexEntry docIndexEntry;
        List<Byte[]> line;

        while ((line = docIndex.readLineOfSpaceSeparatedValues()) != null) {
            docIndexEntry = new DocumentIndexEntry();
            docIndexEntry.fileId = NumberParser.parseDecimalInt(line.get(0));
            docIndexEntry.docId = NumberParser.parseDecimalInt(line.get(1));
            docIndexEntry.titlePos = NumberParser.parseDecimalLong(line.get(2));
            docIndexEntry.abstractPos = NumberParser.parseDecimalLong(line.get(3));
            docIndexEntry.titleLength = NumberParser.parseDecimalLong(line.get(4));
            docIndexEntry.abstractLength = NumberParser.parseDecimalLong(line.get(5));
            values.put(docIndexEntry.docId, docIndexEntry);
        }
    }

    public void loadTmp() throws IOException {
        CustomFileReader docIndex = new CustomFileReader(FilePaths.DOCINDEX_FILE + ".tmp");
        DocumentIndexEntry docIndexEntry;
        List<Byte[]> line;

        while ((line = docIndex.readLineOfSpaceSeparatedValues()) != null) {
            docIndexEntry = new DocumentIndexEntry();
            docIndexEntry.fileId = NumberParser.parseDecimalInt(line.get(0));
            docIndexEntry.docId = NumberParser.parseDecimalInt(line.get(1));
            docIndexEntry.titlePos = NumberParser.parseDecimalLong(line.get(2));
            docIndexEntry.abstractPos = NumberParser.parseDecimalLong(line.get(3));
            docIndexEntry.titleLength = NumberParser.parseDecimalLong(line.get(4));
            docIndexEntry.abstractLength = NumberParser.parseDecimalLong(line.get(5));
            docIndexEntry.numWordsTitle = NumberParser.parseDecimalInt(line.get(6));
            docIndexEntry.numWordsAbstract = NumberParser.parseDecimalInt(line.get(7));
            values.put(docIndexEntry.docId, docIndexEntry);
        }
    }

    public Document buildDocument(Posting posting) throws IOException {
        Document doc = new Document(posting);
        DocumentIndexEntry docIndexEntry = values.get(posting.getDocId());

        doc.setFileId(docIndexEntry.fileId);
        doc.setInventionTitlePos(docIndexEntry.titlePos);
        doc.setPatentAbstractPos(docIndexEntry.abstractPos);
        doc.setInventionTitleLength(docIndexEntry.titleLength);
        doc.setPatentAbstractLength(docIndexEntry.abstractLength);
        doc.loadPatentData(getCacheFile(docIndexEntry.fileId));

        return doc;
    }

    public RandomAccessFile getCacheFile(int fileId) throws FileNotFoundException {
        if (cacheFiles.containsKey(fileId)) {
            return cacheFiles.get(fileId);
        } else {
            RandomAccessFile file = new RandomAccessFile(FilePaths.CACHE_PATH + fileIds.get(fileId), "r");
            cacheFiles.put(fileId, file);
            return file;
        }
    }

    public void loadFileIds() throws IOException {
        String line;
        BufferedReader docIdsFile = new BufferedReader(new FileReader(FilePaths.FILE_IDS_FILE));

        fileIds.clear();

        while ((line = docIdsFile.readLine()) != null) {
            String[] splitEntry = line.split("[ ]");

            // Skip empty lines at the end of the file
            if (splitEntry.length < 2) continue;

            fileIds.put(Integer.parseInt(splitEntry[0]), splitEntry[1]);
        }
    }

    public int numWordsTitleInEntry(int docId) {
        return values.get(docId).numWordsTitle;
    }

    public int numWordsAbstractInEntry(int docId) {
        return values.get(docId).numWordsAbstract;
    }

    private class DocumentIndexEntry {
        public int fileId;
        public int docId;
        public long titlePos;
        public long abstractPos;
        public long titleLength;
        public long abstractLength;
        public int numWordsTitle;
        public int numWordsAbstract;
    }
}
