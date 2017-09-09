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

    void initialize(ImageLoader imageLoader, int position) {
        sImageLoader = imageLoader;
        this.position = position;
    }

    @Override
    public String getImageURL() {
        return sImageLoader.getUrlList().get(position);
    }

    Bitmap getImage() {
        return sImageLoader.getBitmaps().get(position);
    }

    @Override
    public void setImage(Bitmap bitmap) {
        sImageLoader.getBitmaps().add(position, bitmap);
    }

    int getPosition() {
        return position;
    }

    ImageView getImageView() {
        return mImageWeakRef.get();
    }

    Runnable getDownloadRunnable() {
        return mDownloadRunnable;
    }

    @Override
    public void setDownloadThread(Thread currentThread) {
        mCurrentThread = currentThread;
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
//        sImageLoader.getBitmaps().set(position, null);
    }

}
