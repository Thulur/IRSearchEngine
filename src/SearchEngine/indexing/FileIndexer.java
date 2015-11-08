package SearchEngine.indexing;

import SearchEngine.data.Configuration;
import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.data.WordMetaData;
import SearchEngine.utils.WordParser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created by sebastian on 03.11.2015.
 */
public class FileIndexer implements Runnable, ParsedEventListener {
    private XMLParser xmlApp = new XMLParser();
    private HashMap<String, Long> values = new HashMap<>();
    private RandomAccessFile tmpPostingList;
    private String filenameId;
    private String filename;

    public FileIndexer(String filename, ParsedEventListener parsingStateListener) {
        xmlApp.addDocumentParsedListener(parsingStateListener);
        xmlApp.addDocumentParsedListener(this);
        this.filename = filename;
        filenameId = "";

        if (filename.indexOf("ipg") != -1) {
            filenameId = filename.substring(filename.indexOf("ipg") + 3, filename.indexOf("ipg") + 9);
        }

        try {
            tmpPostingList = new RandomAccessFile(FilePaths.PARTIAL_PATH + "tmppostinglist" + filenameId + ".txt", "rw");
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
    }

    @Override
    public void documentParsed(Document document) {
        processDocument(document);
    }

    @Override
    public void finishedParsing() {
        save();
    }

    private void processDocument(Document document) {
        addToIndex(document);
    }

    public void addToIndex(Document document) {
//        Map<String, List<Long>> words = WordParser.getInstance().stem(document.getPatentAbstract(), true, document.getPatentAbstractPos());
//        Map<String, List<Long>> stemmedTitle = WordParser.getInstance().stem(document.getInventionTitle(), true, document.getInventionTitlePos());

        Map<String, List<Long>> words = WordParser.getInstance().snowballStem(document.getPatentAbstract(), true, document.getPatentAbstractPos());
        Map<String, List<Long>> stemmedTitle = WordParser.getInstance().snowballStem(document.getInventionTitle(), true, document.getInventionTitlePos());


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
            metaData.setPatentDocId(document.getDocId());
            metaData.setAbstractPos(document.getPatentAbstractPos());
            metaData.setAbstractLength(document.getPatentAbstractLength());
            metaData.setInventionTitlePos(document.getInventionTitlePos());
            metaData.setInventionTitleLength(document.getInventionTitleLength());

            occurrences.forEach(metaData::addWordOccurrence);
            metaData.sortOccurences();

            try {
                if (values.get(word) == null) {
                    values.put(word, tmpPostingList.getChannel().position());
                    tmpPostingList.writeBytes("-1," + metaData.toString());
                } else {
                    long tmpPos = tmpPostingList.getChannel().position();
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
                dictionaryFile.writeBytes(key + " " + filePos.toString() + "\n");
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
