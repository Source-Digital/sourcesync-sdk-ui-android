package io.sourcesync.sdk.ui.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.sourcesync.sdk.ui.segments.SegmentProcessor;
import io.sourcesync.sdk.ui.segments.SegmentProcessorFactory;
import io.sourcesync.sdk.ui.utils.LayoutUtils;

public class ActivationDetail extends FrameLayout {
    private static final String TAG = "ActivationDetail";
    private SegmentProcessorFactory processorFactory;
    private LinearLayout contentContainer;

    // Constructor for code instantiation
    public ActivationDetail(Context context, JSONArray template, Runnable onClose) {
        super(context);
        initializeView(template, onClose);
    }

    // Constructor for XML inflation
    public ActivationDetail(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Initialize with empty template and null callback
        // These should be set later with setter methods
        initializeView(null, null);
    }

    // Constructor with default style
    public ActivationDetail(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(null, null);
    }

    // Setters for template and close listener
    public void setTemplate(JSONArray template) {
        if (template != null) {
            processTemplate(template);
        }
    }

    public void setOnCloseListener(Runnable onClose) {
        // If we already have header views, update their listeners
    }

    private void initializeView(JSONArray template, Runnable onClose) {
        Log.d(TAG, "Initializing ActivationDetail view");

        try {
            // Create main container
            LinearLayout mainContainer = new LinearLayout(getContext());
            mainContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            mainContainer.setBackgroundColor(Color.argb(153, 0, 0, 0)); // 0.6 alpha (153/255)

            // Create ScrollView to wrap contentContainer
            ScrollView scrollView = new ScrollView(getContext());
            scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            ));

            // Create content container inside the ScrollView
            contentContainer = new LinearLayout(getContext());
            contentContainer.setOrientation(LinearLayout.VERTICAL);
            contentContainer.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            // Set spacing between items in content container
            int spacing = LayoutUtils.dpToPx(getContext(), 25);
            contentContainer.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            contentContainer.setDividerPadding(spacing);
            contentContainer.setPadding(20,10,30,40);
            // Add contentContainer to ScrollView
            scrollView.addView(contentContainer);

            // Create header
            ActivationHeader header = new ActivationHeader(getContext(), onClose);
            // Initialize processor factory
            processorFactory = new SegmentProcessorFactory(contentContainer);

            addView(mainContainer, mainParams);

            // Add header to mainContainer with LinearLayout.LayoutParams
            LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                    LayoutUtils.dpToPx(getContext(), 35),
                    LayoutUtils.dpToPx(getContext(), 35)
            );

            // Set 20dp margin on all sides
            int margin = LayoutUtils.dpToPx(getContext(), 15);
            headerParams.setMargins(margin, margin, margin, margin);
            headerParams.gravity = android.view.Gravity.TOP | android.view.Gravity.START;

            mainContainer.addView(header, headerParams);

            // Add the ScrollView to mainContainer
            LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            int contentMargin = LayoutUtils.dpToPx(getContext(), 20);
            scrollParams.setMargins(contentMargin,
                    0, // Top margin includes header height + its margin
                    contentMargin,
                    contentMargin);

            mainContainer.addView(scrollView, scrollParams);

            // Process template if available
            if (template != null) {
                processTemplate(template);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in initializeView", e);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Consume all touch events to prevent them from propagating
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Don't intercept touch events to allow scrolling and clicking of child views
        return false;
    }

    private void processTemplate(JSONArray template) {
        if (template == null) {
            return;
        }

        try {
            for (int i = 0; i < template.length(); i++) {
                JSONObject segment = template.getJSONObject(i);
                String segmentType = segment.getString("type");

                Log.d(TAG, "Processing segment type: " + segmentType);
                SegmentProcessor processor = processorFactory.getProcessor(segmentType);

                if (processor != null) {
                    try {
                        View segmentView = processor.processSegment(getContext(), segment);

                        if (segmentView != null) {
                            // Check if segmentView has the correct layout params
                            ViewGroup.LayoutParams params = segmentView.getLayoutParams();

                            if (params != null) {
                                Log.d(TAG, "Segment view has params of type: " + params.getClass().getName());

                                // If params are not instance of LinearLayout.LayoutParams, create new ones
                                if (!(params instanceof LinearLayout.LayoutParams)) {
                                    Log.w(TAG, "Converting incorrect params to LinearLayout.LayoutParams");

                                    // Create proper LinearLayout.LayoutParams based on width/height of existing params
                                    LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(
                                            params.width,
                                            params.height
                                    );

                                    // Apply the new params before adding to content container
                                    segmentView.setLayoutParams(newParams);
                                }
                            } else {
                                Log.d(TAG, "Segment view has no layout params, default will be applied");
                            }

                            try {
                                contentContainer.addView(segmentView);
                                Log.d(TAG, "Successfully added segment view to contentContainer");
                            } catch (ClassCastException e) {
                                Log.e(TAG, "ClassCastException when adding view to contentContainer", e);

                                // Emergency fix - force the correct layout params
                                LinearLayout.LayoutParams fixParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                segmentView.setLayoutParams(fixParams);
                                contentContainer.addView(segmentView);
                                Log.d(TAG, "Applied emergency fix and added view");
                            }
                        } else {
                            Log.w(TAG, "Processor returned null view for segment type: " + segmentType);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing segment of type: " + segmentType, e);

                    }
                } else {
                    Log.w(TAG, "No processor found for segment type: " + segmentType);
                }
            }

            // Force layout update
            contentContainer.requestLayout();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}