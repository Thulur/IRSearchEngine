package SearchEngine.data;

import SearchEngine.utils.WordParser;

import javax.print.Doc;
import java.io.*;
import java.util.*;

/**
 * Created by Sebastian on 24.10.2015.
 */
public class Index {
    HashMap<String, Integer> values = new HashMap<>();
//    private FileWriter fileWriter;
//    private LineNumberReader lineNumberReader;

    public Index() {
        try {
            FileWriter fileWriter = new FileWriter("data/postinglist.txt");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToIndex(Document document) {

        List<String> words = WordParser.getInstance().stem(document.getPatentAbstract());
        for (String word: WordParser.getInstance().stem(document.getInventionTitle())) {
            if (!words.contains(word)) {
                words.add(word);
            }
        }
        WordParser.getInstance().removeStopwords(words);

        String patentAbstract = document.getPatentAbstract();
        String inventionTitle = document.getInventionTitle();

        try {
            String fileName = "data/postinglist.txt";

            int numberOfLines = countLines(fileName);

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

                //Wort im Index?

                if (values.get(word) == null) {
                    FileWriter fileWriter = new FileWriter(fileName, true);

                    fileWriter.write(word + "|" + metaData.toString() + "\n");
                    numberOfLines++;
                    values.put(word, numberOfLines);
                    fileWriter.close();
                } else {
                    int lineNumber = values.get(word);

                    try (LineNumberReader lnr = new LineNumberReader(new FileReader(fileName))) {
                        for (int i = 0; i < lineNumber - 1; ++i) {
                            lnr.readLine();
                        }

                        String lineToWrite = lnr.readLine();

                        lnr.close();

                        rewriteFile(fileName, lineToWrite, metaData.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    private void rewriteFile(String fileName, String compareString, String newString) throws IOException {
        try {
            String fileContent = new String();
            String line = new String();

            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            while ((line = reader.readLine()) != null) {
                if (line.equals(compareString)) {
                    fileContent += line + newString + "\n";
                } else {
                    fileContent += line + "\n";
                }
            }

            reader.close();

            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(fileContent);

            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                values.put(line.split("[,]")[0], Integer.parseInt(line.split("[,]")[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToFile(FileWriter fileWriter) {
        try {

            for (String key: values.keySet()) {
                fileWriter.write(key + "," + values.get(key) + "\n");
            }

            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Document> lookUpPostingInFile(String word, String file) {
        List<Document> results = new LinkedList<>();

        if (!values.containsKey(word)) {
            return results;
        }

        int postingListLine = values.get(word);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            for (int i = 0; i < postingListLine - 1; ++i) {
                reader.readLine();
            }
            String posting = reader.readLine();
            reader.close();
            String metaDataString = posting.split("[|]")[1];
            String[] metaDataCollection = metaDataString.split("[;]");

            for (String metaData: metaDataCollection) {
                RandomAccessFile xmlReader = new RandomAccessFile("data/testData.xml", "r");
                String[] metaDataValues = metaData.split("[,]");
                int patentDocId = Integer.parseInt(metaDataValues[1]);
                long abstractPos = Long.parseLong(metaDataValues[2]);
                int abstractLength = Integer.parseInt(metaDataValues[3]);
                long inventionTitlePos = Long.parseLong(metaDataValues[4]);
                int inventionTitleLength = Integer.parseInt(metaDataValues[5]);

                byte[] abstractBuffer = new byte[4096];
                xmlReader.seek(abstractPos);
                xmlReader.read(abstractBuffer);
                String patentAbstract = new String(abstractBuffer, 0, abstractLength);

                byte[] titleBuffer = new byte[512];
                xmlReader.seek(inventionTitlePos);
                xmlReader.read(titleBuffer);
                String title = new String(titleBuffer, 0, inventionTitleLength);

                Document document = new Document();
                document.setDocId(patentDocId);
                document.setPatentAbstract(patentAbstract);
                document.setInventionTitle(title);

                results.add(document);
            }
            // Parse posting read results
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

}
