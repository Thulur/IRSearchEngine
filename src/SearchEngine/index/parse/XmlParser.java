package SearchEngine.index.parse;

import SearchEngine.data.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sebastian on 25.01.2016.
 */
public abstract class XmlParser extends DefaultHandler {
    private List<ParsedEventListener> parsedEventListeners;
    private FileInputStream fileInput;

    public XmlParser() {
        super();
    }

    public abstract void startElement(String uri, String name, String qName, Attributes atts);

    public abstract void endElement(String uri, String name, String qName);

    public abstract void characters(char ch[], int start, int length);

    public void parseFile(String file) throws Exception {
        XMLReader xr = XMLReaderFactory.createXMLReader();

        // Ignores the dtd definition for the moment, we do not want to load it
        xr.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        xr.setContentHandler(this);
        xr.setErrorHandler(this);

        // Parse each file provided on the
        // command line.
        fileInput = new FileInputStream(file);
        InputSource source = new InputSource(fileInput);
        xr.parse(source);
        fileInput.close();
        fileInput = null;
    }

    public void notifyEventListeners(Document document) {
        if (parsedEventListeners != null){
            for (ParsedEventListener eventListener: parsedEventListeners) {
                eventListener.documentParsed(document);
            }
        }
    }

    public long curPos() throws IOException {
        return fileInput.getChannel().position();
    }

    ////////////////////////////////////////////////////////////////////
    // Event handlers.
    ////////////////////////////////////////////////////////////////////
    public void startDocument() {
        return;
    }

    public void endDocument() {
        if (parsedEventListeners != null) {
            for (ParsedEventListener eventListener : parsedEventListeners) {
                eventListener.finishedParsing();
            }
        }
    }

    public void addDocumentParsedListener(ParsedEventListener eventListener) {
        if (parsedEventListeners == null) {
            parsedEventListeners = new LinkedList<>();
        }

        parsedEventListeners.add(eventListener);
    }
}
