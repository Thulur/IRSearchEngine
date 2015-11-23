package SearchEngine.data;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
        ArrayList<Byte> lineBuffer = new ArrayList<>();
        updateBuffer();

        while (indexBuffer[indexBufferPos] != '\n' && indexBuffer[indexBufferPos] != 0) {
            lineBuffer.add(indexBuffer[indexBufferPos]);
            ++indexBufferPos;

            updateBuffer();
        }

        if (lineBuffer.size() == 0) {
            return null;
        }

        ++indexBufferPos;
        String result = "";
        try {
            Byte[] tmpBuffer = new Byte[lineBuffer.size()];
            lineBuffer.toArray(tmpBuffer);
            result = new String(ArrayUtils.toPrimitive(tmpBuffer), "UTF-8");

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
