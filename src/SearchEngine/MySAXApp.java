package SearchEngine;

import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.*;

public class MySAXApp extends DefaultHandler {
	boolean patentGrantEntered = false;
	boolean abstractEntered = false;
	boolean publicationReferenceEntered = false;
	boolean inventionTitleEntered = false;
	boolean docNumberEntered = false;
	boolean abstractParagraphEntered = false;
	Document document;
	List<ParsedEventListener> parsedEventListeners;

	public MySAXApp() {
		super();
	}

	public void parseFiles(List<String> files) throws Exception {
		XMLReader xr = XMLReaderFactory.createXMLReader();
		
		// Ignores the dtd definition for the moment, we do not want to load it
		xr.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		xr.setContentHandler(this);
		xr.setErrorHandler(this);

		// Parse each file provided on the
		// command line.
		for (String file: files) {
			FileReader r = new FileReader(file);
			InputSource source = new InputSource(r);
			xr.parse(source);
		}
	}

	////////////////////////////////////////////////////////////////////
	// Event handlers.
	////////////////////////////////////////////////////////////////////

	public void startDocument() {
		return;
	}

	public void endDocument() {
		return;
	}

	public void startElement(String uri, String name, String qName, Attributes atts) {
		
		switch (name) {
		case "us-patent-grant":
			this.patentGrantEntered = true;
			this.document = new Document();
			break;
		case "abstract":
			this.abstractEntered = true;
			break;
		case "p":
			this.abstractParagraphEntered = true;
			break;
		case "publication-reference":
			this.publicationReferenceEntered = true;
			break;
		case "invention-title":
			this.inventionTitleEntered = true;
			break;
		case "doc-number":
			this.docNumberEntered = true;
			break;
		}	
		
	}

	public void endElement(String uri, String name, String qName) {
		
		switch (name) {
		case "us-patent-grant":
			patentGrantEntered = false;
			abstractEntered = false;
			abstractParagraphEntered = false;
			publicationReferenceEntered = false;
			inventionTitleEntered = false;
			docNumberEntered = false;

			if (parsedEventListeners != null){
				for (ParsedEventListener eventListener: parsedEventListeners) {
					eventListener.documentParsed(document);
				}
			}

			this.document = null;
			break;
		case "abstract":
			abstractEntered = false;
			abstractParagraphEntered = false;
			break;
		case "p":
			abstractParagraphEntered = false;
			break;
		case "publication-reference":
			publicationReferenceEntered = false;
			docNumberEntered = false;
			break;
		case "invention-title":
			inventionTitleEntered = false;
			break;
		case "doc-number":
			docNumberEntered = false;
			break;
		}	
	}

	public void characters(char ch[], int start, int length) {

		if (!this.patentGrantEntered) return;

		if (this.docNumberEntered && this.publicationReferenceEntered) {
			document.setDocId(Integer.parseInt(new String(ch, start,length)));
		}

		if (this.inventionTitleEntered) {
			document.setInventionTitle(new String(ch, start,length));
		}

		if (this.abstractEntered && abstractParagraphEntered) {
			if (document.getPatentAbstract() != null && document.getPatentAbstract() != "") {
				document.setPatentAbstract(document.getPatentAbstract() + " " + new String(ch, start,length));
			} else {
				document.setPatentAbstract(new String(ch, start,length));
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
