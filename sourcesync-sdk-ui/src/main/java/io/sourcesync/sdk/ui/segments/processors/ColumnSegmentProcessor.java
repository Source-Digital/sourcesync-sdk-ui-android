package io.sourcesync.sdk.ui.segments.processors;

import static io.sourcesync.sdk.ui.utils.LayoutUtils.getGravityFromAlignment;
import static io.sourcesync.sdk.ui.utils.LayoutUtils.getSpacingValue;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.sourcesync.sdk.ui.segments.SegmentProcessor;
import io.sourcesync.sdk.ui.segments.SegmentProcessorFactory;
import io.sourcesync.sdk.ui.utils.LayoutUtils;
import io.sourcesync.sdk.ui.utils.SegmentAttributes;

public class ColumnSegmentProcessor implements SegmentProcessor {
    private static final String TAG = "ColumnSegmentProcessor";
    private final SegmentProcessorFactory processorFactory;
    private final ViewGroup parentContainer;

    public ColumnSegmentProcessor(SegmentProcessorFactory processorFactory, ViewGroup parentContainer) {
        this.processorFactory = processorFactory;
        this.parentContainer = parentContainer;
    }

    @Override
    public View processSegment(Context context, JSONObject segment) throws JSONException {
        // Create a vertical LinearLayout to hold the column items
        LinearLayout columnLayout = new LinearLayout(context);
        columnLayout.setOrientation(LinearLayout.VERTICAL);
        // Parse attributes if available
        SegmentAttributes attributes = null;
        if (segment.has("attributes")) {
            JSONObject attributesJson = segment.getJSONObject("attributes");
            attributes = SegmentAttributes.fromJson(attributesJson);

            // Set column alignment
            if (attributes.alignment != null) {
                columnLayout.setGravity(getGravityFromAlignment(attributes.alignment));
            } else {
                columnLayout.setGravity(Gravity.CENTER_VERTICAL);
            }

            // Apply spacing between children
            if (attributes.spacing != null) {
                int spacing = getSpacingValue(context, attributes.spacing);
                columnLayout.setDividerPadding(spacing);
                columnLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            } else {
                // Default spacing
                columnLayout.setDividerPadding(15);
                columnLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            }
        } else {
            // Default alignment and spacing
            columnLayout.setGravity(Gravity.CENTER_VERTICAL);
            columnLayout.setDividerPadding(15);
            columnLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        }

        // Setup layout parameters
        LinearLayout.LayoutParams columnParams = createColumnLayoutParams(context, attributes);
        columnLayout.setLayoutParams(columnParams);

        // Process children elements (if any)
        JSONArray children = segment.optJSONArray("children");
        if (children != null) {
            for (int i = 0; i < children.length(); i++) {
                JSONObject childSegment = children.getJSONObject(i);
                String childType = childSegment.optString("type");

                if (!childType.isEmpty()) {
                    SegmentProcessor processor = processorFactory.getProcessor(childType);
                    if (processor != null) {
                        try {
                            // Process the child segment
                            View childView = processor.processSegment(context, childSegment);

                            // Add the child to the column layout
                            if (childView != null) {
                                columnLayout.addView(childView);

                                // Handle child-specific constraints if needed
                                if ("text".equals(childType)) {
                                    // Text views should stretch horizontally to fill the column
                                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) childView.getLayoutParams();
                                    params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                                    childView.setLayoutParams(params);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing child segment of type: " + childType, e);
                        }
                    } else {
                        Log.w(TAG, "No processor found for child segment type: " + childType);
                    }
                }
            }
        }

        return columnLayout;
    }

    @Override
    public String getSegmentType() {
        return "column";
    }

    // Helper method to create layout parameters based on attributes
    private LinearLayout.LayoutParams createColumnLayoutParams(Context context, SegmentAttributes attributes) {
        LinearLayout.LayoutParams params;

        if (attributes != null && attributes.width != null) {
            if (LayoutUtils.isValidPercentage(attributes.width)) {
                // Calculate weight based on percentage
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

            // Handle height if specified
            if (attributes.height != null) {
                if (LayoutUtils.isValidPercentage(attributes.height)) {
                    int parentHeight = parentContainer.getHeight();
                    if (parentHeight == 0) {
                        parentHeight = parentContainer.getMeasuredHeight();
                    }
                    params.height = LayoutUtils.percentageToPx(context, attributes.height, parentHeight);
                }
            }
        } else {
            // Default to equal weight distribution
            params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );
        }

        return params;
    }
}