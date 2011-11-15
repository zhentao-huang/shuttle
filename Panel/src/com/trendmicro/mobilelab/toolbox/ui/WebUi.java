package com.trendmicro.mobilelab.toolbox.ui;

import com.trendmicro.mobilelab.toolbox.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebUi extends Activity
{
    @Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        WebView webview = (WebView) findViewById(R.id.webview);
        String uri = "http://127.0.0.1:8000/loader";
        webview.loadUrl(uri);
    }



    private static final String TAG = "TrendBox";
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Create WebUi");
        
        setContentView(R.layout.webui);
        
        WebView webView = (WebView) findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setBlockNetworkImage(false);
        settings.setBlockNetworkLoads(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setLoadWithOverviewMode(false);
        webView.setWebViewClient(new WebViewClientDemo());
    }
    
    
    
    private class WebViewClientDemo extends WebViewClient
    {
        @Override
        public void onLoadResource(WebView view, String url)
        {
            // TODO Auto-generated method stub
            super.onLoadResource(view,url);
            Log.i(TAG, "onLoadResource " + url);
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            // TODO Auto-generated method stub
            super.onPageFinished(view,url);
            Log.i(TAG, "onPageFinished " + url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon)
        {
            // TODO Auto-generated method stub
            super.onPageStarted(view,url,favicon);
            
            Log.i(TAG, "onPageStarted " + url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
        {
            // TODO Auto-generated method stub
            super.onReceivedError(view,errorCode,description,failingUrl);
            
            Log.e(TAG, "onReceivedError errorCode = " + errorCode + description + " url = " + failingUrl);
        }

        @Override
        public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg)
        {
            // TODO Auto-generated method stub
            super.onTooManyRedirects(view,cancelMsg,continueMsg);
            
            Log.e(TAG, "onTooManyRedirects " + cancelMsg.toString() + "continue Message = " + continueMsg.toString());
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
//            view.loadUrl(url);
            return false;
        }
    }

}
