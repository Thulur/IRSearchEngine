package SearchEngine.index.parse.elements;

/**
 * Created by sebastian on 04.12.2015.
 */
public class CommonElement implements XmlElement {
    private String elementName;
    private long elementPos;
    private long elementLength;
    private StringBuilder elementContent;

    @Override
    public String getElementName() {
        return elementName;
    }

    @Override
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    @Override
    public long getElementPos() {
        return elementPos;
    }

    @Override
    public void setElementPos(long elementPos) {
        this.elementPos = elementPos;
    }

    @Override
    public long getElementLength() {
        return elementLength;
    }

    @Override
    public void setElementLength(long elementLength) {
        this.elementLength = elementLength;
    }

    @Override
    public StringBuilder getElementContent() {
        return elementContent;
    }

    @Override
    public void setElementContent(StringBuilder elementContent) {
        this.elementContent = elementContent;
    }
}
