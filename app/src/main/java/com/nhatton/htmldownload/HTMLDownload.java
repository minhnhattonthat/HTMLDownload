package com.nhatton.htmldownload;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;

import static com.nhatton.htmldownload.util.HtmlHelper.filterResponse;

/**
 * Created by nhatton on 10/16/17.
 */

public class HTMLDownload {

    private static final int GET_LINKS_COMPLETE = 25;

    private static String mLink;
    private RecyclerView mListView;
    private Context mContext;
    private ArrayList<String> imageLinks;

    private Handler mHandler;

    //Start download all images in background
    private Runnable downloadTask = new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            ImageLoader.startDownloadAll();
        }
    };

    private Runnable getImageLinksTask = new Runnable() {
        @Override
        public void run() {
            HttpHandler handler = new HttpHandler();
            String response = handler.makeServiceCall(mLink);
            Log.d("Response", response);
            imageLinks = filterResponse(response);
            Message completeMessage = mHandler.obtainMessage(GET_LINKS_COMPLETE);
            completeMessage.sendToTarget();
        }
    };

    public HTMLDownload(Context context) {
        mContext = context;

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case GET_LINKS_COMPLETE:
                        ImageLoader.getInstance().setUrlList(imageLinks);
                        ImageAdapter adapter = new ImageAdapter(imageLinks, mContext);
                        mListView.setAdapter(adapter);

                        Thread thread = new Thread(downloadTask);
                        thread.start();
                        break;
                }
            }
        };
    }

    public void load(String link, RecyclerView listView) {
        mLink = link;
        mListView = listView;

        if (link.isEmpty()) {
            return;
        }

        tuneConnection();

        Thread thread = new Thread(getImageLinksTask);
        thread.start();
    }

    private void tuneConnection() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            ImageLoader.getInstance().adjustThreadCount(networkInfo);
        }
    }

    public void resetAll() {
        if (mListView != null) {
            mListView.setAdapter(null);
        }

        ImageLoader.cancelAll();
    }
}
