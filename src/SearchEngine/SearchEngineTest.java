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
        start = System.currentTimeMillis();
        // String query = "";

        // ArrayList<String> results = new ArrayList <> ();

        //compression
        myEngine.compressIndex("");
        System.out.println("Compression time:\t" + (System.currentTimeMillis() - start) + "\tms\n");
        myEngine.loadCompressedIndex("");

        start = System.currentTimeMillis();

        //System.out.println("\n" + args[0] + ":");
        //myEngine.search(args[0], 10, 0).forEach(System.out::println);

        //System.out.println("\nselection:");
        //myEngine.search("selection", 10, 0).forEach(System.out::println);

        //System.out.println("\ncomprises AND consists:");
        //myEngine.search("comprises AND consists", 0, 0).forEach(System.out::println);
        //System.out.println("\nmethods NOT inventions:");
        //myEngine.search("methods NOT inventions", 0, 0).forEach(System.out::println);
        //System.out.println("\ndata OR method:");
        //myEngine.search("data OR method", 10, 0).forEach(System.out::println);

        //System.out.println("\nprov* NOT free:");
        //myEngine.search("prov* NOT free", 10, 0).forEach(System.out::println);
        //System.out.println("\ninc* OR memory:");
        //myEngine.search("inc* OR memory", 0, 0).forEach(System.out::println);

        //System.out.println("\n\"the presented invention\":");
        //myEngine.search("\"the presented invention\"", 0, 0).forEach(System.out::println);
        //System.out.println("\n\"mobile devices\":");
        //myEngine.search("\"mobile devices\"", 10, 0).forEach(System.out::println);

        //System.out.println("data device mobile data");
        //myEngine.search("data device mobile data", 10, 0).forEach(System.out::println);

        //System.out.println("\nprocessing:");
        //myEngine.search("processing", 10, 0).forEach(System.out::println);

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

        //System.out.println("\nrootkit (without prf):");
        //myEngine.search("rootkit", 10, 0).forEach(System.out::println);

        //System.out.println("\nrootkit (with prf = 2):");
        //myEngine.search("rootkit", 10, 2).forEach(System.out::println);

        //System.out.println("\nnetwork access (without prf):");
        //myEngine.search("network access", 10, 0).forEach(System.out::println);

        //System.out.println("\nnetwork access (with prf = 2):");
        //myEngine.search("network access", 10, 2).forEach(System.out::println);

        //System.out.println("\ncommom:");
        //myEngine.search("commom", 10, 0).forEach(System.out::println);

        //System.out.println("\nkontrol:");
        //myEngine.search("kontrol", 10, 0).forEach(System.out::println);

        //System.out.println("\nincluce:");
        //myEngine.search("incluce", 10, 0).forEach(System.out::println);

        //System.out.println("\nstreem:");
        //myEngine.search("streem", 10, 0).forEach(System.out::println);

        //System.out.println("\naccess control:");
        //myEngine.search("access control", 10, 0).forEach(System.out::println);

        //System.out.println("computers:\n");
        //myEngine.search("computers", 10, 0).forEach(System.out::println);

        //System.out.println("\ndata processing:");
        //myEngine.search("data processing", 10, 0).forEach(System.out::println);

        //System.out.println("\nweb servers:");
        //myEngine.search("web servers", 10, 0).forEach(System.out::println);

        //System.out.println("\nvulnerability information:");
        //myEngine.search("vulnerability information", 10, 0).forEach(System.out::println);

        //System.out.println("\ncomputer-readable media:");
        //myEngine.search("computer-readable media", 10, 0).forEach(System.out::println);

//
        System.out.println("\nvulnerability:");
        myEngine.search("vulnerability", 10, 2).forEach(System.out::println);
//
        System.out.println("\n\"mobile devices\":");
        myEngine.search("\"mobile devices\"", 10, 2).forEach(System.out::println);

        time = System.currentTimeMillis() - start;

        System.out.print("Search time:\t" + (time) + "\tmilliseconds\n");
    }

}
