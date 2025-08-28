package io.sourcesync.sdk.ui.divkit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import io.sourcesync.sdk.ui.utils.LayoutUtils

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
    private var onDetailsOutsideClicked: (() -> Unit)? = null

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
        divUrlHandler = context.createDivUrlHandler(onCloseAction = {
            // Handle close action (finish activity, navigate back, etc.)
            Log.d("MainActivity", "Closing activation view...")
            onDetailsCloseClicked?.run()
        }, onExternalUrlAction = { uri ->
            onDetailsActionTriggered.invoke()
        }, onCustomSchemeAction = { uri ->
            onDetailsActionTriggered.invoke()
        })
        Log.d(TAG, "Screen dimensions: ${screenWidth}x${screenHeight}")
        setupOutsideClickOverlay()
    }

    fun setupOutsideClickOverlay() {
        post {
            val parentView = parent as? ViewGroup ?: return@post

            // Create a custom touch delegate that checks bounds
            parentView.setOnTouchListener(TouchOutsideListener())
        }
    }

    private fun createDivConfiguration(): DivConfiguration {
        return DivConfiguration.Builder(PicassoDivImageLoader(context)).actionHandler(divUrlHandler)
            .visualErrorsEnabled(true).build()
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

            val params = LayoutUtils.getLayoutParams(
                widthPercentage,
                heightPercentage,
                screenWidth,
                screenHeight
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
        onDetailsOutsideClicked: () -> Unit,
        onClose: Runnable?
    ) {
        // Clean up existing detail safely
        detailView?.let { existingDetail ->
            existingDetail.safeCleanup()
            removeView(existingDetail)
        }

        this.onDetailsCloseClicked = onClose
        this.onDetailsOutsideClicked = onDetailsOutsideClicked
        onDetailsActionTriggered = onActionTriggered

        try {
            val detailsData = detailsParentJson.asTemplateAndCardParsed()
            detailView =
                ActivationDetails(context, detailsData, createDivConfiguration())

            val params = LayoutUtils.getLayoutParams(
                widthPercentage,
                heightPercentage,
                screenWidth,
                screenHeight
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
        previewParentJson: JSONObject, onClickListener: OnClickListener
    ) {
        showPreview(previewParentJson, 0f, 0f, onClickListener)
    }

    /**
     * Convenience method for showDetail with percentage parameters
     */
    fun showDetail(
        detailsParentJson: JSONObject,
        onActionTriggered: () -> Unit,
        onOutsideClicked: () -> Unit,
        onClose: Runnable?
    ) {
        showDetail(detailsParentJson, 0f, 0f, onActionTriggered, onOutsideClicked, onClose)
    }

    /**
     * Hides the detail view and restores preview.
     */
    fun hideDetails() {
        cleanupDetails()
        previewView?.visibility = VISIBLE
    }

    // Clean up detail view
    fun cleanupDetails() {
        detailView?.let { detail ->
            detail.safeCleanup()
            removeView(detail)
        }
        detailView = null
    }

    // Clean up preview view
    fun cleanupPreview() {
        previewView?.let { preview ->
            preview.safeCleanup()
            removeView(preview)
        }
        previewView = null
    }

    /**
     * Safely cleanup all views
     */
    private fun safeCleanupAll() {
        try {

            // Clean up detail view
            cleanupDetails()

            // Clean up preview view
            cleanupPreview()

            // Clear handlers
            handler.removeCallbacksAndMessages(null)
            onDetailsCloseClicked = null
            onPreviewClickHandler = null
            onDetailsOutsideClicked = null
        } catch (e: Exception) {
            Log.w(TAG, "Error during cleanup: ${e.message}")
        }
    }

    override fun onDetachedFromWindow() {
        // Clean up resources safely
        safeCleanupAll()
        super.onDetachedFromWindow()
    }

    companion object {
        private const val TAG = "SDK:ActivationView"
    }

    /**
     * Custom touch listener that preserves video controls functionality
     */
    private inner class TouchOutsideListener : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Get our bounds in parent coordinates
                val outRect = Rect()
                getHitRect(outRect)

                // Check if touch is outside our bounds
                if (!outRect.contains(event.x.toInt(), event.y.toInt())) {
                    // Check if this touch would hit video controls
                    if (!isVideoControlArea(event, v)) {
                        Log.d(TAG, "Valid outside click detected")
                        if (detailView != null) {
                            onDetailsOutsideClicked?.invoke()
                            hideDetails()
                        }
                        return true
                    }
                }
            }
            return false
        }

        private fun isVideoControlArea(event: MotionEvent, parentView: View): Boolean {
            // Define video control areas (bottom area typically)
            val controlHeight = 100 // dp, convert to pixels as needed
            val bottomControlArea = parentView.height - controlHeight

            // If touch is in control area, let it pass through
            return event.y > bottomControlArea
        }
    }
}