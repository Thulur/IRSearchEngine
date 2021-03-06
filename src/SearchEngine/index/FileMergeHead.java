package SearchEngine.index;

import SearchEngine.data.CustomFileReader;
import SearchEngine.data.FilePaths;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Created by sebastian on 10.11.2015.
 */
public class FileMergeHead {
    private CustomFileReader indexFile;
    private CustomFileReader postinglistFile;
    private String token;
    private long position;
    private String lastReadPostingLine;

    public FileMergeHead(String fileId) {
        try {
            String indexFilename = FilePaths.PARTIAL_PATH + "index" + fileId + ".txt";
            String postinglistFilename = FilePaths.PARTIAL_PATH + "postinglist" + fileId + ".txt";
            indexFile = new CustomFileReader(indexFilename);
            postinglistFile = new CustomFileReader(postinglistFilename);

            String curLine = indexFile.readLine();
            if (curLine != null) {
                String[] lineValues = curLine.split("[ ]");
                // A line in the index online contains a token and a position (there should be at least one entry)
                assert lineValues.length == 2;

                token = lineValues[0];
                position = Long.parseLong(lineValues[1]);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean nextIndexLine() {
        String curLine = null;
        try {
            curLine = indexFile.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (curLine != null) {
            String[] lineValues = curLine.split("[ ]");

            if (lineValues.length == 2) {
                token = lineValues[0];

                position = Long.parseLong(lineValues[1]);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String getPostinglistLine() {
        loadPostinglistLine();
        String result = lastReadPostingLine;

        // Empty the cache
        lastReadPostingLine = null;

        return result;
    }

    public int docNumInCurLine() {
        loadPostinglistLine();
        // Every patent entry ends with a semicolon
        return StringUtils.countMatches(lastReadPostingLine, ";");
    }

    public int getFirstPatentId() {
        loadPostinglistLine();

        int firstComma = lastReadPostingLine.indexOf(",");
        int patentId = Integer.parseInt(lastReadPostingLine.substring(0, firstComma));

        return patentId;
    }

    public String getToken() {
        return token;
    }

    public long getPosition() {
        return position;
    }

    private void loadPostinglistLine() {
        if (lastReadPostingLine == null) {
            try {
                lastReadPostingLine = postinglistFile.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
