package io.sourcesync.sdk.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Outline
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import com.yandex.div.data.DivParsingEnvironment
import com.yandex.div.json.ParsingErrorLogger
import com.yandex.div2.DivData
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.Locale

/**
 * Utility class for layout-related operations.
 */
object LayoutUtils {
    private const val TAG = "LayoutUtils"
    private val imageTargetsMap: MutableMap<String, WeakReference<Target?>> = HashMap()

    /**
     * Convert font size string to DP value.   MOBILE SIZES
     * @param fontSize Font size string (xs, sm, md, lg, xl, xxl)
     * @return Font size in DP
     */
    @JvmStatic
    fun fontSizeToDP(fontSize: String?): Int {
        if (fontSize == null) return 10 // Default size


        return when (fontSize.lowercase(Locale.getDefault())) {
            "xs" -> 8
            "sm" -> 12
            "md" -> 18
            "lg" -> 22
            "xl" -> 26
            "xxl" -> 30
            else -> try {
                // Try to parse as numeric value
                if (fontSize.endsWith("dp") || fontSize.endsWith("sp") || fontSize.endsWith("px")) {
                    fontSize.substring(0, fontSize.length - 2).toInt()
                } else {
                    fontSize.toInt()
                }
            } catch (e: NumberFormatException) {
                16 // Default size on failure
            }
        }
    }

    /**
     * Convert size string to DP value. TV SIZES
     * @param size Size string (xs, sm, md, lg, xl, xxl or numeric with units)
     * @return Size in DP
     */
    fun sizeToDp(size: String?): Int {
        if (size == null) return 16 // Default size


        return when (size.lowercase(Locale.getDefault())) {
            "xs" -> 12
            "sm" -> 18
            "md" -> 24
            "lg" -> 30
            "xl" -> 36
            "xxl" -> 42
            else -> try {
                // Try to parse as numeric value
                if (size.endsWith("dp")) {
                    size.substring(0, size.length - 2).toInt()
                } else if (size.endsWith("px")) {
                    size.substring(0, size.length - 2).toInt() / 2 // Rough px to dp conversion
                } else {
                    size.toInt()
                }
            } catch (e: NumberFormatException) {
                16 // Default size on failure
            }
        }
    }

    /**
     * Convert alignment string to scale type for ImageView
     */
    // Converts alignment string to appropriate scale type
    @JvmStatic
    fun alignmentToScaleType(alignment: String?): ImageView.ScaleType {
        if (alignment == null) return ImageView.ScaleType.FIT_CENTER

        return when (alignment.lowercase(Locale.getDefault())) {
            "left" -> ImageView.ScaleType.FIT_START
            "right" -> ImageView.ScaleType.FIT_END
            else -> ImageView.ScaleType.FIT_CENTER
        }
    }

    // Converts contentMode string to ScaleType
    @JvmStatic
    fun contentModeToScaleType(contentMode: String?): ImageView.ScaleType {
        if (contentMode == null) return ImageView.ScaleType.FIT_CENTER

        return when (contentMode.lowercase(Locale.getDefault())) {
            "center" -> ImageView.ScaleType.CENTER
            "top", "left" -> ImageView.ScaleType.FIT_START
            "bottom", "right" -> ImageView.ScaleType.FIT_END
            else -> ImageView.ScaleType.FIT_CENTER
        }
    }

    /**
     * Get Android Gravity value from alignment string.
     * @param alignment Alignment string (left, center, right)
     * @return Android Gravity constant
     */
    @JvmStatic
    fun getGravityFromAlignment(alignment: String?): Int {
        if (alignment == null) return Gravity.CENTER

        return when (alignment.lowercase(Locale.getDefault())) {
            "left", "start" -> Gravity.START
            "right", "end" -> Gravity.END
            "top" -> Gravity.TOP
            "bottom" -> Gravity.BOTTOM
            "top_left", "top_start" -> Gravity.TOP or Gravity.START
            "top_right", "top_end" -> Gravity.TOP or Gravity.END
            "bottom_left", "bottom_start" -> Gravity.BOTTOM or Gravity.START
            "bottom_right", "bottom_end" -> Gravity.BOTTOM or Gravity.END
            else -> Gravity.CENTER
        }
    }

    /**
     * Convert a named spacing value to pixels
     */
    @JvmStatic
    fun getSpacingValue(context: Context, spacing: String?): Int {
        if (spacing == null) return dpToPx(context, 8f)

        return when (spacing.lowercase(Locale.getDefault())) {
            "none" -> 0
            "xs" -> dpToPx(context, 4f)
            "sm" -> dpToPx(context, 8f)
            "md" -> dpToPx(context, 12f)
            "lg" -> dpToPx(context, 16f)
            "xl" -> dpToPx(context, 24f)
            else ->                 // Try to parse as a number
                try {
                    dpToPx(context, spacing.toInt().toFloat())
                } catch (e: NumberFormatException) {
                    dpToPx(context, 8f) // Default spacing
                }
        }
    }

    /**
     * Check if a string represents a valid percentage.
     * @param value String to check
     * @return true if valid percentage, false otherwise
     */
    @JvmStatic
    fun isValidPercentage(value: String?): Boolean {
        if (value == null) return false
        return value.endsWith("%") && value.length > 1
    }

    /**
     * Convert percentage string to decimal value.
     * @param percentage Percentage string (e.g. "50%")
     * @return Decimal value between 0 and 1 (e.g. 0.5)
     */
    @JvmStatic
    fun percentageToDecimal(percentage: String): Float {
        if (!isValidPercentage(percentage)) return 1.0f

        try {
            val numericPart = percentage.substring(0, percentage.length - 1)
            return numericPart.toFloat() / 100.0f
        } catch (e: NumberFormatException) {
            return 1.0f // Default on error
        }
    }

    /**
     * Converts a percentage string to pixels based on a total dimension
     * @param percentage The percentage string (e.g. "50%")
     * @param totalDimension The total dimension in pixels
     * @return The calculated pixels
     * @throws IllegalArgumentException if the percentage string is invalid
     */
    @JvmStatic
    fun percentageToPx(percentage: String, totalDimension: Int): Int {
        val decimal = percentageToDecimal(percentage)
        return Math.round(totalDimension * decimal)
    }

    /**
     * Convert DP to pixels.
     * @param context Context to get display metrics
     * @param dp Value in DP
     * @return Value in pixels
     */
    @JvmStatic
    fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    fun JSONObject.asTemplateAndCardParsed(): DivData {
        val templates = getJSONObject("templates")
        val card = getJSONObject("card")
        val environment = DivParsingEnvironment(ParsingErrorLogger.LOG)
        environment.parseTemplates(templates)
        return DivData(environment, card)
    }
}