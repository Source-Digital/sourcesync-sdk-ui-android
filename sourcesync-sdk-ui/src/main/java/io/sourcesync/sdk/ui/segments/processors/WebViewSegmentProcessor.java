package io.sourcesync.sdk.ui.segments.processors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import io.sourcesync.sdk.ui.segments.SegmentProcessor;
import io.sourcesync.sdk.ui.utils.LayoutUtils;
import io.sourcesync.sdk.ui.utils.SegmentAttributes;

public class WebViewSegmentProcessor implements SegmentProcessor {
    private static final String TAG = "WebViewSegmentProcessor";

    public WebViewSegmentProcessor() {
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View processSegment(Context context, JSONObject segment) throws JSONException {
        // Extract URL from the segment content
        String url = segment.getString("content");

        // Create a container that will help with sizing
        final LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        // Create a WebView
        final WebView webView = new WebView(context);

        // Configure WebView settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(TAG, "Page started loading: " + url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "Page finished loading: " + url);
                super.onPageFinished(view, url);
            }
        });

        // Get the device screen dimensions for percentage calculations
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        final int screenWidth = displayMetrics.widthPixels;
        final int screenHeight = displayMetrics.heightPixels;

        // Set default container layout params
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        container.setLayoutParams(containerParams);

        // Default WebView parameters
        LinearLayout.LayoutParams webViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LayoutUtils.dpToPx(context, 300) // Default height: 300dp
        );

        // Apply attributes if available
        if (segment.has("attributes")) {
            JSONObject attributesJson = segment.getJSONObject("attributes");
            final SegmentAttributes attributes = SegmentAttributes.fromJson(attributesJson);

            // Process width attribute
            if (attributes.width != null) {
                if (LayoutUtils.isValidPercentage(attributes.width)) {
                    // Calculate exact width based on screen percentage
                    float widthPercentage = LayoutUtils.percentageToDecimal(attributes.width);
                    webViewParams.width = (int) (screenWidth * widthPercentage);
                } else if (attributes.width.equals("auto")) {
                    webViewParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                } else {
                    try {
                        // Try to parse as a pixel value
                        webViewParams.width = LayoutUtils.dpToPx(context, Integer.parseInt(attributes.width));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Invalid width format: " + attributes.width, e);
                        webViewParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    }
                }
            }

            // Process height attribute
            if (attributes.height != null) {
                if (LayoutUtils.isValidPercentage(attributes.height)) {
                    // Calculate exact height based on screen percentage
                    float heightPercentage = LayoutUtils.percentageToDecimal(attributes.height);
                    webViewParams.height = (int) (screenHeight * heightPercentage);
                } else if (attributes.height.equals("auto")) {
                    webViewParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                } else {
                    try {
                        // Try to parse as a pixel value
                        webViewParams.height = LayoutUtils.dpToPx(context, Integer.parseInt(attributes.height));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Invalid height format: " + attributes.height, e);
                        webViewParams.height = LayoutUtils.dpToPx(context, 300); // Default to 300dp
                    }
                }
            }
        }

        // Apply calculated params to WebView
        webView.setLayoutParams(webViewParams);

        // Add WebView to container
        container.addView(webView);

        // Load the URL
        webView.loadUrl(url);

        // Set up a listener to handle parent container size changes (for accurate percentage sizing)
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener to avoid multiple calls
                container.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Get the parent width (if available)
                View parent = (View) container.getParent();
                if (parent != null && segment.has("attributes")) {
                    try {
                        JSONObject attributesJson = segment.getJSONObject("attributes");
                        SegmentAttributes attributes = SegmentAttributes.fromJson(attributesJson);

                        // Recalculate dimensions based on parent size
                        LinearLayout.LayoutParams adjustedParams = new LinearLayout.LayoutParams(
                                webView.getLayoutParams()
                        );

                        int parentWidth = parent.getWidth();
                        int parentHeight = parent.getHeight();

                        // Adjust width if it's a percentage
                        if (LayoutUtils.isValidPercentage(attributes.width)) {
                            float widthPercentage = LayoutUtils.percentageToDecimal(attributes.width);
                            adjustedParams.width = (int) (parentWidth * widthPercentage);
                        }

                        // Adjust height if it's a percentage
                        if (LayoutUtils.isValidPercentage(attributes.height)) {
                            float heightPercentage = LayoutUtils.percentageToDecimal(attributes.height);
                            adjustedParams.height = (int) (parentHeight * heightPercentage);
                        }

                        // Apply the adjusted params
                        webView.setLayoutParams(adjustedParams);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing attributes during resize", e);
                    }
                }
            }
        });

        return container;
    }

    @Override
    public String getSegmentType() {
        return "webview";
    }
}