package SearchEngine.index;

import SearchEngine.data.CustomFileWriter;
import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.data.Posting;
import SearchEngine.index.parse.ParsedEventListener;
import SearchEngine.index.parse.XmlParser;
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
    private XmlParser xmlApp = new XmlParser();
    private HashMap<String, Long> values = new HashMap<>();
    private CustomFileWriter tmpPostingList;
    private CustomFileWriter dictionaryFile;
    private CustomFileWriter postingListFile;
    private CustomFileWriter docIndex;
    private String filenameId;
    private String filename;
    private int docId;
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
            tmpPostingList = new CustomFileWriter(FilePaths.PARTIAL_PATH + "tmppostinglist" + filenameId + ".txt");
            dictionaryFile = new CustomFileWriter(FilePaths.PARTIAL_PATH + "index" + filenameId + ".txt");
            postingListFile = new CustomFileWriter(FilePaths.PARTIAL_PATH + "postinglist" + filenameId + ".txt");
            docIndex = new CustomFileWriter(FilePaths.PARTIAL_PATH + "docindex" + filenameId + ".txt");
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

        try {
            // Flush remaining content of all buffering files
            docIndex.flush();
            tmpPostingList.flush();
            save();
            dictionaryFile.flush();
            postingListFile.flush();

            // Close all used files
            docIndex.close();
            dictionaryFile.close();
            tmpPostingList.close();
            postingListFile.close();
            File tmpFile = new File (FilePaths.PARTIAL_PATH + "tmppostinglist" + filenameId + ".txt");
            tmpFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
            addToIndex(document);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToIndex(Document document) throws IOException {
        ++numPatents;
        String docContent = document.getInventionTitle() + document.getPatentAbstract();
        Map<String, List<Long>> words = WordParser.getInstance().stem(docContent, true);
        docIndex.write(document.getDocIndexEntry() + "\n");

        for (Map.Entry<String, List<Long>> entry : words.entrySet()) {
            String word = entry.getKey();
            List<Long> occurrences = entry.getValue();
            Posting posting = new Posting();
            posting.setFileId(docId);
            posting.setDocId(document.getDocId());
            occurrences.forEach(posting::addWordOccurrence);
            posting.sortOccurrences();

            if (values.get(word) == null) {
                values.put(word, tmpPostingList.position());
                tmpPostingList.write("-1," + posting.toString());
            } else {
                long tmpPos = tmpPostingList.position();
                tmpPostingList.write(values.get(word).toString() + "," + posting.toString());
                values.put(word, tmpPos);
            }
        }
    }

    public int getNumPatents() {
        return numPatents;
    }

    private void save() {
        try {
            Map<String, Long> sortedMap = new TreeMap<>(values);
            RandomAccessFile tmpPostingListReader = new RandomAccessFile(FilePaths.PARTIAL_PATH + "tmppostinglist" + filenameId + ".txt", "r");

            for (Map.Entry<String, Long> entry : sortedMap.entrySet()) {
                String key = entry.getKey();
                long prevEntryPos = entry.getValue();
                String postingListEntry = new String();

                while (prevEntryPos != -1) {
                    tmpPostingListReader.seek(prevEntryPos);
                    byte[] buffer = new byte[8192];
                    tmpPostingListReader.read(buffer);
                    String readString = new String(buffer);
                    int separatorPos = readString.indexOf(";");
                    readString = readString.substring(0, separatorPos + 1);
                    String tmpPos = readString.substring(0, readString.indexOf(","));
                    prevEntryPos = Long.parseLong(tmpPos);
                    postingListEntry = readString.substring(readString.indexOf(",") + 1) + postingListEntry;
                }

                Long filePos = postingListFile.position();
                dictionaryFile.write(key + " " + filePos.toString() + "\n");
                postingListFile.write(postingListEntry + "\n");
            }

            tmpPostingListReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
