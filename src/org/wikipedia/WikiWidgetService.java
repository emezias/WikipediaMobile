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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class WikiWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WikiRemoteViewsFactory(this.getApplicationContext(), intent);
    }

}

class WikiRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static List<WidgetItem> mWidgetItems = new CopyOnWriteArrayList<WidgetItem>();
    private Context mContext;
    private static final String TAG = "WikiRemoteViewsFactory";

    public WikiRemoteViewsFactory(Context context, Intent intent) {
        mContext = context.getApplicationContext();
        
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
    	
    	if(((WikipediaApp)mContext).geonames == null) {
    		final double[] gps = getGPS(mContext);
        	//need to populate the data structure that will be used by the widget
    		((WikipediaApp)mContext).geonames = RestJsonClient.getWikipediaNearbyLocations(gps[0], gps[1], ((WikipediaApp)mContext).language);
    	}
    	//TODO, tighten this up and use a single data structure for the app and the widget
    	for(GeoName gn: ((WikipediaApp)mContext).geonames) {
			mWidgetItems.add(new WidgetItem(gn.getTitle(), gn.getSummary(), gn.getWikipediaUrl()));
    		//mWidgetItems.add(new WidgetItem(gn.getTitle()));
		}
    	//Log.d(TAG, "Wiki Svc Factory onCreate");        
    }
    
    public static void updateWidgetItems(Context ctx) {
    	final double[] gps = getGPS(ctx);
        	//need to populate the data structure that will be used by the widget
    	final ArrayList<GeoName> gnames = RestJsonClient.getWikipediaNearbyLocations(gps[0], 
    				gps[1], WikipediaApp.language);
    	//TODO, tighten this up and use a single data structure for the app and the widget
    	for(GeoName gn: gnames) {
			mWidgetItems.add(new WidgetItem(gn.getTitle(), gn.getSummary(), gn.getWikipediaUrl()));
    		//mWidgetItems.add(new WidgetItem(gn.getTitle()));
		}
    	gnames.clear();
    }
    //this function is straight from the NearMe Activity, could make it public and static to share across the two classes
    private static double[] getGPS(Context ctx) {
		final LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		final List<String> providers = lm.getProviders(true);
		/*
		 * Loop over the array backwards, and if you get an accurate location,
		 * then break out the loop
		 */
		Location l = null;

		for (int i = providers.size() - 1; i >= 0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null)
				break;
		}
		providers.clear();
		double[] gps = new double[2];
		if (l != null) {
			gps[0] = l.getLatitude();
			gps[1] = l.getLongitude();
		}
		return gps;
	}

    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mWidgetItems.clear();
    }

    public int getCount() {
        return ((WikipediaApp)mContext).geonames.size();
    }

    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        // Construct a remote views item based on our widget item xml file 
        // set the title and summary text based on the position.
    	
    	final int itemId = (position % 2 == 0 ? R.layout.widget_listitem
                : R.layout.widget_listitem2);
        final RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);
        rv.setTextViewText(R.id.widget_item, mWidgetItems.get(position).text);
        rv.setTextViewText(R.id.widget_summary, mWidgetItems.get(position).summary);
        /*rv.setTextViewText(R.id.widget_url, mWidgetItems.get(position).url);
         * TODO Consider an active link in a Text view as part of the layout 
         * This could replace the pending intent or provide the functionality with 2.x releases
         * */
        // Next, we set a fill-intent which to fill-in the pending intent template
        // set on the collection view in WikiWidgetProvider.
        final Bundle extras = new Bundle();
        extras.putString(WikiWidgetProvider.URL_TAG, "http://" + mWidgetItems.get(position).url);
        //Log.d(TAG, "set url as extra " + mWidgetItems.get(position).url);
        final Intent fillInIntent = new Intent(); 
        fillInIntent.putExtras(extras);
        //fillInIntent.putExtra(WikiWidgetProvider.URL_TAG, mWidgetItems.get(position).url);
        //Setting the extra on the intent rather than in a bundle did not seem to work
        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.
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
        // For use with a content provider.
    }
}
