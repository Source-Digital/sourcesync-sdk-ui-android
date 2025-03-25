package io.sourcesync.sdk.ui.segments.processors;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log;

import io.sourcesync.sdk.ui.utils.LayoutUtils;
import io.sourcesync.sdk.ui.utils.SegmentAttributes;
import io.sourcesync.sdk.ui.segments.SegmentProcessor;

public class ButtonSegmentProcessor implements SegmentProcessor {
    private static final String TAG = "ButtonSegmentProcessor";

    public ButtonSegmentProcessor() {}

    @Override
    public View processSegment(Context context, JSONObject segment) throws JSONException {
        // Extract content from the segment
        String content = segment.getString("content");

        // Create a button
        Button button = new Button(context);
        button.setText(content);

        // Apply attributes if available
        if (segment.has("attributes")) {
            JSONObject attributesJson = segment.getJSONObject("attributes");
            SegmentAttributes attributes = SegmentAttributes.fromJson(attributesJson);

            // Apply background color if specified
            if (attributes.backgroundColor != null) {
                try {
                    button.setBackgroundColor(Color.parseColor(attributes.backgroundColor));
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid background color format: " + attributes.backgroundColor, e);
                }
            }

            // Apply text color if specified
            if (attributes.textColor != null) {
                try {
                    button.setTextColor(Color.parseColor(attributes.textColor));
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid text color format: " + attributes.textColor, e);
                }
            }

            // Apply font size if specified
            if (attributes.fontSize != null) {
                int dpSize = LayoutUtils.fontSizeToDP(attributes.fontSize);
                button.setTextSize(dpSize);
            }

            // Configure layout parameters
            LinearLayout.LayoutParams params = createLayoutParams(attributes);

            // Apply alignment if specified
            if (attributes.alignment != null) {
                params.gravity = LayoutUtils.getGravityFromAlignment(attributes.alignment);
            } else {
                params.gravity = android.view.Gravity.CENTER;
            }

            button.setPadding(15,15,15,15);
            button.setLayoutParams(params);
        }

        return button;
    }

    @Override
    public String getSegmentType() {
        return "button";
    }

    // Helper method to create layout parameters based on attributes
    private LinearLayout.LayoutParams createLayoutParams(SegmentAttributes attributes) {
        LinearLayout.LayoutParams params;

        // Handle width if specified as percentage
        if (LayoutUtils.isValidPercentage(attributes.width)) {
            float weight = LayoutUtils.percentageToDecimal(attributes.width);
            params = new LinearLayout.LayoutParams(
                    0, // Width will be determined by weight
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    weight
            );
        } else {
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
        }

        return params;
    }
}