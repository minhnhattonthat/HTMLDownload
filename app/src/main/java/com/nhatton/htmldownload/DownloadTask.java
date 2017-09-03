package com.nhatton.htmldownload;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by nhatton on 9/3/17.
 */

public class DownloadTask implements DownloadImageRunnable.TaskRunnableDownloadMethods {

    private ImageLoader sImageLoader;
    private WeakReference<ImageView> mImageWeakRef;
    private int position;

    private Bitmap mBitmap;

    private Runnable mDownloadRunnable;

    public DownloadTask(){
        mDownloadRunnable = new DownloadImageRunnable(this);
        sImageLoader = ImageLoader.getInstance();
    }

    public void initialize(ImageLoader imageLoader, ImageView imageView, int position) {
        this.sImageLoader = imageLoader;
        this.mImageWeakRef = new WeakReference<>(imageView);
        this.position = position;
    }

    @Override
    public void handleDownloadState(int state) {
        sImageLoader.handleState(this, state);
    }

    @Override
    public String getImageURL() {
        return sImageLoader.getUrlList().get(position);
    }

    @Override
    public void setImage(Bitmap bitmap) {
        mBitmap = bitmap;
    }


    public Bitmap getImage() {
        return mBitmap;
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

    public ImageView getImageView() {
        return mImageWeakRef.get();
    }

    public Runnable getDownloadRunnable() {
        return mDownloadRunnable;
    }
}
