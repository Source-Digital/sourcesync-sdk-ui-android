package io.sourcesync.sdk.ui.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.yandex.div.core.view2.Div2View
import com.yandex.div.data.DivParsingEnvironment
import com.yandex.div.json.ParsingErrorLogger
import com.yandex.div2.DivData
import io.sourcesync.sdk.ui.helpers.CustomUrlHandler
import io.sourcesync.sdk.ui.models.ActivationHorizontalAlignment
import io.sourcesync.sdk.ui.models.ActivationPosition
import io.sourcesync.sdk.ui.models.ActivationVerticalAlignment
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

/**
 * Utility class for layout operations, view cleanup, and DivKit integration
 */
object LayoutUtils {

    /**
     * Parses JSON object into DivData with templates and card components
     * @return DivData instance ready for DivKit rendering
     */
    fun JSONObject.asTemplateAndCardParsed(): DivData {
        val templates = getJSONObject("templates")
        val card = getJSONObject("card")
        val environment = DivParsingEnvironment(ParsingErrorLogger.LOG)
        environment.parseTemplates(templates)
        return DivData(environment, card)
    }

    /**
     * Recursively clears all RecyclerView adapters and layout managers in view hierarchy
     * @param tag Log tag for error reporting
     * @param view Root view to search for RecyclerViews
     */
    fun clearRecyclerViews(tag: String, view: View) {
        try {
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    val child = view.getChildAt(i)
                    if (child is RecyclerView) {
                        try {
                            child.adapter = null
                            child.layoutManager = null
                        } catch (e: Exception) {
                            Log.w(tag, "Error clearing RecyclerView: ${e.message}")
                        }
                    }
                    if (child is ViewGroup) {
                        clearRecyclerViews(tag, child)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Error in clearRecyclerViews: ${e.message}")
        }
    }

    /**
     * Checks if view is safely attached and ready for cleanup operations
     * @param tag Log tag for error reporting
     * @param divView View to check attachment state
     * @return true if view is safe to clean up
     */
    fun isSafeForCleanup(tag: String, divView: View?): Boolean {
        return try {
            divView != null && divView.isAttachedToWindow
        } catch (e: Exception) {
            Log.w(tag, "Error checking cleanup safety: ${e.message}")
            false
        }
    }

    /**
     * Forces cleanup operations even when view is not in safe state
     * @param tag Log tag for error reporting
     * @param divView View to forcefully clean up
     */
    fun forceCleanup(tag: String, divView: View?) {
        try {
            Log.w(tag, "Force cleanup initiated")
            divView?.let { view ->
                clearRecyclerViews(tag, view)
                // Don't call cleanup() in force mode to avoid exceptions
            }
        } catch (e: Exception) {
            Log.e(tag, "Error during force cleanup: ${e.message}")
        }
    }

    /**
     * Safely cleans up Div2View with fallback error handling
     * Handles observer registration issues and provides alternative cleanup paths
     * @param tag Log tag for error reporting
     * @param divView Div2View to clean up safely
     */
    fun safeCleanup(tag: String, divView: Div2View?) {
        try {
            Log.d(tag, "Starting safe cleanup")

            divView?.let { view ->
                // Clear all RecyclerViews first
                clearRecyclerViews(tag, view)

                // Clear any pending operations
                view.clearFocus()

                // Cleanup the div view with additional safety
                try {
                    view.cleanup()
                    Log.d(tag, "DivView cleanup completed successfully")
                } catch (observerException: IllegalStateException) {
                    if (observerException.message?.contains("Observer") == true &&
                        observerException.message?.contains("was not registered") == true
                    ) {
                        Log.w(tag, "Observer issue during cleanup, attempting alternative cleanup")

                        // Alternative cleanup approach
                        try {
                            // Try to clear all child views manually
                            if (true) {
                                view.removeAllViews()
                            }
                            Log.d(tag, "Alternative cleanup completed")
                        } catch (alternativeException: Exception) {
                            Log.w(
                                tag,
                                "Alternative cleanup failed: ${alternativeException.message}"
                            )
                        }
                    } else {
                        throw observerException
                    }
                } catch (cleanupException: Exception) {
                    Log.w(tag, "DivView cleanup failed: ${cleanupException.message}")
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Error during safe cleanup: ${e.message}")
        }
    }

    /**
     * Creates FrameLayout parameters with percentage-based dimensions
     * @param widthPercentage Width as percentage of screen (0-1), 0 for WRAP_CONTENT
     * @param heightPercentage Height as percentage of screen (0-1), 0 for WRAP_CONTENT
     * @param screenWidth Screen width in pixels
     * @param screenHeight Screen height in pixels
     * @return LayoutParams with calculated dimensions
     */
    fun getLayoutParams(
        widthPercentage: Float,
        heightPercentage: Float,
        screenWidth: Int,
        screenHeight: Int
    ): LayoutParams {
        // Calculate dimensions based on percentage
        val width = if (widthPercentage <= 0f) {
            LayoutParams.WRAP_CONTENT
        } else {
            (max(screenWidth, screenHeight) * widthPercentage.coerceIn(0f, 1f)).toInt()
        }

        val height = if (heightPercentage <= 0f) {
            LayoutParams.WRAP_CONTENT
        } else {
            (min(screenWidth, screenHeight) * heightPercentage.coerceIn(0f, 1f)).toInt()
        }

        val params = LayoutParams(width, height)

        return params
    }

    /**
     * Applies alignment rules to RelativeLayout parameters based on position
     * @param position ActivationPosition containing alignment configuration
     * @param layoutParams RelativeLayout parameters to modify
     * @return Modified RelativeLayout parameters with alignment rules applied
     */
    fun getLayoutParams(position: ActivationPosition, layoutParams: RelativeLayout.LayoutParams): RelativeLayout.LayoutParams{
        // Apply horizontal alignment
        when (position.activationPosition?.activationHorizontalAlignment) {
            ActivationHorizontalAlignment.LEFT -> {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            }
            ActivationHorizontalAlignment.CENTER -> {
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            }
            ActivationHorizontalAlignment.RIGHT -> {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            }
            null -> {
                // Default to left if not specified
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            }
        }

        // Apply vertical alignment
        when (position.activationPosition?.activationVerticalAlignment) {
            ActivationVerticalAlignment.TOP -> {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            }
            ActivationVerticalAlignment.CENTER -> {
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            }
            ActivationVerticalAlignment.BOTTOM -> {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
            null -> {
                // Default to top if not specified
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            }
        }

        return layoutParams
    }
}

/**
 * Context extension for creating CustomUrlHandler with callback configuration
 * @param onCloseAction Required callback for close actions
 * @param onExternalUrlAction Optional callback for external URL handling
 * @param onCustomSchemeAction Optional callback for custom scheme handling
 * @return Configured CustomUrlHandler instance
 */
fun Context.createDivUrlHandler(
    onCloseAction: () -> Unit,
    onExternalUrlAction: ((Uri) -> Unit)? = null,
    onCustomSchemeAction: ((Uri) -> Unit)? = null,
): CustomUrlHandler {
    return CustomUrlHandler(
        context = this,
        onCloseAction = onCloseAction,
        onExternalUrlAction = onExternalUrlAction,
        onCustomSchemeAction = onCustomSchemeAction
    )
}