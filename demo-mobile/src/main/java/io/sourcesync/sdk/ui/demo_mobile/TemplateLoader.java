package io.sourcesync.sdk.ui.demo_mobile;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Helper class to load templates from JSON files in the assets directory
 */
public class TemplateLoader {
    private static final String TAG = "TemplateLoader";

    /**
     * Load a template from a JSON file in the assets directory
     * @param context Application context
     * @param fileName Name of the file without extension
     * @return A JSONObject representation of the template, or null if loading fails
     */
    public static JSONObject loadTemplate(Context context, String fileName) {
        // Add .json extension if not already present
        if (!fileName.endsWith(".json")) {
            fileName = fileName + ".json";
        }

        try {
            // Open the file from assets directory
            InputStream inputStream = context.getAssets().open(fileName);

            // Read the file content
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            reader.close();
            inputStream.close();

            // Parse the JSON content
            return new JSONObject(stringBuilder.toString());

        } catch (IOException e) {
            Log.e(TAG, "⚠️ Could not find or read template file: " + fileName, e);
            return null;
        } catch (JSONException e) {
            Log.e(TAG, "⚠️ Could not parse template file as JSONObject: " + fileName, e);
            return null;
        }
    }
}
