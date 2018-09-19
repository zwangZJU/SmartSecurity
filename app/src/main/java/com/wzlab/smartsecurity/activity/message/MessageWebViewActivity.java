package com.wzlab.smartsecurity.activity.message;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

import com.wzlab.smartsecurity.R;

public class MessageWebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        setContentView(R.layout.activity_message_web_view);
        WebView mWebView = findViewById(R.id.wv_message);
        String url = getIntent().getStringExtra("url");
        // String url = "http://118.126.95.215:9090/cdz/app/a.html";
        mWebView.loadUrl(url);
    }
}
