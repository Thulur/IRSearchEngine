package SearchEngine;

import java.io.FileReader;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.*;

public class MySAXApp extends DefaultHandler {
	
	boolean publicationReferenceEntered = false;
	boolean patentGrantEntered = false;
	boolean inventionTitleEntered = false;
	boolean docNumberEntered = false;
	
	public MySAXApp() {
		super();
	}

	public static void main(String args[]) throws Exception {
		XMLReader xr = XMLReaderFactory.createXMLReader();
		MySAXApp handler = new MySAXApp();
		
		// Ignores the dtd definition for the moment, we do not want to load it
		xr.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);

		// Parse each file provided on the
		// command line.
		for (int i = 0; i < args.length; i++) {
			FileReader r = new FileReader(args[i]);
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
			this.patentGrantEntered = false;
			this.publicationReferenceEntered = false;
			this.inventionTitleEntered = false;
			this.docNumberEntered = false;
			break;
		case "publication-reference":
			this.publicationReferenceEntered = false;
			this.docNumberEntered = false;
			break;
		case "invention-title":
			this.inventionTitleEntered = false;
			break;
		case "doc-number":
			this.docNumberEntered = false;
			break;
		}	
	}

	public void characters(char ch[], int start, int length) {

		if (!this.patentGrantEntered) return;

		if (this.docNumberEntered && this.publicationReferenceEntered) {
			System.out.print(new String(ch, start,length) + " - ");
		}

		if (this.inventionTitleEntered) {
			System.out.println(new String(ch, start,length));
		}

	}
}
