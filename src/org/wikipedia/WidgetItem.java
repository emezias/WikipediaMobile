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


public class WidgetItem { //extends View implements OnTouchListener {
    public String text;
    public String summary;
    public String url;
    private static final String TAG = "WidgetItem";
    
	/*public WidgetItem(Context ctx, AttributeSet attrs, int defStyle){
		super(ctx, attrs, defStyle);
		init(ctx);		
	}
	
	public WidgetItem(Context ctx, AttributeSet attrs)  {
		super(ctx, attrs);
		init(ctx);		
	}	

	public WidgetItem(Context ctx) {
		super(ctx);
		init(ctx);		
	}	
	
	void init(Context ctx) {
		Log.d(TAG, "init called?");
	}
*/

    public WidgetItem(String text) {
    	//super(ctx);    
        this.text = text;
    }
    
    public WidgetItem(String text, String summary, String url) {
    	//super(ctx);    
        this.text = text;
        this.summary = summary;
        this.url = url;
    }
    
    /*public WidgetItem(Context ctx, String text, String url, String summary) {
    	//super(ctx);    
        this.text = text;
        this.url = url;
        this.summary = summary;
        //this.setOnTouchListener(this);
    }*/
    
    

	/*@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG, "widget item touch");
		Context context = v.getContext();
        final Intent tnt = new Intent(context.getApplicationContext(), WikiWidgetActivity.class);
        tnt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //tnt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        TextView tv = (TextView) v.findViewById(R.id.widget_url);
        String location = null;
        if(tv != null) {
        	location = (String) tv.getText();//intent.getStringExtra(URL_TAG);
        }
        if(location == null) {
        	location = "file:///android_asset/www/index.html";
        }
        Toast.makeText(context, "Loading Wikipedia: " + location, Toast.LENGTH_SHORT).show();
        tnt.putExtra(WikiWidgetProvider.URL_TAG, location);
        Log.d(TAG, "read url tag, value is " + location);
        //intent.putExtra(URL_TAG, location);
        
        context.startActivity(tnt); 
		return false;
	}*/
    
    
    
}
