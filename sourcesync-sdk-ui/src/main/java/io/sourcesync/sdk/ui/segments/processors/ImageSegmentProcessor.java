package io.sourcesync.sdk.ui.segments.processors;

import static io.sourcesync.sdk.ui.utils.LayoutUtils.alignmentToScaleType;
import static io.sourcesync.sdk.ui.utils.LayoutUtils.contentModeToScaleType;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import io.sourcesync.sdk.ui.segments.SegmentProcessor;
import io.sourcesync.sdk.ui.utils.ImageLoader;
import io.sourcesync.sdk.ui.utils.LayoutUtils;
import io.sourcesync.sdk.ui.utils.SegmentAttributes;

public class ImageSegmentProcessor implements SegmentProcessor {
    private static final String TAG = "ImageSegmentProcessor";
    private static ImageLoader imageLoader;

    public ImageSegmentProcessor(ViewGroup parentContainer) {
        if (imageLoader == null) {
            imageLoader = new ImageLoader();
        }
    }

    @Override
    public View processSegment(Context context, JSONObject segment) throws JSONException {
        // Create a container view that will hold the image view
        FrameLayout containerView = new FrameLayout(context);

        // Create the image view
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Add image view to container
        containerView.addView(imageView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        boolean hasFixedWidth = false;
        boolean hasFixedHeight = false;
        boolean isAutoHeight = false;

        // Apply attributes if available
        if (segment.has("attributes")) {
            JSONObject attributesJson = segment.getJSONObject("attributes");
            SegmentAttributes attributes = SegmentAttributes.fromJson(attributesJson);

            // Handle content mode / scale type
            if (attributes.alignment != null) {
                imageView.setScaleType(alignmentToScaleType(attributes.alignment));
            } else if (attributesJson.has("contentMode")) {
                String contentMode = attributesJson.getString("contentMode");
                imageView.setScaleType(contentModeToScaleType(contentMode));
            }

            // Handle size attributes
            JSONObject sizeObj = attributesJson.optJSONObject("size");
            if (sizeObj != null) {
                // Process width value
                if (sizeObj.has("width")) {
                    hasFixedWidth = processWidth(sizeObj.get("width"), containerView);
                }

                // Process height value
                if (sizeObj.has("height")) {
                    String heightValue = sizeObj.getString("height");
                    if ("auto".equals(heightValue)) {
                        isAutoHeight = true;
                    } else {
                        hasFixedHeight = processHeight(heightValue, containerView);
                    }
                }
            } else {
                // Handle width attribute directly
                if (attributes.width != null) {
                    hasFixedWidth = processWidth(attributes.width, containerView);
                }

                // Handle height attribute directly
                if (attributes.height != null) {
                    if ("auto".equals(attributes.height)) {
                        isAutoHeight = true;
                    } else {
                        hasFixedHeight = processHeight(attributes.height, containerView);
                    }
                }
            }

            // Apply minimum height if needed
            if (isAutoHeight) {
                // Set a minimum height to ensure visibility before image loads
                ViewGroup.LayoutParams params = containerView.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }
                params.height = (int) (50 * context.getResources().getDisplayMetrics().density);
                containerView.setLayoutParams(params);
            }
        }

        // Load the image
        String imageUrl = segment.optString("content");
        if (!imageUrl.isEmpty()) {
            Log.d(TAG, String.format("Starting image load for URL: %s", imageUrl));
            imageLoader.loadImage(imageUrl, imageView, isAutoHeight && hasFixedWidth);
        } else {
            // No image URL, provide a placeholder appearance
            imageView.setBackgroundColor(Color.LTGRAY);
        }

        return containerView;
    }

    // Process width value and return true if it's a fixed width
    private boolean processWidth(Object width, View view) {
        if (width instanceof Integer) {
            int widthValue = (Integer) width;
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.LayoutParams(
                        widthValue,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            } else {
                params.width = widthValue;
            }
            view.setLayoutParams(params);
            return true;
        } else if (width instanceof String widthString) {

            if ("auto".equals(widthString)) {
                return false;
            } else if (LayoutUtils.isValidPercentage(widthString)) {
                float percentage = LayoutUtils.percentageToDecimal(widthString);
                if (percentage > 0) {
                    // Let the parent handle percentage constraints
                    return false;
                }
                return false;
            } else {
                try {
                    int widthValue = Integer.parseInt(widthString);
                    ViewGroup.LayoutParams params = view.getLayoutParams();
                    if (params == null) {
                        params = new ViewGroup.LayoutParams(
                                widthValue,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                    } else {
                        params.width = widthValue;
                    }
                    view.setLayoutParams(params);
                    return true;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing width value: " + widthString, e);
                }
            }
        }
        return false;
    }

    // Process height value and return true if it's a fixed height
    private boolean processHeight(Object height, View view) {
        if (height instanceof Integer) {
            int heightValue = (Integer) height;
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        heightValue
                );
            } else {
                params.height = heightValue;
            }
            view.setLayoutParams(params);
            return true;
        } else if (height instanceof String heightString) {

            if ("auto".equals(heightString)) {
                return false;
            } else if (LayoutUtils.isValidPercentage(heightString)) {
                float percentage = LayoutUtils.percentageToDecimal(heightString);
                if (percentage > 0) {
                    // Let the parent handle percentage constraints
                    return false;
                }
                return false;
            } else {
                try {
                    int heightValue = Integer.parseInt(heightString);
                    ViewGroup.LayoutParams params = view.getLayoutParams();
                    if (params == null) {
                        params = new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                heightValue
                        );
                    } else {
                        params.height = heightValue;
                    }
                    view.setLayoutParams(params);
                    return true;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing height value: " + heightString, e);
                }
            }
        }
        return false;
    }

    @Override
    public String getSegmentType() {
        return "image";
    }
}