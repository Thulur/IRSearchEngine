package SearchEngine;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by dennis on 26.10.15.
 */
public class PostingListWriter {

    public PostingListWriter() {
        try {
            FileWriter fileWriter = new FileWriter("data/postinglist.txt");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
