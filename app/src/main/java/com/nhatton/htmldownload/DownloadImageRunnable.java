package com.nhatton.htmldownload;

import java.net.URL;

/**
 * Created by nhatton on 9/3/17.
 */

public class DownloadImageRunnable implements Runnable{

    // Defines a field that contains the calling object of type PhotoTask.
    final TaskRunnableDownloadMethods mImageTask;

    /**
     *
     * An interface that defines methods that PhotoTask implements. An instance of
     * PhotoTask passes itself to an PhotoDownloadRunnable instance through the
     * PhotoDownloadRunnable constructor, after which the two instances can access each other's
     * variables.
     */
    interface TaskRunnableDownloadMethods {

        /**
         * Sets the Thread that this instance is running on
         * @param currentThread the current Thread
         */
        void setDownloadThread(Thread currentThread);

        /**
         * Returns the current contents of the download buffer
         * @return The byte array downloaded from the URL in the last read
         */
        byte[] getByteBuffer();

        /**
         * Sets the current contents of the download buffer
         * @param buffer The bytes that were just read
         */
        void setByteBuffer(byte[] buffer);

        /**
         * Defines the actions for each state of the PhotoTask instance.
         * @param state The current state of the task
         */
        void handleDownloadState(int state);

        /**
         * Gets the URL for the image being downloaded
         * @return The image URL
         */
        String getImageURL();
    }

    public DownloadImageRunnable(TaskRunnableDownloadMethods imageTask){
        mImageTask = imageTask;
    }

    @Override
    public void run() {
        String url = mImageTask.getImageURL();

    }
}
