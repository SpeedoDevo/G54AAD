package hu.devo.aad;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

/**
 * Keeps track of the time it takes to find the first and all occurrences of a string in a SuffixTree.
 * Created by Barnabas on 03/01/2016.
 */
public class SearchTimer extends Timer {
    /**
     * (termLength,[time])
     */
    Hashtable<Integer, ArrayList<Long>> firsts;
    /**
     * (hits x (termLength,time))
     */
    Hashtable<Integer, Hashtable<Integer, Long>> lasts;

    SearchTimer() {
        if (Settings.DO_SEARCH_FIRST) {
            firsts = new Hashtable<>(100);
        }
        if (Settings.DO_SEARCH_ALL) {
            lasts = new Hashtable<>(100);
        }
    }

    void firstFound(int termLength) {
        if (Settings.DO_SEARCH_FIRST) {
            //determine time early for accuracy
            long time = sinceStart();
            if (firsts.containsKey(termLength)) {
                //if there is already list for this key then just add it
                firsts.get(termLength).add(time);
            } else {
                //otherwise create the list and then add it
                ArrayList<Long> l = new ArrayList<>(Settings.SEARCH_RUNS);
                l.add(time);
                firsts.put(termLength, l);
            }
        }
    }

    void lastFound(int hits, int termLength) {
        if (Settings.DO_SEARCH_ALL && hits > 1) {
            //determine time early for accuracy
            long time = sinceStart();
            if (lasts.containsKey(termLength)) {
                //if there is already a table for this key then put it
                lasts.get(termLength).put(hits, time);
            } else {
                //otherwise create the table and then put it
                Hashtable<Integer, Long> ht = new Hashtable<>(10);
                ht.put(hits, time);
                lasts.put(termLength, ht);
            }
        }
    }

    public void writeToFile() {
        try {
            if (Settings.DO_SEARCH_FIRST) {
                FileOutputStream fos = Util.getExperimentFOS("search-firsts");
                //write results in a row, to the experiment file
                for (int key : Util.asSortedList(firsts.keySet())) {
                    Util.writeArrayListToFOS(firsts.get(key), fos, String.valueOf(key));
                }
            }

            if (Settings.DO_SEARCH_ALL) {
                StringBuilder sb = new StringBuilder("hits\\length,");
                //start the file with column labels
                List<Integer> cols = Util.asSortedList(lasts.keySet());
                for (int col : cols) {
                    sb.append(col).append(',');
                }
                sb.append('\n');

                //list all the values in this TreeSet
                TreeSet<Integer> rows = new TreeSet<>();
                for (Hashtable<Integer, Long> ht : lasts.values()) {
                    rows.addAll(ht.keySet());
                }

                //build the sb, by traversing the table
                for (int row : rows) {
                    //a row starts with the hit number
                    sb.append(row).append(',');
                    for (int col : cols) {
                        //then a cell is added for each column
                        if (lasts.get(col).containsKey(row)) {
                            //a value is appended if there is a value associated with the column key
                            sb.append(lasts.get(col).get(row)).append(',');
                        } else {
                            //otherwise the cell is empty
                            sb.append(',');
                        }
                    }
                    //close the row
                    sb.append('\n');
                }

                //write the sb to an experiment file
                Util.getExperimentFOS("search-lasts").write(sb.toString().getBytes());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
