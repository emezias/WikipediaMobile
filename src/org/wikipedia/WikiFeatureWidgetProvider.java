package org.wikipedia;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WikiFeatureWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "WikiFeatureWidgetProvider";
/*  
 * This class will display feature of the day articles from Wikipedia
 * It also acts as a broadcast receiver on a touch of the list that will open the selected Wikipedia page
 */
    public static final String CLICK = "org.wikipedia.CLICK";
    public static final String URL_TAG = "org.wikipedia.EXTRA_URL";

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    	//nothing to clean up after the widget is removed from the homescreen
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
    	//precedes delete?
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //This code will execute when an item in the widget is touched
    	//Log.d(TAG, "WikiFeatureWidgetProvider receive");
        if (((String)intent.getAction()).equals(CLICK)) {
        	//final Intent tnt = new Intent(context.getApplicationContext(), WikiWidgetActivity.class);
        	//final Intent tnt = new Intent(context.getApplicationContext(), WikipediaActivity.class);
        	//the phone gap activity hangs up when it is called with an explicit intent and URL data set

            String location = intent.getStringExtra(URL_TAG);
            Log.d(TAG, "location " + location);

            final Intent tnt = new Intent("android.intent.action.VIEW", Uri.parse(location));
            Toast.makeText(context, "Loading Wikipedia", Toast.LENGTH_SHORT).show();
            //Log.d(TAG, "read url tag, value is " + location);
            tnt.putExtra(URL_TAG, location);
            tnt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            context.startActivity(tnt);
        	
        }
        super.onReceive(context, intent);
    }
    

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update each of the widgets with the remote adapter
    	// the update interval is once per day
    	FeatureRemoteViewsFactory.updateWidgetItems();
        for (int i = 0; i < appWidgetIds.length; ++i) {
        	Log.d(TAG, "wiki provider update");
            // This intent points to the Service class that creates view objects for this widget
            final Intent intent = new Intent(context, WikiFeatureWidgetSvc.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            //not sure about this call to set data, just following sample code
            
            //Creating the remote views container object that will hold the views of the collection
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_stack);
            rv.setRemoteAdapter(appWidgetIds[i], R.id.stack_view, intent);
            rv.setEmptyView(R.id.stack_view, R.id.empty_view);

            
            final Intent toastIntent = new Intent(context, WikiFeatureWidgetProvider.class);
            // Set the action for each view of the stack widget to invoke
            // getActivity pending intents did not seem to be working on Honeycomb
            // toastIntent.setAction(Intent.ACTION_VIEW);
            // from the Svc, fillInIntent.setData(Uri.parse(mWidgetItems.get(position).url)); 
            
            // following the code example to use an embedded broadcast receiver
            // code to execute when a list item is touched is defined above in onReceive
            toastIntent.setAction(CLICK);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            //again, not sure about this call to set data, just following sample code
            //setData allows the Service to replace the extras on the intent template
            toastIntent.setData(Uri.parse(toastIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent);
            //set as many of the intent flags here as possible
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
