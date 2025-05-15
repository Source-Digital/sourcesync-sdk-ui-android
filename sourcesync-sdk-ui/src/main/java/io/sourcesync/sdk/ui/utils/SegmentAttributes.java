package io.sourcesync.sdk.ui.utils;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.util.Log;

/**
 * Class to represent segment attributes parsed from JSON.
 */
public class SegmentAttributes {
    private static final String TAG = "SegmentAttributes";

    // Basic attributes
    public String font;
    public String fontSize;
    public String color;
    public String weight;
    public String alignment;
    public String width;
    public String height;
    public String style;
    public Boolean underline;

    // Advanced text attributes
    public String textDecoration;    // "none", "underline", "line-through"
    public String fontStyle;         // "normal", "italic"
    public Float letterSpacing;      // Letter spacing multiplier
    public Float lineHeight;         // Line height multiplier
    public String textTransform;     // "none", "uppercase", "lowercase", "capitalize"
    public Integer maxLines;         // Maximum number of lines
    public String overflow;          // "clip", "ellipsis"
    public Float opacity;            // 0.0 to 1.0

    // Background and border attributes
    public String backgroundColor;
    public Integer borderRadius;     // In DP
    public Integer borderWidth;      // In DP
    public String borderColor;
    public BackgroundGradient backgroundGradient;
    public BackgroundImage backgroundImage;
    public String textColor;

    // Shadow attributes
    public Shadow shadow;
    public TextShadow textShadow;

    // Margin and padding
    public Object margin;            // Can be String or JSONObject
    public Object padding;           // Can be String or JSONObject
    public String spacing;

    /**
     * Parse attributes from JSON.
     */
    public static SegmentAttributes fromJson(JSONObject json) {
        SegmentAttributes attributes = new SegmentAttributes();

        try {
            // Parse basic attributes
            if (json.has("font")) attributes.font = json.getString("font");
            if (json.has("size")) attributes.fontSize = json.getString("size");
            if (json.has("color")) attributes.color = json.getString("color");
            if (json.has("weight")) attributes.weight = json.getString("weight");
            if (json.has("alignment")) attributes.alignment = json.getString("alignment");
            if (json.has("width")) attributes.width = json.getString("width");
            if (json.has("height")) attributes.height = json.getString("height");
            if (json.has("underline")) attributes.underline = json.getBoolean("underline");
            if (json.has("textColor")) attributes.textColor = json.getString("textColor");

            // Parse advanced text attributes
            if (json.has("textDecoration")) attributes.textDecoration = json.getString("textDecoration");
            if (json.has("fontStyle")) attributes.fontStyle = json.getString("fontStyle");
            if (json.has("letterSpacing")) attributes.letterSpacing = (float)json.getDouble("letterSpacing");
            if (json.has("lineHeight")) attributes.lineHeight = (float)json.getDouble("lineHeight");
            if (json.has("textTransform")) attributes.textTransform = json.getString("textTransform");
            if (json.has("maxLines")) attributes.maxLines = json.getInt("maxLines");
            if (json.has("overflow")) attributes.overflow = json.getString("overflow");
            if (json.has("opacity")) attributes.opacity = (float)json.getDouble("opacity");

            // Parse background and border attributes
            if (json.has("backgroundColor")) attributes.backgroundColor = json.getString("backgroundColor");
            if (json.has("borderRadius")) attributes.borderRadius = json.getInt("borderRadius");
            if (json.has("borderWidth")) attributes.borderWidth = json.getInt("borderWidth");
            if (json.has("borderColor")) attributes.borderColor = json.getString("borderColor");

            // Parse background image
            if (json.has("backgroundImage")) {
                JSONObject imageJson = json.getJSONObject("backgroundImage");
                attributes.backgroundImage = new BackgroundImage();

                if (imageJson.has("url")) attributes.backgroundImage.url = imageJson.getString("url");
                if (imageJson.has("contentMode")) attributes.backgroundImage.contentMode = imageJson.getString("contentMode");
                if (imageJson.has("opacity")) attributes.backgroundImage.opacity = (float)imageJson.getDouble("opacity");
            }

            // Parse background gradient
            if (json.has("backgroundGradient")) {
                JSONObject gradientJson = json.getJSONObject("backgroundGradient");
                attributes.backgroundGradient = new BackgroundGradient();
                attributes.backgroundGradient.type = gradientJson.getString("type");

                attributes.backgroundGradient.colors = gradientJson.getJSONArray("colors");

                if (gradientJson.has("startPoint")) {
                    JSONObject startPoint = gradientJson.getJSONObject("startPoint");
                    attributes.backgroundGradient.startPoint = new Point();
                    attributes.backgroundGradient.startPoint.x = startPoint.getDouble("x");
                    attributes.backgroundGradient.startPoint.y = startPoint.getDouble("y");
                }

                if (gradientJson.has("endPoint")) {
                    JSONObject endPoint = gradientJson.getJSONObject("endPoint");
                    attributes.backgroundGradient.endPoint = new Point();
                    attributes.backgroundGradient.endPoint.x = endPoint.getDouble("x");
                    attributes.backgroundGradient.endPoint.y = endPoint.getDouble("y");
                }

                if (gradientJson.has("animated")) {
                    attributes.backgroundGradient.animated = gradientJson.getBoolean("animated");
                }

                if (gradientJson.has("animationDuration")) {
                    attributes.backgroundGradient.animationDuration = gradientJson.getInt("animationDuration");
                }
            }
            // Handle spacing token
            if (json.has("spacing")) {
                attributes.spacing = json.get("spacing").toString().toLowerCase();
            }
            // Parse shadow
            if (json.has("shadow")) {
                JSONObject shadowJson = json.getJSONObject("shadow");
                attributes.shadow = new Shadow();

                if (shadowJson.has("color")) attributes.shadow.color = shadowJson.getString("color");
                if (shadowJson.has("opacity")) attributes.shadow.opacity = (float)shadowJson.getDouble("opacity");
                if (shadowJson.has("radius")) attributes.shadow.radius = shadowJson.getInt("radius");

                if (shadowJson.has("offset")) {
                    JSONObject offsetJson = shadowJson.getJSONObject("offset");
                    attributes.shadow.offset = new Point();
                    attributes.shadow.offset.x = offsetJson.getDouble("x");
                    attributes.shadow.offset.y = offsetJson.getDouble("y");
                }
            }

            // Parse text shadow
            if (json.has("textShadow")) {
                JSONObject shadowJson = json.getJSONObject("textShadow");
                attributes.textShadow = new TextShadow();

                if (shadowJson.has("color")) attributes.textShadow.color = shadowJson.getString("color");
                if (shadowJson.has("blurRadius")) attributes.textShadow.blurRadius = (float)shadowJson.getDouble("blurRadius");

                if (shadowJson.has("offset")) {
                    JSONObject offsetJson = shadowJson.getJSONObject("offset");
                    attributes.textShadow.offset = new Point();
                    attributes.textShadow.offset.x = (float)offsetJson.getDouble("x");
                    attributes.textShadow.offset.y = (float)offsetJson.getDouble("y");
                }
            }

            // Parse margin
            if (json.has("margin")) {
                Object marginObj = json.get("margin");
                if (marginObj instanceof String || marginObj instanceof JSONObject) {
                    attributes.margin = marginObj;
                }
            }

            // Parse padding
            if (json.has("padding")) {
                Object paddingObj = json.get("padding");
                if (paddingObj instanceof String || paddingObj instanceof JSONObject) {
                    attributes.padding = paddingObj;
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing attributes", e);
        }

        return attributes;
    }

    // Inner classes for structured attributes

    public static class Point {
        public double x;
        public double y;
    }

    public static class Shadow {
        public String color;
        public Float opacity;
        public Integer radius;
        public Point offset;
    }

    public static class TextShadow {
        public String color;
        public Float blurRadius;
        public Point offset;
    }

    public static class BackgroundGradient {
        public String type;          // "linear", "radial"
        public JSONArray colors;
        public Point startPoint;
        public Point endPoint;
        public Boolean animated;
        public Integer animationDuration;
    }

    public static class BackgroundImage {
        public String url;
        public String contentMode;    // "scaleToFill", "scaleAspectFit", "scaleAspectFill"
        public Float opacity;         // 0.0 to 1.0
    }
}