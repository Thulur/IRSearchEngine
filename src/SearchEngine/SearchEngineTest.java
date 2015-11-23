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
        
        long start = System.currentTimeMillis();

        myEngine.index("");
        
        long time = System.currentTimeMillis() - start;
        
        System.out.print("Indexing Time:\t" + time + "\tms\n");
        
        // String query = "";
        
        // ArrayList<String> results = new ArrayList <> ();

        //compression
        myEngine.compressIndex("");
        myEngine.loadCompressedIndex("");

        start = System.currentTimeMillis();

        //System.out.println("\nselection:");
        //myEngine.search("selection", 0, 0).forEach(System.out::println);

        //System.out.println("\ncomprises AND consists:");
        //myEngine.search("comprises AND consists", 0, 0).forEach(System.out::println);
        //System.out.println("\nmethods NOT inventions:");
        //myEngine.search("methods NOT inventions", 0, 0).forEach(System.out::println);
        //System.out.println("\ndata OR method:");
        //myEngine.search("data OR method", 10, 0).forEach(System.out::println);

        //System.out.println("\nprov* NOT free:");
        //myEngine.search("prov* NOT free", 10, 0).forEach(System.out::println);
        /*System.out.println("\ninc* OR memory:");
        myEngine.search("inc* OR memory", 0, 0).forEach(System.out::println);

        System.out.println("\n\"the presented invention\":");
        myEngine.search("\"the presented invention\"", 0, 0).forEach(System.out::println);
        System.out.println("\n\"mobile devices\":");
        myEngine.search("\"mobile devices\"", 0, 0).forEach(System.out::println);*/

        //System.out.println("data device mobile data");
        //myEngine.search("data device mobile data", 10, 0).forEach(System.out::println);

        /*RandomAccessFile test = new RandomAccessFile(FilePaths.CACHE_PATH + "ipg150317.xml", "rw");
        test.seek(653538546);
        byte[] test2 = new byte[64];
        test.read(test2);
        System.out.println(new String(test2));*/
        System.out.println("\nprocessing:");
        myEngine.search("processing", 10, 0).forEach(System.out::println);

        //System.out.println("\ncomputers:");
        //myEngine.search("computers", 10, 0).forEach(System.out::println);

        //System.out.println("\n\"mobile devices\":");
        //myEngine.search("\"mobile devices\"", 10, 0).forEach(System.out::println);

        //System.out.println("\ndata:");
        //myEngine.search("data", 10, 0).forEach(System.out::println);

        //System.out.println("\ndigital (without prf):");
        //myEngine.search("digital", 10, 0).forEach(System.out::println);

        //System.out.println("\ndigital (with prf = 2):");
        //myEngine.search("digital", 10, 2).forEach(System.out::println);

        time = System.currentTimeMillis() - start;

        System.out.print("Search time:\t" + (time) + "\tmilliseconds\n");
    }

}
