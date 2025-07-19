package io.sourcesync.sdk.ui.divkit

import android.content.Context
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.FrameLayout
import com.yandex.div.core.DivActionHandler
import com.yandex.div.core.DivConfiguration
import com.yandex.div.core.DivViewFacade
import com.yandex.div.json.expressions.ExpressionResolver
import com.yandex.div2.DivAction
import io.sourcesync.sdk.ui.utils.LayoutUtils.asTemplateAndCardParsed
import io.sourcesync.sdk.ui.utils.PicassoDivImageLoader
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

        Log.d(TAG, "Screen dimensions: ${screenWidth}x${screenHeight}")
    }

    private fun createDivConfiguration(): DivConfiguration {
        return DivConfiguration.Builder(PicassoDivImageLoader(context))
            .actionHandler(CustomActionHandler())
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

            Log.d(TAG, "Preview dimensions: ${width}x${height} (${widthPercentage*100}% x ${heightPercentage*100}%)")

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
     * @param widthPercentage Width as percentage of screen width (0.0 to 1.0).
     * @param heightPercentage Height as percentage of screen height (0.0 to 1.0).
     */
    fun showDetail(
        detailsParentJson: JSONObject,
        widthPercentage: Float = 1.0f,
        heightPercentage: Float = 1.0f,
        onClose: Runnable?
    ) {
        // Clean up existing detail safely
        detailView?.let { existingDetail ->
            existingDetail.safeCleanup()
            removeView(existingDetail)
        }

        this.onDetailsCloseClicked = onClose

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

            Log.d(TAG, "Detail dimensions: ${width}x${height} (${widthPercentage*100}% x ${heightPercentage*100}%)")

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
    fun showDetail(detailsParentJson: JSONObject, onClose: Runnable?) {
        showDetail(detailsParentJson, 0f, 0f, onClose)
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

    // Custom action handler to handle the close action
    private inner class CustomActionHandler : DivActionHandler() {

        override fun handleAction(
            action: DivAction,
            view: DivViewFacade,
            resolver: ExpressionResolver
        ): Boolean {
            val url = action.url?.evaluate(resolver) ?: return super.handleAction(action, view, resolver)
            // Check if it's our close action
            if (url.toString().startsWith("div-action://close")) {
                // Handle the close action
                onDetailsCloseClicked?.run()
                return true
            }
            // Let the parent class handle other actions
            return super.handleAction(action, view, resolver)
        }
    }
}