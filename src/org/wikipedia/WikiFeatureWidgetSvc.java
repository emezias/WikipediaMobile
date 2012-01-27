package org.wikipedia;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class WikiFeatureWidgetSvc extends RemoteViewsService {
	/*
	 * This class helps the WidgetProvider class populate the views of the collection
	 * The Provider creates the stack, this service creates the views in that stack 
	 */
	
	@Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FeatureRemoteViewsFactory(this.getApplicationContext(), intent);
    }

}

class FeatureRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "FeatureRemoteViewsFactory";
    /* mWidgetItems will cache the data that will be used to fill the items in the list
    	it is a collection of WidgetItem classes
    	refer to widget_item.xml and the WidgetItem class
    */
    private static List<PictureEntry> mFeatureWidgetItems = new CopyOnWriteArrayList<PictureEntry>();
    private Context myContext;

    public FeatureRemoteViewsFactory(Context context, Intent intent) {
        myContext = context.getApplicationContext();        
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
    	
    	mFeatureWidgetItems = WikiFeedParser.parsePhotos(WikiFeedParser.FEATURED_FEED);
    	Log.d(TAG, "list size is " + mFeatureWidgetItems.size());
    	//Log.d(TAG, "Wiki Svc Factory onCreate");
    	}
    
    public static void updateWidgetItems( ) {
    	//static method called by onUpdate from the widget provider
    	mFeatureWidgetItems = WikiFeedParser.parsePhotos(WikiFeedParser.FEATURED_FEED);

    }
        
    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mFeatureWidgetItems.clear();
    }

    public int getCount() {
        return mFeatureWidgetItems.size();
    }    

    private static int[] layout_ID = {
    	R.layout.feature_widget_item, R.layout.feature_widget_item2
    };
    
    public RemoteViews getViewAt(int position) {
        // position goes from 0 to getCount() - 1.
    	
        /* Create a remote views object from the pic widget item xml file
         	alternate the background colors of the widget
    		set the text of the title and summary based on the position in the list */
        final RemoteViews rv = new RemoteViews(myContext.getPackageName(), layout_ID[position%2]);
        rv.setTextViewText(R.id.widget_item, mFeatureWidgetItems.get(position).title);
        rv.setTextViewText(R.id.widget_summary, mFeatureWidgetItems.get(position).summary);
        //now pull the bitmap down from the web and resize it for display
        //Temporary code TODO
        /*************/
        
        if(mFeatureWidgetItems.get(position).photo != null) {
        	rv.setImageViewBitmap(R.id.widget_pic, mFeatureWidgetItems.get(position).photo);
        } else {
        	rv.setImageViewResource(R.id.widget_pic, R.drawable.icon);
        	Log.d(TAG, "null photo");
        }
        /*************/
        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putString(WikiWidgetProvider.URL_TAG, mFeatureWidgetItems.get(position).wikipediaUrl);
        
        //Log.d(TAG, "set url as extra " + mWidgetItems.get(position).wikipediaUrl);
        Intent fillInIntent = new Intent(); //new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);
        rv.setOnClickFillInIntent(R.id.widget_pic, fillInIntent);

        // The extra information specific to this list item must be set with a bundle!!
        // The substitution did not seem to work when the extra was set directly on the intent
        //fillInIntent.putExtra(WikiWidgetProvider.URL_TAG, mWidgetItems.get(position).url);;

        // Return the remote views object for display inside the widget
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
