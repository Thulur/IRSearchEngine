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
        Set<String> test = instance.stem("processing apparatus, control method,", false).keySet();
        Set<String> test2 = instance.stem("information processing apparatus, control method, and control program", false).keySet();
    }
}