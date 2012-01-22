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

public class WikiWidgetProvider extends AppWidgetProvider {
    /*public static final String TOAST_ACTION = "com.example.android.stackwidget.TOAST_ACTION";
    public static final String EXTRA_ITEM = "com.example.android.stackwidget.EXTRA_ITEM";*/
	public static final String CLICK = "org.wikipedia.CLICK";
    private static final String TAG = "WikiProvider";
    public static final String URL_TAG = "org.wikipedia.EXTRA_URL";
    public static final String SUMMARY = "org.wikipedia.SUMMARY";
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(TAG, "todo, add the gps data fetch here");
        //TODO, add the gps data fetch here
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //AppWidgetManager mgr = AppWidgetManager.getInstance(context);
    	final String action = intent.getAction();
    	Log.d(TAG, "wiki provider receive " + action);
        if (action.equals(CLICK)) {
        	final Intent tnt = new Intent(context.getApplicationContext(), WikiWidgetActivity.class);
        	//final Intent tnt = new Intent(context.getApplicationContext(), WikipediaActivity.class);
        	//tnt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            tnt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            String location = intent.getStringExtra(URL_TAG);
            if(location == null) {
            	location = "";
            } else {
            	location = makeMobile(location);
            }
            
            Toast.makeText(context, "Loading Wikipedia: " + location, Toast.LENGTH_SHORT).show();
            //tnt.putExtra(URL_TAG, location);
            //Uri page = Uri.parse(location);
            //Log.d(TAG, "read url tag, value is " + location);
            tnt.putExtra(URL_TAG, location);
            //tnt.setData(page);
            context.startActivity(tnt);
        	
        }
        super.onReceive(context, intent);
    }
    
    public static String makeMobile(String location) {
    	final StringBuilder tmp = new StringBuilder(location);
    	tmp.insert(tmp.indexOf("en")+3, "m.");
    	//tmp.insert(0, "http:");
    	return tmp.toString();
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {
        	Log.d(TAG, "wiki provider update");
            // Here we setup the intent which points to the StackViewService which will
            // provide the views for this collection.
            Intent intent = new Intent(context, WikiWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_list);
            rv.setRemoteAdapter(appWidgetIds[i], R.id.list_view, intent);
            rv.setEmptyView(R.id.list_view, R.id.empty_view);

            final Intent toastIntent = new Intent(context, WikiWidgetProvider.class);
            // Set the action for the intent.
            // When the user touches a particular view, it will have the effect of
            // broadcasting TOAST_ACTION.
            toastIntent.setAction(CLICK);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            toastIntent.setData(Uri.parse(toastIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.list_view, toastPendingIntent);
            
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
            // Here we setup the a pending intent template. Individual items of a collection
            // cannot setup their own pending intents, instead, the collection as a whole can
            // setup a pending intent template, and the individual items can set a fillInIntent
            // to create unique before on an item to item basis.
            //Intent toastIntent = new Intent(context, WikiWidgetProvider.class);
            //
            /*Intent toastIntent = new Intent(context, WikiWidgetProvider.class);
            //Intent toastIntent = new Intent(context, WikiWidgetActivity.class);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            toastIntent.setAction(WIDGET_ACTION);
            toastIntent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.list_view, toastPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);*/
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
