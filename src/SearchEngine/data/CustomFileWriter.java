package SearchEngine.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by sebastian on 23.11.2015.
 */
public class CustomFileWriter {
    private int bufferSize = 2 << 15;
    RandomAccessFile file;
    StringBuilder buffer = new StringBuilder();

    public CustomFileWriter(String fileName) throws FileNotFoundException {
        file = new RandomAccessFile(fileName, "rw");
    }

    public void write(String s) throws IOException {
        buffer.append(s);

        if (buffer.length() > (bufferSize)) {
            writeResetBuffer(buffer, file);
        }
    }

    private void flush() throws IOException {
        writeResetBuffer(buffer, file);
    }

    public void close() throws IOException {
        flush();
        file.close();
        file = null;
        buffer = null;
    }

    public long position() throws IOException {
        return file.getFilePointer() + buffer.length();
    }

    private void writeResetBuffer(StringBuilder buffer, RandomAccessFile file) throws IOException {
        file.write(buffer.toString().getBytes("UTF-8"));
        buffer.setLength(0);
    }
}
