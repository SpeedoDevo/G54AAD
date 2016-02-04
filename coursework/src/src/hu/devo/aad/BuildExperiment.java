package hu.devo.aad;

import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Creates a build measuring experiment.
 * This experiment splits the imported text file into SPLIT equal partitions and builds a
 * SuffixTree BUILD_RUNS times of each partition, measuring the time it takes to process
 * every RECORD_EVERY_NTH_CHAR character.
 * Created by Barnabas on 29/12/2015.
 */
public class BuildExperiment {

    String text;

    BuildExperiment(String text) {
        this.text = text;
    }

    /**
     * Runs the build experiments.
     */
    void run() {
        try {
            System.out.println("Starting build experiment");
            //determine the split size
            int splitTextSize = text.length() / Settings.SPLIT;

            //we collect the file access here, so that it happens in a batch.
            ArrayList<IORunnable> ios = new ArrayList<>(Settings.BUILD_RUNS * Settings.SPLIT);

            //for every spit
            for (int i = 0; i < Settings.SPLIT; i++) {
                //make a human readable name
                final String baseName = "build " + (1 + i);

                //collect the run-times here so that it can be averaged later
                ArrayList<ArrayList<Long>> averager = new ArrayList<>(Settings.BUILD_RUNS);

                //for every run
                for (int j = 0; j < Settings.BUILD_RUNS; j++) {
                    //log the name so we can keep track of it
                    final String name = baseName + (char) ('a' + j);
                    System.out.println(name);

                    //force a garbage collection so it hopefully doesn't happen while we are measuring the time
                    Util.gc();
                    //the tree is built using the
                    SuffixTree st = new SuffixTree().build(
                            text.substring(i * splitTextSize, (i + 1) * splitTextSize)
                    );

                    //copy the reference so that the tree can be gc'd
                    final BuildTimer build = st.build;

                    //save the writing of results for later
                    ios.add(new IORunnable() {
                        public void run(FileOutputStream fos) {
                            build.writeToFile(fos, name);
                        }
                    });
                    //save the results for averaging
                    averager.add(build.characterProcessingTimes);
                }

                //count the averages for all runs
                final ArrayList<Float> averages = new ArrayList<>(averager.get(0).size());
                for (int x = 0; x < averager.get(0).size(); x++) {
                    Long sum = 0L;
                    for (ArrayList<Long> times : averager) {
                        sum += times.get(x);
                    }
                    averages.add((float) sum / averager.size());
                }
                //and write the results
                ios.add(new IORunnable() {
                    public void run(FileOutputStream fos) {
                        Util.writeArrayListToFOS(averages, fos, baseName + " avg");
                    }
                });
                Util.gc();
            }

            FileOutputStream fos = Util.getExperimentFOS("build");
            for (IORunnable r : ios) {
                r.run(fos);
            }
            System.out.println("Build experiment done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    interface IORunnable {
        void run(FileOutputStream fos);
    }

}
