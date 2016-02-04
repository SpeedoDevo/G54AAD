package hu.devo.aad;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Some static utility methods.
 * Created by Barnabas on 30/12/2015.
 */
public class Util {
    /**
     * Writes an ArrayList to file in one row with a name as the first column, and using commas as
     * delimiters (CSV format).
     *
     * @param list  the list
     * @param fos   the fos
     * @param title the title
     */
    static void writeArrayListToFOS(ArrayList list, FileOutputStream fos, String title) {
        StringBuilder sb = new StringBuilder(title).append(',');
        for (Object el : list) {
            sb.append(el).append(',');
        }
        sb.append('\n');
        try {
            fos.write(sb.toString().getBytes());
        } catch (IOException ignored) {
        }
    }

    /**
     * Forces out garbage collection. WARNING, takes a lot of time. Useful for timing experiments.
     */
    static void gc() {
        if (Settings.DO_GC) {
            Object obj = new Object();
            WeakReference ref = new WeakReference<Object>(obj);
            obj = null;
            while (ref.get() != null) {
                System.gc();
            }
        }
    }

    /**
     * Gets a FileOutputStream using a standardised naming for recording experiment results.
     *
     * @param name the name of the experiment
     * @return the standardised FileOutputStream for the experiment
     * @throws IOException if the stream couldn't be opened
     */
    static FileOutputStream getExperimentFOS(String name) throws IOException {
        String fileName = (new SimpleDateFormat("yyyy-MM-dd--HH-mm"))
                .format(new Date()) + "--" + name + "-experiment.csv";
        File f = new File(Settings.RESULT_PATH + fileName);
        if (!f.createNewFile()) {
            //file exists, empty it
            PrintWriter pw = new PrintWriter(f);
            pw.write("");
            pw.close();
        }

        return new FileOutputStream(f);
    }

    static Random gen = new Random();

    /**
     * @param min min
     * @param max max
     * @return returns a random integer in range [min, max)
     */
    static int randBetween(int min, int max) {
        return gen.nextInt((max - min) + 1) + min;
    }

    /**
     * @param c the Collection to sort
     * @return the contents of the Collection as a sorted (Array)List
     */
    static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        Collections.sort(list);
        return list;
    }

    static long lastProgress;

    /**
     * Standardised way to log the progress of a long running process. Prints the progress in the
     * format ###.#% (ie. " 42.2%") every {@link Settings#LOG_PROGRESS_EVERY_NTH_SEC} seconds.
     *
     * @param progress the current iteration number
     * @param max      the max iteration number
     */
    static void logProgress(int progress, int max) {
        if (System.nanoTime() - lastProgress > Settings.LOG_PROGRESS_EVERY_NTH_SEC * 1000000000L) {
            System.out.printf("%5.1f%%\n", (float) (100 * progress) / max);
            lastProgress = System.nanoTime();
        }
    }
}
