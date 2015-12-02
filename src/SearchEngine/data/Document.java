package SearchEngine.data;

import SearchEngine.data.output.OutputFormat;
import SearchEngine.utils.WordParser;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created by sebastian on 22.10.2015.
 */
public class Document {
    private int docId;
    private int fileId;
    private String inventionTitle = "";
    private long inventionTitlePos;
    private long inventionTitleLength;
    private String patentAbstract = "";
    private long patentAbstractPos;
    private long patentAbstractLength;

    public Document() {

    }

    public Document(int docId, String cacheFile) {
        this.docId = docId;
    }

    public Document(Posting posting) {
        this.docId = posting.getDocId();
        this.fileId = posting.getFileId();
    }

    public Document(int docId, String inventionTitle, String patentAbstract) {
        this.docId = docId;
        this.inventionTitle = inventionTitle;
        this.patentAbstract = patentAbstract;
    }

    public void loadPatentData(RandomAccessFile cacheReader) throws IOException {
        inventionTitle = removeTags(readTillTag(cacheReader, inventionTitlePos, inventionTitleLength, 2, "</invent"));
        patentAbstract = removeTags(readTillTag(cacheReader, patentAbstractPos, patentAbstractLength, 2, "</abstract"));
    }

    public String getDocIndexEntry() {
        return docId + " " + inventionTitlePos + " " + patentAbstractPos + " " + inventionTitleLength + " " + patentAbstractLength;
    }

    private String readTillTag(RandomAccessFile file, long pos, long length, int realLengthFactor, String tag) throws IOException {
        int tmpLength = realLengthFactor * Math.toIntExact(length);
        byte[] readData = new byte[tmpLength];

        file.seek(pos);
        file.read(readData);

        int tagPos = 0;
        int bufferPos = 0;
        while (bufferPos < tmpLength && tagPos < tag.length()) {
            if (readData[bufferPos] == tag.charAt(tagPos)) {
                ++tagPos;
            } else {
                tagPos = 0;
            }

            ++bufferPos;
        }

        return new String(readData, 0, bufferPos - tag.length(), "UTF-8");
    }

    private String readLineFromFile(RandomAccessFile file, long pos, long length) throws IOException {
        int tmpLength = Math.toIntExact(length);
        byte[] readData = new byte[tmpLength];

        file.seek(pos);
        file.read(readData);

        return new String(readData, 0, tmpLength);
    }

    private String removeTags(String s) {
        String tagsRemoved = s.replaceAll("<.[^(><.)]*>", "");
        String htmlUnescaped = StringEscapeUtils.unescapeHtml4(tagsRemoved);
        String removedNewlines = htmlUnescaped.replaceAll("[\\t\\n\\r]", " ");
        String removedSpaces = removedNewlines.replaceAll("[ ]+", " ");
        return removedSpaces;
    }

    public String getInventionTitle() {
        return inventionTitle;
    }

    public void setInventionTitle(String inventionTitle) {
        this.inventionTitle = inventionTitle;
    }

    public long getInventionTitlePos() {
        return inventionTitlePos;
    }

    public void setInventionTitlePos(long inventionTitlePos) {
        this.inventionTitlePos = inventionTitlePos;
    }

    public long getInventionTitleLength() {
        return inventionTitleLength;
    }

    public void setInventionTitleLength(long inventionTitleLength) {
        this.inventionTitleLength = inventionTitleLength;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public String getPatentAbstract() {
        return patentAbstract;
    }

    public void setPatentAbstract(String patentAbstract) {
        this.patentAbstract = patentAbstract;
    }

    public long getPatentAbstractPos() {
        return patentAbstractPos;
    }

    public void setPatentAbstractPos(long patentAbstractPos) {
        this.patentAbstractPos = patentAbstractPos;
    }

    public long getPatentAbstractLength() {
        return patentAbstractLength;
    }

    public void setPatentAbstractLength(long patentAbstractLength) {
        this.patentAbstractLength = patentAbstractLength;
    }

    public int getFileId() {
        return fileId;
    }

    public String generateSnippet(String query) {
        return generateSnippet(query, null);
    }

    public String generateSnippet(String query, OutputFormat outputFormat) {
        // For coloring and highlighting take a look at https://en.wikipedia.org/wiki/ANSI_escape_code
        // http://askubuntu.com/questions/528928/how-to-do-underline-bold-italic-strikethrough-color-background-and-size-i
        int displayedChars = 200;
        StringBuilder snippet = new StringBuilder();
        ArrayList<String> booleanTokens = new ArrayList<>();
        booleanTokens.add("OR");
        booleanTokens.add("AND");
        booleanTokens.add("NOT");

        // Occurrence average

        Map<String, List<Integer>> indicesMap = new HashMap<>();

        String[] queryTerms = query.split(" ");

        int sum = 0;
        int average = 0;

        for (String queryTerm: queryTerms) {
            if (indicesMap.get(queryTerm) == null) {
                indicesMap.put(queryTerm, new ArrayList<>());
            }

            String stemmedTerm = WordParser.getInstance().stemSingleWord(queryTerm);

            for (int i = -1; (i = patentAbstract.toLowerCase().indexOf(stemmedTerm, i + 1)) != -1; ) {
                indicesMap.get(queryTerm).add(i);
                sum += i;
            }

            if (indicesMap.get(queryTerm).size() != 0) {
                average += sum/indicesMap.get(queryTerm).size();
            }
            sum = 0;
        }

        average = average/queryTerms.length;

        Map<String, Integer> nearestIndices = new HashMap<>();

        for (String queryTerm: indicesMap.keySet()) {
            if (nearestIndices.get(queryTerm) == null) {
                if (indicesMap.get(queryTerm).size() > 0) nearestIndices.put(queryTerm, indicesMap.get(queryTerm).get(0));
            }

            for (int index: indicesMap.get(queryTerm)) {
                if (Math.abs(average - index) < Math.abs(average - nearestIndices.get(queryTerm))) {
                    nearestIndices.put(queryTerm, index);
                }
            }
        }

        List<Integer> indices = new ArrayList<>(nearestIndices.values());

        int CONTEXT_RANGE = 50;

        if (indices.size() >= 1) {
            Collections.sort(indices);
            int start = ((indices.get(0) - CONTEXT_RANGE) < 0) ? 0 : indices.get(0) - CONTEXT_RANGE;
            int end = ((indices.get(indices.size() - 1) + CONTEXT_RANGE) > patentAbstract.length()) ? patentAbstract.length() : indices.get(indices.size() - 1) + CONTEXT_RANGE;


            for (int i = start; i >= 0; --i) {
                if ((Character.isUpperCase(patentAbstract.charAt(i)) && (i == 0 || patentAbstract.charAt(i-2) == '.'))
                        || (i >= 2 && patentAbstract.charAt(i-2) == ';')) {
                    start = i;
                    i = 0;
                }
            }

            if (end - start < displayedChars) {
                for (int i = end; i < patentAbstract.length()-1 && end - start < displayedChars; ++i) {
                    if (patentAbstract.charAt(i) == ' ') {
                        end = i + 1;
                    }
                }
            } else {
                end = (patentAbstract.indexOf(" ", end) != -1) ? patentAbstract.indexOf(" ", end) : patentAbstract.length() - 1;
            }

            if ((end - start) < displayedChars) {
                for (int i = start; i >= 0 && end - start < displayedChars; --i) {
                    if ((Character.isUpperCase(patentAbstract.charAt(i)) && (i == 0 || patentAbstract.charAt(i-2) == '.'))
                            || (i >= 2 && patentAbstract.charAt(i-2) == ';')) {
                        start = i;
                    }
                }
            }

            snippet.append(patentAbstract.substring(start, end));
            snippet.append("...");
        } else {
            int end = (displayedChars < (patentAbstract.length() - 1)) ? displayedChars : patentAbstract.length() - 1;

            for (int i = end; i < patentAbstract.length() -1; ++i) {
                if (patentAbstract.charAt(i) == ' ') {
                    end = i + 1;
                    i = patentAbstract.length();
                }
            }

            snippet.append(patentAbstract.substring(0, end));
            snippet.append("...");
        }

        if (outputFormat != null) {
            formatSnippet(snippet, query.split(" "), booleanTokens, outputFormat);
        } else {
            snippet.replace(0, 0, "0" + docId + " " + inventionTitle + " ");
        }

        return snippet.toString();
    }

    private void formatSnippet(StringBuilder snippet, String[] terms, List<String> ignored, OutputFormat outputFormat) {
        int charsPerLine = 80;
        double possibleNumLines = Math.ceil(1d * snippet.length() / charsPerLine);

        for (int i = 1; i < possibleNumLines; ++i) {
            int newlinePos = snippet.indexOf(" ", i * charsPerLine);

            if (newlinePos != -1) {
                snippet.replace(newlinePos, newlinePos + 1, "\n\t\t");
            }
        }

        List<String> tmpTerms = Arrays.asList(terms);
        tmpTerms.removeAll(ignored);

        StringBuilder titleString = new StringBuilder("0" + docId + " " + inventionTitle + "\n\t\t");
        colorWords(snippet, tmpTerms, outputFormat.getTextHighlight(), outputFormat.getTextStandard(), outputFormat.getEnd());
        colorWords(titleString, tmpTerms, outputFormat.getTitleHighlight(), outputFormat.getTitleStandard(), outputFormat.getEnd());

        snippet.replace(0, 0, titleString.toString());
    }

    private void colorWords(StringBuilder input, List<String> terms, String highlight, String standard, String end) {
        int pos = -1;
        input.replace(0, 0, standard);
        String tmpSnippet = input.toString().toLowerCase();

        for (String queryTerm: terms) {
            String term = WordParser.getInstance().stemSingleWord(queryTerm);
            while ((pos = tmpSnippet.indexOf(term, pos + 1)) != -1) {
                // Take into account that we already replace some whitespaces with \n\t\t
                if (pos == 0 || tmpSnippet.charAt(pos - 1) == ' ' || tmpSnippet.charAt(pos - 1) == '\t') {
                    input.replace(pos, pos, highlight);
                    input.replace(pos, pos, end);
                    int nextSpace = input.indexOf(" ", pos + highlight.length());

                    if (input.indexOf("\t", pos) != -1 && input.indexOf("\t", pos) < nextSpace) {
                        nextSpace = input.indexOf("\t", pos + highlight.length());
                    }

                    if (nextSpace != -1) {
                        input.replace(nextSpace, nextSpace, standard);
                        input.replace(nextSpace, nextSpace, end);
                        tmpSnippet = input.toString().toLowerCase();
                        pos = nextSpace;
                    } else {
                        input.append(end);
                    }
                }
            }
        }
        // Reset all formatting options
        input.append(end);
    }
}
