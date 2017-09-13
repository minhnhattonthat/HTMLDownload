package com.nhatton.htmldownload;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import static com.nhatton.htmldownload.ImageLoader.DECODE_COMPLETE;

/**
 * Created by nhatton on 9/12/17.
 */

public class DecodeBitmapRunnable implements Runnable {

    final TaskRunnableDecodeMethods mImageTask;

    interface TaskRunnableDecodeMethods {

        void setDecodeThread(Thread currentThread);

        byte[] getByteBuffer();

        void handleDecodeState(int state);

        void setImage(Bitmap bitmap);
    }

    DecodeBitmapRunnable(TaskRunnableDecodeMethods imageTask) {
        mImageTask = imageTask;
    }

    @Override
    public void run() {

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        mImageTask.setDecodeThread(Thread.currentThread());

        Bitmap bitmap = null;

        //Decode bitmap
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

        int targetWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        bitmapOptions.inJustDecodeBounds = true;

        int sampleSize = bitmapOptions.outWidth / targetWidth;

        /*
             * If either of the scaling factors is > 1, the image's
             * actual dimension is larger that the available dimension.
             * This means that the BitmapFactory must compress the image
             * by the larger of the scaling factors. Setting
             * inSampleSize accomplishes this.
             */
        if (sampleSize > 1) {
            bitmapOptions.inSampleSize = sampleSize;
        }

        bitmapOptions.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeByteArray(mImageTask.getByteBuffer(), 0, mImageTask.getByteBuffer().length);

        mImageTask.setImage(bitmap);

        mImageTask.handleDecodeState(DECODE_COMPLETE);

        mImageTask.setDecodeThread(null);

        Thread.interrupted();
    }
}
