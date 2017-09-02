package com.nhatton.htmldownload;

/**
 * Created by nhatton on 8/26/17.
 */

public final class Counter {
    private static long startTime;
    private static long endTime;
    public static final Counter INSTANCE = new Counter();

    public final long getStartTime() {
        return startTime;
    }

    public final void setStartTime(long var1) {
        startTime = var1;
    }

    public final long getEndTime() {
        return endTime;
    }

    public final void setEndTime(long var1) {
        endTime = var1;
    }

    public final void start() {
        startTime = System.currentTimeMillis();
    }

    public final void end() {
        endTime = System.currentTimeMillis();
    }

    public final long count() {
        return endTime - startTime;
    }

}
