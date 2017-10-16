package com.nhatton.htmldownload;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;

import static com.nhatton.htmldownload.util.HtmlHelper.autoCorrectUrl;
import static com.nhatton.htmldownload.util.HtmlHelper.filterResponse;

/**
 * Created by nhatton on 10/16/17.
 */

public class HTMLDownload {

    private static String mLink;
    private RecyclerView mListView;
    private Context mContext;

    public HTMLDownload(Context context){
        mContext = context;
    }

    public void load(String link, RecyclerView listView) {
        mLink = link;
        mListView = listView;

        if (link.isEmpty()) {
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpHandler handler = new HttpHandler();
                String response = handler.makeServiceCall(mLink);
                Log.d("Response", response);
                final ArrayList<String> images = filterResponse(response);

                Handler mHandler = new Handler(mContext.getMainLooper());
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        ImageAdapter adapter = new ImageAdapter(images, mContext);
                        mListView.setAdapter(adapter);
                    }
                };
                mHandler.post(r);
            }
        });
        thread.start();
    }

    public void resetAll() {
        if (mListView != null) {
            mListView.setAdapter(null);
        }

        ImageLoader.cancelAll();
    }
}
