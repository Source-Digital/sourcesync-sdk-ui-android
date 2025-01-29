// ActivationView.java
package io.sourcesync.sdk.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

public class ActivationView extends FrameLayout {
    private TextView titleView;

    public ActivationView(Context context) {
        super(context);
        init();
    }

    public ActivationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        titleView = new TextView(getContext());
        addView(titleView);
    }

    public void setContent(JSONObject activation, boolean isDetailMode) {
        try {
            String title = activation.getString("title");
            if (isDetailMode) {
                title += " (detail mode)";
            }
            titleView.setText(title);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}