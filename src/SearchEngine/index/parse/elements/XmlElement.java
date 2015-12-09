package SearchEngine.index.parse.elements;

/**
 * Created by sebastian on 03.12.2015.
 */
public interface XmlElement {
    String getElementName();
    void setElementName(String elementName);

    long getElementPos();
    void setElementPos(long elementPos);

    long getElementLength();
    void setElementLength(long elementLength);

    StringBuilder getElementContent();
    void setElementContent(StringBuilder elementContent);
}
