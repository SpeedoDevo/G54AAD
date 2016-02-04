package hu.devo.aad;

/**
 * Some common functionality for the timers.
 * Created by Barnabas on 03/01/2016.
 */
public class Timer {
    long startTime;

    void startTiming() {
        startTime = System.nanoTime();
    }

    long sinceStart() {
        return System.nanoTime() - startTime;
    }
}
