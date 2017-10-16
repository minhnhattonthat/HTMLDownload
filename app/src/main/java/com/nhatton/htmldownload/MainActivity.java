package com.nhatton.htmldownload;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.nhatton.htmldownload.util.HtmlHelper.autoCorrectUrl;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText linkText;
    private RecyclerView listView;

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

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HTMLDownload htmlDownload = new HTMLDownload(MainActivity.this);
                htmlDownload.load(linkText.getText().toString(), listView);
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

        linkText.setText("https://cryptid-creations.deviantart.com/gallery/");
    }

}
