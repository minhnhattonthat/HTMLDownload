package com.nhatton.htmldownload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by nhatton on 8/31/17.
 */

public class ImageLoader{

    private static final int DOWNLOAD_COMPLETE = 25;
    /*
         * Gets the number of available cores
         * (not always the same as the maximum number of cores)
         */
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private static final int MAXIMUM_POOL_SIZE = 30;

    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    // A queue of Runnables
    private final BlockingQueue<Runnable> mDownloadWorkQueue;

    private static ImageLoader sInstance;
    private final ThreadPoolExecutor mDownloadThreadPool;
    private Handler mHandler;

    static {
        sInstance = new ImageLoader();
    }

    private ImageLoader(){

        mDownloadWorkQueue = new LinkedBlockingQueue<Runnable>();

        mDownloadThreadPool = new ThreadPoolExecutor(NUMBER_OF_CORES,MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDownloadWorkQueue);

        mHandler = new Handler(Looper.getMainLooper()){
        /*
             * handleMessage() defines the operations to perform when
             * the Handler receives a new Message to process.
             */

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

    }

    public static ImageLoader getInstance(){
        return sInstance;
    }

    public static Runnable startDownload(ImageView imageView){

        Runnable downloadTask = sInstance.mDownloadWorkQueue.poll();

        // If the queue was empty, create a new task instead.
        if (null == downloadTask) {
            downloadTask = new DownloadImageRunnable();
        }

        sInstance.mDownloadThreadPool.execute(downloadTask);

        return downloadTask;
    }

    public void handleState(DownloadImageRunnable downloadTask, int state) {
        switch (state) {
            // The task finished downloading the image
            case DOWNLOAD_COMPLETE:

        }
    }

}
