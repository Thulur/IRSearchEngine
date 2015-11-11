package SearchEngine.index;

import SearchEngine.data.Document;
import SearchEngine.data.FilePaths;
import SearchEngine.utils.IndexEncoder;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Sebastian on 24.10.2015.
 */
public class Index {
    TreeMap<String, Long> values = new TreeMap<>();
    Map<Integer, String> docIds = new HashMap<>();

    public Index() {

    }

    public void loadFromFile(String file) {
        try {
            String line;

            BufferedReader docIdsFile = new BufferedReader(new FileReader(FilePaths.DOC_IDS_FILE));

            while ((line = docIdsFile.readLine()) != null) {
                String[] splitEntry = line.split("[ ]");

                // Skip empty lines at the end of the file
                if (splitEntry.length < 2) continue;

                docIds.put(Integer.parseInt(splitEntry[0]), splitEntry[1]);
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

    public void mergePartialIndices(List<String> paritalFiles) {
        if (paritalFiles.size() == 1) {
            String filenameId = "";
            String filename = paritalFiles.get(0);

            if (filename.indexOf("ipg") != -1) {
                filenameId = getIpgId(filename);
            }

            replaceIndexWithPartial(filenameId);
        } else {
            Map<String, List<FileMergeHead>> curTokens = new TreeMap<>();

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
                RandomAccessFile index = new RandomAccessFile(FilePaths.INDEX_PATH, "rw");;
                RandomAccessFile postingList = new RandomAccessFile(FilePaths.POSTINGLIST_PATH, "rw");

                // The iterator use is intended here because the collection changes every iteration
                while (curTokens.keySet().iterator().hasNext()) {
                    String curWord = curTokens.keySet().iterator().next();
                    Map<Integer, FileMergeHead> sortedPostings = new TreeMap<>();

                    index.writeUTF(curWord + " " + postingList.getChannel().position());

                    for (FileMergeHead file: curTokens.get(curWord)) {
                        sortedPostings.put(file.getFirstPatentId(), file);

                        if (file.nextLine()) {
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
                    while (files.hasNext()) {
                        FileMergeHead file = files.next();
                        postingList.writeBytes(file.getPostinglistLine());
                    }

                    postingList.writeBytes("\n");

                    curTokens.remove(curWord);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getIpgId(String filename) {
        return filename.substring(filename.indexOf("ipg") + 3, filename.indexOf("ipg") + 9);
    }

    private void replaceIndexWithPartial(String filenameId) {
        File oldIndex = new File(FilePaths.PARTIAL_PATH + "index" + filenameId + ".txt");
        File newIndex = new File(FilePaths.INDEX_PATH);
        File oldPostingList = new File(FilePaths.PARTIAL_PATH + "postinglist" + filenameId + ".txt");
        File newPostingList = new File(FilePaths.POSTINGLIST_PATH);

        try {
            Files.copy(oldIndex.toPath(), newIndex.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(oldPostingList.toPath(), newPostingList.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            BufferedReader postingReader = new BufferedReader(new FileReader(FilePaths.POSTINGLIST_PATH));
            RandomAccessFile indexWriter = new RandomAccessFile(FilePaths.COMPRESSED_INDEX_PATH, "rw");
            RandomAccessFile postingWriter = new RandomAccessFile(FilePaths.COMPRESSED_POSTINGLIST_PATH, "rw");

            loadFromFile(FilePaths.INDEX_PATH);

            for (String key: values.keySet()) {
                String[] input = postingReader.readLine().split(";");

                int[] docIds = new int[input.length];
                long[] patentDocIds = new long[input.length];
                int[] numberOfOccurrences = new int[input.length];
                LinkedList<long[]> occurrences = new LinkedList<>();
                long[] abstractPositions = new long[input.length];
                int[] abstractLenghts = new int[input.length];
                long[] invTitlePositions = new long[input.length];
                int[] invTitleLenghts = new int[input.length];

                for (int i = 0; i < input.length; i++) {
                    String[] split = input[i].split(",");

                    docIds[i] = Integer.parseInt(split[0]);
                    patentDocIds[i] = Long.parseLong(split[1]);
                    invTitlePositions[i] = Long.parseLong(split[2]);
                    abstractPositions[i] = Long.parseLong(split[3]);
                    invTitleLenghts[i] = Integer.parseInt(split[4]);
                    abstractLenghts[i] = Integer.parseInt(split[5]);
                    numberOfOccurrences[i] = Integer.parseInt(split[6]);

                    occurrences.add(i, new long[numberOfOccurrences[i]]);

                    for (int j = 0; j < numberOfOccurrences[i]; j++) {
                        occurrences.get(i)[j] = Long.parseLong(split[j+7]);
                    }
                }

                long[] patentDocIdDeltas = new long[input.length];
                patentDocIdDeltas[0] = patentDocIds[0];

                LinkedList<long[]> occurrenceDeltas = new LinkedList<>();

                for (int i = 1; i < input.length ; i++) {
                    patentDocIdDeltas[i] = patentDocIds[i] - patentDocIds[i-1];
                }

                for (int i = 0; i < input.length; i++) {
                    occurrenceDeltas.add(i, new long[numberOfOccurrences[i]]);

                    // error prevention, see issue #10 on github
                    if (numberOfOccurrences[i] > 0) {

                        occurrenceDeltas.get(i)[0] = occurrences.get(i)[0];

                        for (int j = 1; j < occurrences.get(i).length ; j++) {
                            occurrenceDeltas.get(i)[j] = occurrences.get(i)[j] - occurrences.get(i)[j-1];
                        }

                    }
                }

                // create compressed string

                String compressed = new String();

                for (int i = 0; i < input.length; i++) {
                    compressed += IndexEncoder.convertToVByte(docIds[i]);
                    compressed += IndexEncoder.convertToVByte(patentDocIdDeltas[i]);
                    compressed += IndexEncoder.convertToVByte(invTitlePositions[i]);
                    compressed += IndexEncoder.convertToVByte(abstractPositions[i]);
                    compressed += IndexEncoder.convertToVByte(invTitleLenghts[i]);
                    compressed += IndexEncoder.convertToVByte(abstractLenghts[i]);
                    compressed += IndexEncoder.convertToVByte(numberOfOccurrences[i]);

                    for (int j = 0; j < occurrenceDeltas.get(i).length; j++) {
                        compressed += IndexEncoder.convertToVByte(occurrenceDeltas.get(i)[j]);
                    }

                    compressed += ";";
                }

                long seek = postingWriter.getChannel().position();

                postingWriter.writeBytes(compressed + "\n");

                indexWriter.writeUTF(key + " " + seek);
            }
            postingWriter.close();
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String decompressLine(String line) {
        String[] input = line.split(";");

        String[] decimalInput = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            String tempHexString = new String();
            int relevantBitPositionFactor = 0;

            for (int j = 0; j < input[i].length(); j++) {
                tempHexString += "" + input[i].charAt(j) + input[i].charAt(j + 1);
                ++j;

                String tempBinaryString = new BigInteger(tempHexString, 16).toString(2);

                int offset = tempBinaryString.length() % 8;

                if (offset != 0) {
                    for (int k = 0; k < 8 - offset; k++) {
                        tempBinaryString = '0' + tempBinaryString;
                    }
                }

                if (tempBinaryString.charAt(8*relevantBitPositionFactor) == '1') {
                    if (decimalInput[i] == null) {
                        decimalInput[i] = convertToDecimalString(tempBinaryString) + ",";
                    } else {
                        decimalInput[i] += convertToDecimalString(tempBinaryString) + ",";
                    }
                    tempHexString = "";
                    relevantBitPositionFactor = 0;
                } else {
                    ++relevantBitPositionFactor;
                }

            }
            decimalInput[i] = decimalInput[i].substring(0, decimalInput[i].length()-1);
        }

        int[] docIds = new int[decimalInput.length];
        long[] patentDocIdDeltas = new long[decimalInput.length];
        int[] numberOfOccurrences = new int[decimalInput.length];
        LinkedList<long[]> occurrenceDeltas = new LinkedList<>();
        long[] abstractPositions = new long[decimalInput.length];
        int[] abstractLenghts = new int[decimalInput.length];
        long[] invTitlePositions = new long[decimalInput.length];
        int[] invTitleLenghts = new int[decimalInput.length];

        for (int i = 0; i < decimalInput.length; i++) {
            String[] split = decimalInput[i].split(",");

            docIds[i] = Integer.parseInt(split[0]);
            patentDocIdDeltas[i] = Long.parseLong(split[1]);
            invTitlePositions[i] = Long.parseLong(split[2]);
            abstractPositions[i] = Long.parseLong(split[3]);
            invTitleLenghts[i] = Integer.parseInt(split[4]);
            abstractLenghts[i] = Integer.parseInt(split[5]);

            numberOfOccurrences[i] = Integer.parseInt(split[6]);

            occurrenceDeltas.add(i, new long[numberOfOccurrences[i]]);

            for (int j = 0; j < numberOfOccurrences[i]; j++) {
                occurrenceDeltas.get(i)[j] = Long.parseLong(split[j+7]);
            }
        }

        long[] patentDocIds = new long[decimalInput.length];
        patentDocIds[0] = patentDocIdDeltas[0];

        LinkedList<long[]> occurrences = new LinkedList<>();

        for (int i = 1; i < decimalInput.length ; i++) {
            patentDocIds[i] = patentDocIdDeltas[i] + patentDocIds[i-1];
        }

        for (int i = 0; i < decimalInput.length; i++) {
            occurrences.add(i, new long[numberOfOccurrences[i]]);

            // error prevention, see issue #10 on github
            if (occurrenceDeltas.get(i).length > 0) {
                occurrences.get(i)[0] = occurrenceDeltas.get(i)[0];

                for (int j = 1; j < occurrenceDeltas.get(i).length; j++) {
                    occurrences.get(i)[j] = occurrenceDeltas.get(i)[j] + occurrences.get(i)[j-1];
                }
            }
        }

        String decompressed = new String();

        for (int i = 0; i < decimalInput.length; i++) {
            decompressed += docIds[i] + ",";
            decompressed += patentDocIds[i] + ",";
            decompressed += invTitlePositions[i] + ",";
            decompressed += abstractPositions[i] + ",";
            decompressed +=  invTitleLenghts[i] + ",";
            decompressed += abstractLenghts[i] + ",";
            decompressed += numberOfOccurrences[i];

            for (int j = 0; j < occurrences.get(i).length; j++) {
                decompressed += "," + occurrences.get(i)[j];
            }

            decompressed += ";";
        }

        return decompressed;
    }

    private String convertToDecimalString(String tempBinaryString) {

        String strippedBinaryString = new String();

        for (int i = 0; i < tempBinaryString.length(); i++) {
            if ((i % 8) != 0) {
                strippedBinaryString += tempBinaryString.charAt(i);
            }
        }

        Long decimalValue = Long.parseLong(strippedBinaryString, 2);

        String decimalString = decimalValue.toString();

        return decimalString;
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
                int size = metaDataValues.length;
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

    public List<Document> lookUpPostingInFileWithCompression(String word) {
        List<Document> results = new LinkedList<>();

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
                String posting = postingReader.readLine();

                posting = decompressLine(posting);

                postingReader.close();
                // TODO:
                // Most random line ever, just open a file so java does not throw an error (improve this solution somehow!!!)
                RandomAccessFile xmlReader = new RandomAccessFile(FilePaths.CACHE_PATH + docIds.get(0), "r");

                int lastDocId = 0;
                String[] metaDataCollection = posting.split("[;]");

                for (String metaData: metaDataCollection) {
                    String[] metaDataValues = metaData.split("[,]");
                    int curDocId = Integer.parseInt(metaDataValues[0]);

                    if (lastDocId != curDocId) {
                        xmlReader = new RandomAccessFile(FilePaths.CACHE_PATH + docIds.get(curDocId), "r");
                    }

                    int patentDocId = Integer.parseInt(metaDataValues[1]);
                    long inventionTitlePos = Long.parseLong(metaDataValues[2]);
                    long abstractPos = Long.parseLong(metaDataValues[3]);

                    String patentAbstract = readStringFromFile(xmlReader, abstractPos);
                    String title = readStringFromFile(xmlReader, inventionTitlePos);

                    Document document = new Document(patentDocId, title, patentAbstract);

                    results.add(document);
                }
                xmlReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return results;
    }
}
