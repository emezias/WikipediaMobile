package org.wikipedia;

/*
 * props to Eric Burke
 * http://www.ibm.com/developerworks/opensource/library/x-android/
 */
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Xml;

public class WikiFeedParser {
	private static final String TAG = "WikiFeedParser";
    static final String POTD_STREAM = "http://toolserver.org/~skagedal/feeds/potd.xml";
    static final String FEATURED_FEED = "http://toolserver.org/~skagedal/feeds/fa.xml";
    
 // names of the XML tags
    static final  String DESCRIPTION = "description";
    static final  String LINK = "link";
    static final  String TITLE = "title";
    static final  String ITEM = "item";
    private static HttpURLConnection mConnection;
    private static URL mLocation;
    
	public static ArrayList<PictureEntry> parsePhotos(String feedURL) {
		ArrayList<PictureEntry> photos = null;
		XmlPullParser parser = Xml.newPullParser();
		try {
	    	URL url = new URL(feedURL);
	    	
	    	//create the new connection
	    	HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	
	    	//set up some things on the connection
	    	urlConnection.setRequestMethod("GET");
	    	urlConnection.setDoOutput(true);
	
	    	//and connect!
	    	urlConnection.connect();
	    	Log.d(TAG, "content length is " + urlConnection.getContentLength());
	    	//this will be used in reading the data from the internet
	    	InputStream iStream = urlConnection.getInputStream();
	    	//InputStreamReader inputStream = new InputStreamReader(iStream);

			// auto-detect the encoding from the stream
			parser.setInput(iStream, null);
			int eventType = parser.getEventType();
			PictureEntry currentPic = null;
			boolean done = false;
			while (eventType != XmlPullParser.END_DOCUMENT && !done){
				String name = null;
				switch (eventType){
					case XmlPullParser.START_DOCUMENT:
						photos = new ArrayList<PictureEntry>();
						break;
					case XmlPullParser.START_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase(ITEM)){
							currentPic = new PictureEntry();
						} else if (currentPic != null){
							if (name.equalsIgnoreCase(LINK)){
								currentPic.setWikipediaUrl(parser.nextText());
							} else if (name.equalsIgnoreCase(DESCRIPTION)){
								currentPic.setSummary(parser.nextText());
							} else if (name.equalsIgnoreCase(TITLE)){
								currentPic.setTitle(parser.nextText());
							} else parser.nextText();
						}
						break;
					case XmlPullParser.END_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase("item") && currentPic != null){
							photos.add(currentPic);
						} else if (name.equalsIgnoreCase("channel")){
							done = true;
						}
						break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			Log.e("WikieedParser failed", e.getMessage(), e);
			//throw new RuntimeException(e);
		}
		return photos;
	}
	
	public static Bitmap downloadPics(String jpgUrl) {
		if(jpgUrl == null) {
			//if the feed does not contain any src then return null
			Log.i(TAG, "no image in this description");
			return null;
			
		}
		Bitmap photo2Display = null;
	    //thanks to Android snippets
	    try {
	    	//set the download URL, a url that points to a file on the internet
	    	//this is the file to be downloaded
	    	mLocation = new URL(jpgUrl);
	
	    	//create the new connection
	    	mConnection = (HttpURLConnection) mLocation.openConnection();
	    	
	    	//set up some things on the connection
	    	mConnection.setRequestMethod("GET");
	    	mConnection.setDoOutput(true);
	
	    	//and connect!
	    	mConnection.connect();
	
	    	//this will be used in reading the data from the internet
	    	BufferedInputStream inputStream = new BufferedInputStream(mConnection.getInputStream());
	    	BitmapFactory.Options options = new BitmapFactory.Options();
	    	if(mConnection.getContentLength() > 50000) {
	    		//an absolute guess at the max
	    		options.inSampleSize = 2;
	    	}
	    	
		    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		    photo2Display = BitmapFactory.decodeStream(inputStream, null, options);
		    mConnection.disconnect();
		    mLocation = null;
	    //catch some possible errors...
	    } catch (MalformedURLException e) {
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	    
	    return photo2Display;
	    // see http://androidsnippets.com/download-an-http-file-to-sdcard-with-progress-notification
	}
}
