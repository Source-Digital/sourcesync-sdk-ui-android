package io.sourcesync.sdk.ui.demo_mobile;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Simple utility for loading GIF images from assets
 */
public class SimpleGifLoader {
    private static final String TAG = "SimpleGifLoader";

    /**
     * Load a GIF from assets directory and return it as a Drawable
     *
     * @param context The context to access assets
     * @param gifName The name of the GIF file in assets (with or without extension)
     * @return
     */
    public static GifDrawable loadGifFromAssets(Context context, String gifName) {
        // If name doesn't include extension, add it
        String fileName = gifName.endsWith(".gif") ? gifName : gifName + ".gif";

        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;

        try {
            // Open the GIF file from assets
            inputStream = assetManager.open(fileName);

            // Create a GifDrawable from the input stream
          return new GifDrawable(inputStream);

        } catch (IOException e) {
            Log.e(TAG, "GIF not found or couldn't be loaded: " + fileName, e);
        } finally {
            // Close the input stream
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing input stream", e);
                }
            }
        }
        return null;
    }
}
