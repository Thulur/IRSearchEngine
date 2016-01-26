package SearchEngine.index.parse;

import SearchEngine.data.Document;
import SearchEngine.index.parse.elements.CommonElement;
import SearchEngine.index.parse.elements.XmlElement;
import org.xml.sax.Attributes;
import java.io.IOException;

public class ContentParser extends XmlParser {
	private boolean patentGrantEntered = false;
	private boolean publicationReferenceEntered = false;
	private boolean docNumberEntered = false;
	private Document document;
	private String tmpPatentId = "";
	private XmlElement curElement = null;

	public ContentParser() {
		super();
	}

	////////////////////////////////////////////////////////////////////
	// Event handlers.
	////////////////////////////////////////////////////////////////////
	@Override
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
			// reset everything if appl-type!="utility"
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

	@Override
	public void endElement(String uri, String name, String qName) {
		switch (name) {
		case "us-patent-grant":
			patentGrantEntered = false;
			publicationReferenceEntered = false;
			docNumberEntered = false;

			if (document != null){
				notifyEventListeners(document);
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

	@Override
	public void characters(char ch[], int start, int length) {
		if (!this.patentGrantEntered) return;

		if (this.docNumberEntered && this.publicationReferenceEntered) {
			// catch Exception here (we do not know if id is an integer at this point, reset parse state if no integer)
			tmpPatentId += new String(ch, start,length);
		}

		if (curElement != null) {
			long filePos = 0;

			try {
				filePos = curPos() - ch.length + start;
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
}
