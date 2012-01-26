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
    private static List<PictureEntry> mWidgetItems = new CopyOnWriteArrayList<PictureEntry>();
    private Context mContext;

    public PicRemoteViewsFactory(Context context, Intent intent) {
        mContext = context.getApplicationContext();        
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
    	
    	//TODO, tighten this up and use a single data structure for the app and the widget
    	mWidgetItems = WikiFeedParser.parsePhotos();
    	Log.d(TAG, "list size is " + mWidgetItems.size());
    	//Log.d(TAG, "Wiki Svc Factory onCreate");
    	}
    
    public static void updateWidgetItems( ) {
    	mWidgetItems = WikiFeedParser.parsePhotos();

    }
    

    /*
     * this is filling the cached list data needed to instantiate the collection of views
     */
    private static ArrayList<PictureEntry> getPicWikiPages( ) {
    	final ArrayList<PictureEntry> feed = WikiFeedParser.parsePhotos();
    	/*final String input = (Html.fromHtml(feed.toString())).toString();
    	Log.d(TAG, feed.toString());*/
		ArrayList<PictureEntry> picList = new ArrayList<PictureEntry>();
		Pattern regex = Pattern.compile("(<link>(.*?)</link>)", Pattern.DOTALL);
  	    //<link>http://en.wikipedia.org/wiki/Template:POTD/2012-01-26</link>
  	  	Matcher matcher = regex.matcher(feed.toString());
  	  	ArrayList<String> wikiPages = new ArrayList<String>();
	    //run through the string and pull out the page urls
  	  	
		while (matcher.find()) {
		    String mainURL = matcher.group(2).replace("en.", "en.m.");
		    Log.d(TAG, mainURL);
		    picList.add(new PictureEntry(mainURL));
		}
		regex = Pattern.compile("(<href='http://en.wikipedia.org//(.*?)(' ))", Pattern.DOTALL);
		matcher = regex.matcher(feed.toString());
  	  	PictureEntry tmpItem;
  	  	int counter = 0;
		while (matcher.find()) {
			tmpItem = picList.get(counter);
			Log.d(TAG, "photo URL is " + matcher.group());
			tmpItem.setPhoto(downloadPics(matcher.group()));
		}
  	  //<title>January 26: Cologne</title>
		regex = Pattern.compile("(<title>(:*?)</title>)", Pattern.DOTALL);
		while (matcher.find()) {
			tmpItem = picList.get(counter);
			Log.d(TAG, "title is " + matcher.group());
			tmpItem.setTitle(matcher.group());
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
    

    private static int[] widgetID = {
    	R.layout.pic_widget_item, R.layout.pic_widget_item2
    };
    
    public RemoteViews getViewAt(int position) {
        // position goes from 0 to getCount() - 1.

        /* Create a remote views object from the pic widget item xml file
         	alternate the background colors of the widget
    		set the text of the title and summary based on the position in the list */
        final RemoteViews rv = new RemoteViews(mContext.getPackageName(), widgetID[position%2]);
        rv.setTextViewText(R.id.widget_item, mWidgetItems.get(position).title);
        rv.setTextViewText(R.id.widget_summary, mWidgetItems.get(position).summary);
        //now pull the bitmap down from the web and resize it for display
        //Temporary code TODO
        /*************/
        
        if(mWidgetItems.get(position).photo != null) {
        	rv.setImageViewBitmap(R.id.widget_pic, mWidgetItems.get(position).photo);
        } else {
        	rv.setImageViewResource(R.id.widget_pic, R.drawable.icon);
        }
        /*************/
        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putString(WikiWidgetProvider.URL_TAG, mWidgetItems.get(position).wikipediaUrl);
        
        Log.d(TAG, "set url as extra " + mWidgetItems.get(position).wikipediaUrl);
        Intent fillInIntent = new Intent(); //new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        // The extra information specific to this list item must be set with a bundle!!
        // The substitution did not seem to work when the extra was set directly on the intent
        //fillInIntent.putExtra(WikiWidgetProvider.URL_TAG, mWidgetItems.get(position).url);;

        // Return the remote views object for display inside the widget
        return rv;
    }
   
	public static Bitmap downloadPics(String jpgUrl) {
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
	    	final int downloadSize = urlConnection.getContentLength();
	    	//create a buffer...
	    	if(inputStream != null) {
	    		if(downloadSize < MAX_SIZE) {
	    			photo2Display = BitmapFactory.decodeStream(inputStream);
	    		} else {
			    	//options.inJustDecodeBounds = true;
	    		    BitmapFactory.Options options = new BitmapFactory.Options();
	    		    // scale to nearest power of 2 - faster
	    		    options.inSampleSize = (int)Math.pow(2, (int)
	    		    		(Math.log10(Math.sqrt((double)downloadSize/MAX_SIZE)) / Math.log10(2) + 1)); 
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
