package SearchEngine.index.parse;

import SearchEngine.data.Document;

/**
 * Created by sebastian on 22.10.2015.
 */
public interface ParsedEventListener {
    void documentParsed(Document document);
    void finishedParsing();
}
