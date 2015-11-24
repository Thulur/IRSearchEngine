package SearchEngine.index;

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

public class XMLParser extends DefaultHandler {
	boolean patentGrantEntered = false;
	boolean abstractEntered = false;
	boolean publicationReferenceEntered = false;
	boolean inventionTitleEntered = false;
	boolean docNumberEntered = false;
	boolean abstractParagraphEntered = false;
	Document document;
	List<ParsedEventListener> parsedEventListeners;
	FileInputStream fileInput;
	String tmpPatentId = "";

	public XMLParser() {
		super();
	}

	public void parseFiles(String file) throws Exception {
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
	}

	////////////////////////////////////////////////////////////////////
	// Event handlers.
	////////////////////////////////////////////////////////////////////

	public void startDocument() {
		return;
	}

	public void endDocument() {
		if (parsedEventListeners != null){
			for (ParsedEventListener eventListener: parsedEventListeners) {
				eventListener.finishedParsing();
			}
		}
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
		case "application-reference":
			// reset everything if appl-type!="utility"
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

			if (parsedEventListeners != null && document != null){
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
			if (publicationReferenceEntered) {
				try {
					int patentId = Integer.parseInt(tmpPatentId);

					document.setDocId(patentId);
				} catch (NumberFormatException e) {
					// The current patent type is not a utility, utility patents have got integer ids
					patentGrantEntered = false;
					abstractEntered = false;
					abstractParagraphEntered = false;
					publicationReferenceEntered = false;
					inventionTitleEntered = false;
					docNumberEntered = false;
					document = null;
				}

				tmpPatentId = "";
			}
			docNumberEntered = false;
			break;
		}
	}

	public void characters(char ch[], int start, int length) {
		if (!this.patentGrantEntered) return;

		if (this.docNumberEntered && this.publicationReferenceEntered) {
			// catch Exception here (we do not know if id is an integer at this point, reset parse state if no integer)
			tmpPatentId += new String(ch, start,length);
		}

		if (this.inventionTitleEntered) {
			long filePos = 0;

			try {
				filePos = fileInput.getChannel().position() - ch.length + start;
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (document.getInventionTitle() != null && document.getInventionTitle() != "") {
				document.setInventionTitle(document.getInventionTitle() + new String(ch, start, length));
			} else {
				document.setInventionTitle(new String(ch, start, length));
				document.setInventionTitlePos(filePos);
			}

			document.setInventionTitleLength(document.getInventionTitleLength() + length);
		}

		if (this.abstractEntered && abstractParagraphEntered) {
			long filePos = 0;

			try {
				filePos = fileInput.getChannel().position() - ch.length + start;
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (document.getPatentAbstract() != null && document.getPatentAbstract() != "") {
				document.setPatentAbstract(document.getPatentAbstract() + new String(ch, start,length));
			} else {
				document.setPatentAbstract(new String(ch, start, length));
				document.setPatentAbstractPos(filePos);
			}

			document.setPatentAbstractLength(document.getPatentAbstractLength() + length);
		}
	}

	public void addDocumentParsedListener(ParsedEventListener eventListener) {
		if (parsedEventListeners == null) {
			parsedEventListeners = new LinkedList<>();
		}

		parsedEventListeners.add(eventListener);
	}
}
