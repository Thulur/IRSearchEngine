package SearchEngine.index;

import SearchEngine.data.CustomFileWriter;
import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.data.Posting;
import SearchEngine.index.parse.ParsedEventListener;
import SearchEngine.index.parse.XmlParser;
import SearchEngine.utils.NumberParser;
import SearchEngine.utils.WordParser;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

/**
 * Created by sebastian on 03.11.2015.
 */
public class FileIndexer implements Callable<Integer>, ParsedEventListener {
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
    public Integer call() {
        try {
            System.out.println("Start Parsing");
            xmlApp.parseFiles(FilePaths.RAW_PARTIAL_PATH + filename);
            System.out.println("Finish Parsing");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            docIndex.close();
            tmpPostingList.close();
            System.out.println("Start Saving");
            save();
            System.out.println("Finish Saving");

            // Close all used files
            dictionaryFile.close();
            postingListFile.close();
            File tmpFile = new File (FilePaths.PARTIAL_PATH + "tmppostinglist" + filenameId + ".txt");
            tmpFile.delete();
            docIndex = null;
            dictionaryFile = null;
            tmpPostingList = null;
            postingListFile = null;
            values.clear();
            values = null;
            xmlApp = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return numPatents;
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
        StringBuilder docContent = new StringBuilder(document.getInventionTitle());
        docContent.append(" ");
        docContent.append(document.getPatentAbstract());
        docContent.append(" ");
        docContent.append(document.getClaims());
        docContent.append(" ");
        docContent.append(document.getDescription());
        Map<String, List<Long>> words = WordParser.getInstance().stem(docContent.toString(), true);
        document.setFileId(docId);
        docIndex.write(document.getDocIndexEntry());
        docIndex.write(" ".concat(String.valueOf(WordParser.getInstance().stemToString(document.getInventionTitle(), true).split("[ ]").length)));
        docIndex.write(" ".concat(String.valueOf(WordParser.getInstance().stemToString(document.getPatentAbstract(), true).split("[ ]").length)));
        docIndex.write("\n");

        for (Map.Entry<String, List<Long>> entry : words.entrySet()) {
            String word = entry.getKey();
            List<Long> occurrences = entry.getValue();
            Posting posting = new Posting();
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

    private void save() {
        try {
            Map<String, Long> sortedMap = new TreeMap<>(values);
            RandomAccessFile tmpPostingListReader = new RandomAccessFile(FilePaths.PARTIAL_PATH + "tmppostinglist" + filenameId + ".txt", "r");
            byte separator = ';';
            byte comma = ',';

            for (Map.Entry<String, Long> entry : sortedMap.entrySet()) {
                String key = entry.getKey();
                long prevEntryPos = entry.getValue();
                StringBuilder postingListEntry = new StringBuilder();

                while (prevEntryPos != -1) {
                    int separatorPos = -1;
                    byte[] readBytes = new byte[0];
                    tmpPostingListReader.seek(prevEntryPos);

                    while (separatorPos == -1) {
                        byte[] buffer = new byte[2<<6];
                        tmpPostingListReader.read(buffer);

                        readBytes = ArrayUtils.addAll(readBytes, buffer);
                        separatorPos = ArrayUtils.indexOf(readBytes, separator);
                    }

                    int commaPos = ArrayUtils.indexOf(readBytes, comma);
                    prevEntryPos = NumberParser.parseDecimalLong(readBytes, 0, commaPos);
                    postingListEntry.insert(0, new String(readBytes, commaPos + 1, separatorPos - commaPos));
                }

                Long filePos = postingListFile.position();
                postingListEntry.append("\n");
                dictionaryFile.write(key + " " + filePos.toString() + "\n");
                postingListFile.write(postingListEntry.toString());
            }

            tmpPostingListReader.close();
            sortedMap.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
