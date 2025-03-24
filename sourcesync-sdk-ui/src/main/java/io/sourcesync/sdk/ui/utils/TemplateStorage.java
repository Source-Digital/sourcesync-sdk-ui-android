package io.sourcesync.sdk.ui.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class TemplateStorage {
    private static final String TAG = "TemplateStorage";

    public static JSONObject getDefaultPreviewTemplate() {
        try {
            JSONObject template = new JSONObject();
            JSONArray templateArray = new JSONArray();

            // First text element
            JSONObject text1 = new JSONObject();
            text1.put("type", "text");
            text1.put("content", "A very basic preview template");

            JSONObject text1Attributes = new JSONObject();
            text1Attributes.put("font", "system");
            text1Attributes.put("size", "md");
            text1Attributes.put("color", "#EEEEEE");
            text1Attributes.put("weight", "bold");

            text1.put("attributes", text1Attributes);
            templateArray.put(text1);

            // Second text element
            JSONObject text2 = new JSONObject();
            text2.put("type", "text");
            text2.put("content", "Click to see details");

            JSONObject text2Attributes = new JSONObject();
            text2Attributes.put("font", "system");
            text2Attributes.put("size", "md");
            text2Attributes.put("color", "#FFFFFF");
            text2Attributes.put("alignment", "center");
            text2Attributes.put("style", "italic");

            text2.put("attributes", text2Attributes);
            templateArray.put(text2);

            // Add template array to main object
            template.put("template", templateArray);

            return template;
        } catch (JSONException e) {
            Log.e(TAG, "Error creating default preview template", e);
            return new JSONObject();
        }
    }

    public static JSONObject getDefaultDetailTemplate() {
        try {
            JSONObject template = new JSONObject();
            JSONArray templateArray = new JSONArray();

            // Title text element
            JSONObject titleText = new JSONObject();
            titleText.put("type", "text");
            titleText.put("content", "Full Content View");

            JSONObject titleTextAttributes = new JSONObject();
            titleTextAttributes.put("font", "system");
            titleTextAttributes.put("size", "xl");
            titleTextAttributes.put("color", "#FFFFFF");
            titleTextAttributes.put("weight", "bold");
            titleTextAttributes.put("alignment", "center");

            titleText.put("attributes", titleTextAttributes);
            templateArray.put(titleText);

            // Image element
            JSONObject image = new JSONObject();
            image.put("type", "image");
            image.put("content", "https://storage.googleapis.com/source-uploads-production/uploads/user-media/bbb_image1_aee8ad527d.png");

            JSONObject imageAttributes = new JSONObject();
            JSONObject imageSize = new JSONObject();
            imageSize.put("width", "20%");
            imageSize.put("height", "20%");

            imageAttributes.put("size", imageSize);
            imageAttributes.put("contentMode", "scaletofill");
            imageAttributes.put("alignment", "center");

            image.put("attributes", imageAttributes);
            templateArray.put(image);

            // Content text element
            JSONObject contentText = new JSONObject();
            contentText.put("type", "text");
            contentText.put("content", "This is the main content that appears when the preview is clicked.");

            JSONObject contentTextAttributes = new JSONObject();
            contentTextAttributes.put("font", "system");
            contentTextAttributes.put("size", "lg");
            contentTextAttributes.put("color", "#FFFFFF");
            contentTextAttributes.put("alignment", "center");

            contentText.put("attributes", contentTextAttributes);
            templateArray.put(contentText);

            // Add template array to main object
            template.put("template", templateArray);

            return template;
        } catch (JSONException e) {
            Log.e(TAG, "Error creating default detail template", e);
            return new JSONObject();
        }
    }
}