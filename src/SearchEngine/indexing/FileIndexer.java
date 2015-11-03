package SearchEngine.indexing;

import SearchEngine.SearchEngine;
import SearchEngine.data.Document;
import SearchEngine.data.WordMetaData;
import SearchEngine.utils.WordParser;

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

    public FileIndexer(String filename, ParsedEventListener parsingStateListener) {
        xmlApp.addDocumentParsedListener(parsingStateListener);
        xmlApp.addDocumentParsedListener(this);

        try {
            tmpPostingList = new RandomAccessFile("data/tmppostinglist.txt", "rw");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            xmlApp.parseFiles(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

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
        List<String> words = WordParser.getInstance().stem(document.getPatentAbstract());
        WordParser.getInstance().stem(document.getInventionTitle()).stream().filter(word -> !words.contains(word)).forEach(words::add);
        WordParser.getInstance().removeStopwords(words);

        String patentAbstract = document.getPatentAbstract().toLowerCase();
        String inventionTitle = document.getInventionTitle().toLowerCase();

        for (String word: words) {
            WordMetaData metaData = new WordMetaData();
            metaData.setPatentDocId(document.getDocId());
            metaData.setAbstractPos(document.getPatentAbstractPos());
            metaData.setAbstractLength(document.getPatentAbstractLength());
            metaData.setInventionTitlePos(document.getInventionTitlePos());
            metaData.setInventionTitleLength(document.getInventionTitleLength());

            for (int i = -1; (i = patentAbstract.indexOf(word, i + 1)) != -1; ) {
                metaData.addWordOccurrence(i + metaData.getAbstractPos());
            }

            for (int i = -1; (i = inventionTitle.indexOf(word, i + 1)) != -1; ) {
                metaData.addWordOccurrence(i + metaData.getInventionTitlePos());
            }

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
            RandomAccessFile dictionaryFile = new RandomAccessFile("data/index.txt", "rw");
            RandomAccessFile postingListFile = new RandomAccessFile("data/postinglist.txt", "rw");
            Map<String, Long> sortedMap = new TreeMap<>(values);

            for (Map.Entry<String, Long> entry : sortedMap.entrySet()) {
                String key = entry.getKey();
                long prevEntryPos = entry.getValue();
                String postingListEntry = new String();

                while (prevEntryPos != -1) {
                    tmpPostingList.seek(prevEntryPos);
                    byte[] buffer = new byte[2048];
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
