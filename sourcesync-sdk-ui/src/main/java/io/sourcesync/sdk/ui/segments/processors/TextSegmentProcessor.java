package io.sourcesync.sdk.ui.segments.processors;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.sourcesync.sdk.ui.segments.SegmentProcessor;
import io.sourcesync.sdk.ui.utils.LayoutUtils;
import io.sourcesync.sdk.ui.utils.SegmentAttributes;

public class TextSegmentProcessor implements SegmentProcessor {
    private static final String TAG = "TextSegmentProcessor";

    public TextSegmentProcessor() {
    }

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
            applyTextAttributes(attributedString, attributes, content.length());

            // Apply advanced text styling
            applyAdvancedTextAttributes(textView, attributes);

            // Apply alignment if specified
            if (attributes.alignment != null) {
                textView.setGravity(LayoutUtils.getGravityFromAlignment(attributes.alignment));
            } else {
                textView.setGravity(android.view.Gravity.CENTER);
            }

            // Configure layout parameters
            LinearLayout.LayoutParams params = createLayoutParams(attributes);

            // Apply margin and padding
            applyMarginAndPadding(textView, params, attributes);

            textView.setLayoutParams(params);

            // Apply background styling
            applyBackgroundStyling(context,textView, attributes);
        }

        textView.setText(attributedString);
        return textView;
    }

    @Override
    public String getSegmentType() {
        return "text";
    }

    // Helper function to apply text attributes
    private void applyTextAttributes(SpannableStringBuilder attributedString,
                                     SegmentAttributes attributes,
                                     int end) {
        if (attributes == null) return;

        // Apply font size
        if (attributes.fontSize != null) {
            int dpSize = LayoutUtils.fontSizeToDP(attributes.fontSize);
            attributedString.setSpan(
                    new AbsoluteSizeSpan(dpSize, true),
                    0, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
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
        if ("italic".equals(attributes.fontStyle)) {
            attributedString.setSpan(
                    new StyleSpan(Typeface.ITALIC),
                    0, end,
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        // Apply underline
        if ("underline".equals(attributes.textDecoration)) {
            attributedString.setSpan(
                    new UnderlineSpan(),
                    0, end,
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        // Apply strikethrough
        if ("line-through".equals(attributes.textDecoration)) {
            attributedString.setSpan(
                    new StrikethroughSpan(),
                    0, end,
                    SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

    }

    // Apply advanced text attributes
    private void applyAdvancedTextAttributes(TextView textView, SegmentAttributes attributes) {
        if (attributes == null) return;

        // Apply text transform
        if (attributes.textTransform != null) {
            switch (attributes.textTransform.toLowerCase()) {
                case "uppercase":
                    textView.setAllCaps(true);
                    break;
                case "lowercase":
                    // Need to set text itself as lowercase
                    textView.setText(textView.getText().toString().toLowerCase());
                    break;
                case "capitalize":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        textView.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_NONE);
                    }
                    // Manually capitalize first letter of each word
                    break;
            }
        }

        // Apply line height
        if (attributes.lineHeight != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                textView.setLineHeight((int) (textView.getTextSize() * attributes.lineHeight));
            } else {
                textView.setLineSpacing(0, attributes.lineHeight);
            }
        }

        // Apply max lines if specified
        if (attributes.maxLines != null) {
            textView.setMaxLines(attributes.maxLines);

            // Apply ellipsis if overflow is set
            if ("ellipsis".equals(attributes.overflow)) {
                textView.setEllipsize(android.text.TextUtils.TruncateAt.END);
            }
        }

        // Apply text shadow
        if (attributes.textShadow != null) {
            try {
                int shadowColor = Color.parseColor(attributes.textShadow.color);
                float blurRadius = attributes.textShadow.blurRadius;
                float dx = (float) attributes.textShadow.offset.x;
                float dy = (float) attributes.textShadow.offset.y;

                textView.setShadowLayer(blurRadius, dx, dy, shadowColor);
            } catch (Exception e) {
                Log.e(TAG, "Error setting text shadow", e);
            }
        }

        // Apply opacity
        if (attributes.opacity != null) {
            textView.setAlpha(attributes.opacity);
        }

        if (attributes.letterSpacing != null) {
            textView.setLetterSpacing(attributes.letterSpacing);
        }
    }

    // Apply background styling
    private void applyBackgroundStyling(Context context, TextView textView, SegmentAttributes attributes) {
        if (attributes == null) return;

        GradientDrawable background = new GradientDrawable();
        boolean hasCustomBackground = false;

        // Apply background color
        if (attributes.backgroundColor != null) {
            try {
                background.setColor(Color.parseColor(attributes.backgroundColor));
                hasCustomBackground = true;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid background color format: " + attributes.backgroundColor, e);
            }
        }

        // Apply border radius
        if (attributes.borderRadius != null) {
            background.setCornerRadius(LayoutUtils.dpToPx(textView.getContext(), attributes.borderRadius));
            hasCustomBackground = true;
        }

        // Apply border width and color
        if (attributes.borderWidth != null && attributes.borderColor != null) {
            try {
                int borderColor = Color.parseColor(attributes.borderColor);
                int borderWidth = LayoutUtils.dpToPx(textView.getContext(), attributes.borderWidth);
                background.setStroke(borderWidth, borderColor);
                hasCustomBackground = true;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid border color format: " + attributes.borderColor, e);
            }
        }

        // Apply background gradient
        if (attributes.backgroundGradient != null) {
            try {
                String gradientType = attributes.backgroundGradient.type;
                JSONArray colorsArray = attributes.backgroundGradient.colors;
                int[] colors = new int[colorsArray.length()];

                for (int i = 0; i < colorsArray.length(); i++) {
                    colors[i] = Color.parseColor(colorsArray.getString(i));
                }

                if ("linear".equals(gradientType)) {
                    background.setColors(colors);
                    background.setGradientType(GradientDrawable.LINEAR_GRADIENT);

                    // Set gradient orientation
                    float startX = (float) attributes.backgroundGradient.startPoint.x;
                    float startY = (float) attributes.backgroundGradient.startPoint.y;
                    float endX = (float) attributes.backgroundGradient.endPoint.x;
                    float endY = (float) attributes.backgroundGradient.endPoint.y;

                    GradientDrawable.Orientation orientation = determineGradientOrientation(startX, startY, endX, endY);
                    background.setOrientation(orientation);

                    hasCustomBackground = true;
                } else if ("radial".equals(gradientType)) {
                    background.setColors(colors);
                    background.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                    hasCustomBackground = true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting gradient background", e);
            }
        }

        // Apply shadow - needs to be done with elevation on newer Android versions
        if (attributes.shadow != null) {
            textView.setElevation(LayoutUtils.dpToPx(textView.getContext(), attributes.shadow.radius));

            // For colored shadows, we would need a custom solution
            // This is a simplified approach using native elevation
        }
        // Apply background image
        if (attributes.backgroundImage != null && attributes.backgroundImage.url != null) {
            // Use the reusable image loader from LayoutUtils
            LayoutUtils.loadBackgroundImage(
                    context,
                    textView,
                    attributes.backgroundImage.url,
                    attributes.backgroundImage.contentMode,
                    attributes.backgroundImage.opacity,
                    hasCustomBackground ? background : null,
                    attributes.borderRadius
            );
        } else if (hasCustomBackground) {
            // If no background image but has other background styling
            textView.setBackground(background);
        }
    }

    // Helper method to map start/end points to gradient orientation
    private GradientDrawable.Orientation determineGradientOrientation(float startX, float startY, float endX, float endY) {
        // Calculate the angle of the gradient
        double angle = Math.toDegrees(Math.atan2(endY - startY, endX - startX));

        // Normalize angle to 0-360
        if (angle < 0) {
            angle += 360;
        }

        // Map angle to orientation
        if (angle >= 337.5 || angle < 22.5) {
            return GradientDrawable.Orientation.LEFT_RIGHT;
        } else if (angle >= 22.5 && angle < 67.5) {
            return GradientDrawable.Orientation.TL_BR;
        } else if (angle >= 67.5 && angle < 112.5) {
            return GradientDrawable.Orientation.TOP_BOTTOM;
        } else if (angle >= 112.5 && angle < 157.5) {
            return GradientDrawable.Orientation.TR_BL;
        } else if (angle >= 157.5 && angle < 202.5) {
            return GradientDrawable.Orientation.RIGHT_LEFT;
        } else if (angle >= 202.5 && angle < 247.5) {
            return GradientDrawable.Orientation.BR_TL;
        } else if (angle >= 247.5 && angle < 292.5) {
            return GradientDrawable.Orientation.BOTTOM_TOP;
        } else {
            return GradientDrawable.Orientation.BL_TR;
        }
    }

    // Apply margin and padding
    private void applyMarginAndPadding(TextView textView, LinearLayout.LayoutParams params, SegmentAttributes attributes) {
        if (attributes == null) return;

        // Apply padding
        if (attributes.padding != null) {
            int paddingLeft = 15;
            int paddingTop = 25;
            int paddingRight = 15;
            int paddingBottom = 25;

            if (attributes.padding instanceof String) {
                // Simple padding
                int paddingValue = LayoutUtils.fontSizeToDP((String) attributes.padding);
                paddingLeft = paddingRight = paddingTop = paddingBottom = paddingValue;
            } else if (attributes.padding instanceof JSONObject paddingObj) {
                // Directional padding
                try {
                    paddingLeft = paddingObj.has("left") ? LayoutUtils.fontSizeToDP(paddingObj.getString("left")) : 15;
                    paddingTop = paddingObj.has("top") ? LayoutUtils.fontSizeToDP(paddingObj.getString("top")) : 25;
                    paddingRight = paddingObj.has("right") ? LayoutUtils.fontSizeToDP(paddingObj.getString("right")) : 15;
                    paddingBottom = paddingObj.has("bottom") ? LayoutUtils.fontSizeToDP(paddingObj.getString("bottom")) : 25;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }

            textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        } else {
            textView.setPadding(15, 25, 15, 25);
        }

        // Apply margin
        if (attributes.margin != null) {
            int marginLeft = 0;
            int marginTop = 0;
            int marginRight = 0;
            int marginBottom = 0;

            if (attributes.margin instanceof String) {
                // Simple margin
                int marginValue = LayoutUtils.fontSizeToDP((String) attributes.margin);
                marginLeft = marginRight = marginTop = marginBottom = marginValue;
            } else if (attributes.margin instanceof JSONObject marginObj) {
                // Directional margin
                try {
                    marginLeft = marginObj.has("left") ? LayoutUtils.fontSizeToDP(marginObj.getString("left")) : 0;
                    marginTop = marginObj.has("top") ? LayoutUtils.fontSizeToDP(marginObj.getString("top")) : 0;
                    marginRight = marginObj.has("right") ? LayoutUtils.fontSizeToDP(marginObj.getString("right")) : 0;
                    marginBottom = marginObj.has("bottom") ? LayoutUtils.fontSizeToDP(marginObj.getString("bottom")) : 0;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            params.setMargins(marginLeft, marginTop, marginRight, marginBottom);
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
}