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
        
//         myEngine.loadIndex("disregard");
        
        // String query = "";
        
        // ArrayList<String> results = new ArrayList <> ();

        //compression
        myEngine.compressIndex("disregard");

        // results = myEngine.search("selection", 0, 0);

        int numberOfSearches = 10000;

        long start = System.nanoTime();

        for (int i = 0; i < numberOfSearches; ++i) {
//            System.out.println("file-system");
            myEngine.search("file-system", 0, 0);
//            System.out.println("included");
            myEngine.search("included", 0, 0);
//            System.out.println("storing");
            myEngine.search("storing", 0, 0);
        }

        long time = System.nanoTime() - start;

        System.out.print("Search time:\t" + (time/(numberOfSearches*1000)) + "\tmicroseconds\n");
    }

}
