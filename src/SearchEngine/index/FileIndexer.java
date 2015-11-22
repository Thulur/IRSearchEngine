package SearchEngine.index;

import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.data.Posting;
import SearchEngine.utils.WordParser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by sebastian on 03.11.2015.
 */
public class FileIndexer implements Runnable, ParsedEventListener {
    private XMLParser xmlApp = new XMLParser();
    private HashMap<String, Long> values = new HashMap<>();
    private RandomAccessFile tmpPostingList;
    private RandomAccessFile cacheFile;
    private String filenameId;
    private String filename;
    private int docId;
    private StringBuilder tmpFileBuffer = new StringBuilder();
    private int numPatents = 0;

    public FileIndexer(String filename, int docId, ParsedEventListener parsingStateListener) {
        xmlApp.addDocumentParsedListener(parsingStateListener);
        xmlApp.addDocumentParsedListener(this);
        this.filename = filename;
        filenameId = "";
        this.docId = docId;

        if (filename.indexOf("ipg") != -1) {
            filenameId = filename.substring(filename.indexOf("ipg") + 3, filename.indexOf("ipg") + 9);
        }

        try {
            tmpPostingList = new RandomAccessFile(FilePaths.PARTIAL_PATH + "tmppostinglist" + filenameId + ".txt", "rw");
            cacheFile = new RandomAccessFile(FilePaths.CACHE_PATH + "cache" + filenameId + ".txt", "rw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            xmlApp.parseFiles(FilePaths.RAW_PARTIAL_PATH + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }

        flush();
        save();
    }

    @Override
    public void documentParsed(Document document) {
        processDocument(document);
    }

    @Override
    public void finishedParsing() {
        //save();
    }

    private void processDocument(Document document) {
        addToIndex(document);
    }

    public void addToIndex(Document document) {
        ++numPatents;
        Map<String, List<Long>> words = WordParser.getInstance().stem(document.getInventionTitle(), true, document.getInventionTitlePos());
        Map<String, List<Long>> stemmedAbstract = WordParser.getInstance().stem(document.getPatentAbstract(), true, document.getPatentAbstractPos());

        for (Map.Entry<String, List<Long>> entry : stemmedAbstract.entrySet()) {
            String word = entry.getKey();
            List<Long> occurrences = entry.getValue();

            if (words.containsKey(word)) {
                List<Long> tmpList = words.get(word);
                tmpList.addAll(occurrences);
            } else {
                words.put(word, occurrences);
            }
        }

        for (Map.Entry<String, List<Long>> entry : words.entrySet()) {
            String word = entry.getKey();
            List<Long> occurrences = entry.getValue();
            Posting metaData = new Posting();
            metaData.setFileId(docId);
            metaData.setDocId(document.getDocId());
            metaData.setInventionTitlePos(document.getInventionTitlePos());
            metaData.setAbstractPos(document.getPatentAbstractPos());
            metaData.setInventionTitleLength(document.getInventionTitleLength());
            metaData.setAbstractLength(document.getPatentAbstractLength());
            occurrences.forEach(metaData::addWordOccurrence);
            metaData.sortOccurrences();

            try {
                if (values.get(word) == null) {
                    values.put(word, tmpPostingList.getFilePointer() + tmpFileBuffer.length());
                    write("-1," + metaData.toString());
                } else {
                    long tmpPos = tmpPostingList.getFilePointer() + tmpFileBuffer.length();
                    write(values.get(word).toString() + "," + metaData.toString());
                    values.put(word, tmpPos);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getNumPatents() {
        return numPatents;
    }

    private void write(String s) {
        tmpFileBuffer.append(s);

        if (tmpFileBuffer.length() > 16384) {
            writeResetBuffer();
        }
    }

    private void flush() {
        writeResetBuffer();
    }

    private void writeResetBuffer() {
        try {
            tmpPostingList.writeBytes(tmpFileBuffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        tmpFileBuffer.setLength(0);
    }

    private void save() {
        try {
            RandomAccessFile dictionaryFile = new RandomAccessFile(FilePaths.PARTIAL_PATH + "index" + filenameId + ".txt", "rw");
            RandomAccessFile postingListFile = new RandomAccessFile(FilePaths.PARTIAL_PATH + "postinglist" + filenameId + ".txt", "rw");
            Map<String, Long> sortedMap = new TreeMap<>(values);

            for (Map.Entry<String, Long> entry : sortedMap.entrySet()) {
                String key = entry.getKey();
                long prevEntryPos = entry.getValue();
                String postingListEntry = new String();

                while (prevEntryPos != -1) {
                    tmpPostingList.seek(prevEntryPos);
                    byte[] buffer = new byte[8192];
                    tmpPostingList.read(buffer);
                    String readString = new String(buffer);
                    int separatorPos = readString.indexOf(";");
                    readString = readString.substring(0, separatorPos + 1);
                    String tmpPos = readString.substring(0, readString.indexOf(","));
                    prevEntryPos = Long.parseLong(tmpPos);
                    postingListEntry = readString.substring(readString.indexOf(",") + 1) + postingListEntry;
                }

                Long filePos = postingListFile.getChannel().position();
                dictionaryFile.writeUTF(key + " " + filePos.toString());
                postingListFile.writeBytes(postingListEntry + "\n");
            }

            dictionaryFile.close();
            postingListFile.close();
            tmpPostingList.close();
            File tmpFile = new File (FilePaths.PARTIAL_PATH + "tmppostinglist" + filenameId + ".txt");
            tmpFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
