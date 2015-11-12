package SearchEngine.index;

import SearchEngine.data.FilePaths;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by sebastian on 10.11.2015.
 */
public class FileMergeHead {
    private RandomAccessFile indexFile;
    private RandomAccessFile postinglistFile;
    private String token;
    private long position;
    private String lastReadPostingLine;

    public FileMergeHead(String fileId) {
        try {

            String indexFilename = FilePaths.PARTIAL_PATH + "index" + fileId + ".txt";
            String postinglistFilename = FilePaths.PARTIAL_PATH + "postinglist" + fileId + ".txt";
            indexFile = new RandomAccessFile(indexFilename, "r");
            postinglistFile = new RandomAccessFile(postinglistFilename, "r");

            String curLine = indexFile.readUTF();
            String[] lineValues = curLine.split("[ ]");
            // A line in the index online contains a token and a position (there should be at least one entry)
            assert lineValues.length == 2;

            token = lineValues[0];
            position = Long.parseLong(lineValues[1]);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean nextLine() {
        String curLine = null;

        try {
            curLine = indexFile.readUTF();
        } catch (IOException e) {

        }

        if (curLine == null) {
            return false;
        } else {
            String[] lineValues = curLine.split("[ ]");

            if (lineValues.length == 2) {
                token = lineValues[0];
                position = Long.parseLong(lineValues[1]);
                return true;
            } else {
                return false;
            }
        }
    }

    public String getPostinglistLine() {
        String result = "";

        // First take a look at cached values
        if (lastReadPostingLine != null) {
            result = lastReadPostingLine;
        } else {
            try {
                result = postinglistFile.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Something should be read every time
        assert result != "";

        // Empty the cache
        lastReadPostingLine = null;

        return result;
    }

    public int getFirstPatentId() {
        try {
            lastReadPostingLine = postinglistFile.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int firstComma = lastReadPostingLine.indexOf(",");
        int secondComma = lastReadPostingLine.indexOf(",", firstComma + 1);

        int patentId = Integer.parseInt(lastReadPostingLine.substring(firstComma + 1, secondComma));

        return patentId;
    }

    public String getToken() {
        return token;
    }

    public long getPosition() {
        return position;
    }
}
