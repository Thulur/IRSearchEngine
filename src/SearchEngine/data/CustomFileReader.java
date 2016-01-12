package SearchEngine.data;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sebastian on 23.11.2015.
 */
public class CustomFileReader {
    RandomAccessFile file;
    int bufferSize = 2 << 16;
    private byte[] buffer = new byte[bufferSize];
    private int bufferPos = -1;
    Thread preloadThread;
    Preloader preloader;

    public CustomFileReader(String fileName) throws IOException {
        file = new RandomAccessFile(fileName, "r");

        preloader = new Preloader(file);
        preloadThread = new Thread(preloader);
        preloader.run();
    }

    public byte[] read() throws IOException {
        updateBuffer();
        bufferPos = bufferSize;
        return buffer;
    }

    public List<Byte[]> readLineOfSpaceSeparatedValues() throws IOException {
        List<Byte[]> result = new LinkedList<>();
        ArrayList<Byte> lineBuffer = new ArrayList<>();
        Byte[] tmpBuffer;
        updateBuffer();

        while (buffer[bufferPos] != '\n' && buffer[bufferPos] != 0) {
            if (buffer[bufferPos] == ' ') {
                tmpBuffer = new Byte[lineBuffer.size()];
                lineBuffer.toArray(tmpBuffer);
                result.add(tmpBuffer);
                lineBuffer = new ArrayList<>();
            } else {
                lineBuffer.add(buffer[bufferPos]);
            }

            ++bufferPos;
            updateBuffer();
        }
        ++bufferPos;

        if (lineBuffer.size() > 0) {
            tmpBuffer = new Byte[lineBuffer.size()];
            result.add(lineBuffer.toArray(tmpBuffer));
        }

        if (result.size() == 0) {
            preloadThread.interrupt();
            return null;
        }

        return result;
    }

    public String readLineTill(char character) throws IOException {
        ArrayList<Byte> lineBuffer = new ArrayList<>();
        updateBuffer();

        while (buffer[bufferPos] != '\n' && buffer[bufferPos] != 0 && buffer[bufferPos] != character) {
            lineBuffer.add(buffer[bufferPos]);
            ++bufferPos;
            updateBuffer();
        }

        ++bufferPos;
        if (lineBuffer.size() == 0) {
            preloadThread.interrupt();
            return null;
        }

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

    public String readLine() throws IOException {
        ArrayList<Byte> lineBuffer = new ArrayList<>();
        updateBuffer();

        while (buffer[bufferPos] != '\n' && buffer[bufferPos] != 0) {
            lineBuffer.add(buffer[bufferPos]);
            ++bufferPos;
            updateBuffer();
        }

        if (lineBuffer.size() == 0) {
            preloadThread.interrupt();
            return null;
        }

        ++bufferPos;
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
        preloadThread = null;
        preloader = null;
        buffer = null;
        file = null;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    private void updateBuffer() throws IOException {
        if (bufferPos < 0 || bufferPos >= bufferSize) {
            while (preloader.isLoadingNeeded()) continue;
            buffer = preloader.getPreloadBuffer();
            preloader.run();
            bufferPos = 0;
        }
    }
}
