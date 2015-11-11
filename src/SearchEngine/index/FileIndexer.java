package SearchEngine.index;

import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.data.WordMetaData;
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
        Map<String, List<Long>> words = WordParser.getInstance().stem(document.getPatentAbstract(), true, document.getPatentAbstractPos());
        Map<String, List<Long>> stemmedTitle = WordParser.getInstance().stem(document.getInventionTitle(), true, document.getInventionTitlePos());

        for (Map.Entry<String, List<Long>> entry : stemmedTitle.entrySet()) {
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
            WordMetaData metaData = new WordMetaData();
            metaData.setDocId(docId);
            metaData.setPatentDocId(document.getDocId());
            try {
                metaData.setInventionTitlePos(cacheFile.getFilePointer());
                cacheFile.writeUTF(document.getInventionTitle());
                metaData.setAbstractPos(cacheFile.getFilePointer());
                cacheFile.writeUTF(document.getPatentAbstract());
            } catch (IOException e) {
                e.printStackTrace();
            }
            metaData.setAbstractLength(document.getPatentAbstractLength());
            metaData.setInventionTitleLength(document.getInventionTitleLength());

            occurrences.forEach(metaData::addWordOccurrence);
            metaData.sortOccurences();

            try {
                if (values.get(word) == null) {
                    values.put(word, tmpPostingList.getFilePointer());
                    tmpPostingList.writeBytes("-1," + metaData.toString());
                } else {
                    long tmpPos = tmpPostingList.getFilePointer();
                    tmpPostingList.writeBytes(values.get(word).toString() + "," + metaData.toString());
                    values.put(word, tmpPos);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
