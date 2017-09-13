package com.nhatton.htmldownload;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.nhatton.htmldownload.ImageLoader.DOWNLOAD_COMPLETE;
import static com.nhatton.htmldownload.ImageLoader.DOWNLOAD_START;

/**
 * Created by nhatton on 9/3/17.
 */

public class DownloadImageRunnable implements Runnable {

    // Defines a field that contains the calling object of type PhotoTask.
    final TaskRunnableDownloadMethods mImageTask;

    /**
     * An interface that defines methods that PhotoTask implements. An instance of
     * PhotoTask passes itself to an PhotoDownloadRunnable instance through the
     * PhotoDownloadRunnable constructor, after which the two instances can access each other's
     * variables.
     */
    interface TaskRunnableDownloadMethods {

        /**
         * Sets the Thread that this instance is running on
         *
         * @param currentThread the current Thread
         */
        void setDownloadThread(Thread currentThread);

        /**
         * Returns the current contents of the download buffer
         *
         * @return The byte array downloaded from the URL in the last read
         */
        byte[] getByteBuffer();

        /**
         * Sets the current contents of the download buffer
         *
         * @param buffer The bytes that were just read
         */
        void setByteBuffer(byte[] buffer);

        /**
         * Defines the actions for each state of the PhotoTask instance.
         *
         * @param state The current state of the task
         */
        void handleDownloadState(int state);

        /**
         * Gets the URL for the image being downloaded
         *
         * @return The image URL
         */
        String getImageURL();
    }

    DownloadImageRunnable(TaskRunnableDownloadMethods imageTask) {
        mImageTask = imageTask;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        mImageTask.setDownloadThread(Thread.currentThread());

        String url = mImageTask.getImageURL();

        mImageTask.handleDownloadState(DOWNLOAD_START);

        Bitmap bitmap = null;

        InputStream is = null;
        byte[] byteBuffer = null;

        try {
            HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();

            is = httpConn.getInputStream();

            int contentLength = httpConn.getContentLength();

            if (contentLength == -1) {
                byte[] tempBuffer = new byte[1024 * 2];

                int bufferLeft = tempBuffer.length;
                int bufferOffset = 0;
                int result = 0;

                outer:
                do {
                    while (bufferLeft > 0) {
                        result = is.read(tempBuffer, bufferOffset, bufferLeft);
                        if (result < 0) {
                            break outer;
                        }
                        bufferOffset = bufferOffset + result;
                        bufferLeft = bufferLeft - result;
                    }
                    bufferLeft = 1024 * 2;
                    int newSize = tempBuffer.length + bufferLeft;
                    byte[] expandedBuffer = new byte[newSize];
                    System.arraycopy(tempBuffer, 0, expandedBuffer, 0, tempBuffer.length);
                    tempBuffer = expandedBuffer;

                } while (true);

                byteBuffer = new byte[bufferOffset];
                System.arraycopy(tempBuffer, 0, byteBuffer, 0, bufferOffset);

            } else {
                byteBuffer = new byte[contentLength];

                int remain = contentLength;

                int bufferOffset = 0;

                while (remain > 0) {
                    int result = is.read(byteBuffer, bufferOffset, remain);

                    bufferOffset = bufferOffset + result;
                    remain = remain - result;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        mImageTask.setByteBuffer(byteBuffer);

        mImageTask.handleDownloadState(DOWNLOAD_COMPLETE);

        mImageTask.setDownloadThread(null);

        Thread.interrupted();
    }
}
