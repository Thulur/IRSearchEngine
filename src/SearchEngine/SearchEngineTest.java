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

        //myEngine.index();

        long time = System.currentTimeMillis() - start;

        System.out.print("Indexing Time:\t" + time + "\tms\n");
        start = System.currentTimeMillis();
        // String query = "";

        // ArrayList<String> results = new ArrayList <> ();

        //compression
        //myEngine.compressIndex();
        System.out.println("Compression time:\t" + (System.currentTimeMillis() - start) + "\tms\n");
        myEngine.loadCompressedIndex();
        start = System.currentTimeMillis();

        //System.out.println("\nselection:");
        //myEngine.search("selection", 10).forEach(System.out::println);

        //System.out.println("\ncomprises AND consists:");
        //myEngine.search("comprises AND consists", 0).forEach(System.out::println);
        //System.out.println("\nmethods NOT inventions:");
        //myEngine.search("methods NOT inventions", 0).forEach(System.out::println);
        //System.out.println("\ndata OR method:");
        //myEngine.search("data OR method", 10).forEach(System.out::println);

        //System.out.println("\nprov* NOT free:");
        //myEngine.search("prov* NOT free", 10).forEach(System.out::println);
        //System.out.println("\ninc* OR memory:");
        //myEngine.search("inc* OR memory", 0).forEach(System.out::println);

        //System.out.println("\n\"the presented invention\":");
        //myEngine.search("\"the presented invention\"", 0).forEach(System.out::println);
        //System.out.println("\n\"mobile devices\":");
        //myEngine.search("\"mobile devices\"", 10).forEach(System.out::println);

        //System.out.println("data device mobile data");
        //myEngine.search("data device mobile data", 10).forEach(System.out::println);

        //System.out.println("\nprocessing:");
        //myEngine.search("processing", 10).forEach(System.out::println);

        //System.out.println("\ncomputers:");
        //myEngine.search("computers", 10).forEach(System.out::println);

        //System.out.println("\n\"mobile devices\":");
        //myEngine.search("\"mobile devices\"", 10).forEach(System.out::println);

        //System.out.println("\ndata:");
        //myEngine.search("data", 10).forEach(System.out::println);

        //System.out.println("\ndigital:");
        //myEngine.search("digital", 10).forEach(System.out::println);

        //System.out.println("\ndigital:");
        //myEngine.search("digital", 10).forEach(System.out::println);

        //System.out.println("\nrootkit:");
        //myEngine.search("rootkit", 10).forEach(System.out::println);

        //System.out.println("\nrootkit:");
        //myEngine.search("rootkit", 10).forEach(System.out::println);

        //System.out.println("\nnetwork access:");
        //myEngine.search("network access", 10).forEach(System.out::println);

        //System.out.println("\nnetwork access:");
        //myEngine.search("network access", 10).forEach(System.out::println);

        //System.out.println("\ncommom:");
        //myEngine.search("commom", 10).forEach(System.out::println);

        //System.out.println("\nkontrol:");
        //myEngine.search("kontrol", 10).forEach(System.out::println);

        //System.out.println("\nincluce:");
        //myEngine.search("incluce", 10).forEach(System.out::println);

        //System.out.println("\nstreem:");
        //myEngine.search("streem", 10).forEach(System.out::println);

        //System.out.println("\naccess control:");
        //myEngine.search("access control", 10).forEach(System.out::println);

        //System.out.println("computers:\n");
        //myEngine.search("computers", 10).forEach(System.out::println);

        //System.out.println("\ndata processing:");
        //myEngine.search("data processing", 10).forEach(System.out::println);

        //System.out.println("\nweb servers:");
        //myEngine.search("web servers", 10).forEach(System.out::println);

        //System.out.println("\nvulnerability information:");
        //myEngine.search("vulnerability information", 10).forEach(System.out::println);

        //System.out.println("\ncomputer-readable media:");
        //myEngine.search("computer-readable media", 10).forEach(System.out::println);

        //System.out.println("\ngraph editor:");
        //myEngine.search("\"graph editor\"", 20).forEach(System.out::println);

        //System.out.println("\nfossil hydrocarbons:");
        //myEngine.search("fossil hydrocarbons", 20).forEach(System.out::println);

        //System.out.println("\nphysiological AND saline:");
        //myEngine.search("physiological AND saline", 20).forEach(System.out::println);

        //System.out.println("\ntires NOT pressure:");
        //myEngine.search("tires NOT pressure", 20).forEach(System.out::println);

        //System.out.println("\nLinkTo:07920906:");
        //myEngine.search("LinkTo:07920906", 10).forEach(System.out::println);

        //System.out.println("\nLinkTo:07904949:");
        //myEngine.search("LinkTo:07904949", 10).forEach(System.out::println);

        //System.out.println("\nLinkTo:08078787:");
        //myEngine.search("LinkTo:08078787", 10).forEach(System.out::println);

        //System.out.println("\nLinkTo:07865308 AND 07925708:");
        //myEngine.search("LinkTo:07865308 AND 07925708", 10).forEach(System.out::println);

        //System.out.println("\nLinkTo:07947864 AND 07947142:");
        //myEngine.search("LinkTo:07947864 AND 07947142", 10).forEach(System.out::println);

        //System.out.println("\nview guidelines:");
        //myEngine.search("view guidelines", 20).forEach(System.out::println);

        //System.out.println("\non-chip OR OCV:");
        //myEngine.search("on-chip OR OCV", 20).forEach(System.out::println);
//
        //System.out.println("\n\"mobile devices\":");
        //myEngine.search("\"mobile devices\"", 10).forEach(System.out::println);

        //System.out.println("\nadd-on:");
        //myEngine.search("add-on", 20).forEach(System.out::println);

        time = System.currentTimeMillis() - start;

        System.out.print("Search time:\t" + (time) + "\tmilliseconds\n");
    }

}
