package SearchEngine.index;

import SearchEngine.data.CustomFileReader;
import SearchEngine.data.CustomFileWriter;
import SearchEngine.data.FilePaths;
import SearchEngine.utils.NumberParser;

import java.io.IOException;
import java.util.*;

/**
 * Created by sebastian on 26.01.2016.
 */
public class CitationIndex {
    Map<Integer, Long> values = new HashMap<>();

    public void load(String file) throws IOException {
        CustomFileReader docIndex = new CustomFileReader(file);
        List<Byte[]> line;
        int docId;
        long pos;

        while ((line = docIndex.readLineOfSpaceSeparatedValues()) != null) {
            docId = NumberParser.parseDecimalInt(line.get(0));
            pos = NumberParser.parseDecimalLong(line.get(1));
            values.put(docId, pos);
        }
    }

    public void mergePartialIndices(List<String> paritalFileIds) {
        Map<String, List<FileMergeHead>> curTokens = new TreeMap<>();

        for (String partialFile: paritalFileIds) {
            FileMergeHead file = new FileMergeHead("citation" + getIpgId(partialFile));

            if (file.getToken() == null) continue;

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
            CustomFileWriter postingList = new CustomFileWriter(FilePaths.POSTINGLIST_CITATION_PATH);
            CustomFileWriter indexFile = new CustomFileWriter(FilePaths.INDEX_CITATION_PATH);

            while (curTokens.keySet().iterator().hasNext()) {
                String curDocId = curTokens.keySet().iterator().next();
                indexFile.write(curDocId + " " + postingList.position() + "\n");

                for (FileMergeHead file: curTokens.get(curDocId)) {
                    postingList.write(file.getPostinglistLine());

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

                postingList.write("\n");
                curTokens.remove(curDocId);
            }

            postingList.close();
            indexFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public List<Integer> lookUpCitationsInFile(Integer docId) throws IOException {
        List<Integer> results = new LinkedList<>();
        CustomFileReader citationReader = new CustomFileReader(FilePaths.POSTINGLIST_CITATION_PATH);
        citationReader.seek(values.get(docId));

        for (String citationId: citationReader.readLine().split(";")) {
             results.add(NumberParser.parseDecimalInt(citationId));
        }

        return results;
    }


    private String getIpgId(String filename) {
        if (filename.indexOf("ipg") < 0) return "";

        return filename.substring(filename.indexOf("ipg") + 3, filename.indexOf("ipg") + 9);
    }
}
