package hu.devo.aad;

public class Main {

    public static void main(String[] args) {
        String text = TextLoader.load();
        if (Settings.DO_BUILD_EXPERIMENT) {
            new BuildExperiment(text).run();
        }
        if (Settings.DO_SEARCH_FIRST || Settings.DO_SEARCH_ALL) {
            new SearchExperiment(text).run();
        }
    }
}
