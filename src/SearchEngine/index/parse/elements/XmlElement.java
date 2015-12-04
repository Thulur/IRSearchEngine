package SearchEngine.index.parse.elements;

/**
 * Created by sebastian on 03.12.2015.
 */
public interface XmlElement {
    String getElementName();
    void setElementName(String elementName);

    long getElementLength();
    void setElementLength(long elementLength);

    String getElementContent();
    void setElementContent(String elementContent);
}
