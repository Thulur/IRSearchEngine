package SearchEngine.utils;

import junit.framework.TestCase;

import java.util.Set;

/**
 * Created by sebastian on 08.11.2015.
 */
public class WordParserTest extends TestCase {
    WordParser instance;

    public void setUp() throws Exception {
        super.setUp();

        instance = WordParser.getInstance();
    }

    public void testStem() throws Exception {
        Set<String> corenlpSnowball = instance.stem("processing apparatus, control method,", false).keySet();
        Set<String> snowballOnly = instance.snowballStem("processing apparatus, control method,", false, 0l).keySet();
    }
}