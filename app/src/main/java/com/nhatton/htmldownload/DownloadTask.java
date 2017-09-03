package com.nhatton.htmldownload;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by nhatton on 9/3/17.
 */

public class DownloadTask implements DownloadImageRunnable.TaskRunnableDownloadMethods {

    private static ImageLoader sImageLoader;
    private WeakReference<ImageView> mImageWeakRef;
    private int position;

    private Bitmap mBitmap;

    private Runnable mDownloadRunnable;

    /*
     * Field containing the Thread this task is running on.
     */
    Thread mThreadThis;

    // The Thread on which this task is currently running.
    Thread mCurrentThread;

    DownloadTask() {
        mDownloadRunnable = new DownloadImageRunnable(this);
        sImageLoader = ImageLoader.getInstance();
    }

    void initialize(ImageLoader imageLoader, ImageView imageView, int position) {
        sImageLoader = imageLoader;
        this.mImageWeakRef = new WeakReference<>(imageView);
        this.position = position;
    }

    @Override
    public String getImageURL() {
        return sImageLoader.getUrlList().get(position);
    }

    Bitmap getImage() {
        return mBitmap;
    }

    @Override
    public void setImage(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    ImageView getImageView() {
        return mImageWeakRef.get();
    }

    Runnable getDownloadRunnable() {
        return mDownloadRunnable;
    }

    /*
     * Returns the Thread that this Task is running on. The method must first get a lock on a
     * static field, in this case the ThreadPool singleton. The lock is needed because the
     * Thread object reference is stored in the Thread object itself, and that object can be
     * changed by processes outside of this app.
     */
    public Thread getCurrentThread() {
        synchronized (sImageLoader) {
            return mCurrentThread;
        }
    }

    /*
     * Sets the identifier for the current Thread. This must be a synchronized operation; see the
     * notes for getCurrentThread()
     */
    public void setCurrentThread(Thread thread) {
        synchronized (sImageLoader) {
            mCurrentThread = thread;
        }
    }

    @Override
    public void handleDownloadState(int state) {
        sImageLoader.handleState(this, state);
    }

    /**
     * Recycles an DownloadTask object before it's put back into the pool. One reason to do
     * this is to avoid memory leaks.
     */
    void recycle() {

        // Deletes the weak reference to the mImageWeakRef
        if (null != mImageWeakRef) {
            mImageWeakRef.clear();
            mImageWeakRef = null;
        }

        // Releases references to the BitMap
        mBitmap = null;
    }

}
