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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class WikiPicWidgetService extends RemoteViewsService {
	/*
	 * This class helps the WidgetProvider class populate the views of the collection
	 * The Provider creates the stack, this service creates the views in that stack 
	 */
	
	@Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new PicRemoteViewsFactory(this.getApplicationContext(), intent);
    }

}

class PicRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "PicRemoteViewsFactory";
    private static final String POTD_STREAM = "http://toolserver.org/~skagedal/feeds/potd.xml";
    private static final int MAX_SIZE = 5120;
    /* mWidgetItems will cache the data that will be used to fill the items in the list
    	it is a collection of WidgetItem classes
    	refer to widget_item.xml and the WidgetItem class
    */
    private static List<WidgetItem> mWidgetItems = new CopyOnWriteArrayList<WidgetItem>();
    private Context mContext;

    public PicRemoteViewsFactory(Context context, Intent intent) {
        mContext = context.getApplicationContext();        
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
    	
    	//TODO, tighten this up and use a single data structure for the app and the widget
    	final ArrayList<PictureEntry> tmpList = getPicWikiPages();

    	for(PictureEntry pic: tmpList) {
			mWidgetItems.add(new WidgetItem(pic.getTitle(), pic.getSummary(), pic.getWikipediaUrl()));
    		//mWidgetItems.add(new WidgetItem(gn.getTitle()));
		}
    	//Log.d(TAG, "Wiki Svc Factory onCreate");
        
    }
    
    public static void updateWidgetItems( ) {
    	final ArrayList<PictureEntry> tmpList = getPicWikiPages();

    	for(PictureEntry pic: tmpList) {
			mWidgetItems.add(new WidgetItem(pic.getTitle(), pic.getSummary(), pic.getWikipediaUrl()));
    		//mWidgetItems.add(new WidgetItem(gn.getTitle()));
		}
    }
    
    //Pattern matching method to identify the page url for the page displayed in the widget's list view
    public static ArrayList<String> getSiteURLs(String subjectString){
    	//Pattern matching parse 
    	  Pattern regex = Pattern.compile("(<link>(.*?)</link>)", Pattern.DOTALL);
    	  Matcher matcher = regex.matcher(subjectString);
    	  ArrayList<String> wikiPages = new ArrayList<String>();
  	    	//run through the string and pull out the page urls
    	  while (matcher.find()) {
    	    String mainURL = matcher.group(2).replace("en.", "en.m.");
    	    Log.d(TAG, mainURL);
    	    wikiPages.add(mainURL);
    	  }
    	
    	  return wikiPages;

    }

    /*
     * method to create json object from the atom/xml feed
     * this is filling the cached list data needed to instantiate the collection of views
     */
    private static ArrayList<PictureEntry> getPicWikiPages( ) {
    	
		HttpURLConnection urlConnection = null;
		ArrayList<PictureEntry> picList = new ArrayList<PictureEntry>();
		try {
			String requestUrl = "http://evecal.appspot.com/feedParser?feedLink=http://toolserver.org/~skagedal/feeds/potd.xml&response=json";
			Log.d(TAG, requestUrl);
			URL url = new URL(requestUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			String jsonStr = RestJsonClient.convertStreamToString(urlConnection.getInputStream());
			//Log.d("RestJsonClient", jsonStr);

			// getting data and if we don't we just get out of here!
			JSONArray picoftheday = null;
			try {
				JSONObject json = new JSONObject(jsonStr);
				picoftheday = json.getJSONArray("items");
				//Log.d(TAG, "json array size is " + picoftheday.length());
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
					//Log.d(TAG, "index in json array is " + i);
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
    
    static String imageFetch(InputStream input) {
    	/*
    	 * This method is a work in progress
    	 * Kenneth is working to parse the RSS feed more efficiently
    	 * input is the result of a URLConnection
    	 */
    	//Log.d(TAG, "piclist image fetch");
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
    
    //This structure is a temporary measure used to demonstrate the proposed functionality
    private static int[] images = {
    	R.drawable.photo1bird, R.drawable.photo1bird, R.drawable.photo2chichen_itza, R.drawable.photo3periclimenes, R.drawable.photo4da_vinci,
    	R.drawable.photo5psalm_23, R.drawable.photo6molybdenum, R.drawable.photo7edwardteller1958, R.drawable.photo8eumecesfasciatus,
    	R.drawable.photo9caterpillar
    };

    private static int[] widgetID = {
    	R.layout.pic_widget_item, R.layout.pic_widget_item2
    };
    
    public RemoteViews getViewAt(int position) {
        // position goes from 0 to getCount() - 1.

        /* Create a remote views object from the pic widget item xml file
         	alternate the background colors of the widget
    		set the text of the title and summary based on the position in the list */
        final RemoteViews rv = new RemoteViews(mContext.getPackageName(), widgetID[position%2]);
        rv.setTextViewText(R.id.widget_item, mWidgetItems.get(position).text);
        rv.setTextViewText(R.id.widget_summary, mWidgetItems.get(position).summary);
        //now pull the bitmap down from the web and resize it for display
        //Temporary code TODO
        /*************/
        if(position < 10) {
        	rv.setImageViewResource(R.id.widget_pic, images[position]);
        } else {
        	rv.setImageViewResource(R.id.widget_pic, R.drawable.icon);
        }
        /*************/
        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putString(WikiWidgetProvider.URL_TAG, mWidgetItems.get(position).url);
        
        //Log.d(TAG, "set url as extra " + mWidgetItems.get(position).url);
        Intent fillInIntent = new Intent(); //new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        // The extra information specific to this list item must be set with a bundle!!
        // The substitution did not seem to work when the extra was set directly on the intent
        //fillInIntent.putExtra(WikiWidgetProvider.URL_TAG, mWidgetItems.get(position).url);;

        // Return the remote views object for display inside the widget
        return rv;
    }
    
	public static Bitmap download(String jpgUrl) {
		byte[] buffer = null;
		int downloadedSize = 0;
		Bitmap photo2Display = null;
	    //thanks to Android snippets
	    try {
	    	//set the download URL, a url that points to a file on the internet
	    	//this is the file to be downloaded
	    	URL url = new URL(jpgUrl);
	
	    	//create the new connection
	    	HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	
	    	//set up some things on the connection
	    	urlConnection.setRequestMethod("GET");
	    	urlConnection.setDoOutput(true);
	
	    	//and connect!
	    	urlConnection.connect();
	
	    	//this will be used in reading the data from the internet
	    	InputStream inputStream = urlConnection.getInputStream();
	    	//create a buffer...
	    	buffer = new byte[1024];
	    	int bufferLength = 0; //used to store a temporary size of the buffer
	
	    	//now, read through the input buffer and write the contents to the file
	    	while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
	    		//add up the size so we know how much is downloaded
	    		downloadedSize += bufferLength;
	    	}
	    	if(buffer != null) {
	    		if(downloadedSize < MAX_SIZE) {
	    			photo2Display = BitmapFactory.decodeStream(inputStream);
	    		} else {
			    	//options.inJustDecodeBounds = true;
	    		    BitmapFactory.Options options = new BitmapFactory.Options();
	    		    // scale to nearest power of 2 - faster
	    		    options.inSampleSize = (int)Math.pow(2, (int)
	    		    		(Math.log10(Math.sqrt((double)downloadedSize / MAX_SIZE)) / Math.log10(2) + 1)); 
	    		    // 2 ^ (log2(lengthRatio)+1)
	    		    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
	    		    photo2Display = BitmapFactory.decodeStream(inputStream, null, options);

	    			}
		    	} //end buffer processing, bitmap is ready
	
	    //catch some possible errors...
	    } catch (MalformedURLException e) {
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	    
	    return photo2Display;
	    // see http://androidsnippets.com/download-an-http-file-to-sdcard-with-progress-notification
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
