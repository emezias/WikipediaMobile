package org.wikipedia;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class WikiWidgetActivity extends Activity {
	private static final String TAG = "WikiWidgetActivity";
	private WebView mWebView;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.widget_activity);
	    
	    mWebView = (WebView) findViewById(R.id.webview);
	    mWebView.getSettings().setJavaScriptEnabled(true);

	    
	}

	@Override
	protected void onResume() {
		// Get the URL from the widget and load it
		super.onResume();
	    final Bundle b = getIntent().getExtras();
	    String location = getIntent().getStringExtra(WikiWidgetProvider.URL_TAG);
	    if(location != null) {
	    	Log.d(TAG, "first try " + location);
	    }
	    Log.d(TAG, "onResume of widget activity"); 
	    if(b != null) {
	    	location = b.getString(WikiWidgetProvider.URL_TAG);
            if(location == null) {
            	location = "file:///android_asset/www/index.html";
            } else {
            	}
    	    Log.d(TAG, "location? " + location);            	
	    } else {
	    	location = "file:///android_asset/www/index.html"; //file:///android_asset/www/index.html
	    }
	    Log.d(TAG, "loading now" + location);
    	mWebView.loadUrl(location);
	    mWebView.invalidate();
	}
	
}
