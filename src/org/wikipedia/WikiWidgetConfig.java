package org.wikipedia;

import android.app.Activity;
import android.os.Bundle;

public class WikiWidgetConfig extends Activity {

	private static final String TAG = "WikiWidgetConfig";
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
	}
}
