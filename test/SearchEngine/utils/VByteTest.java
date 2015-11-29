package SearchEngine.utils;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Created by sebastian on 08.11.2015.
 */
public class VByteTest extends TestCase {

    public void testConvertToVByte() throws Exception {
        // 1 Byte example (also fits into 1 byte after the encoding)
        Assert.assertEquals("8f", VByte.encode(15l));
        // 1 Byte example
        Assert.assertEquals("019f", VByte.encode(159l));
        // 2 Byte example
        Assert.assertEquals("040091", VByte.encode(65553l));
        // 3 Byte example
        Assert.assertEquals("09020015d4", VByte.encode(2420116180l));
        // 4 Byte example
        Assert.assertEquals("1014232004ea", VByte.encode(555198448234l));
        // 5 Byte example
        Assert.assertEquals("29480039462092", VByte.encode(182793928806418l));
        // 6 Byte example
        Assert.assertEquals("4a00044404077faa", VByte.encode(41658452254261162l));
        // 7 Byte example
        Assert.assertEquals("4a00044404077faa", VByte.encode(41658452254261162l));
        // 8 Byte example
        Assert.assertEquals("6078486c00120871ac", VByte.encode(6985403392290076844l));
    }
}