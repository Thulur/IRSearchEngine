package SearchEngine.index;

import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.data.Posting;
import SearchEngine.utils.IndexEncoder;
import SearchEngine.utils.NumberParser;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Sebastian on 24.10.2015.
 */
public class Index {
    TreeMap<String, Long> values = new TreeMap<>();
    Map<Integer, String> fileIds = new HashMap<>();
    int numDocuments;

    public Index() {

    }

    public void loadFromFile(String file) {
        try {
            String line;

            BufferedReader docIdsFile = new BufferedReader(new FileReader(FilePaths.FILE_IDS_FILE));

            while ((line = docIdsFile.readLine()) != null) {
                String[] splitEntry = line.split("[ ]");

                // Skip empty lines at the end of the file
                if (splitEntry.length < 2) continue;

                fileIds.put(Integer.parseInt(splitEntry[0]), splitEntry[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String line;

            RandomAccessFile indexFile = indexFile = new RandomAccessFile(file, "r");
            values = new TreeMap<>();

            while ((line = indexFile.readUTF()) != null) {
                if (line == null) {
                    int i = 0;
                }

                String[] splitEntry = line.split("[ ]");

                // Skip empty lines at the end of the file
                if (splitEntry.length < 2) continue;

                values.put(splitEntry[0], Long.parseLong(splitEntry[1]));
            }
        } catch (IOException e) {

        }
    }

    public void mergePartialIndices(List<String> paritalFiles, int numPatents) {
        Map<String, List<FileMergeHead>> curTokens = new TreeMap<>();
        numDocuments = numPatents;
        for (String partialFile: paritalFiles) {
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
            RandomAccessFile tmpIndexFile = new RandomAccessFile(FilePaths.INDEX_PATH + ".tmp", "rw");
            RandomAccessFile tmpPostingList = new RandomAccessFile(FilePaths.POSTINGLIST_PATH + ".tmp", "rw");
            RandomAccessFile indexFile = new RandomAccessFile(FilePaths.INDEX_PATH, "rw");
            RandomAccessFile postingList = new RandomAccessFile(FilePaths.POSTINGLIST_PATH, "rw");
            Map<String, Double> docWeights = new HashMap<>();

            // The iterator use is intended here because the collection changes every iteration
            while (curTokens.keySet().iterator().hasNext()) {
                String curWord = curTokens.keySet().iterator().next();
                Map<Integer, FileMergeHead> sortedPostings = new TreeMap<>();

                tmpIndexFile.writeUTF(curWord + " " + tmpPostingList.getChannel().position());

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
                    Double docVector = (1 + Math.log10(Double.parseDouble(values[Posting.POSTING_NUM_OCC_POS - 1]))) * Math.log10(numPatents / entryCount);
                    double docVectorSum;

                    if (docWeights.containsKey(values[Posting.POSTING_DOC_ID_POS - 1])) {
                        docVectorSum = docWeights.get(values[Posting.POSTING_DOC_ID_POS - 1]) + docVector * docVector;
                    } else {
                        docVectorSum = docVector * docVector;
                    }

                    docWeights.put(values[Posting.POSTING_DOC_ID_POS - 1], docVectorSum);
                    vectorLine.append(docVector + "," + entry + ";");
                }

                tmpPostingList.writeBytes(vectorLine.toString() + "\n");
                curTokens.remove(curWord);
            }

            // Reset vector file position
            tmpPostingList.seek(0);
            String line;
            StringBuilder processedLine = new StringBuilder();
            loadFromFile(FilePaths.INDEX_PATH + ".tmp");
            Iterator<String> indexEntryIterator = values.keySet().iterator();
            while ((line = tmpPostingList.readLine()) != null && indexEntryIterator.hasNext()) {
                for (String entry: line.split("[;]")) {
                    String[] entryValues = entry.split("[,]");
                    Double normalizedWeight = Double.parseDouble(entryValues[Posting.POSTING_WEIGHT_POS]) /
                            Math.sqrt(docWeights.get(entryValues[Posting.POSTING_DOC_ID_POS]));
                    processedLine.append(Math.round(1000 * normalizedWeight) + entry.substring(entry.indexOf(",")) + ";");
                }

                processedLine.append("\n");
                String temp = indexEntryIterator.next();
                indexFile.writeUTF(temp + " " + postingList.getFilePointer());
                postingList.writeBytes(processedLine.toString());
                processedLine.setLength(0);
            }

            tmpIndexFile.close();
            tmpPostingList.close();
            File deleteTmpIndexFile = new File(FilePaths.INDEX_PATH + ".tmp");
            deleteTmpIndexFile.delete();
            File deleteTmpPostinglistFile = new File(FilePaths.INDEX_PATH + ".tmp");
            deleteTmpPostinglistFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getIpgId(String filename) {
        if (filename.indexOf("ipg") < 0) return "";

        return filename.substring(filename.indexOf("ipg") + 3, filename.indexOf("ipg") + 9);
    }

    private String readStringFromFile(RandomAccessFile file, long pos) {
        String result = "";

        try {
            file.seek(pos);
            result = file.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void compressIndex() {
        //read entry from file
        try {
            RandomAccessFile postingReader = new RandomAccessFile(FilePaths.POSTINGLIST_PATH, "r");
            RandomAccessFile indexWriter = new RandomAccessFile(FilePaths.COMPRESSED_INDEX_PATH, "rw");
            RandomAccessFile postingWriter = new RandomAccessFile(FilePaths.COMPRESSED_POSTINGLIST_PATH, "rw");

            loadFromFile(FilePaths.INDEX_PATH);

            String curLine;
            StringBuilder compressed = new StringBuilder();
            int i;
            int semicolonPos;
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
            for (String key: values.keySet()) {
                compressed.setLength(0);
                i = 0;
                lastEntry = false;
                numCount = 0;
                numOcc = 0;
                lastPatentId = 0;
                curLine = postingReader.readLine();

                while (i < curLine.length()) {
                    commaPos = curLine.indexOf(",", i);
                    semicolonPos = curLine.indexOf(";", i);
                    if (commaPos != -1 && commaPos < semicolonPos) {
                        separatorPos = commaPos;
                    } else {
                        separatorPos = semicolonPos;

                        if (commaPos == -1) {
                            lastEntry = true;
                        }
                    }

                    curNum = Long.parseLong(curLine.substring(i, separatorPos));

                    if (numCount == Posting.POSTING_DOC_ID_POS) {
                        patentIdDelta = curNum - lastPatentId;
                        lastPatentId = curNum;
                        compressed.append(IndexEncoder.convertToVByte(patentIdDelta));
                    } else if (numCount == Posting.POSTING_NUM_OCC_POS) {
                        compressed.append(IndexEncoder.convertToVByte(curNum));
                        numOcc = curNum;
                    } else if (numCount >= Posting.POSTING_NUM_OCC_POS + 1) {
                        occurrenceDelta = curNum - lastOccurrence;
                        lastOccurrence = curNum;
                        compressed.append(IndexEncoder.convertToVByte(occurrenceDelta));

                            if (numCount == numOcc + Posting.POSTING_NUM_OCC_POS) {
                            numCount = -1;
                            lastOccurrence = 0;

                            if (lastEntry) {
                                break;
                            }
                        }
                    } else {
                        compressed.append(IndexEncoder.convertToVByte(curNum));
                    }

                    i = separatorPos + 1;
                    ++numCount;
                }

                seek = postingWriter.getFilePointer();

                postingWriter.writeBytes(compressed + "\n");
                indexWriter.writeUTF(key + " " + seek);
            }
            postingWriter.close();
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Posting> decompressLine(String line) {
        List<Posting> postings = new LinkedList<>();
        int numCount = 0;
        long patentId = 0;
        long occurrence = 0;
        long numOcc = 0;
        int lastStart = 0;
        long curNum;
        Posting posting = new Posting();

        for (int i = 0; i < line.length() - 1; i += 2) {
            // A hexadecimal value greater or equal 8 is found => the 8th bit of a byte is set
            if (line.charAt(i) >= '8') {
                curNum = convertToDecimal(NumberParser.parseHexadecimalLong(line.substring(lastStart, i + 2)));

                if (numCount == Posting.POSTING_WEIGHT_POS) {
                    posting.setWeight(Math.toIntExact(curNum) / 1000d);
                }
                else if (numCount == Posting.POSTING_FILE_ID_POS) {
                    posting.setCacheFile(FilePaths.CACHE_PATH + fileIds.get(Math.toIntExact(curNum)));
                }
                else if (numCount == Posting.POSTING_DOC_ID_POS) {
                    patentId += curNum;
                    posting.setDocId(Math.toIntExact(patentId));
                } else if (numCount == Posting.POSTING_TITLE_L_POS) {
                    posting.setInventionTitlePos(curNum);
                } else if (numCount == Posting.POSTING_ABSTRACT_L_POS) {
                    posting.setAbstractPos(curNum);
                } else if (numCount == Posting.POSTING_TITLE_P_POS) {
                    posting.setInventionTitleLength(curNum);
                } else if (numCount == Posting.POSTING_ABSTRACT_P_POS) {
                    posting.setAbstractLength(curNum);
                } else if (numCount == Posting.POSTING_NUM_OCC_POS) {
                    numOcc = curNum;
                } else if (numCount >= Posting.POSTING_NUM_OCC_POS + 1) {
                    occurrence += curNum;
                    posting.addWordOccurrence(occurrence);

                    if (numCount == numOcc + Posting.POSTING_NUM_OCC_POS) {
                        numCount = -1;
                        occurrence = 0;
                        postings.add(posting);
                        posting = new Posting();
                    }
                }

                ++numCount;
                lastStart = i + 2;
            }
        }

        return postings;
    }

    private long convertToDecimal(long vByteValue) {
        long result = 0;
        int i = 0;

        while (vByteValue > 0) {
            result += ((vByteValue & 127) << (7 * i));
            vByteValue >>= 8;
            ++i;
        }

        return result;
    }

    public List<Document> lookUpPostingInFile(String word) {
        List<Document> results = new LinkedList<>();

        if (!values.containsKey(word)) {
            return results;
        }

        long postingListSeek = values.get(word);

        try {
            RandomAccessFile postingReader = new RandomAccessFile(FilePaths.POSTINGLIST_PATH, "r");

            postingReader.seek(postingListSeek);
            String posting = postingReader.readLine();
            postingReader.close();

            RandomAccessFile xmlReader = new RandomAccessFile(FilePaths.RAW_PARTIAL_PATH + "testData.xml", "r");

            String[] metaDataCollection = posting.split("[;]");

            for (String metaData: metaDataCollection) {
                String[] metaDataValues = metaData.split("[,]");
                int patentDocId = Integer.parseInt(metaDataValues[1]);
                long inventionTitlePos = Long.parseLong(metaDataValues[2]);
                long abstractPos = Long.parseLong(metaDataValues[3]);

                String patentAbstract = readStringFromFile(xmlReader, abstractPos);
                String title = readStringFromFile(xmlReader, inventionTitlePos);

                Document document = new Document(patentDocId, patentAbstract, title);

                results.add(document);
            }
            xmlReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    public List<Posting> lookUpPostingInFileWithCompression(String word) {
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
            if (!values.containsKey(word)) {
                return results;
            } else {
                matches.add(values.get(word));
            }
        }

        for (long postingListSeek: matches) {
            try {
                RandomAccessFile postingReader = new RandomAccessFile(FilePaths.COMPRESSED_POSTINGLIST_PATH, "r");

                postingReader.seek(postingListSeek);
                String postingListLine = postingReader.readLine();
                results = decompressLine(postingListLine);

                for (Posting posting: results) {
                    posting.setToken(word);
                }

                postingReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    public int getNumDocuments() {
        return numDocuments;
    }

    public String getCacheFile(int fileId) {
        return fileIds.get(fileId);
    }
}
