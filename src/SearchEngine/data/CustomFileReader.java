package SearchEngine.data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by sebastian on 23.11.2015.
 */
public class CustomFileReader {
    RandomAccessFile file;
    private byte[] indexBuffer = new byte[16384];
    private int indexBufferPos = -1;

    public CustomFileReader(String fileName) throws IOException {
        file = new RandomAccessFile(fileName, "r");
        updateBuffer();
    }

    public String readLine() throws IOException {
        byte[] lineBuffer = new byte[2048];
        int linePos = 0;
        updateBuffer();

        while (indexBuffer[indexBufferPos] != '\n' && indexBuffer[indexBufferPos] != 0) {
            lineBuffer[linePos] = indexBuffer[indexBufferPos];
            ++linePos;
            ++indexBufferPos;

            updateBuffer();
        }

        if (linePos == 0) {
            return null;
        }

        ++indexBufferPos;
        String result = "";
        try {
            result = new String(lineBuffer, 0, linePos, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void close() throws IOException {
        file.close();
    }

    private void updateBuffer() throws IOException {
        if (indexBufferPos < 0 || indexBufferPos >= 16384) {
            Arrays.fill(indexBuffer, (byte) 0);
            file.read(indexBuffer);
            indexBufferPos = 0;
        }
    }
}
