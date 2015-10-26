package SearchEngine;

import java.util.ArrayList;

/**
 *
 * @author: Your team name
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 *
 * You can run your search engine using this file
 * You can use/change this file during the development of your search engine.
 * Any changes you make here will be ignored for the final test!
 */

public class SearchEngineTest {
    
    
    public static void main(String args[]) throws Exception {

        SearchEngine myEngine = new SearchEngineMajorRelease();
        
        // long start = System.currentTimeMillis();
        
        myEngine.index("");
        
        // long time = System.currentTimeMillis() - start;
        
        // System.out.print("Indexing Time:\t" + time + "\tms\n");
        
         myEngine.loadIndex("disregard");
        
        // String query = "";
        
        // ArrayList<String> results = new ArrayList <> ();
        
        // results = myEngine.search("selection", 0, 0);
        System.out.println("selection:");
        myEngine.search("selection", 0, 0).forEach(System.out::println);
        System.out.println("device:");
        myEngine.search("device", 0, 0).forEach(System.out::println);
        System.out.println("justify:");
        myEngine.search("justify", 0, 0).forEach(System.out::println);
        System.out.println("write:");
        myEngine.search("write", 0, 0).forEach(System.out::println);
    }

}
