package SearchEngine.index.parse;

import SearchEngine.data.Document;
import org.xml.sax.Attributes;

/**
 * Created by sebastian on 25.01.2016.
 */
public class CitationParser extends XmlParser {
    private boolean publicationReferenceEntered = false;
    private boolean patentGrantEntered = false;
    private boolean docNumberCitationEntered = false;
    private boolean citationEntered = false;
    private boolean patcitEntered = false;
    private boolean docNumberEntered = false;
    private boolean countryEntered = false;
    private String tmpPatentId = "";
    private String country = "";
    private Document document;

    public CitationParser() {
        super();
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
        switch (name) {
            case "us-patent-grant":
                patentGrantEntered = true;
                document = new Document();
                break;
            case "publication-reference":
                publicationReferenceEntered = true;
                break;
            case "application-reference":
                // reset everything if appl-type!="utility"
                break;
            case "us-citation":
            case "citation":
                citationEntered = true;
                break;
            case "patcit":
                patcitEntered = true;
                break;
            case "country":
                countryEntered = true;
                break;
            case "doc-number":
                if (publicationReferenceEntered == true) {
                    docNumberEntered = true;
                }

                if (patcitEntered == true) {
                    docNumberCitationEntered = true;
                }
                break;
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        switch (name) {
            case "us-patent-grant":
                patentGrantEntered = false;

                if (document != null && document.getCitations().size() > 0){
                    notifyEventListeners(document);
                }

                document = new Document();
                break;
            case "publication-reference":
                publicationReferenceEntered = false;
                break;
            case "application-reference":
                // reset everything if appl-type!="utility"
                break;
            case "us-citation":
            case "citation":
                citationEntered = false;
                patcitEntered = false;
                docNumberEntered = false;
                break;
            case "patcit":
                patcitEntered = false;
                docNumberEntered = false;
                break;
            case "country":
                if (!country.equals("US")) {
                    patcitEntered = false;
                }

                country = "";
                countryEntered = false;
                break;
            case "doc-number":
                try {
                    int patentId = Integer.parseInt(tmpPatentId);

                    if (docNumberEntered) {
                        document.setDocId(patentId);
                    }

                    if (docNumberCitationEntered && document != null) {
                        document.getCitations().add(patentId);
                    }
                } catch (NumberFormatException e) {
                    citationEntered = false;
                    patcitEntered = false;

                    if (docNumberEntered) {
                        document = null;
                    }
                }

                docNumberCitationEntered = false;
                docNumberEntered = false;
                tmpPatentId = "";
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (!patentGrantEntered) return;

        if (docNumberEntered || (docNumberCitationEntered && patcitEntered)) {
            tmpPatentId += new String(ch, start,length);
        }

        if (countryEntered) {
            country += new String(ch, start,length);
        }
    }
}
