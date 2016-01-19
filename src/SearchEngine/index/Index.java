package SearchEngine.index;

import SearchEngine.data.*;
import SearchEngine.utils.NumberParser;
import SearchEngine.utils.VByte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Sebastian on 24.10.2015.
 */
public class Index {
    TreeMap<String, Long> values = new TreeMap<>();
    TreeMap<String, Long> skipValues = new TreeMap<>();
    DocumentIndex docIndex = new DocumentIndex();
    String indexFile;
    int numDocuments = -1;

    public Index() {

    }

    public void loadFromFile(String indexFile, String docIndexFile) throws IOException {
        String line;
        CustomFileReader skipIndexFileReader;

        this.indexFile = indexFile;
        docIndex.loadFileIds();

        skipIndexFileReader = new CustomFileReader(indexFile.concat(".skp"));
        while ((line = skipIndexFileReader.readLine()) != null) {
            // Skip empty lines at the end of the file
            if (line.indexOf(" ") < 0) continue;

            skipValues.put(line.substring(0, line.indexOf(" ")), NumberParser.parseDecimalLong(line.substring(line.indexOf(" ") + 1)));
        }

        skipIndexFileReader.close();

        docIndex.load(docIndexFile);
    }

    public void mergePartialIndices(List<String> paritalFileIds, int numPatents) {
        Map<String, List<FileMergeHead>> curTokens = new TreeMap<>();
        numDocuments = numPatents;

        for (String partialFile: paritalFileIds) {
            FileMergeHead file = new FileMergeHead(getIpgId(partialFile));

            if (curTokens.containsKey(file.getToken())) {
                List<FileMergeHead> tmpList = curTokens.get(file.getToken());
                tmpList.add(file);
            } else {
                List<FileMergeHead> tmpList = new LinkedList<>();
                tmpList.add(file);
                curTokens.put(file.getToken(), tmpList);
            }
        }

        try {
            RandomAccessFile indexDataFile = new RandomAccessFile(FilePaths.INDEX_DATA_FILE, "rw");
            CustomFileWriter tmpIndexFile = new CustomFileWriter(FilePaths.INDEX_PATH + ".tmp");
            CustomFileWriter tmpPostingList = new CustomFileWriter(FilePaths.POSTINGLIST_PATH + ".tmp");
            CustomFileWriter indexFile = new CustomFileWriter(FilePaths.INDEX_PATH);
            CustomFileWriter postingList = new CustomFileWriter(FilePaths.POSTINGLIST_PATH);
            Map<String, Double> docWeights = new HashMap<>();

            // Create document index
            createDocIndex(paritalFileIds);

            // Write number of patents to file
            indexDataFile.writeBytes(String.valueOf(numDocuments));
            indexDataFile.close();
            
            docIndex.loadFileIds();
            docIndex.loadTmp();

            // The iterator use is intended here because the collection changes every iteration
            while (curTokens.keySet().iterator().hasNext()) {
                String curWord = curTokens.keySet().iterator().next();
                Map<Integer, FileMergeHead> sortedPostings = new TreeMap<>();
                tmpIndexFile.write(curWord + " " + tmpPostingList.position() + "\n");

                for (FileMergeHead file: curTokens.get(curWord)) {
                    sortedPostings.put(file.getFirstPatentId(), file);

                    if (file.nextIndexLine()) {
                        if (curTokens.containsKey(file.getToken())) {
                            List<FileMergeHead> tmpList = curTokens.get(file.getToken());
                            tmpList.add(file);
                        } else {
                            List<FileMergeHead> tmpList = new LinkedList<>();
                            tmpList.add(file);
                            curTokens.put(file.getToken(), tmpList);
                        }
                    }
                }

                Iterator<FileMergeHead> files = sortedPostings.values().iterator();
                StringBuilder line = new StringBuilder();
                StringBuilder vectorLine = new StringBuilder();
                int entryCount = 0;
                while (files.hasNext()) {
                    FileMergeHead file = files.next();
                    entryCount += file.docNumInCurLine();
                    line.append(file.getPostinglistLine());
                }

                for (String entry: line.toString().split("[;]")) {
                    String[] values = entry.split(",");

                    Double docVector = (1 + Math.log10(Double.parseDouble(values[Posting.POSTING_NUM_OCC_POS - 1]))) * Math.log10(1 + (numPatents / entryCount));
                    double docVectorSum;

                    if (docWeights.containsKey(values[Posting.POSTING_DOC_ID_POS - 1])) {
                        docVectorSum = docWeights.get(values[Posting.POSTING_DOC_ID_POS - 1]) + docVector * docVector;
                    } else {
                        docVectorSum = docVector * docVector;
                    }

                    docWeights.put(values[Posting.POSTING_DOC_ID_POS - 1], docVectorSum);

                    Posting posting = new Posting().fromStringWithoutWeight(entry);
                    double maxFactor = 0;
                    if (posting.getOccurrences().get(0) < docIndex.numWordsTitleInEntry(posting.getDocId())) {
                        maxFactor = Configuration.TITLE_EXTRA_WEIGHT_FACTOR;
                    } else if (posting.getOccurrences().get(0) < docIndex.numWordsTitleInEntry(posting.getDocId()) + docIndex.numWordsAbstractInEntry(posting.getDocId())) {
                        maxFactor = Configuration.ABSTRACT_EXTRA_WEIGHT_FACTOR;
                    }
                    docVector += maxFactor * docVector;

                    vectorLine.append(docVector);
                    vectorLine.append(",".concat(entry).concat(";"));
                }

                tmpPostingList.write(vectorLine.toString().concat("\n"));
                curTokens.remove(curWord);
            }
            tmpPostingList.close();
            tmpIndexFile.close();

            normalizeTermDocWeight(indexFile, postingList, docWeights);

            cleanupDocIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanupDocIndex() throws IOException {
        File deleteDocIndexFile = new File(FilePaths.DOCINDEX_FILE);
        File newDocIndexFile = new File(FilePaths.DOCINDEX_FILE.concat(".tmp"));
        deleteDocIndexFile.renameTo(newDocIndexFile);
        CustomFileReader docIndexReader = new CustomFileReader(FilePaths.DOCINDEX_FILE.concat(".tmp"));
        CustomFileWriter docIndexWriter = new CustomFileWriter(FilePaths.DOCINDEX_FILE);
        String line;

        while ((line = docIndexReader.readLine()) != null) {
            StringBuilder cutLine = new StringBuilder(line);
            cutLine.setLength(cutLine.lastIndexOf(" "));
            cutLine.setLength(cutLine.lastIndexOf(" "));
            docIndexWriter.write(cutLine.toString().concat("\n"));
        }

        docIndexReader.close();
        docIndexWriter.close();

        docIndex = new DocumentIndex();
    }

    private void createDocIndex(List<String> paritalFileIds) throws IOException {
        CustomFileWriter docIndexWriter = new CustomFileWriter(FilePaths.DOCINDEX_FILE.concat(".tmp"));

        for (String fileId: paritalFileIds) {
            CustomFileReader docIndexReader = new CustomFileReader(FilePaths.PARTIAL_PATH + "docindex" + getIpgId(fileId) + ".txt");
            String line;

            while ((line = docIndexReader.readLine()) != null) {
                docIndexWriter.write(line + "\n");
            }

            docIndexReader.close();
        }

        docIndexWriter.close();
    }

    private void normalizeTermDocWeight(CustomFileWriter indexFile, CustomFileWriter postingList, Map<String, Double> docWeights) throws IOException {
        CustomFileReader tmpPostingListReader = new CustomFileReader(FilePaths.POSTINGLIST_PATH + ".tmp");
        CustomFileReader tmpIndexReader = new CustomFileReader(FilePaths.INDEX_PATH + ".tmp");
        String postingLine;
        String indexLine;
        StringBuilder processedEntry = new StringBuilder();
        while ((indexLine = tmpIndexReader.readLine()) != null) {
            String temp = indexLine.split("[ ]")[0];
            indexFile.write(temp.concat(" ") + postingList.position() + "\n");

            while ((postingLine = tmpPostingListReader.readLineTill(';')) != null) {
                String[] entryValues = postingLine.split("[,]");
                Double normalizedWeight = Double.parseDouble(entryValues[Posting.POSTING_WEIGHT_POS]) /
                        Math.sqrt(docWeights.get(entryValues[Posting.POSTING_DOC_ID_POS]));
                processedEntry.append(Math.round(100000 * normalizedWeight) + postingLine.substring(postingLine.indexOf(",")).concat(";"));
                postingList.write(processedEntry.toString());
                processedEntry.setLength(0);
            }

            processedEntry.append("\n");
        }

        tmpPostingListReader.close();
        File deleteTmpIndexFile = new File(FilePaths.INDEX_PATH + ".tmp");
        deleteTmpIndexFile.delete();
        File deleteTmpPostinglistFile = new File(FilePaths.POSTINGLIST_PATH + ".tmp");
        deleteTmpPostinglistFile.delete();
    }

    public Document buildDocument(Posting posting) throws IOException {
        return docIndex.buildDocument(posting);
    }

    public RandomAccessFile getCacheFile(int fileId) throws FileNotFoundException {
        return docIndex.getCacheFile(fileId);
    }

    private String getIpgId(String filename) {
        if (filename.indexOf("ipg") < 0) return "";

        return filename.substring(filename.indexOf("ipg") + 3, filename.indexOf("ipg") + 9);
    }

    public void compressIndex() {
        //read entry from file
        try {
            CustomFileReader postingReader = new CustomFileReader(FilePaths.POSTINGLIST_PATH);
            CustomFileReader indexReader = new CustomFileReader(FilePaths.INDEX_PATH);
            CustomFileWriter indexWriter = new CustomFileWriter(FilePaths.COMPRESSED_INDEX_PATH);
            CustomFileWriter indexSkipWriter = new CustomFileWriter(FilePaths.COMPRESSED_INDEX_PATH + ".skp");
            CustomFileWriter postingWriter = new CustomFileWriter(FilePaths.COMPRESSED_POSTINGLIST_PATH);

            String curEntry;
            String key;
            StringBuilder compressed = new StringBuilder();
            int indexEntries = 0;
            int i;
            int commaPos;
            int separatorPos;
            long lastPatentId;
            long patentIdDelta;
            long lastOccurrence = 0;
            long occurrenceDelta;
            int numCount;
            long numOcc;
            long curNum;
            long seek;
            boolean lastEntry;
            while ((key = indexReader.readLine()) != null) {
                lastEntry = false;
                numCount = 0;
                numOcc = 0;
                lastPatentId = 0;
                key = key.split(" ")[0];

                ++indexEntries;
                if (indexEntries % 10000 == 0) {
                    indexSkipWriter.write(key.concat(" ") + indexWriter.position() + "\n");
                }

                seek = postingWriter.position();
                indexWriter.write(key.concat(" ") + seek + "\n");

                while ((curEntry = postingReader.readLineTill(';')) != null) {
                    i = 0;
                    compressed.setLength(0);

                    while (i < curEntry.length()) {
                        commaPos = curEntry.indexOf(",", i);
                        if (commaPos != -1) {
                            separatorPos = commaPos;
                        } else {
                            separatorPos = curEntry.length();
                            lastEntry = true;
                        }

                        curNum = Long.parseLong(curEntry.substring(i, separatorPos));

                        if (numCount == Posting.POSTING_DOC_ID_POS) {
                            patentIdDelta = curNum - lastPatentId;
                            lastPatentId = curNum;
                            compressed.append(VByte.encode(patentIdDelta));
                        } else if (numCount == Posting.POSTING_NUM_OCC_POS) {
                            compressed.append(VByte.encode(curNum));
                            numOcc = curNum;
                        } else if (numCount >= Posting.POSTING_NUM_OCC_POS + 1) {
                            occurrenceDelta = curNum - lastOccurrence;
                            lastOccurrence = curNum;
                            compressed.append(VByte.encode(occurrenceDelta));

                            if (numCount == numOcc + Posting.POSTING_NUM_OCC_POS) {
                                numCount = 0;
                                lastOccurrence = 0;

                                if (lastEntry) {
                                    break;
                                }
                            }
                        } else {
                            compressed.append(VByte.encode(curNum));
                        }

                        i = separatorPos + 1;
                        ++numCount;
                    }

                    postingWriter.write(compressed.toString());
                }

                postingWriter.write("\n");
            }
            postingWriter.close();
            indexSkipWriter.close();
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Posting> decompressLine(long readPos) throws IOException {
        TreeSet<Posting> postings = new TreeSet<>();
        int numCount = 0;
        long patentId = 0;
        long occurrence = 0;
        long numOcc = 0;
        long curNum;
        Posting posting = new Posting();
        CustomFileReader postingReader;
        byte[] buffer;
        byte[] curNumBuffer = new byte[18];
        int curNumBufferLength = 0;

        postingReader = new CustomFileReader(FilePaths.COMPRESSED_POSTINGLIST_PATH);
        postingReader.seek(readPos);
        buffer = postingReader.read();

        for (int i = 0; ; i += 2) {
            if (buffer[i] == '\n') {
                break;
            }

            curNumBuffer[curNumBufferLength] = buffer[i];
            curNumBuffer[curNumBufferLength + 1] = buffer[i + 1];
            curNumBufferLength += 2;

            // A hexadecimal value greater or equal 8 is found => the 8th bit of a byte is set
            if (buffer[i] >= '8') {
                curNum = VByte.decode(NumberParser.parseHexadecimalLong(curNumBuffer, 0, curNumBufferLength));

                if (numCount == Posting.POSTING_WEIGHT_POS) {
                    posting.setWeight(Math.toIntExact(curNum) / 100000d);
                }
                else if (numCount == Posting.POSTING_DOC_ID_POS) {
                    patentId += curNum;
                    posting.setDocId(Math.toIntExact(patentId));
                } else if (numCount == Posting.POSTING_NUM_OCC_POS) {
                    numOcc = curNum;
                } else if (numCount >= Posting.POSTING_NUM_OCC_POS + 1) {
                    occurrence += curNum;
                    posting.addWordOccurrence(occurrence);

                    if (numCount == numOcc + Posting.POSTING_NUM_OCC_POS) {
                        numCount = -1;
                        occurrence = 0;

                        if (postings.size() > 10000) {
                            if (postings.first().compareTo(posting) < 0) {
                                postings.add(posting);
                                postings.pollFirst();
                            }
                        } else {
                            postings.add(posting);
                        }

                        posting = new Posting();
                    }
                }

                ++numCount;
                curNumBufferLength = 0;
            }

            if (i == buffer.length - 2) {
                buffer = postingReader.read();

                // Set i to -2 because the for loop directly increases it to 0
                i = -2;
            }
        }

        return new ArrayList<>(postings);
    }

    public List<Posting> lookUpPostingInFile(String word) throws IOException {
        List<Posting> results = new LinkedList<>();

        ArrayList<Long> matches = new ArrayList<>();

        if (word.contains("*")) {
            word = word.replaceAll("\\*", "\\\\w*");

            Matcher matcher;

            for (String key: values.keySet()) {
                matcher = Pattern.compile(word).matcher(key);

                if (matcher.find()) matches.add(values.get(key));
            }
        } else {
            loadIndexAt(word);

            if (!values.containsKey(word)) {
                return results;
            } else {
                matches.add(values.get(word));
            }
        }

        for (long postingListSeek: matches) {
            results = decompressLine(postingListSeek);

            for (Posting posting: results) {
                posting.setToken(word);
            }
        }

        return results;
    }

    private void loadIndexAt(String word) throws IOException {
        String line;
        CustomFileReader indexFileReader;
        long position = skipValues.floorEntry(word).getValue();
        int readValues = 0;

        indexFileReader = new CustomFileReader(indexFile);
        indexFileReader.seek(position);
        values.clear();
        while ((line = indexFileReader.readLine()) != null && readValues < 10000) {
            // Skip empty lines at the end of the file
            if (line.indexOf(" ") < 0) continue;

            values.put(line.substring(0, line.indexOf(" ")), NumberParser.parseDecimalLong(line.substring(line.indexOf(" ") + 1)));
            ++readValues;
        }

        indexFileReader.close();
    }

    public int getNumDocuments() {
        if (numDocuments < 0) {
            try {
                CustomFileReader indexDataFile = new CustomFileReader(FilePaths.INDEX_DATA_FILE);
                numDocuments = Integer.parseInt(indexDataFile.readLine());
                indexDataFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return numDocuments;
    }
}
