package SearchEngine.index;

import SearchEngine.data.Document;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class XMLParser {
	boolean patentGrantEntered = false;
	boolean abstractEntered = false;
	boolean publicationReferenceEntered = false;
	boolean inventionTitleEntered = false;
	boolean docNumberEntered = false;
	boolean abstractParagraphEntered = false;
	private Document document;
	private List<ParsedEventListener> parsedEventListeners;
	private String tmpPatentId = "";
	private XMLStreamReader parser;
	private FileInputStream fileInput;

	public XMLParser() {
		super();
	}

	public void parseFiles(String file) throws Exception {
		fileInput = new FileInputStream(file);
		XMLInputFactory factory = XMLInputFactory.newInstance();
		parser = factory.createXMLStreamReader(fileInput);

		while (parser.hasNext()){
			switch (parser.getEventType()) {
				case XMLStreamConstants.START_DOCUMENT:
					startDocument();
					break;
				case XMLStreamConstants.END_DOCUMENT:
					endDocument();
					parser.close();
					break;
				case XMLStreamConstants.NAMESPACE:
					break;
				case XMLStreamConstants.START_ELEMENT:
					startElement(parser.getLocalName());
					break;
				case XMLStreamConstants.CHARACTERS:
					characters(parser.getText(), parser.getLocation().getCharacterOffset());
					break;
				case XMLStreamConstants.END_ELEMENT:
					endElement(parser.getLocalName());
					break;
				default:
					break;
			}
			parser.next();
		}
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

	public void startElement(String name) {
		switch (name) {
		case "us-patent-grant":
			this.patentGrantEntered = true;
			this.document = new Document();
			break;
		case "abstract":
			document.setPatentAbstractPos(parser.getLocation().getCharacterOffset() - 20);
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
			document.setInventionTitlePos(parser.getLocation().getCharacterOffset() - 20);
			this.inventionTitleEntered = true;
			break;
		case "doc-number":
			this.docNumberEntered = true;
			break;
		}	
		
	}

	public void endElement(String name) throws IOException {
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

	public void characters(String text, long pos) throws IOException {
		if (!this.patentGrantEntered) return;

		if (this.docNumberEntered && this.publicationReferenceEntered) {
			// catch Exception here (we do not know if id is an integer at this point, reset parse state if no integer)
			tmpPatentId += text;
		}

		if (this.inventionTitleEntered) {
			if (document.getInventionTitle() != null && document.getInventionTitle() != "") {
				document.setInventionTitle(document.getInventionTitle() + text);
			} else {
				document.setInventionTitle(text);
			}

			document.setInventionTitleLength(pos - document.getInventionTitlePos());
		}

		if (this.abstractEntered && abstractParagraphEntered) {
			if (document.getPatentAbstract() != null && document.getPatentAbstract() != "") {
				document.setPatentAbstract(document.getPatentAbstract() + text);
			} else {
				document.setPatentAbstract(text);
			}

			document.setPatentAbstractLength(pos - document.getPatentAbstractPos());
		}
	}

	public void addDocumentParsedListener(ParsedEventListener eventListener) {
		if (parsedEventListeners == null) {
			parsedEventListeners = new LinkedList<>();
		}

		parsedEventListeners.add(eventListener);
	}
}
