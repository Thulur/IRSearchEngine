package SearchEngine;

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

        long start = System.nanoTime();

        System.out.println("\nselection:");
        myEngine.search("selection", 0, 0).forEach(System.out::println);

        System.out.println("\ncomprises AND consists:");
        myEngine.search("comprises AND consists", 0, 0).forEach(System.out::println);
        System.out.println("\nmethods NOT inventions:");
        myEngine.search("methods NOT inventions", 0, 0).forEach(System.out::println);
        System.out.println("\ndata OR method:");
        myEngine.search("data OR method", 0, 0).forEach(System.out::println);

        System.out.println("\nprov* NOT free:");
        myEngine.search("prov* NOT free", 0, 0).forEach(System.out::println);
        System.out.println("\ninc* OR memory:");
        myEngine.search("inc* OR memory", 0, 0).forEach(System.out::println);

        System.out.println("\n\"the presented invention\":");
        myEngine.search("\"the presented invention\"", 0, 0).forEach(System.out::println);
        System.out.println("\n\"mobile devices\":");
        myEngine.search("\"mobile devices\"", 0, 0).forEach(System.out::println);

        long time = System.nanoTime() - start;

        System.out.print("Search time:\t" + (time/1000) + "\tmicroseconds\n");
    }

}
