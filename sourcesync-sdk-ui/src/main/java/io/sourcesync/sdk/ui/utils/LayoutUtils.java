package io.sourcesync.sdk.ui.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for layout-related operations.
 */
public class LayoutUtils {
    private static final String TAG = "LayoutUtils";
    private static final Map<String, WeakReference<Target>> imageTargetsMap = new HashMap<>();

    /**
     * Convert font size string to DP value.   MOBILE SIZES
     * @param fontSize Font size string (xs, sm, md, lg, xl, xxl)
     * @return Font size in DP
     */
    public static int fontSizeToDP(String fontSize) {
        if (fontSize == null) return 10; // Default size

        switch (fontSize.toLowerCase()) {
            case "xs": return 8;
            case "sm": return 12;
            case "md": return 18;
            case "lg": return 22;
            case "xl": return 26;
            case "xxl": return 30;
            default:
                try {
                    // Try to parse as numeric value
                    if (fontSize.endsWith("dp") || fontSize.endsWith("sp") || fontSize.endsWith("px")) {
                        return Integer.parseInt(fontSize.substring(0, fontSize.length() - 2));
                    } else {
                        return Integer.parseInt(fontSize);
                    }
                } catch (NumberFormatException e) {
                    return 16; // Default size on failure
                }
        }
    }

    /**
     * Convert size string to DP value. TV SIZES
     * @param size Size string (xs, sm, md, lg, xl, xxl or numeric with units)
     * @return Size in DP
     */
    public static int sizeToDp(String size) {
        if (size == null) return 16; // Default size

        switch (size.toLowerCase()) {
            case "xs": return 12;
            case "sm": return 18;
            case "md": return 24;
            case "lg": return 30;
            case "xl": return 36;
            case "xxl": return 42;
            default:
                try {
                    // Try to parse as numeric value
                    if (size.endsWith("dp")) {
                        return Integer.parseInt(size.substring(0, size.length() - 2));
                    } else if (size.endsWith("px")) {
                        return Integer.parseInt(size.substring(0, size.length() - 2)) / 2; // Rough px to dp conversion
                    } else {
                        return Integer.parseInt(size);
                    }
                } catch (NumberFormatException e) {
                    return 16; // Default size on failure
                }
        }
    }

    /**
     * Convert alignment string to scale type for ImageView
     */
    // Converts alignment string to appropriate scale type
    public static ImageView.ScaleType alignmentToScaleType(String alignment) {
        if (alignment == null) return ImageView.ScaleType.FIT_CENTER;

        return switch (alignment.toLowerCase()) {
            case "left" -> ImageView.ScaleType.FIT_START;
            case "right" -> ImageView.ScaleType.FIT_END;
            default -> ImageView.ScaleType.FIT_CENTER;
        };
    }

    // Converts contentMode string to ScaleType
    public static  ImageView.ScaleType contentModeToScaleType(String contentMode) {
        if (contentMode == null) return ImageView.ScaleType.FIT_CENTER;

        return switch (contentMode.toLowerCase()) {
            case "center" -> ImageView.ScaleType.CENTER;
            case "top", "left" -> ImageView.ScaleType.FIT_START;
            case "bottom", "right" -> ImageView.ScaleType.FIT_END;
            default -> ImageView.ScaleType.FIT_CENTER;
        };
    }

    /**
     * Get Android Gravity value from alignment string.
     * @param alignment Alignment string (left, center, right)
     * @return Android Gravity constant
     */
    public static int getGravityFromAlignment(String alignment) {
        if (alignment == null) return Gravity.CENTER;

        return switch (alignment.toLowerCase()) {
            case "left", "start" -> Gravity.START;
            case "right", "end" -> Gravity.END;
            case "top" -> Gravity.TOP;
            case "bottom" -> Gravity.BOTTOM;
            case "top_left", "top_start" -> Gravity.TOP | Gravity.START;
            case "top_right", "top_end" -> Gravity.TOP | Gravity.END;
            case "bottom_left", "bottom_start" -> Gravity.BOTTOM | Gravity.START;
            case "bottom_right", "bottom_end" -> Gravity.BOTTOM | Gravity.END;
            default -> Gravity.CENTER;
        };
    }

    /**
     * Convert a named spacing value to pixels
     */
    public static int getSpacingValue(Context context, String spacing) {
        if (spacing == null) return dpToPx(context, 8);

        switch (spacing.toLowerCase()) {
            case "none": return 0;
            case "xs": return dpToPx(context, 4);
            case "sm": return dpToPx(context, 8);
            case "md": return dpToPx(context, 12);
            case "lg": return dpToPx(context, 16);
            case "xl": return dpToPx(context, 24);
            default:
                // Try to parse as a number
                try {
                    return dpToPx(context, Integer.parseInt(spacing));
                } catch (NumberFormatException e) {
                    return dpToPx(context, 8); // Default spacing
                }
        }
    }

    /**
     * Check if a string represents a valid percentage.
     * @param value String to check
     * @return true if valid percentage, false otherwise
     */
    public static boolean isValidPercentage(String value) {
        if (value == null) return false;
        return value.endsWith("%") && value.length() > 1;
    }

    /**
     * Convert percentage string to decimal value.
     * @param percentage Percentage string (e.g. "50%")
     * @return Decimal value between 0 and 1 (e.g. 0.5)
     */
    public static float percentageToDecimal(String percentage) {
        if (!isValidPercentage(percentage)) return 1.0f;

        try {
            String numericPart = percentage.substring(0, percentage.length() - 1);
            return Float.parseFloat(numericPart) / 100.0f;
        } catch (NumberFormatException e) {
            return 1.0f; // Default on error
        }
    }

    /**
     * Converts a percentage string to pixels based on a total dimension
     * @param percentage The percentage string (e.g. "50%")
     * @param totalDimension The total dimension in pixels
     * @return The calculated pixels
     * @throws IllegalArgumentException if the percentage string is invalid
     */
    public static int percentageToPx(String percentage, int totalDimension) {
        float decimal = percentageToDecimal(percentage);
        return Math.round(totalDimension * decimal);
    }

    /**
     * Convert DP to pixels.
     * @param context Context to get display metrics
     * @param dp Value in DP
     * @return Value in pixels
     */
    public static int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    /**
     * Load a background image for a view with proper caching and memory management.
     *
     * @param context The context
     * @param view The target view
     * @param imageUrl The URL of the image to load
     * @param contentMode The content mode for scaling ("scaleToFill", "scaleAspectFit", etc.)
     * @param opacity The opacity of the image (0.0 to 1.0)
     * @param backgroundDrawable Optional background drawable to layer with the image
     * @param borderRadius Optional border radius to apply
     * @param onImageLoaded Optional callback for when the image is loaded
     */
    public static void loadBackgroundImage(
            Context context,
            View view,
            String imageUrl,
            String contentMode,
            Float opacity,
            Drawable backgroundDrawable,
            Integer borderRadius,
            OnImageLoadedListener onImageLoaded) {

        if (imageUrl == null || imageUrl.isEmpty()) {
            if (backgroundDrawable != null) {
                view.setBackground(backgroundDrawable);
            }
            return;
        }

        // Create a unique key for this target based on the URL and view id
        final String targetKey = imageUrl + view.hashCode();

        // First check if there's an existing reference to prevent memory leaks
        WeakReference<Target> existingTargetRef = imageTargetsMap.get(targetKey);
        if (existingTargetRef != null && existingTargetRef.get() != null) {
            // Clear the old target
            Picasso.get().cancelRequest(existingTargetRef.get());
            imageTargetsMap.remove(targetKey);
        }

        // Create a target for Picasso to load the image
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {
                    // Create drawable from bitmap
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);

                    // Scale based on content mode
                    if ("scaleToFill".equals(contentMode)) {
                        bitmapDrawable.setTileModeXY(android.graphics.Shader.TileMode.CLAMP,
                                android.graphics.Shader.TileMode.CLAMP);
                    }

                    // Apply opacity if specified
                    if (opacity != null) {
                        bitmapDrawable.setAlpha((int)(opacity * 255));
                    }

                    // Handle border radius and background differently depending on API level
                    if (borderRadius != null && borderRadius > 0) {
                        // Create a shape drawable for the rounded corners
                        GradientDrawable roundedDrawable = new GradientDrawable();
                        roundedDrawable.setCornerRadius(dpToPx(context, borderRadius));

                        // For Lollipop and above, we enable hardware acceleration for proper clipping
                        view.setClipToOutline(true);

                        // Create the final drawable (either with or without background color/gradient)
                        if (backgroundDrawable != null) {
                            // Both image and background - need a layer drawable
                            Drawable[] layers = new Drawable[]{bitmapDrawable, backgroundDrawable};
                            LayerDrawable layerDrawable = new LayerDrawable(layers);
                            view.setBackground(layerDrawable);
                        } else {
                            // Just the image
                            view.setBackground(bitmapDrawable);
                        }

                        // Apply rounded corners using outline provider
                        view.setOutlineProvider(new android.view.ViewOutlineProvider() {
                            @Override
                            public void getOutline(View view, android.graphics.Outline outline) {
                                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(),
                                        dpToPx(context, borderRadius));
                            }
                        });
                    } else {
                        // No border radius, just set the background normally
                        if (backgroundDrawable != null) {
                            // Both image and background - need a layer drawable
                            Drawable[] layers = new Drawable[]{bitmapDrawable, backgroundDrawable};
                            LayerDrawable layerDrawable = new LayerDrawable(layers);
                            view.setBackground(layerDrawable);
                        } else {
                            // Just the image
                            view.setBackground(bitmapDrawable);
                        }
                    }

                    // Call the callback if provided
                    if (onImageLoaded != null) {
                        onImageLoaded.onSuccess(bitmapDrawable);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error setting background image", e);
                    if (onImageLoaded != null) {
                        onImageLoaded.onError(e);
                    }
                }

                // Remove the target reference after successful loading
                imageTargetsMap.remove(targetKey);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Log.e(TAG, "Failed to load background image: " + e.getMessage());

                // Apply the regular background if image loading fails
                if (backgroundDrawable != null) {
                    view.setBackground(backgroundDrawable);
                }

                // Call the callback if provided
                if (onImageLoaded != null) {
                    onImageLoaded.onError(e);
                }

                // Remove the target reference
                imageTargetsMap.remove(targetKey);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // You could set a placeholder here if needed
                if (backgroundDrawable != null) {
                    view.setBackground(backgroundDrawable);
                }

                // Call the callback if provided
                if (onImageLoaded != null) {
                    onImageLoaded.onPrepare();
                }
            }
        };

        // Store a weak reference to the target to prevent memory leaks
        imageTargetsMap.put(targetKey, new WeakReference<>(target));

        // Start the image loading
        Picasso.get()
                .load(imageUrl)
                .into(target);
    }

    /**
     * Overloaded version without callback
     */
    public static void loadBackgroundImage(
            Context context,
            View view,
            String imageUrl,
            String contentMode,
            Float opacity,
            Drawable backgroundDrawable,
            Integer borderRadius) {

        loadBackgroundImage(context, view, imageUrl, contentMode, opacity,
                backgroundDrawable, borderRadius, null);
    }

    /**
     * Map gradient start/end points to gradient orientation
     */
    public static GradientDrawable.Orientation determineGradientOrientation(
            float startX, float startY, float endX, float endY) {

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

    /**
     * Create a GradientDrawable from a background gradient configuration
     */
    public static GradientDrawable createGradientDrawable(SegmentAttributes.BackgroundGradient gradient) {
        GradientDrawable gradientDrawable = new GradientDrawable();

        try {
            String gradientType = gradient.type;
            int[] colors = new int[gradient.colors.length()];

            for (int i = 0; i < gradient.colors.length(); i++) {
                colors[i] = Color.parseColor(gradient.colors.getString(i));
            }

            if ("linear".equals(gradientType)) {
                gradientDrawable.setColors(colors);
                gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);

                // Set gradient orientation
                float startX = (float)gradient.startPoint.x;
                float startY = (float)gradient.startPoint.y;
                float endX = (float)gradient.endPoint.x;
                float endY = (float)gradient.endPoint.y;

                GradientDrawable.Orientation orientation = determineGradientOrientation(startX, startY, endX, endY);
                gradientDrawable.setOrientation(orientation);
            } else if ("radial".equals(gradientType)) {
                gradientDrawable.setColors(colors);
                gradientDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating gradient drawable", e);
        }

        return gradientDrawable;
    }

    /**
     * Interface for image loading callbacks
     */
    public interface OnImageLoadedListener {
        void onSuccess(Drawable drawable);
        void onError(Exception e);
        void onPrepare();
    }
}