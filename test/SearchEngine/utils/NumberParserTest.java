package SearchEngine.utils;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Created by sebastian on 13.11.2015.
 */
public class NumberParserTest extends TestCase {

    public void testParseHexadecimalLong() throws Exception {
        Assert.assertEquals(0xffl, NumberParser.parseHexadecimalLong("ff"));
        Assert.assertEquals(-1l, NumberParser.parseHexadecimalLong("z"));
    }
}