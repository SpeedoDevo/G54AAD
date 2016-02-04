package hu.devo.aad;

/**
 * Experiment settings.
 * Created by Barnabas on 04/01/2016.
 */
public class Settings {

    /*
     * Settings for both experiments.
     */
    public static final int KBS_TO_LOAD = 5000;
    public static final String DATA_PATH = "data/";
    //    public static final String FILE_TO_LOAD = "war and peace.txt";
    public static final String FILE_TO_LOAD = "1000basepairs.txt";
    public static final String RESULT_PATH = "results/";
    public static final boolean DO_GC = true;
    public static final int LOG_PROGRESS_EVERY_NTH_SEC = 1;
    public static final boolean LOG_SUFFIX_TREE_STEPS = false;

    /*
     * Build experiment settings.
     */
    public static final boolean DO_BUILD_EXPERIMENT = true;
    public static final int RECORD_EVERY_NTH_CHAR = 100;
    public static final int BUILD_RUNS = 20;
    public static final int SPLIT = 2;


    /*
     * Search experiment settings.
     */
    public static final boolean DO_SEARCH_FIRST = true;
    public static final boolean DO_SEARCH_ALL = true;
    public static final int LONGEST_SEARCH_TERM = 10000;
    public static final int SEARCH_RUNS = 10;
}
