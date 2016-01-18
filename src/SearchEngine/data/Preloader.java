package SearchEngine.data;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by sebastian on 24.11.2015.
 */
public class Preloader implements  Runnable {
    int bufferSize = 2 << 16;
    private boolean loadingNeeded = true;
    private byte[] preloadBuffer = new byte[bufferSize];
    RandomAccessFile file;

    public Preloader(RandomAccessFile file) throws IOException {
        this.file = file;
    }

    @Override
    public void run() {
        if (loadingNeeded) {
            try {
                preloadBuffer = new byte[bufferSize];
                file.read(preloadBuffer);
                loadingNeeded = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getPreloadBuffer() {
        loadingNeeded = true;
        return preloadBuffer;
    }

    public boolean isLoadingNeeded() {
        return loadingNeeded;
    }

    public void seek(long position) throws IOException {
        file.seek(position);
        loadingNeeded = true;
    }
}
