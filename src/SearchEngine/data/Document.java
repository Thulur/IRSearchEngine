package SearchEngine.data;

import SearchEngine.data.output.OutputFormat;
import SearchEngine.utils.WordParser;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

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
    private String description;
    private String claims;

    public Document() {

    }

    public Document(int docId, String cacheFile) {
        this.docId = docId;
    }

    public Document(Posting posting) {
        this.docId = posting.getDocId();
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
        return fileId + " " + docId + " " + inventionTitlePos + " " + patentAbstractPos + " " + inventionTitleLength + " " + patentAbstractLength;
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

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClaims() {
        return claims;
    }

    public void setClaims(String claims) {
        this.claims = claims;
    }

    public String generateSnippet(String query) {
        return generateSnippet(query, -1, null);
    }

    public String generateSnippet(String query, double ndcg, OutputFormat outputFormat) {
        // For coloring and highlighting take a look at https://en.wikipedia.org/wiki/ANSI_escape_code
        // http://askubuntu.com/questions/528928/how-to-do-underline-bold-italic-strikethrough-color-background-and-size-i
        int displayedChars = 200;
        boolean isPhraseQuery = false;
        StringBuilder snippet = new StringBuilder();
        ArrayList<String> booleanTokens = new ArrayList<>();
        booleanTokens.add("OR");
        booleanTokens.add("AND");
        booleanTokens.add("NOT");
        Map<String, Integer> sentenceRanking = new HashMap<>();

        // Occurrence average

        Map<String, List<Integer>> indicesMap = new HashMap<>();

        String[] queryTerms = query.split(" ");

        if (query.startsWith("\"") && query.endsWith("\"")) {
            queryTerms = query.substring(1, query.length() - 1).split(" ");
            isPhraseQuery = true;
        }

        for (String sentence: patentAbstract.split("[;.]")) {
            sentenceRanking.put(sentence, 0);
        }

        for (Map.Entry<String, Integer> ranking: sentenceRanking.entrySet()) {
            for (String queryTerm: queryTerms) {
                if (booleanTokens.contains(queryTerm)) continue;

                int termCount = StringUtils.countMatches(ranking.getKey().toLowerCase(), WordParser.getInstance().stemSingleWord(queryTerm.toLowerCase()));
                sentenceRanking.put(ranking.getKey(), ranking.getValue() + termCount);
            }
        }

        List<HashMap.Entry<String, Integer>> tmpList = new ArrayList<>(sentenceRanking.entrySet());
        Collections.sort(tmpList, (obj1, obj2) -> ((Comparable) ((obj2)).getValue()).compareTo(((obj1)).getValue()));

        if (tmpList.get(0).getValue() != 0) {
            snippet.append(tmpList.get(0).getKey());

            int i = 1;
            while (snippet.length() < displayedChars && i < tmpList.size()) {
                snippet.append(tmpList.get(i).getKey());

                if (snippet.length() > displayedChars) {
                    snippet.setLength(displayedChars);
                    snippet.append("...");
                }

                ++i;
            }
        } else  if (patentAbstract.length() < displayedChars) {
            snippet.append(patentAbstract);
            snippet.append("...");
        } else {
            snippet.append(patentAbstract.substring(0, displayedChars));
            snippet.append("...");
        }


        if (outputFormat != null) {
            formatSnippet(snippet, queryTerms, booleanTokens, ndcg, outputFormat, isPhraseQuery);
        } else {
            snippet.replace(0, 0, "0" + docId + " " + inventionTitle + " ");
        }

        return snippet.toString();
    }

    private void formatSnippet(StringBuilder snippet, String[] terms, List<String> ignored, double ndcg, OutputFormat outputFormat, boolean isPhraseQuery) {
        List<String> allTerms = Arrays.asList(terms);
        List<String> tmpTerms = new ArrayList<>();
        for (String term: allTerms) {
            if (ignored.contains(term)) continue;

            tmpTerms.add(term);
        }

        StringBuilder titleString = new StringBuilder("0" + docId + "\t" + inventionTitle);
        if (isPhraseQuery) {
            colorPhrase(snippet, tmpTerms, outputFormat.getTextHighlight(),
                    outputFormat.getTextStandard(), outputFormat.getEnd());
            colorPhrase(titleString, tmpTerms, outputFormat.getTitleHighlight(),
                    outputFormat.getTitleStandard(), outputFormat.getEnd());
            formatLines(snippet);
        } else {
            formatLines(snippet);
            colorWords(snippet, tmpTerms, outputFormat.getTextHighlight(),
                    outputFormat.getTextStandard(), outputFormat.getEnd());
            colorWords(titleString, tmpTerms, outputFormat.getTitleHighlight(),
                    outputFormat.getTitleStandard(), outputFormat.getEnd());
        }

        snippet.replace(0, 0, titleString.toString() + "\t" + ndcg + "\n");
    }

    private void formatLines(StringBuilder snippet) {
        int charsPerLine = 80;
        double possibleNumLines = Math.ceil(1d * snippet.length() / charsPerLine);

        for (int i = 1; i < possibleNumLines; ++i) {
            int newlinePos = snippet.indexOf(" ", i * charsPerLine);

            if (newlinePos != -1) {
                snippet.replace(newlinePos, newlinePos + 1, "\n");
            }
        }

        snippet.append("\n");
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

    private void colorPhrase(StringBuilder input, List<String> terms, String textHighlight, String textStandard, String end) {
        input.insert(0, textStandard);
        String tmpSnippet = input.toString().toLowerCase();
        int pos = 0;
        boolean notComplete = true;

        while ((pos = tmpSnippet.indexOf(WordParser.getInstance().stemSingleWord(terms.get(0)), pos)) != -1 && notComplete) {
            int start = pos;
            boolean phraseFound = false;

            for (int i = 1; i < terms.size() && notComplete; ++i) {
                int wordPos = tmpSnippet.indexOf(WordParser.getInstance().stemSingleWord(terms.get(i)), pos);
                int spacePos = tmpSnippet.indexOf(" ", pos);

                if (wordPos == spacePos + 1 && i == terms.size() - 1) {
                    pos = tmpSnippet.indexOf(" ", pos);
                    if (pos == -1) notComplete = false;
                    ++pos;
                    pos = tmpSnippet.indexOf(" ", pos);
                    if (pos == -1) notComplete = false;
                    phraseFound = true;
                }
                else if (wordPos == spacePos + 1) {
                    pos = tmpSnippet.indexOf(" ", pos);
                    if (pos == -1) notComplete = false;
                    ++pos;
                } else {
                    // If the first word is found but not the second, do not test the same position again, skip it
                    ++pos;
                }
            }

            if (phraseFound) {
                if (pos == -1) {
                    input.insert(start, textHighlight);
                    input.append(end);
                } else {
                    input.insert(pos, end);
                    input.insert(start, textHighlight);
                    pos += end.length() + textHighlight.length();
                }

                tmpSnippet = input.toString().toLowerCase();
            }
        }

        input.append(end);
    }
}
