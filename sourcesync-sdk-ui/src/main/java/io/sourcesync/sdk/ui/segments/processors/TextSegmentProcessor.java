package io.sourcesync.sdk.ui.segments.processors;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log;

import io.sourcesync.sdk.ui.utils.LayoutUtils;
import io.sourcesync.sdk.ui.utils.SegmentAttributes;
import io.sourcesync.sdk.ui.segments.SegmentProcessor;

public class TextSegmentProcessor implements SegmentProcessor {
    private static final String TAG = "TextSegmentProcessor";

    public TextSegmentProcessor() {}

    @Override
    public View processSegment(Context context, JSONObject segment) throws JSONException {
        // Extract content from the segment
        String content = segment.getString("content");

        // Create a TextView
        TextView textView = new TextView(context);
        SpannableStringBuilder attributedString = new SpannableStringBuilder(content);

        // Apply attributes if available
        if (segment.has("attributes")) {
            JSONObject attributesJson = segment.getJSONObject("attributes");
            SegmentAttributes attributes = SegmentAttributes.fromJson(attributesJson);

            // Apply text styling attributes
            applyTextAttributes(context, attributedString, attributes,
                    content.length());

            // Apply alignment if specified
            if (attributes.alignment != null) {
                textView.setGravity(LayoutUtils.getGravityFromAlignment(attributes.alignment));
            } else {
                textView.setGravity(android.view.Gravity.CENTER);
            }

            // Configure layout parameters
            LinearLayout.LayoutParams params = createLayoutParams(attributes);
            textView.setPadding(15,25,15,25);
            textView.setLayoutParams(params);
        }

        textView.setText(attributedString);
        return textView;
    }

    @Override
    public String getSegmentType() {
        return "text";
    }

    // Helper function to apply text attributes
    private void applyTextAttributes(Context context,
                                     SpannableStringBuilder attributedString,
                                     SegmentAttributes attributes,
                                     int end) {
        if (attributes == null) return;

        // Apply font size
        if (attributes.fontSize != null) {
            int dpSize = LayoutUtils.fontSizeToDP(attributes.fontSize);
            attributedString.setSpan(
                    new AbsoluteSizeSpan(dpSize, true),
                    0, end,
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        // Apply text color
        if (attributes.color != null) {
            try {
                int color = Color.parseColor(attributes.color);
                attributedString.setSpan(
                        new ForegroundColorSpan(color),
                        0, end,
                        SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid color format: " + attributes.color, e);
            }
        }

        // Apply text style (bold)
        if ("bold".equals(attributes.weight)) {
            attributedString.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    0, end,
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        // Apply text style (italic)
        if ("italic".equals(attributes.style)) {
            attributedString.setSpan(
                    new StyleSpan(Typeface.ITALIC),
                    0, end,
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        // Apply underline
        if (Boolean.TRUE.equals(attributes.underline)) {
            attributedString.setSpan(
                    new UnderlineSpan(),
                    0, end,
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
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
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
        }

        return params;
    }

    // Helper method to map alignment string to Android Gravity
    private int alignmentToGravity(String alignment) {
        if (alignment == null) return android.view.Gravity.CENTER;

        return switch (alignment.toLowerCase()) {
            case "left" -> android.view.Gravity.START;
            case "right" -> android.view.Gravity.END;
            default -> android.view.Gravity.CENTER;
        };
    }
}