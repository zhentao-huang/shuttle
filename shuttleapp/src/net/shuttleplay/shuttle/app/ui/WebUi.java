package net.shuttleplay.shuttle.app.ui;

import net.shuttleplay.shuttle.common.NetUtil;
import net.shuttleplay.shuttle.R;
import net.shuttleplay.shuttle.app.TrendBox;
import net.shuttleplay.shuttle.app.TrendBoxDownloader;
import net.shuttleplay.shuttle.app.TrendBoxEditor;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebUi extends Activity
{

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null)
        {
            Log.d(TAG, "WebUI.onRestoreInstanceState and no data found");
            return;
        }
        Log.d(TAG, "WebUI.onRestoreInstanceState " + savedInstanceState.getString(SAVED_URL));
        
    }



    private static final String SAVED_URL = "SAVED_URL";
    
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        String url = mWebView.getUrl();
        outState.putString(SAVED_URL, url);
        super.onSaveInstanceState(outState);
        if (outState == null)
        {
            Log.d(TAG, "onSaveInstanceState and no data found");
            return;
        }
        Log.d(TAG, "WebUI.onSaveInstanceState " + outState.getString(SAVED_URL));
    }



    @Override
    public void onBackPressed()
    {
        if (mWebView.canGoBack())
        {
            mWebView.goBack();
        }
        else
        {
            super.onBackPressed();
        }
    }



    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "WebUI.onResume");
    }



    private static final String TAG = "TrendBox";
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "WebUI.onCreate");
        
        setContentView(R.layout.webui);
        
        if (mWebView == null)
        {
            mWebView = (WebView) findViewById(R.id.webview);
        }
        
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setBlockNetworkImage(false);
//        settings.setBlockNetworkLoads(false);                 //Only for API Level 8
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setLoadWithOverviewMode(false);
        mWebView.setWebViewClient(new WebViewClientDemo());
        
        String url = "http://" + NetUtil.getLocalIpAddress(this)+ ":8000/";
        
        if (savedInstanceState != null && savedInstanceState.getString(SAVED_URL) != null)
        {
            url = savedInstanceState.getString(SAVED_URL);
        }
        else if (savedInstanceState == null)
        {
            Log.d(TAG, "savedInstaceState = null");
        }
        else 
        {
            Log.d(TAG, "No saved url can be found");
        }
        mWebView.loadUrl(url);
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
            return false;
        }
    }
    
    private WebView mWebView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.webui, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.forward:
        {
            mWebView.goForward();
            break;
        }
        case R.id.refresh:
        {
            mWebView.clearCache(false);
            mWebView.reload();
            break;
        }
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem item = menu.findItem(R.id.forward);
        if (item != null)
        {
            boolean canGoForward = mWebView.canGoForward();
            item.setEnabled(canGoForward);
        }
        return true;
    }



}
