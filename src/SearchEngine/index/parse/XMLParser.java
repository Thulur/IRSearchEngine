package SearchEngine.index.parse;

import SearchEngine.data.Document;
import SearchEngine.index.parse.elements.CommonElement;
import SearchEngine.index.parse.elements.XmlElement;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class XmlParser extends DefaultHandler {
	private boolean patentGrantEntered = false;
	private boolean publicationReferenceEntered = false;
	private boolean docNumberEntered = false;
	private Document document;
	private List<ParsedEventListener> parsedEventListeners;
	private FileInputStream fileInput;
	private String tmpPatentId = "";
	private XmlElement curElement = null;

	public XmlParser() {
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
		parsedEventListeners = null;
		fileInput = null;
		curElement = null;
		document = null;
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

	public void startElement(String uri, String name, String qName, Attributes atts) {
		switch (name) {
		case "us-patent-grant":
			this.patentGrantEntered = true;
			this.document = new Document();
			break;
		case "abstract":
			curElement = new CommonElement();
			curElement.setElementName(name);
			break;
		case "publication-reference":
			this.publicationReferenceEntered = true;
			break;
		case "application-reference":
			// Only parse utility patents
			if (!atts.getValue("appl-type").equals("utility")) {
				curElement = null;
				patentGrantEntered = false;
				publicationReferenceEntered = false;
				docNumberEntered = false;
				document = null;
			}
			break;
		case "invention-title":
			curElement = new CommonElement();
			curElement.setElementName(name);
			break;
		case "doc-number":
			this.docNumberEntered = true;
			break;
		case "description":
			curElement = new CommonElement();
			curElement.setElementName(name);
			break;
		case "claims":
			curElement = new CommonElement();
			curElement.setElementName(name);
			break;
		}
	}

	public void endElement(String uri, String name, String qName) {
		switch (name) {
		case "us-patent-grant":
			patentGrantEntered = false;
			publicationReferenceEntered = false;
			docNumberEntered = false;

			if (parsedEventListeners != null && document != null){
				for (ParsedEventListener eventListener: parsedEventListeners) {
					eventListener.documentParsed(document);
				}
			}

			this.document = null;
			break;
		case "abstract":
			if (document != null) {
				document.setPatentAbstract(curElement.getElementContent().toString());
				document.setPatentAbstractLength(curElement.getElementLength());
				document.setPatentAbstractPos(curElement.getElementPos());
			}

			curElement = null;
			break;
		case "publication-reference":
			publicationReferenceEntered = false;
			docNumberEntered = false;
			break;
		case "invention-title":
			if (document != null) {
				document.setInventionTitle(curElement.getElementContent().toString());
				document.setInventionTitleLength(curElement.getElementLength());
				document.setInventionTitlePos(curElement.getElementPos());
			}

			curElement = null;
			break;
		case "doc-number":
			if (publicationReferenceEntered) {
				try {
					int patentId = Integer.parseInt(tmpPatentId);

					document.setDocId(patentId);
				} catch (NumberFormatException e) {
					// The current patent type is not a utility, utility patents have got integer ids
					curElement = null;
					patentGrantEntered = false;
					publicationReferenceEntered = false;
					docNumberEntered = false;
					document = null;
				}

				tmpPatentId = "";
			}
			docNumberEntered = false;
			break;
		case "description":
			if (document != null) {
				String description = curElement.getElementContent().toString();
				description = description.replaceAll("[\\t\\n\\r]", " ");
				description = description.replaceAll("[ ]+", " ");
				document.setDescription(description);
			}

			curElement = null;
			break;
		case "claims":
			if (document != null) {
				String claims = curElement.getElementContent().toString();
				claims = claims.replaceAll("[\\t\\n\\r]", " ");
				claims = claims.replaceAll("[ ]+", " ");
				document.setClaims(claims);
			}

			curElement = null;
			break;
		}
	}

	public void characters(char ch[], int start, int length) {
		if (!this.patentGrantEntered) return;

		if (this.docNumberEntered && this.publicationReferenceEntered) {
			// catch Exception here (we do not know if id is an integer at this point, reset parse state if no integer)
			tmpPatentId += new String(ch, start,length);
		}

		if (curElement != null) {
			long filePos = 0;

			try {
				filePos = fileInput.getChannel().position() - ch.length + start;
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (curElement.getElementContent() != null && curElement.getElementContent().length() > 0) {
				curElement.setElementContent(curElement.getElementContent().append(ch, start, length));
			} else {
				curElement.setElementContent(new StringBuilder(new String(ch, start, length)));
				curElement.setElementPos(filePos);
			}

			curElement.setElementLength(curElement.getElementLength() + length);
		}
	}

	public void addDocumentParsedListener(ParsedEventListener eventListener) {
		if (parsedEventListeners == null) {
			parsedEventListeners = new LinkedList<>();
		}

		parsedEventListeners.add(eventListener);
	}
}
