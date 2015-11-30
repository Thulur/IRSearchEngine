package SearchEngine.data;

import SearchEngine.utils.WordParser;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        inventionTitle = removeTags(readTillTag(cacheReader, inventionTitlePos, inventionTitleLength, "</invent"));
        patentAbstract = removeTags(readTillTag(cacheReader, patentAbstractPos, patentAbstractLength, "</abstract"));
    }

    public String getDocIndexEntry() {
        return docId + " " + inventionTitlePos + " " + patentAbstractPos + " " + inventionTitleLength + " " + patentAbstractLength;
    }

    private String readTillTag(RandomAccessFile file, long pos, long length, String tag) throws IOException {
        int tmpLength = 2 * Math.toIntExact(length);
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
        // For coloring and highlighting take a look at https://en.wikipedia.org/wiki/ANSI_escape_code
        // http://askubuntu.com/questions/528928/how-to-do-underline-bold-italic-strikethrough-color-background-and-size-i
        int displayedChars = 160;
        StringBuilder snippet = new StringBuilder();
        ArrayList<String> booleanTokens = new ArrayList<>();
        booleanTokens.add("OR");
        booleanTokens.add("AND");
        booleanTokens.add("NOT");

        ArrayList<Integer> indices = new ArrayList<>();

        for (String queryTerm: query.split(" ")) {
            if (booleanTokens.contains(queryTerm)) continue;

            String stemmedTerm = WordParser.getInstance().stemSingleWord(queryTerm);
            int index = patentAbstract.toLowerCase().indexOf(stemmedTerm);
            if (index >= 0) indices.add(index);
        }

        snippet.append("\t\t");

        if (indices.size() >= 1) {
            Collections.sort(indices);
            int start = ((indices.get(0) - 50) < 0) ? 0 : indices.get(0) - 50;
            int end = ((indices.get(indices.size()-1) + 50) > patentAbstractLength) ? (int) patentAbstractLength : indices.get(indices.size()-1) + 50;

            for (int i = start; i >= 0; --i) {
                if ((Character.isUpperCase(patentAbstract.charAt(i)) && (i == 0 || patentAbstract.charAt(i-2) == '.'))
                        || (i >= 2 && patentAbstract.charAt(i-2) == ';')) {
                    start = i;
                    i = 0;
                }
            }

            for (int i = end; i < patentAbstract.length()-1 && end - start < displayedChars; ++i) {
                if (patentAbstract.charAt(i) == ' ') {
                    end = i + 1;
                }
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

        for (int i = 1; i < snippet.length() / 80; ++i) {
            int newlinePos = snippet.toString().indexOf(' ', i * 80);
            snippet.replace(newlinePos, newlinePos + 1, "\n\t\t");
        }

        snippet = new StringBuilder(colorWords(snippet, query.split(" "), booleanTokens, "\033[34;0m", "\033[30;0m"));
        snippet.replace(0, 0, colorWords(new StringBuilder("0" + docId + " " + inventionTitle + "\n"), query.split(" "), booleanTokens, "\033[34;1m", "\033[33;1m"));

        return snippet.toString();
    }

    private String colorWords(StringBuilder input, String[] terms, List<String> ignored, String highlight, String standard) {
        for (String queryTerm: terms) {
            if (ignored.contains(queryTerm)) continue;
            int pos = -1;
            String stemmedTerm = WordParser.getInstance().stemSingleWord(queryTerm);
            input.replace(0, 0, standard);
            String tmpSnippet = input.toString().toLowerCase();

            while ((pos = tmpSnippet.indexOf(stemmedTerm, pos + 1)) != -1) {
                if (pos == 0 || tmpSnippet.charAt(pos - 1) == ' ') {
                    input.replace(pos, pos, highlight);
                    int nextSpace = input.indexOf(" ", pos);

                    if (nextSpace != -1) {
                        input.replace(nextSpace, nextSpace, standard);
                        tmpSnippet = input.toString().toLowerCase();
                        pos = nextSpace;
                    } else {
                        input.append(standard);
                    }
                }
            }
            // Reset all formatting options
            input.append("\033[0m");
        }

        return input.toString();
    }
}
