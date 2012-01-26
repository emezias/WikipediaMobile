package org.wikipedia;

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
    private static final String POTD_STREAM = "http://toolserver.org/~skagedal/feeds/potd.xml";
    private static final int MAX_SIZE = 55000;
    private static HttpURLConnection mConnection;
    private static URL mLocation;
    
	public static ArrayList<PictureEntry> parsePhotos() {
		ArrayList<PictureEntry> photos = null;
		XmlPullParser parser = Xml.newPullParser();
		try {
	    	URL url = new URL(POTD_STREAM);
	    	
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
						if (name.equalsIgnoreCase("item")){
							currentPic = new PictureEntry();
						} else if (currentPic != null){
							if (name.equalsIgnoreCase("link")){
								currentPic.setWikipediaUrl(parser.nextText());
							} else if (name.equalsIgnoreCase("description")){
								currentPic.setSummary(parser.nextText());
							} else if (name.equalsIgnoreCase("title")){
								currentPic.setTitle(parser.nextText());
							}	
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
	    	final int downloadSize = mConnection.getContentLength();
	    	Log.d(TAG, "content length = " + downloadSize);
	    	photo2Display = BitmapFactory.decodeStream(inputStream);
	    	//Log.d(TAG, "width = " + photo2Display.getWidth());
	    	/*//create a buffer...
	    	//if(inputStream != null) {
	    		if(downloadSize < MAX_SIZE) {
	    			photo2Display = BitmapFactory.decodeStream(inputStream);
	    			if(photo2Display != null) {
	    				Log.d(TAG, "no compression, width = " + photo2Display.getWidth());
	    			}
	    		} else {
			    	//options.inJustDecodeBounds = true;
	    		    BitmapFactory.Options options = new BitmapFactory.Options();
	    		    // scale to nearest power of 2 - faster
	    		    options.inSampleSize = (int)Math.pow(2, (int)
	    		    		(Math.log10(Math.sqrt((double)downloadSize/MAX_SIZE)) / Math.log10(2) + 1)); 
	    		    // 2 ^ (log2(lengthRatio)+1)
	    		    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
	    		    photo2Display = BitmapFactory.decodeStream(inputStream, null, options);
	    		    Log.d(TAG, "compressed?  width = " + photo2Display.getWidth());
	    			}
		    	//} //end buffer processing, bitmap is ready
*/		mConnection.disconnect();
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
