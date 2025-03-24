package io.sourcesync.sdk.ui.core;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.sourcesync.sdk.ui.segments.SegmentProcessorFactory;
import io.sourcesync.sdk.ui.utils.LayoutUtils;
import io.sourcesync.sdk.ui.segments.SegmentProcessor;

public class ActivationPreview extends FrameLayout {
    private static final String TAG = "ActivationPreview";
    private LinearLayout contentContainer;
    private SegmentProcessorFactory processorFactory;

    // Constructor for programmatic creation
    public ActivationPreview(Context context, JSONObject previewData) {
        super(context);
        initializeView(previewData);
    }

    // Constructor for XML inflation
    public ActivationPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(new JSONObject());
    }

    // Constructor with style attribute
    public ActivationPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(new JSONObject());
    }

    private void initializeView(JSONObject previewData) {
        // Create content container
        contentContainer = new LinearLayout(getContext());
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));

        // Apply background color and opacity
        contentContainer.setBackgroundColor(Color.argb(153, 0, 0, 0)); // 0.6 * 255 = 153

        // Initialize processor factory
        processorFactory = new SegmentProcessorFactory(contentContainer);

        // Add content container to frame layout
        addView(contentContainer);

        // Set padding values
        int paddingTop = LayoutUtils.dpToPx(getContext(), 10);
        int paddingBottom = LayoutUtils.dpToPx(getContext(), 10);
        int paddingLeft = LayoutUtils.dpToPx(getContext(), 16);
        int paddingRight = LayoutUtils.dpToPx(getContext(), 16);

        // Apply padding to content container
        contentContainer.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

        // Set layout parameters with constraints
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentContainer.getLayoutParams();
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        contentContainer.setLayoutParams(params);

        // Set minimum width and height
        contentContainer.setMinimumWidth((int)(getWidth() * 0.9));
        contentContainer.setMinimumHeight((int)(getHeight() * 0.8));

        // Make the preview clickable
        setClickable(true);
        setFocusable(true);

        // Process template if provided
        try {
            if (previewData.has("template")) {
                JSONArray template = previewData.getJSONArray("template");
                processTemplate(template);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error processing template", e);
        }
    }

    private void processTemplate(JSONArray template) {
        try {
            for (int i = 0; i < template.length(); i++) {
                JSONObject segment = template.getJSONObject(i);
                String segmentType = segment.getString("type");

                SegmentProcessor processor = processorFactory.getProcessor(segmentType);
                if (processor != null) {
                    View segmentView = processor.processSegment(getContext(), segment);
                    if (segmentView != null) {
                        contentContainer.addView(segmentView);
                    }
                } else {
                    Log.w(TAG, "No processor found for segment type: " + segmentType);
                }
            }

            // Force layout update
            contentContainer.requestLayout();
        } catch (JSONException e) {
            Log.e(TAG, "Error processing template segments", e);
        }
    }
}