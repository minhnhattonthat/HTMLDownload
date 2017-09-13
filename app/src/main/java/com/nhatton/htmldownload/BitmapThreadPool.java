package com.nhatton.htmldownload;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by nhatton on 9/9/17.
 */

public class BitmapThreadPool {

    private static BitmapThreadPool mInstance;
    private ThreadPoolExecutor mThreadPoolExec;
    private static final int NUMBER_OF_CORE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = NUMBER_OF_CORE  * 2;
    private static final int KEEP_ALIVE = 10;
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

    public static synchronized void post(Runnable runnable) {
        if (mInstance == null) {
            synchronized (BitmapThreadPool.class){
                if(mInstance == null){
                    mInstance = new BitmapThreadPool();
                }
            }
        }
        mInstance.mThreadPoolExec.execute(runnable);
    }

    private BitmapThreadPool() {
        mThreadPoolExec = new ThreadPoolExecutor(
                NUMBER_OF_CORE,
                MAX_POOL_SIZE,
                KEEP_ALIVE,
                TimeUnit.SECONDS,
                workQueue);
    }

    public static void finish() {
        mInstance.mThreadPoolExec.shutdown();
    }
}
