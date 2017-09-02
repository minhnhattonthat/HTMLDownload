package com.nhatton.htmldownload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

/**
 * Created by nhatton on 8/26/17.
 */

public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
    private WeakReference<ImageView> imageView;
    private WeakReference<DownloadListener> listener;
    private int position;

    DownloadImageTask(ImageView imageView, int position, DownloadListener listener) {
        this.imageView = new WeakReference<>(imageView);
        this.position = position;
        this.listener = new WeakReference<>(listener);
    }

    protected Bitmap doInBackground(String... urls) {
        return loadImageFromNetwork(urls[0]);
    }

    protected void onPostExecute(Bitmap result) {
        ImageView iv = imageView.get();
        if(iv == null) return;

        iv.setImageBitmap(result);
        DownloadListener listener = this.listener.get();
        if(listener == null) return;

        listener.onDownloadFinished(position);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    private static Bitmap loadImageFromNetwork(String url) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    interface DownloadListener{
        void onDownloadFinished(int position);
    }

}

