package hu.devo.aad;

/**
 * Creates a new experiment that builds a SuffixTree and measures the time it takes to find a
 * random substring of increasing length up to LONGEST_SEARCH_TERM.
 * Created by Barnabas on 30/12/2015.
 */
public class SearchExperiment {
    String text;

    public SearchExperiment(String text) {
        this.text = text;
    }

    public void run() {
        System.out.println("Starting search experiment, building tree");
        //build a tree that we can search in
        SuffixTree st = new SuffixTree().build(text);
        System.out.println("Tree built, starting search");
        //increase the length of the searched substring
        Util.gc();
        for (int i = 1; i < Settings.LONGEST_SEARCH_TERM; i++) {
            //search for the same length substring SEARCH_RUNS times
            for (int j = 0; j < Settings.SEARCH_RUNS; j++) {
                int rand = Util.randBetween(0, text.length() - i);
                //search for a random substring of length i
                st.search(text.substring(rand, rand + i));
                Util.logProgress(i * Settings.SEARCH_RUNS + j,
                        Settings.SEARCH_RUNS * Settings.LONGEST_SEARCH_TERM);
            }
        }
        st.search.writeToFile();
        System.out.println("Search experiment done");
    }
}
