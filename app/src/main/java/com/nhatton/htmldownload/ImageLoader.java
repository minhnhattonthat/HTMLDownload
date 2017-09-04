package com.nhatton.htmldownload;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;

/**
 * Created by nhatton on 8/31/17.
 */

public class ImageLoader {

    static final int DOWNLOAD_START = 9;
    static final int DOWNLOAD_COMPLETE = 25;
    /*
    * Gets the number of available cores
    * (not always the same as the maximum number of cores)
    */
    private static final int NUMBER_OF_CORES = 4;

    private static final int MAXIMUM_POOL_SIZE = 40;

    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    // A queue of Runnables
    private final BlockingQueue<Runnable> mDownloadWorkQueue;

    // A queue of ImageLoader tasks. Tasks are handed to a ThreadPool.
    private final Queue<DownloadTask> mDownloadTaskWorkQueue;

    private static ImageLoader sInstance;
    private ThreadPoolExecutor mDownloadThreadPool;
    private Handler mHandler;

    private ArrayList<String> urlList;

    private ArrayList<Bitmap> bitmaps;

    static {
        sInstance = new ImageLoader();
    }

    private ImageLoader() {

        mDownloadWorkQueue = new LinkedBlockingQueue<Runnable>();

        mDownloadTaskWorkQueue = new LinkedBlockingQueue<DownloadTask>();

        mDownloadThreadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDownloadWorkQueue);

        mHandler = new Handler(Looper.getMainLooper()) {

            /*
             * handleMessage() defines the operations to perform when
             * the Handler receives a new Message to process.
             */
            @Override
            public void handleMessage(Message msg) {
                DownloadTask task = (DownloadTask) msg.obj;
                ImageView imageView = task.getImageView();

                if (imageView != null) {
                    switch (msg.what) {
                        case DOWNLOAD_COMPLETE:
                            imageView.setImageBitmap(task.getImage());
                            recycleTask(task);
                            break;
                        default:
                            super.handleMessage(msg);
                    }
                }

            }
        };

    }

    static ImageLoader getInstance() {
        return sInstance;
    }

    ArrayList<String> getUrlList() {
        return urlList;
    }

    void setUrlList(ArrayList<String> urlList) {
        this.urlList = urlList;
        bitmaps = new ArrayList<>();
        while (bitmaps.size() < urlList.size()) bitmaps.add(null);
    }

    ArrayList<Bitmap> getBitmaps(){
        return bitmaps;
    }

    static DownloadTask startDownload(ImageView imageView, int position) {

        DownloadTask downloadTask = sInstance.mDownloadTaskWorkQueue.poll();

        // If the queue was empty, create a new task instead.
        if (null == downloadTask) {
            downloadTask = new DownloadTask();
        }

        downloadTask.initialize(sInstance, imageView, position);

        sInstance.mDownloadThreadPool.execute(downloadTask.getDownloadRunnable());

        return downloadTask;
    }

    static void startDownloadAll(){

        Log.e("Start download", String.valueOf(currentTimeMillis()));

        for(int i = 0; i < sInstance.urlList.size(); i ++){
            DownloadTask downloadTask = sInstance.mDownloadTaskWorkQueue.poll();

            // If the queue was empty, create a new task instead.
            if (null == downloadTask) {
                downloadTask = new DownloadTask();
            }

            downloadTask.initialize(sInstance, null, i);

            sInstance.mDownloadThreadPool.execute(downloadTask.getDownloadRunnable());

        }
        sInstance.mDownloadThreadPool.shutdown();
        try {
            sInstance.mDownloadThreadPool.awaitTermination(10, TimeUnit.SECONDS);
            Counter.INSTANCE.end();
            Log.e("Total download time", String.valueOf(Counter.INSTANCE.count()) + "ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sInstance.mDownloadThreadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, sInstance.mDownloadWorkQueue);

    }

    void handleState(DownloadTask downloadTask, int state) {
        switch (state) {
            // The task finished downloading the image
            case DOWNLOAD_COMPLETE:
                // Gets a Message object, stores the state in it, and sends it to the Handler
                Message completeMessage = mHandler.obtainMessage(state, downloadTask);
                completeMessage.sendToTarget();
                break;

            case DOWNLOAD_START:
                break;
            default:
                break;
        }
    }

    /**
     * Cancels all Threads in the ThreadPool
     */
    public static void cancelAll() {

        /*
         * Creates an array of tasks that's the same size as the task work queue
         */
        DownloadTask[] taskArray = new DownloadTask[sInstance.mDownloadWorkQueue.size()];

        // Populates the array with the task objects in the queue
        sInstance.mDownloadWorkQueue.toArray(taskArray);

        // Stores the array length in order to iterate over the array
        int taskArraylen = taskArray.length;

        /*
         * Locks on the singleton to ensure that other processes aren't mutating Threads, then
         * iterates over the array of tasks and interrupts the task's current Thread.
         */
        synchronized (sInstance) {

            // Iterates over the array of tasks
            for (int taskArrayIndex = 0; taskArrayIndex < taskArraylen; taskArrayIndex++) {

                // Gets the task's current thread
                Thread thread = taskArray[taskArrayIndex].mThreadThis;

                // if the Thread exists, post an interrupt to it
                if (null != thread) {
                    thread.interrupt();
                }
            }
        }

    }

    /**
     * Recycles tasks by calling their internal recycle() method and then putting them back into
     * the task queue.
     *
     * @param downloadTask The task to recycle
     */
    void recycleTask(DownloadTask downloadTask) {

        // Frees up memory in the task
        downloadTask.recycle();

        // Puts the task object back into the queue for re-use.
        mDownloadTaskWorkQueue.offer(downloadTask);
    }
}
