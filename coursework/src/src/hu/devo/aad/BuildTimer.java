package hu.devo.aad;

import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Keeps track of the time it takes to process RECORD_EVERY_NTH_CHAR character.
 * Created by Barnabas on 29/12/2015.
 */
public class BuildTimer extends Timer {
    ArrayList<Long> characterProcessingTimes = new ArrayList<>(1000);

    void characterFinished(int i) {
        if (i % Settings.RECORD_EVERY_NTH_CHAR == 0) {
            characterProcessingTimes.add(System.nanoTime() - startTime);
        }
    }

    void stopTiming() {
        characterProcessingTimes.add(System.nanoTime() - startTime);
    }

    void writeToFile(FileOutputStream fos, String name) {
        Util.writeArrayListToFOS(characterProcessingTimes, fos, name);
    }
}
