// ActivationSDK.java
package io.sourcesync.sdk.ui;

import android.content.Context;
import android.view.ViewGroup;
import org.json.JSONObject;

public class ActivationSDK {
    public static ActivationView preview(Context context, JSONObject activation, ViewGroup container) {
        ActivationView view = new ActivationView(context);
        view.setContent(activation, false);
        container.addView(view);
        return view;
    }

    public static ActivationView detail(Context context, JSONObject activation, ViewGroup container) {
        ActivationView view = new ActivationView(context);
        view.setContent(activation, true);
        container.addView(view);
        return view;
    }
}
