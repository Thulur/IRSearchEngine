package SearchEngine.utils;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Created by sebastian on 08.11.2015.
 */
public class IndexEncoderTest extends TestCase {

    public void testConvertToVByte() throws Exception {
        Assert.assertEquals("8f", IndexEncoder.convertToVByte(15l));
        Assert.assertEquals("019f", IndexEncoder.convertToVByte(159l));
        Assert.assertEquals("040091", IndexEncoder.convertToVByte(65553l));
        // TODO: Test numbers with 4-8 bytes (1-3 already done), 8 bytes will currently fail cause of an overflow
    }
}