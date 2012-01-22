package org.wikipedia;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.phonegap.DroidGap;

public class WikipediaActivity extends DroidGap {
    /** Called when the activity is first created. */
	
	public class WikipediaWebViewClient extends GapViewClient {
		public WikipediaWebViewClient(DroidGap ctx) {
			super(ctx);
		}
		
		//@Override
		//public void onLoadResource(WebView view, String url) {
		//	Log.d("WikipediaWebViewClient", "OnLoadResource "+url);
		//}
		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // NearBy stuff
		SharedPreferences preferences = getSharedPreferences("nearby", MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove("doSearchNearBy");
		editor.commit();
		
        super.loadUrl("file:///android_asset/www/index.html");
        this.webViewClient = new WikipediaWebViewClient(this);
        this.appView.setWebViewClient(this.webViewClient);
    }
    
    
    
    @Override
	protected void onResume() {
		// This is where the activity can pick up the URL from the widget
		super.onResume();
		final Bundle extras = getIntent().getExtras();
		if(extras != null) {
			final String extraURL = extras.getString(WikiWidgetProvider.URL_TAG);
			
			if(extraURL != null) {
				Log.d(TAG, "url in wikipedia activity is " + extraURL);
			}
		}
		
		
	}



	@Override
    public void onReceivedError(final int errorCode, final String description, final String failingUrl) {
    	// no-op!
    }
}