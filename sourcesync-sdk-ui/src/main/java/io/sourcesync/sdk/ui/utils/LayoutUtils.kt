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
    /**
     * Overloaded version without callback
     */
    @JvmStatic
    fun loadBackgroundImage(
        context: Context,
        view: View,
        imageUrl: String?,
        contentMode: String,
        opacity: Float?,
        backgroundDrawable: Drawable?,
        borderRadius: Int?
    ) {
        if (imageUrl.isNullOrEmpty()) {
            if (backgroundDrawable != null) {
                view.background = backgroundDrawable
            }
            return
        }

        // Create a unique key for this target based on the URL and view id
        val targetKey = imageUrl + view.hashCode()

        // First check if there's an existing reference to prevent memory leaks
        val existingTargetRef = imageTargetsMap[targetKey]
        if (existingTargetRef?.get() != null) {
            // Clear the old target
            Picasso.get().cancelRequest(existingTargetRef.get()!!)
            imageTargetsMap.remove(targetKey)
        }

        // Create a target for Picasso to load the image
        val target: Target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                try {
                    // Create drawable from bitmap
                    val bitmapDrawable = BitmapDrawable(context.resources, bitmap)

                    // Scale based on content mode
                    if ("scaleToFill" == contentMode) {
                        bitmapDrawable.setTileModeXY(
                            Shader.TileMode.CLAMP,
                            Shader.TileMode.CLAMP
                        )
                    }

                    // Apply opacity if specified
                    if (opacity != null) {
                        bitmapDrawable.alpha = (opacity * 255).toInt()
                    }

                    // Handle border radius and background differently depending on API level
                    if (borderRadius != null && borderRadius > 0) {
                        // Create a shape drawable for the rounded corners
                        val roundedDrawable = GradientDrawable()
                        roundedDrawable.cornerRadius =
                            dpToPx(context, borderRadius.toFloat()).toFloat()

                        // For Lollipop and above, we enable hardware acceleration for proper clipping
                        view.clipToOutline = true

                        // Create the final drawable (either with or without background color/gradient)
                        if (backgroundDrawable != null) {
                            // Both image and background - need a layer drawable
                            val layers = arrayOf(bitmapDrawable, backgroundDrawable)
                            val layerDrawable = LayerDrawable(layers)
                            view.background = layerDrawable
                        } else {
                            // Just the image
                            view.background = bitmapDrawable
                        }

                        // Apply rounded corners using outline provider
                        view.outlineProvider = object : ViewOutlineProvider() {
                            override fun getOutline(view: View, outline: Outline) {
                                outline.setRoundRect(
                                    0, 0, view.width, view.height,
                                    dpToPx(context, borderRadius.toFloat()).toFloat()
                                )
                            }
                        }
                    } else {
                        // No border radius, just set the background normally
                        if (backgroundDrawable != null) {
                            // Both image and background - need a layer drawable
                            val layers = arrayOf(bitmapDrawable, backgroundDrawable)
                            val layerDrawable = LayerDrawable(layers)
                            view.background = layerDrawable
                        } else {
                            // Just the image
                            view.background = bitmapDrawable
                        }
                    }

                    // Call the callback if provided
//                    onImageLoaded?.onSuccess(bitmapDrawable)
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting background image", e)
//                    onImageLoaded?.onError(e)
                }

                // Remove the target reference after successful loading
                imageTargetsMap.remove(targetKey)
            }

            override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {
                Log.e(TAG, "Failed to load background image: " + e.message)

                // Apply the regular background if image loading fails
                if (backgroundDrawable != null) {
                    view.background = backgroundDrawable
                }

                // Call the callback if provided
//                onImageLoaded?.onError(e)

                // Remove the target reference
                imageTargetsMap.remove(targetKey)
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable) {
                // You could set a placeholder here if needed
                if (backgroundDrawable != null) {
                    view.background = backgroundDrawable
                }

                // Call the callback if provided
//                onImageLoaded?.onPrepare()
            }
        }

        // Store a weak reference to the target to prevent memory leaks
        imageTargetsMap[targetKey] =
            WeakReference(target)

        // Start the image loading
        Picasso.get()
            .load(imageUrl)
            .into(target)
    }

    fun JSONObject.asTemplateAndCardParsed(): DivData {
        val templates = getJSONObject("template")
        val card = getJSONObject("card")
        val environment = DivParsingEnvironment(ParsingErrorLogger.LOG)
        environment.parseTemplates(templates)
        return DivData(environment, card)
    }
}