package SearchEngine;

import SearchEngine.utils.WordParser;
import edu.stanford.nlp.ling.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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


        long start = System.nanoTime();

//        System.out.println("comprises AND consists" + "\n");
//        myEngine.search("comprises AND consists", 0, 0).forEach(System.out::println);
//        System.out.println("methods NOT inventions" + "\n");
//        myEngine.search("methods NOT inventions", 0, 0).forEach(System.out::println);
//        System.out.println("data OR method" + "\n");
//        myEngine.search("data OR method", 0, 0).forEach(System.out::println);
        String test = "inc*".toLowerCase();



        System.out.println("inc* OR memory" + "\n");
        myEngine.search("inc* OR memory", 0, 0).forEach(System.out::println);

        myEngine.search("inc*", 0, 0).forEach(System.out::println);

        long time = System.nanoTime() - start;

        System.out.print("Search time:\t" + (time/1000) + "\tmicroseconds\n");
    }

}
