/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wikipedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class WikiPicWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new PicRemoteViewsFactory(this.getApplicationContext(), intent);
    }

}

class PicRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private List<WidgetItem> mWidgetItems = new ArrayList<WidgetItem>();
    private Context mContext;
    private static final String TAG = "PicRemoteViewsFactory";
    private int mAppWidgetId;

    public PicRemoteViewsFactory(Context context, Intent intent) {
        mContext = context.getApplicationContext();
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
    	
    	//TODO, tighten this up and use a single data structure for the app and the widget
    	final ArrayList<PictureEntry> tmpList = getPicWikiPages();
    	Log.d(TAG, "tmplist size " + tmpList.size());
    	for(PictureEntry pic: tmpList) {
			mWidgetItems.add(new WidgetItem(pic.getTitle(), pic.getSummary(), pic.getWikipediaUrl()));
    		//mWidgetItems.add(new WidgetItem(gn.getTitle()));
		}
    	Log.d(TAG, "Wiki Svc Factory onCreate");
        
    }
    
    public static String getSiteURL(String subjectString){
    	//Pattern matching parse 
    	  Pattern regex = Pattern.compile("(<link>(.*?)</link>)", Pattern.DOTALL);
    	  Matcher matcher = regex.matcher(subjectString);

    	  if (matcher.find()) {
    	    String mainURL = matcher.group(2).replace("en.", "en.m.");
    	    Log.d(TAG, mainURL);
    	    return mainURL;

    	} else {
    	  return "";
    	}


    }


    
    ArrayList<PictureEntry> getPicWikiPages( ) {
    	Log.d(TAG, "getPicWikiPages");
		HttpURLConnection urlConnection = null;
		ArrayList<PictureEntry> picList = new ArrayList<PictureEntry>();
		try {
			String requestUrl = "http://evecal.appspot.com/feedParser?feedLink=http://toolserver.org/~skagedal/feeds/potd.xml&response=json";
			Log.d(TAG, requestUrl);
			URL url = new URL(requestUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			String jsonStr = RestJsonClient.convertStreamToString(urlConnection.getInputStream());
			Log.d("RestJsonClient", jsonStr);

			// getting data and if we don't we just get out of here!
			JSONArray picoftheday = null;
			try {
				JSONObject json = new JSONObject(jsonStr);
				picoftheday = json.getJSONArray("items");
				Log.d(TAG, "json array size is " + picoftheday.length());
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
				return null;
			} 

			for (int i = 0; i < picoftheday.length(); i++) {
				try {
					JSONObject picObject = picoftheday.getJSONObject(i);
					picList.add(new PictureEntry(
							picObject.getString("link"), 
							picObject.getString("title"), 
							picObject.getString("description")));
					Log.d(TAG, "index in json array is " + i);
				} catch(JSONException e) {
					// ignore exception and keep going!
					e.printStackTrace();
				}
			}
			//return picList;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			urlConnection.disconnect();
		}
		Log.d(TAG, "picList size is " + picList.size());
		String request = "http://toolserver.org/~skagedal/feeds/potd.xml";
		Log.d(TAG, request);
		URL url;
		try {
			url = new URL(request);
			
			urlConnection = (HttpURLConnection) url.openConnection();
			imageFetch(urlConnection.getInputStream());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
		return picList;
	}

    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mWidgetItems.clear();
    }

    public int getCount() {
        return mWidgetItems.size();
    }
    
    String imageFetch(InputStream input) {
    	Log.d(TAG, "piclist image fetch");
    	ArrayList<String> imgs = new ArrayList<String>();
    	BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		StringBuilder sb = new StringBuilder(500);
		int c;

		try {
			while ((c = reader.read()) != -1) {
				sb.append(c);
			}
		} catch (IOException e) {
			Log.e(TAG, "EXC");
			e.printStackTrace();
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
		}
		sb.trimToSize();
		Log.d(TAG, "stringbuilder length is " + sb.length());
		sb.substring(sb.indexOf("<item>"));
		Log.d(TAG, sb.toString());
		String pointer;
		int tmpIndex;
		Log.d(TAG,"while loop");
		while(sb.toString().contains("<description>")) {
			
			tmpIndex = sb.indexOf("<description>");
			sb.substring(tmpIndex);
			pointer = sb.substring(sb.indexOf("src=")+5, sb.indexOf("width")-2);
			//pull out the jpg web location
			Log.d(TAG, "next image = " + pointer);
			imgs.add(pointer);
		}
		
		
		return sb.toString();

    }
    
    private static int[] images = {
    	R.drawable.photo1bird, R.drawable.photo1bird, R.drawable.photo2chichen_itza, R.drawable.photo3periclimenes, R.drawable.photo4da_vinci,
    	R.drawable.photo5psalm_23, R.drawable.photo6molybdenum, R.drawable.photo7edwardteller1958, R.drawable.photo8eumecesfasciatus,
    	R.drawable.photo9caterpillar
    };

    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        // We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.pic_widget_item);
        rv.setTextViewText(R.id.widget_item, mWidgetItems.get(position).text);
        rv.setTextViewText(R.id.widget_summary, mWidgetItems.get(position).summary);
        if(position < 10) {
        	rv.setImageViewResource(R.id.widget_pic, images[position]);
        } else {
        	rv.setImageViewResource(R.id.widget_pic, R.drawable.icon);
        }
        /*rv.setTextViewText(R.id.widget_url, mWidgetItems.get(position).url);*/
        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putString(WikiWidgetProvider.URL_TAG, mWidgetItems.get(position).url);
        
        Log.d(TAG, "set url as extra " + mWidgetItems.get(position).url);
        Intent fillInIntent = new Intent(); //new Intent();
        //fillInIntent.setData(Uri.parse(mWidgetItems.get(position).url));  
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.

        // Return the remote views object.
        return rv;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
    }
}
