package com.nhatton.htmldownload;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText linkText;
    private RecyclerView listView;
    private String response;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linkText = findViewById(R.id.get_link);
        Button getButton = findViewById(R.id.get_button);
        Button webViewButton = findViewById(R.id.web_view_button);
        Button resetButton = findViewById(R.id.reset_button);
        listView = findViewById(R.id.list_view);

        listView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        final HttpHandler handler = new HttpHandler();

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Counter.INSTANCE.start();

                String link = linkText.getText().toString();
                link = autoCorrectUrl(link);

                response = handler.makeServiceCall(link);
                Log.d("Response", response);
                final ArrayList<String> images = filterResponse();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageAdapter adapter = new ImageAdapter(images, MainActivity.this);
                        listView.setAdapter(adapter);
                    }
                });
            }
        });

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!linkText.getText().toString().isEmpty()) {
                    thread.start();
                }
            }
        });

        webViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!linkText.getText().toString().isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                    intent.putExtra("url", autoCorrectUrl(linkText.getText().toString()));
                    startActivity(intent);
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linkText.getText().clear();

            }
        });
    }

    private ArrayList<String> filterResponse() {

        ArrayList<String> result = new ArrayList<>();
        String regex = "(<img.*src|content)=\"https?://.*\\.(jpg|png)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(response);

        int i = 0;
        while (matcher.find(i)) {
            String item = response.substring(matcher.start(), matcher.end());
            item = item.replaceFirst("(<img.*src|content)=\"", "");
            if (item.indexOf("http") != item.lastIndexOf("http")) {
                item = item.substring(item.lastIndexOf("http"));
            }
            result.add(item);
            i = matcher.end();
        }

        Log.e("Number of images", String.valueOf(result.size()));
        return result;
    }

    private static String autoCorrectUrl(String url) {
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        return url;
    }
}
