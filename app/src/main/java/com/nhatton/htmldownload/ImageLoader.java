package com.nhatton.htmldownload;

import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
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
    static final int ALL_DOWNLOAD_COMPLETE = 259;
    static final int DECODE_COMPLETE = 92;
    /*
    * Gets the number of available cores
    * (not always the same as the maximum number of cores)
    */
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private static final int MAXIMUM_POOL_SIZE = NUMBER_OF_CORES * 2;

    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;

    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    // A queue of Runnables
    private final BlockingQueue<Runnable> mDownloadWorkQueue;

    // A queue of ImageLoader tasks. Tasks are handed to a ThreadPool.
    private final BlockingQueue<ImageTask> mImageTaskWorkQueue;

    private static ImageLoader sInstance;
    private ThreadPoolExecutor mDownloadThreadPool;
    private Handler mHandler;

    private ArrayList<String> urlList;

    private ArrayList<Bitmap> bitmaps;

    static {
        sInstance = new ImageLoader();
    }

    private ImageLoader() {

        mDownloadWorkQueue = new LinkedBlockingQueue<>();

        mImageTaskWorkQueue = new LinkedBlockingQueue<>();

        mDownloadThreadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDownloadWorkQueue);

        mHandler = new Handler(Looper.getMainLooper()) {

            /*
             * handleMessage() defines the operations to perform when
             * the Handler receives a new Message to process.
             */
            @Override
            public void handleMessage(Message msg) {
                ImageTask task = (ImageTask) msg.obj;
//                ImageView imageView = task.getImageView();

                switch (msg.what) {
                    case DOWNLOAD_COMPLETE:
//                        imageView.setImageBitmap(task.getImage());
                        sInstance.mDownloadThreadPool.execute(task.getDecodeRunnable());
                        recycleTask(task);
                        break;
                    case ALL_DOWNLOAD_COMPLETE:
                        break;
                    case DECODE_COMPLETE:
                        recycleTask(task);
                        break;
                    default:
                        super.handleMessage(msg);
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

    ArrayList<Bitmap> getBitmaps() {
        return bitmaps;
    }

    static ImageTask startDownload(ImageView imageView, int position) {

        ImageTask imageTask = sInstance.mImageTaskWorkQueue.poll();

        // If the queue was empty, create a new task instead.
        if (null == imageTask) {
            imageTask = new ImageTask();
        }

        imageTask.initialize(sInstance, imageView, position);

        sInstance.mDownloadThreadPool.execute(imageTask.getDownloadRunnable());

        return imageTask;
    }

    private static ImageTask startDownload(int position) {

        ImageTask imageTask = sInstance.mImageTaskWorkQueue.poll();

        // If the queue was empty, create a new task instead.
        if (null == imageTask) {
            imageTask = new ImageTask();
        }

        imageTask.initialize(sInstance, position);
        sInstance.mDownloadThreadPool.execute(imageTask.getDownloadRunnable());

        return imageTask;
    }

    static void startDownloadAll() {

        Log.e("Start download", String.valueOf(currentTimeMillis()));

        for (int i = 0; i < sInstance.urlList.size(); i++) {
            startDownload(i);
        }

//        sInstance.mDownloadThreadPool.shutdown();

//        try {
//            sInstance.mDownloadThreadPool.awaitTermination(60, TimeUnit.SECONDS);
//            Counter.INSTANCE.end();
//            Log.e("Total download time", String.valueOf(Counter.INSTANCE.count()) + "ms");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        //Init the threadpool again after finish
//        sInstance.mDownloadThreadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, MAXIMUM_POOL_SIZE,
//                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, new LinkedBlockingQueue<Runnable>());

    }

    void handleState(ImageTask imageTask, int state) {
        switch (state) {
            // The task finished downloading the image
            case DOWNLOAD_COMPLETE:
                // Gets a Message object, stores the state in it, and sends it to the Handler
                Message completeMessage = mHandler.obtainMessage(state, imageTask);
                completeMessage.sendToTarget();
                break;
            case DOWNLOAD_START:
                break;
            case DECODE_COMPLETE:
                completeMessage = mHandler.obtainMessage(state, imageTask);
                completeMessage.sendToTarget();
                break;
            default:
                break;
        }
    }

    /**
     * Cancels all Threads in the ThreadPool
     */
    static void cancelAll() {

        /*
         * Creates an array of tasks that's the same size as the task work queue
         */
        ImageTask[] taskArray = new ImageTask[sInstance.mImageTaskWorkQueue.size()];

        // Populates the array with the task objects in the queue
        sInstance.mImageTaskWorkQueue.toArray(taskArray);

        /*
         * Locks on the singleton to ensure that other processes aren't mutating Threads, then
         * iterates over the array of tasks and interrupts the task's current Thread.
         */
        synchronized (ImageLoader.class) {

            // Iterates over the array of tasks
            for (ImageTask aTaskArray : taskArray) {

                // Gets the task's current thread
                Thread thread = aTaskArray.mCurrentThread;

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
     * @param imageTask The task to recycle
     */
    private void recycleTask(ImageTask imageTask) {

        // Frees up memory in the task
        imageTask.recycle();

        // Puts the task object back into the queue for re-use.
        mImageTaskWorkQueue.offer(imageTask);
    }

    void reset() {
        bitmaps = new ArrayList<>();
        urlList = new ArrayList<>();
    }

    public void adjustThreadCount(NetworkInfo info) {
        if (info == null || !info.isConnectedOrConnecting()) {
            setThreadCount(NUMBER_OF_CORES);
            return;
        }
        switch (info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_WIMAX:
            case ConnectivityManager.TYPE_ETHERNET:
                setThreadCount(4);
                break;
            case ConnectivityManager.TYPE_MOBILE:
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:  // 4G
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        setThreadCount(3);
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS: // 3G
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        setThreadCount(2);
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS: // 2G
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        setThreadCount(1);
                        break;
                    default:
                        setThreadCount(NUMBER_OF_CORES);
                }
                break;
            default:
                setThreadCount(NUMBER_OF_CORES);
        }
    }

    private void setThreadCount(int count) {
        mDownloadThreadPool.setCorePoolSize(count);
        mDownloadThreadPool.setMaximumPoolSize(count * 2);
    }
}
