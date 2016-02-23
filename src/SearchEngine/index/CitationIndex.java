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
    Map<Integer, Double> pageRank = new HashMap<>();

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


    public void computePageRanks() {
        HashMap<Integer, List<Integer>> citationGraph = new HashMap<>();
        HashMap<Integer, List<Integer>> inverseCitationGraph = new HashMap<>();

        for (Integer docId: values.keySet()) {
            //TODO: think about correct handling of exception
            try {
                citationGraph.put(docId, lookUpCitationsInFile(docId));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //compute inverse graph
        for (Integer docId: citationGraph.keySet()) {
            inverseCitationGraph.put(docId, new LinkedList<>());
        }
        for (Integer docId: citationGraph.keySet()) {
            for (Integer targetDocId: citationGraph.get(docId)) {
                if (inverseCitationGraph.get(targetDocId) != null) {
                    inverseCitationGraph.get(targetDocId).add(docId);
                }
            }
        }

        // numberOfPatents should be the number of all patents in the dataset
        // this is assuming that every patent occurs in the citationIndex
        double numberOfPatents = 1117856;
        double DAMPING_FACTOR = 0.85;

        for (Integer docId: citationGraph.keySet()) {
            pageRank.put(docId, (1.0/numberOfPatents));
        }
        for (int i = 0; i < 50; ++i) {
            HashMap<Integer, Double> tmpPageRank = new HashMap<>();

            for (Integer docId: pageRank.keySet()) {
                double sum = 0.0;
                for (Integer inLink: citationGraph.get(docId)) {
                    if (pageRank.get(inLink) != null) {
                        sum += pageRank.get(inLink)/inverseCitationGraph.get(inLink).size();
                    } else {
                        sum += 1.0/numberOfPatents;
                    }
                }
                double rank = (1 - DAMPING_FACTOR)/numberOfPatents + DAMPING_FACTOR * sum;
                tmpPageRank.put(docId, rank);
            }

            pageRank = tmpPageRank;
        }

        double sum = 0;
        for (Double value: pageRank.values()) {
            sum += value;
        }

        sum += (numberOfPatents - citationGraph.size()) * ((1 - DAMPING_FACTOR)/numberOfPatents);

        System.out.println("Sum: " + sum);
        System.out.println("7861321: " + pageRank.get(7861321));
        System.out.println("7886437: " + pageRank.get(7886437));
        System.out.println("8074432: " + pageRank.get(8074432));
        System.out.println("8074897: " + pageRank.get(8074897));
        System.out.println("8074994: " + pageRank.get(8074994));
    }

    private Double computeRankOfPage(Integer docId, HashMap<Integer, List<Integer>> citationGraph, HashMap<Integer, List<Integer>> inverseCitationGraph, double DAMPING_FACTOR) {
        double sum = 0.0;
        if (citationGraph.get(docId) != null) {
            for (Integer inLink: citationGraph.get(docId)) {
                if (inverseCitationGraph.get(inLink) != null) {
                    sum += computeRankOfPage(inLink, citationGraph, inverseCitationGraph, DAMPING_FACTOR)/inverseCitationGraph.get(inLink).size();
                } else {
                    sum += computeRankOfPage(inLink, citationGraph, inverseCitationGraph, DAMPING_FACTOR)/1.0;
                }
            }
        } else {
            sum = 1.0/(double)citationGraph.size();
        }
        double rank = (1 - DAMPING_FACTOR/(double)citationGraph.size()) * sum;
        return rank;
    }

    private String getIpgId(String filename) {
        if (filename.indexOf("ipg") < 0) return "";

        return filename.substring(filename.indexOf("ipg") + 3, filename.indexOf("ipg") + 9);
    }
}
