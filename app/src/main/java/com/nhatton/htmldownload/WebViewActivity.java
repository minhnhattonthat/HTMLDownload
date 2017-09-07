package com.nhatton.htmldownload;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class WebViewActivity extends AppCompatActivity {

    WebView wv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        String url = getIntent().getStringExtra("url");

        wv = findViewById(R.id.web_view);

        wv.getSettings().setJavaScriptEnabled(false);

        wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        Map<String, String> noCacheHeaders = new HashMap<String, String>(2);
        noCacheHeaders.put("Cache-Control", "no-cache");

        wv.clearCache(true);

        wv.clearHistory();

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Counter.INSTANCE.start();
                Log.e("Start html", url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Counter.INSTANCE.end();
                Toast.makeText(WebViewActivity.this,
                        String.valueOf(Counter.INSTANCE.count()) + "ms",
                        Toast.LENGTH_SHORT)
                        .show();
                Log.e("Webview download time", String.valueOf(Counter.INSTANCE.count()) + "ms");
                view.clearCache(true);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.e("Override html", request.getUrl().toString());
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

//        wv.loadUrl(url);
        wv.loadUrl(url, noCacheHeaders);
    }

    @Override
    protected void onPause() {
        wv.clearCache(true);
        wv.clearHistory();
        wv.destroy();
        super.onPause();
    }
}
