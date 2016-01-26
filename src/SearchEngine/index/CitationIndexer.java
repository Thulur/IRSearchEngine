package SearchEngine.index;

import SearchEngine.data.CustomFileWriter;
import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.index.parse.CitationParser;
import SearchEngine.index.parse.ParsedEventListener;
import SearchEngine.utils.NumberParser;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by sebastian on 26.01.2016.
 */
public class CitationIndexer implements Runnable, ParsedEventListener {
    private CustomFileWriter postingListFile;
    private CustomFileWriter dictionaryFile;
    private CustomFileWriter tmpPostingList;
    private HashMap<Integer, Long> values = new HashMap<>();
    private CitationParser citationParser = new CitationParser();
    private String filenameId = "";
    private String filename = "";

    public CitationIndexer(String filename, ParsedEventListener parsedEventListener) {
        citationParser.addDocumentParsedListener(this);
        this.filename = filename;

        if (filename.indexOf("ipg") != -1) {
            filenameId = filename.substring(filename.indexOf("ipg") + 3, filename.indexOf("ipg") + 9);
        }

        try {
            tmpPostingList = new CustomFileWriter(FilePaths.PARTIAL_PATH + "tmppostinglistcitation" + filenameId + ".txt");
            dictionaryFile = new CustomFileWriter(FilePaths.PARTIAL_PATH + "indexcitation" + filenameId + ".txt");
            postingListFile = new CustomFileWriter(FilePaths.PARTIAL_PATH + "postinglistcitation" + filenameId + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Start Parsing Citation");
            citationParser.parseFile(FilePaths.RAW_PARTIAL_PATH + filename);
            System.out.println("Finish Parsing Citation");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            tmpPostingList.close();
            save();
            postingListFile.close();
            dictionaryFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void documentParsed(Document document) {
        try {
            for (Integer docId: document.getCitations()) {
                if (docId >= Document.minID && docId <= Document.maxID) {
                    if (values.get(docId) == null) {
                        values.put(docId, tmpPostingList.position());
                        tmpPostingList.write("-1," + document.getDocId());
                    } else {
                        long tmpPos = tmpPostingList.position();
                        tmpPostingList.write(values.get(docId).toString().concat(",") + document.getDocId());
                        values.put(docId, tmpPos);
                    }
                    tmpPostingList.write(";");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            Map<Integer, Long> sortedMap = new TreeMap<>(values);
            RandomAccessFile tmpPostingListReader = new RandomAccessFile(FilePaths.PARTIAL_PATH + "tmppostinglistcitation" + filenameId + ".txt", "r");
            byte separator = ';';
            byte comma = ',';

            for (Map.Entry<Integer, Long> entry : sortedMap.entrySet()) {
                Integer key = entry.getKey();
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
                dictionaryFile.write(key + " ".concat(filePos.toString()).concat("\n"));
                postingListFile.write(postingListEntry.toString());
            }

            tmpPostingListReader.close();
            sortedMap.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finishedParsing() {

    }
}
