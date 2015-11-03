package SearchEngine.indexing;

import SearchEngine.data.Document;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Sebastian on 24.10.2015.
 */
public class Index {
    HashMap<String, Long> values = new HashMap<>();

    public Index() {

    }

    /*
    * method printIndex() for testing purposes only
    * also to get an understanding of how to work with HashMaps
    * FYI: (Work in progress)We really should introduce junit soon (tests are also made for a better understanding of code :D)
    */
    // index should still be savable and loadable, so we dont have to index every time we start the program
    public void printIndex() {
        for (String key: values.keySet()) {
            System.out.println(key + "-" + values.get(key));
        }
    }


    public void loadFromFile(BufferedReader reader) {
        try {
            values = new HashMap<>();

            String line = new String();

            while ((line = reader.readLine()) != null) {
                values.put(line.split("[ ]")[0], Long.parseLong(line.split("[ ]")[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Document> lookUpPostingInFile(String word, String file) {
        List<Document> results = new LinkedList<>();

        if (!values.containsKey(word)) {
            return results;
        }

        long postingListLine = values.get(word);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            for (int i = 0; i < postingListLine - 1; ++i) {
                reader.readLine();
            }
            String posting = reader.readLine();
            reader.close();
            String[] metaDataCollection = posting.split("[;]");

            for (String metaData: metaDataCollection) {
                RandomAccessFile xmlReader = new RandomAccessFile("data/testData.xml", "r");
                String[] metaDataValues = metaData.split("[,]");
                int size = metaDataValues.length;
                int patentDocId = Integer.parseInt(metaDataValues[1]);
                long abstractPos = Long.parseLong(metaDataValues[size-4]);
                int abstractLength = Integer.parseInt(metaDataValues[size-3]);
                long inventionTitlePos = Long.parseLong(metaDataValues[size-2]);
                int inventionTitleLength = Integer.parseInt(metaDataValues[size-1]);

                String patentAbstract = readStringFromFile(xmlReader, 4096, abstractPos, abstractLength);
                String title = readStringFromFile(xmlReader, 512, inventionTitlePos, inventionTitleLength);

                Document document = new Document(patentDocId, patentAbstract, title);

                results.add(document);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    private String readStringFromFile(RandomAccessFile file, int buffersize, long pos, int length) {
        byte[] titleBuffer = new byte[buffersize];

        try {
            file.seek(pos);
            file.read(titleBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String(titleBuffer, 0, length);
    }

    public void compressIndex() {
        //read entry from file

        try {
            BufferedReader br = new BufferedReader(new FileReader("data/postinglist.txt"));
            FileWriter fw = new FileWriter("data/compressed_postinglist.txt");

            String line = new String();

            while ((line = br.readLine()) != null) {

                String[] input = line.split(";");

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
                    compressed += convertToVByte(docIds[i]);
                    compressed += convertToVByte(patentDocIdDeltas[i]);
                    compressed += convertToVByte(invTitlePositions[i]);
                    compressed += convertToVByte(abstractPositions[i]);
                    compressed += convertToVByte(invTitleLenghts[i]);
                    compressed += convertToVByte(abstractLenghts[i]);
                    compressed += convertToVByte(numberOfOccurrences[i]);

                    for (int j = 0; j < occurrenceDeltas.get(i).length; j++) {
                        compressed += convertToVByte(occurrenceDeltas.get(i)[j]);
                    }

                    compressed += ";";
                }

                fw = new FileWriter("data/compressed_postinglist.txt", true);

                fw.write(compressed + "\n");

                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decompressLine() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("data/compressed_postinglist.txt"));

            // this should be replaced with an RandomAccessFile reader
            String line = br.readLine();

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

            System.out.println(decompressed);

        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private String convertToVByte(long input) {
        String binaryString = Long.toBinaryString(input);

        String vByteString = new String();

        if (binaryString.length() >= 8) {
            int count = 0;

            for (int i = binaryString.length(); i > 0; i--) {
                ++count;

                if ((count % 8) == 0 && count == 8) {
                    vByteString = '1' + vByteString;
                    ++i;
                } else if ((count % 8) == 0 && count != 8) {
                    vByteString = '0' + vByteString;
                    ++i;
                } else {
                    vByteString = binaryString.charAt(i-1) + vByteString;
                }
            }
        } else {
            int offset = binaryString.length() % 8;

            vByteString = binaryString;

            for (int i = 0; i < 8 - offset - 1; i++) {
                vByteString = '0' + vByteString;
            }

            vByteString = '1' + vByteString;
        }

        String outputString = Long.toHexString(new BigInteger(vByteString, 2).longValue());

        if ((outputString.length() % 2) != 0) {
            outputString = '0' + outputString;
        }

        return outputString;
    }
}
