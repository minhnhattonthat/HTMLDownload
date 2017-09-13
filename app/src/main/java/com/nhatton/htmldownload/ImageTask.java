package com.nhatton.htmldownload;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by nhatton on 9/3/17.
 */

public class ImageTask implements DownloadImageRunnable.TaskRunnableDownloadMethods, DecodeBitmapRunnable.TaskRunnableDecodeMethods {

    private static ImageLoader sImageLoader;
    private WeakReference<ImageView> mImageWeakRef;
    private int position;

    private Runnable mDownloadRunnable;
    private Runnable mDecodeRunnable;

    private byte[] mByteBuffer;

    // The Thread on which this task is currently running.
    Thread mCurrentThread;


    ImageTask() {
        mDownloadRunnable = new DownloadImageRunnable(this);
        mDecodeRunnable = new DecodeBitmapRunnable(this);
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

    @Override
    public void setImage(Bitmap bitmap) {
        sImageLoader.getBitmaps().add(position, bitmap);
    }

    @Override
    public byte[] getByteBuffer() {
        return mByteBuffer;
    }


    @Override
    public void setDecodeThread(Thread currentThread) {

    }

    @Override
    public void setByteBuffer(byte[] buffer) {
        mByteBuffer = buffer;
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

    Runnable getDecodeRunnable() {
        return mDecodeRunnable;
    }

    @Override
    public void setDownloadThread(Thread currentThread) {
        mCurrentThread = currentThread;
    }

    @Override
    public void handleDownloadState(int state) {
        sImageLoader.handleState(this, state);
    }

    @Override
    public void handleDecodeState(int state) {
        sImageLoader.handleState(this, state);
    }

    /**
     * Recycles an ImageTask object before it's put back into the pool. One reason to do
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
