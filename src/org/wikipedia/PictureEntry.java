package org.wikipedia;

import android.graphics.Bitmap;
import android.text.Html;
import android.util.Log;

public class PictureEntry {
	private static final String TAG = "PictureEntry";
	String wikipediaUrl;
	String title;
	String summary;
	Bitmap photo;
	private StringBuilder sb = new StringBuilder();

	public PictureEntry() {
		super();
	}
	
	public PictureEntry(String wikipediaUrl, String title, String summary) {
		super();
		this.wikipediaUrl = wikipediaUrl;
		this.title = title;
		this.summary = summary;
	}
	
	public PictureEntry(String wikipediaUrl) {
		super();
		this.wikipediaUrl = wikipediaUrl;
	}

	public String getWikipediaUrl() {
		return wikipediaUrl;
	}

	private int index;
	public void setWikipediaUrl(String wikipediaUrl) {
		sb.append(wikipediaUrl);
		index = sb.indexOf("http");
		if(index != 0) {
			sb.delete(0, index);
		}
		sb.insert(sb.indexOf("."), ".m");
		Log.d(TAG, "wikipedia url is " + sb.toString());
		this.wikipediaUrl = sb.toString();
		sb.setLength(0);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		sb.append(title);
		sb.delete(0, sb.indexOf(": ") +1);
		this.title = sb.toString();
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		//Log.d(TAG, summary);
		sb.append((Html.fromHtml(summary)).toString());
		while(!Character.isLetter(sb.charAt(0))) {
			sb.deleteCharAt(0);
		}
		this.summary = sb.toString();
		//Log.d(TAG, "summary:" + this.summary);
		sb.setLength(0);
		setPhoto(summary);
		sb.setLength(0);
	}
	
	public Bitmap getPhoto() {
		return photo;
	}
	
	public void setPhoto(String sumString) {
		Log.d(TAG, "setPhoto");
		this.photo = WikiFeedParser.downloadPics(getPhotoURL(sumString));
	}
	
	public String getPhotoURL(String subjectString){
		//passing in description string from the atom feed
		//pulling out the bitmap to download for the pic of the day
		//Log.d(TAG, "getPhotoURL");
		sb.append(subjectString);
		int indya=0, indyb=0;
		indya = sb.indexOf("src=") + 5;
		indyb = sb.indexOf("width=", indya) -2;
		//Log.d(TAG, "indices are " + indya + " : " + indyb);
		sb = new StringBuilder(sb.substring(indya, indyb));
		sb.insert(0, "http:");
		/*sb.delete(5, 23);
		sb.insert(9, ".m");*/
		//src="//upload.wikimedia.org/wikipedia/commons/thumb/5/51/Chichen_Itza_3.jpg/300px-Chichen_Itza_3.jpg" width="300"
		//http://en.wikipedia.org//en.wikipedia.org/wiki/File:Cologne_-_Panoramic_Image_of_the_old_town_at_dusk.jpg
		//Log.d(TAG, "photo url is " + sb.toString());
/*		this.summary = sb.toString();
		//Log.d(TAG, "summary:" + this.summary);
		sb.setLength(0);
	    Pattern regex = Pattern.compile("(<href='http://en.wikipedia.org//(.*?)(\" ))", Pattern.DOTALL);
		Matcher matcher = regex.matcher(subjectString);
		if (matcher.find()) {
		    String mainURL = matcher.group(2).replace("en.", "en.m.");
		    System.out.println(mainURL);
			Log.d(TAG, "photo url is " + mainURL);
		    return mainURL;
		} else {
			Log.e(TAG, "photo url is empty");
			return "";
		}*/
		return sb.toString();
	}


	
	public void setPhoto(Bitmap pic) {
		this.photo = pic;
	}

}
