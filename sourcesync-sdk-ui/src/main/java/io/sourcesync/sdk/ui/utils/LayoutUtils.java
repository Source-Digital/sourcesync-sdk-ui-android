package io.sourcesync.sdk.ui.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LayoutUtils {
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("^\\s*(\\d+(?:\\.\\d+)?)\\s*%\\s*$");

    /**
     * Convert text alignment string to gravity
     */
    public static int getGravityFromAlignment(String alignment) {
        if (alignment == null) return Gravity.CENTER;

        return switch (alignment.toLowerCase()) {
            case "left", "leading" -> Gravity.START;
            case "right", "trailing" -> Gravity.END;
            case "justify" -> Gravity.FILL;
            default -> Gravity.CENTER;
        };
    }

    /**
     * Get horizontal gravity for views
     */
    public static int getHorizontalGravity(String alignment) {
        if (alignment == null) return Gravity.CENTER_HORIZONTAL;

        return switch (alignment.toLowerCase()) {
            case "left", "leading" -> Gravity.START;
            case "right", "trailing" -> Gravity.END;
            case "fill" -> Gravity.FILL_HORIZONTAL;
            default -> Gravity.CENTER_HORIZONTAL;
        };
    }

    /**
     * Convert alignment to Linear Layout orientation
     */
    public static int getLinearLayoutOrientation(String orientation) {
        if (orientation == null) return LinearLayout.VERTICAL;

        switch (orientation.toLowerCase()) {
            case "horizontal":
                return LinearLayout.HORIZONTAL;
            case "vertical":
            default:
                return LinearLayout.VERTICAL;
        }
    }

    /**
     * Convert dp to pixels
     */
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Convert dp to pixels (float version)
     */
    public static float dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return dp * density;
    }

    /**
     * Convert dp to pixels (double version)
     */
    public static double dpToPx(Context context, double dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return dp * density;
    }

    /**
     * Validates if a string is a valid percentage value (e.g. "50%")
     * @param value The string to validate
     * @return true if the string is a valid percentage, false otherwise
     */
    public static boolean isValidPercentage(String value) {
        if (value == null) return false;
        Matcher matcher = PERCENTAGE_PATTERN.matcher(value);
        return matcher.matches();
    }

    /**
     * Converts a percentage string to a decimal value (e.g. "50%" → 0.5f)
     * @param percentage The percentage string to convert
     * @return The decimal value
     * @throws IllegalArgumentException if the percentage string is invalid
     */
    public static float percentageToDecimal(String percentage) {
        if (!isValidPercentage(percentage)) {
            throw new IllegalArgumentException("Invalid percentage format: " + percentage);
        }

        Matcher matcher = PERCENTAGE_PATTERN.matcher(percentage);
        matcher.find(); // We know this succeeds because of isValidPercentage

        try {
            float decimalValue = Float.parseFloat(Objects.requireNonNull(matcher.group(1))) / 100f;

            // Ensure value is within valid range to prevent constraint issues
            if (decimalValue <= 0) {
                return 0.01f; // Small positive value to prevent zero
            } else if (!Float.isFinite(decimalValue)) {
                return 0.5f; // Reasonable default if not finite
            } else if (decimalValue > 1.0f) {
                return 1.0f; // Cap at 100%
            }

            return decimalValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse percentage value: " + percentage, e);
        }
    }

    /**
     * Converts a percentage string to pixels based on a total dimension
     * @param context The Android context
     * @param percentage The percentage string (e.g. "50%")
     * @param totalDimension The total dimension in pixels
     * @return The calculated pixels
     * @throws IllegalArgumentException if the percentage string is invalid
     */
    public static int percentageToPx(Context context, String percentage, int totalDimension) {
        float decimal = percentageToDecimal(percentage);
        return Math.round(totalDimension * decimal);
    }

    /**
     * Converts a percentage string to pixels based on a total dimension (float version)
     * @param context The Android context
     * @param percentage The percentage string (e.g. "50%")
     * @param totalDimension The total dimension in pixels
     * @return The calculated pixels
     * @throws IllegalArgumentException if the percentage string is invalid
     */
    public static float percentageToPxFloat(Context context, String percentage, float totalDimension) {
        float decimal = percentageToDecimal(percentage);
        return totalDimension * decimal;
    }

    /**
     * Convert a named font size to dp
     */
    public static int fontSizeToDP(String fontSize) {
        if (fontSize == null) return 16;

        switch (fontSize.toLowerCase()) {
            case "xxs": return 6;
            case "xs": return 10;
            case "sm": return 14;
            case "md": return 16;
            case "lg": return 20;
            case "xl": return 24;
            case "xxl": return 32;
            default:
                // Try to parse as a number
                try {
                    return Integer.parseInt(fontSize);
                } catch (NumberFormatException e) {
                    return 16; // Default medium size
                }
        }
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
}