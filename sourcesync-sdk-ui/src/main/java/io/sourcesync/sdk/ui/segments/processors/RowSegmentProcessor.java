package io.sourcesync.sdk.ui.segments.processors;

import static io.sourcesync.sdk.ui.utils.LayoutUtils.getGravityFromAlignment;
import static io.sourcesync.sdk.ui.utils.LayoutUtils.getSpacingValue;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.view.View;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.view.ViewGroup;
import android.util.Log;

import io.sourcesync.sdk.ui.utils.LayoutUtils;
import io.sourcesync.sdk.ui.utils.SegmentAttributes;
import io.sourcesync.sdk.ui.segments.SegmentProcessor;
import io.sourcesync.sdk.ui.segments.SegmentProcessorFactory;

public class RowSegmentProcessor implements SegmentProcessor {
    private static final String TAG = "RowSegmentProcessor";
    private final SegmentProcessorFactory processorFactory;

    public RowSegmentProcessor(SegmentProcessorFactory processorFactory, ViewGroup parentContainer) {
        this.processorFactory = processorFactory;
    }

    @Override
    public View processSegment(Context context, JSONObject segment) throws JSONException {
        // Create a main container view
        FrameLayout containerView = new FrameLayout(context);

        // Create a horizontal LinearLayout
        LinearLayout stackView = new LinearLayout(context);
        stackView.setOrientation(LinearLayout.HORIZONTAL);

        // Parse attributes if present
        SegmentAttributes attributes = null;
        if (segment.has("attributes")) {
            JSONObject attributesJson = segment.getJSONObject("attributes");
            attributes = SegmentAttributes.fromJson(attributesJson);

            // Set alignment if specified
            if (attributes.alignment != null) {
                stackView.setGravity(getGravityFromAlignment(attributes.alignment));
            } else {
                stackView.setGravity(android.view.Gravity.CENTER_VERTICAL);
            }

            // Get spacing value
            int spacing = 8; // Default spacing (8dp)
            if (attributes.spacing != null) {
                spacing = getSpacingValue(context, attributes.spacing);
            }

            // Apply spacing to linear layout
            stackView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            stackView.setDividerPadding(spacing);
        } else {
            // Default alignment and spacing
            stackView.setGravity(android.view.Gravity.CENTER_VERTICAL);
            stackView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            stackView.setDividerPadding(8);
        }

        // Add stack view to container
        containerView.addView(stackView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));

        // Process children
        JSONArray children = segment.optJSONArray("children");
        if (children != null) {
            // Calculate percentage distributions
            float explicitPercentageTotal = 0f;
            int childrenWithExplicitPercentage = 0;

            // First pass: count children with explicit percentages
            for (int i = 0; i < children.length(); i++) {
                try {
                    JSONObject childSegment = children.getJSONObject(i);
                    if (childSegment.has("attributes")) {
                        JSONObject childAttributesJson = childSegment.getJSONObject("attributes");
                        SegmentAttributes childAttrs = SegmentAttributes.fromJson(childAttributesJson);

                        if (LayoutUtils.isValidPercentage(childAttrs.width)) {
                            float percentage = LayoutUtils.percentageToDecimal(childAttrs.width);
                            if (percentage > 0) {
                                explicitPercentageTotal += percentage;
                                childrenWithExplicitPercentage++;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error calculating percentages", e);
                }
            }

            int totalChildren = children.length();
            int childrenWithoutPercentage = totalChildren - childrenWithExplicitPercentage;

            // Adjust percentages if needed
            float adjustmentFactor = 1.0f;
            if (explicitPercentageTotal > 0.9f && childrenWithoutPercentage > 0) {
                // Need to leave room for children without percentages
                adjustmentFactor = 0.9f / explicitPercentageTotal;
            } else if (explicitPercentageTotal > 0.98f) {
                // Tiny adjustment to prevent rounding/calculation issues
                adjustmentFactor = 0.98f / explicitPercentageTotal;
            }

            // Second pass: process each child
            for (int i = 0; i < children.length(); i++) {
                JSONObject childSegment = children.getJSONObject(i);
                String childType = childSegment.optString("type");

                if (!childType.isEmpty()) {
                    SegmentProcessor processor = processorFactory.getProcessor(childType);
                    if (processor != null) {
                        try {
                            // Process the child segment
                            View childView = processor.processSegment(context, childSegment);

                            if (childView != null) {
                                // Create a wrapper view to handle width constraints
                                FrameLayout wrapperView = new FrameLayout(context);

                                // Add child to wrapper
                                wrapperView.addView(childView, new FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                ));

                                // Determine child's layout parameters based on attributes
                                LinearLayout.LayoutParams wrapperParams;

                                // Check if child has explicit percentage width
                                boolean hasExplicitPercentage = false;
                                float childPercentage = 0f;

                                if (childSegment.has("attributes")) {
                                    JSONObject childAttributesJson = childSegment.getJSONObject("attributes");
                                    SegmentAttributes childAttrs = SegmentAttributes.fromJson(childAttributesJson);

                                    if (LayoutUtils.isValidPercentage(childAttrs.width)) {
                                        childPercentage = LayoutUtils.percentageToDecimal(childAttrs.width);
                                        if (childPercentage > 0) {
                                            hasExplicitPercentage = true;
                                            // Apply adjusted percentage
                                            float safePercentage = childPercentage * adjustmentFactor;

                                            // Verify value is valid
                                            if (safePercentage > 0 && safePercentage <= 1.0f) {
                                                wrapperParams = new LinearLayout.LayoutParams(
                                                        0, // Width determined by weight
                                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                                        safePercentage
                                                );
                                            } else {
                                                // Fallback for invalid percentages
                                                wrapperParams = new LinearLayout.LayoutParams(
                                                        100, // Fallback width
                                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                                );
                                            }
                                        } else {
                                            wrapperParams = new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                            );
                                        }
                                    } else {
                                        wrapperParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        );
                                    }
                                } else {
                                    wrapperParams = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                    );
                                }

                                // For views without explicit percentages when some views have percentages
                                if (!hasExplicitPercentage && childrenWithoutPercentage > 0 && childrenWithExplicitPercentage > 0) {
                                    // Calculate remaining space and divide equally
                                    float remainingPercentage = Math.max(0.01f, (1.0f - (explicitPercentageTotal * adjustmentFactor)));
                                    float equalShare = remainingPercentage / childrenWithoutPercentage;

                                    // Verify value is valid
                                    if (equalShare > 0) {
                                        wrapperParams = new LinearLayout.LayoutParams(
                                                0, // Width determined by weight
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                equalShare
                                        );
                                    }
                                }

                                // Add wrapper to stack view
                                stackView.addView(wrapperView, wrapperParams);
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

        return containerView;
    }

    @Override
    public String getSegmentType() {
        return "row";
    }
}