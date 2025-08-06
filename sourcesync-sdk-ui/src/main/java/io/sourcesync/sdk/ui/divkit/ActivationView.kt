package io.sourcesync.sdk.ui.divkit

import android.content.Context
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.FrameLayout
import com.yandex.div.core.DivConfiguration
import io.sourcesync.sdk.ui.utils.EnhancedDivUrlHandler
import io.sourcesync.sdk.ui.utils.LayoutUtils.asTemplateAndCardParsed
import io.sourcesync.sdk.ui.utils.PicassoDivImageLoader
import io.sourcesync.sdk.ui.utils.createDivUrlHandler
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

/**
 * A view representing an activation component with preview and detail views.
 */
class ActivationView(private val context: Context) : FrameLayout(context) {
    private var onDetailsCloseClicked: Runnable? = null
    private var previewView: ActivationPreview? = null
    private var detailView: ActivationDetails? = null
    private var onPreviewClickHandler: Runnable? = null
    private val handler = Handler()
    private var divUrlHandler: EnhancedDivUrlHandler
    private lateinit var onDetailsActionTriggered: () -> Unit
    // Screen dimensions
    private val screenWidth: Int
    private val screenHeight: Int

    init {
        // Get screen dimensions
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels

        // Create the URL handler
        divUrlHandler = context.createDivUrlHandler(
            onCloseAction = {
                // Handle close action (finish activity, navigate back, etc.)
                Log.d("MainActivity", "Closing activation view...")
                onDetailsCloseClicked?.run()
            },
            onExternalUrlAction = { uri ->
                // Optional: Custom handling for external URLs
            },
            onCustomSchemeAction = { uri ->
                // Optional: Handle custom schemes not covered by default implementation
            },
            onDetailsActionTriggered
        )
        Log.d(TAG, "Screen dimensions: ${screenWidth}x${screenHeight}")
    }


    private fun createDivConfiguration(): DivConfiguration {
        return DivConfiguration.Builder(PicassoDivImageLoader(context))
            .actionHandler(divUrlHandler)
            .visualErrorsEnabled(true)
            .build()
    }

    /**
     * Shows the preview view with given data.
     *
     * @param previewParentJson JSON data for preview.
     * @param onClickListener Listener to execute on click.
     * @param widthPercentage Width as percentage of screen width (0.0 to 1.0).
     * @param heightPercentage Height as percentage of screen height (0.0 to 1.0).
     */
    @Throws(JSONException::class)
    fun showPreview(
        previewParentJson: JSONObject,
        widthPercentage: Float = 0f,
        heightPercentage: Float = 0f,
        onClickListener: OnClickListener
    ) {
        // Clean up existing preview safely
        previewView?.let { existingPreview ->
            existingPreview.safeCleanup()
            removeView(existingPreview)
        }

        this.onPreviewClickHandler = Runnable { onClickListener.onClick(this) }
        val previewData = previewParentJson.asTemplateAndCardParsed()

        try {
            previewView = ActivationPreview(context, previewData, createDivConfiguration())
            previewView?.setOnClickListener {
                onPreviewClickHandler?.let { handler ->
                    previewView?.visibility = GONE
                    handler.run()
                }
            }

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

            Log.d(
                TAG,
                "Preview dimensions: ${width}x${height} (${widthPercentage * 100}% x ${heightPercentage * 100}%)"
            )

            previewView?.let { addView(it, params) }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating preview view: " + e.message)
        }
    }

    /**
     * Shows the detail view with given data.
     *
     * @param detailsParentJson JSON data for detail.
     * @param onClose Runnable to execute on close.
     * @param onActionTriggered Callback to be called when an Action is triggered
     * @param widthPercentage Width as percentage of screen width (0.0 to 1.0).
     * @param heightPercentage Height as percentage of screen height (0.0 to 1.0).
     */
    fun showDetail(
        detailsParentJson: JSONObject,
        widthPercentage: Float = 1.0f,
        heightPercentage: Float = 1.0f,
        onActionTriggered: () -> Unit,
        onClose: Runnable?
    ) {
        // Clean up existing detail safely
        detailView?.let { existingDetail ->
            existingDetail.safeCleanup()
            removeView(existingDetail)
        }

        this.onDetailsCloseClicked = onClose
        onDetailsActionTriggered = onActionTriggered

        try {
            val detailsData = detailsParentJson.asTemplateAndCardParsed()
            detailView = ActivationDetails(context, detailsData, createDivConfiguration())

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

            Log.d(
                TAG,
                "Detail dimensions: ${width}x${height} (${widthPercentage * 100}% x ${heightPercentage * 100}%)"
            )

            detailView?.let { addView(it, params) }
        } catch (e: JSONException) {
            Log.e(TAG, "Error creating detail view: " + e.message)
        }
    }

    /**
     * Convenience method for showPreview with percentage parameters
     */
    @Throws(JSONException::class)
    fun showPreview(
        previewParentJson: JSONObject,
        onClickListener: OnClickListener
    ) {
        showPreview(previewParentJson, 0f, 0f, onClickListener)
    }

    /**
     * Convenience method for showDetail with percentage parameters
     */
    fun showDetail(detailsParentJson: JSONObject,onActionTriggered: () -> Unit, onClose: Runnable?) {
        showDetail(detailsParentJson, 0f, 0f,onActionTriggered, onClose)
    }

    /**
     * Hides the detail view and restores preview.
     */
    fun hideDetails() {
        detailView?.let { detail ->
            detail.safeCleanup()
            removeView(detail)
            detailView = null
            previewView?.visibility = VISIBLE
        }
    }

    /**
     * Safely cleanup all views
     */
    private fun safeCleanupAll() {
        try {
            // Clean up detail view
            detailView?.let { detail ->
                detail.safeCleanup()
                removeView(detail)
            }
            detailView = null

            // Clean up preview view
            previewView?.let { preview ->
                preview.safeCleanup()
                removeView(preview)
            }
            previewView = null

            // Clear handlers
            handler.removeCallbacksAndMessages(null)
            onDetailsCloseClicked = null
            onPreviewClickHandler = null
        } catch (e: Exception) {
            Log.w(TAG, "Error during cleanup: ${e.message}")
        }
    }

    /**
     * Get screen width in pixels
     */
    fun getScreenWidth(): Int = screenWidth

    /**
     * Get screen height in pixels
     */
    fun getScreenHeight(): Int = screenHeight

    override fun onDetachedFromWindow() {
        // Clean up resources safely
        safeCleanupAll()
        super.onDetachedFromWindow()
    }

    /**
     * Public method to manually cleanup resources
     * Call this from your fragment's onDestroyView() if needed
     */
    fun cleanup() {
        safeCleanupAll()
    }

    companion object {
        private const val TAG = "SDK:ActivationView"
    }
}