package io.sourcesync.sdk.ui.utils

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.yandex.div.core.view2.Div2View
import com.yandex.div.data.DivParsingEnvironment
import com.yandex.div.json.ParsingErrorLogger
import com.yandex.div2.DivData
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

/**
 * Utility class for layout-related operations.
 */
object LayoutUtils {
    fun JSONObject.asTemplateAndCardParsed(): DivData {
        val templates = getJSONObject("templates")
        val card = getJSONObject("card")
        val environment = DivParsingEnvironment(ParsingErrorLogger.LOG)
        environment.parseTemplates(templates)
        return DivData(environment, card)
    }

    /**
     * Safely cleanup all RecyclerViews in the view hierarchy
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
     * Check if the view is in a safe state for cleanup
     */
    fun isSafeForCleanup(tag:String,divView:View?): Boolean {
        return try {
            divView != null && divView.isAttachedToWindow
        } catch (e: Exception) {
            Log.w(tag, "Error checking cleanup safety: ${e.message}")
            false
        }
    }

    /**
     * Force cleanup even if view is not in a safe state
     */
    fun forceCleanup(tag:String, divView: View?) {
        try {
            Log.w(tag, "Force cleanup initiated")
            divView?.let { view ->
                clearRecyclerViews(tag,view)
                // Don't call cleanup() in force mode to avoid exceptions
            }
        } catch (e: Exception) {
            Log.e(tag, "Error during force cleanup: ${e.message}")
        }
    }

    /**
     * Safe cleanup method that should be called before view destruction
     */
    fun safeCleanup(tag:String, divView: Div2View?) {
        try {
            Log.d(tag, "Starting safe cleanup")

            divView?.let { view ->
                // Clear all RecyclerViews first
                clearRecyclerViews(tag,view)

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
}