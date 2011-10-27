package com.trendmicro.mobilelab.toolbox.ui;

import com.trendmicro.mobilelab.toolbox.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebUi extends Activity
{
    private static final String TAG = "TrendBox";
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Create WebUi");
        
        setContentView(R.layout.webui);
        
        WebView webView = (WebView) findViewById(R.id.webview);
        String uri = "http://127.0.0.1:8000/main";
        webView.setWebViewClient(new WebViewClientDemo());
        webView.loadUrl(uri);
    }
    
    private class WebViewClientDemo extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            view.loadUrl(url);
            return true;
        }
    }

}
