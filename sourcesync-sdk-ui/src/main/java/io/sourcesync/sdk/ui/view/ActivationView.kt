package io.sourcesync.sdk.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.yandex.div.DivDataTag
import com.yandex.div.core.Div2Context
import com.yandex.div.core.view2.Div2View
import com.yandex.div2.DivData
import io.sourcesync.sdk.ui.utils.LayoutUtils
import io.sourcesync.sdk.ui.utils.LayoutUtils.asTemplateAndCardParsed
import org.json.JSONObject

/**
 * Unified activation view that can display any view type.
 */
class ActivationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var divView: Div2View? = null
    private var config: ActivationConfig? = null
    private var layoutParam: RelativeLayout.LayoutParams? = null
    private var touchOutsideListener: TouchOutsideListener? = null

    // Constants for better maintainability
    private val videoControlHeightDp = 100
    private val videoControlHeightPx by lazy {
        (videoControlHeightDp * resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val TAG = "ActivationView"

        /**
         * Creates ActivationView from DivData with configuration
         * @param context Android context
         * @param previewData DivData for rendering
         * @param config ActivationConfig with handlers and positioning
         * @return Configured ActivationView instance
         */
        @JvmStatic
        fun createFromDivData(
            context: Context,
            previewData: DivData,
            config: ActivationConfig
        ): ActivationView {
            return ActivationView(context).apply {
                setConfig(config)
                setViewData(previewData)
            }
        }

        /**
         * Creates ActivationView from JSON with configuration
         * @param context Android context
         * @param previewJson JSONObject containing view data
         * @param config ActivationConfig with handlers and positioning
         * @return Configured ActivationView instance
         * @throws ActivationViewException if JSON parsing fails
         */
        @JvmStatic
        fun createFromJson(
            context: Context,
            previewJson: JSONObject,
            config: ActivationConfig
        ): ActivationView {
            return ActivationView(context).apply {
                setConfig(config)
                setViewDataFromJson(previewJson)
            }
        }
    }

    /**
     * Sets configuration for activation view behavior and appearance
     * @param config ActivationConfig containing handlers and positioning
     */
    fun setConfig(config: ActivationConfig) {
        if (config.onOutsideClickHandler != null) {
            setupOutsideClickHandling()
        }
        this.config = config
    }

    /**
     * Displays the view with DivData
     * @param viewData DivData to render
     * @throws IllegalStateException if config not set
     */
    fun setViewData(viewData: DivData) {
        val cfg = config ?: throw IllegalStateException("Config must be set before data")
        initializeView(viewData, cfg)
    }

    /**
     * Displays the view with JSON data
     * @param jsonObject JSONObject containing activation data
     * @throws ActivationViewException if JSON parsing fails
     */
    fun setViewDataFromJson(jsonObject: JSONObject) {
        try {
            val divData = jsonObject.asTemplateAndCardParsed()
            setViewData(divData)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON data", e)
            throw ActivationViewException("Invalid JSON data format", e)
        }
    }

    /**
     * Gets the calculated layout parameters for positioning
     * @return RelativeLayout.LayoutParams or null if not initialized
     */
    fun getViewLayoutParams(): RelativeLayout.LayoutParams? = layoutParam

    /**
     * Initializes view components with data and configuration
     * @param data DivData for rendering
     * @param config View configuration settings
     * @throws ActivationViewException if initialization fails
     */
    private fun initializeView(data: DivData, config: ActivationConfig) {
        cleanup()

        try {
            setupDivView(data, config)
            setupClickHandlers(config)
            setupLayoutParams(config)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize view", e)
            throw ActivationViewException("View initialization failed", e)
        }
    }


    /**
     * Creates and configures Div2View with themed context
     * @param data DivData to set on the view
     * @param config Configuration containing div settings
     */
    private fun setupDivView(data: DivData, config: ActivationConfig) {
        val themedContext = ContextThemeWrapper(context, context.applicationInfo.theme)

        divView = Div2View(
            Div2Context(
                baseContext = themedContext,
                configuration = config.divConfiguration
            )
        ).apply {
            val dataTag = DivDataTag("SourceSync-ActivationView")
            setData(data, dataTag)
        }

        divView?.let { addView(it) }
    }

    /**
     * Configures click handlers from config
     * @param config Configuration containing click handlers
     */
    private fun setupClickHandlers(config: ActivationConfig) {
        setOnClickListener {
            config.onPreviewClickHandler?.onClick(this)
        }
    }

    /**
     * Calculates and sets layout parameters for positioning
     * @param config Configuration containing position settings
     */
    private fun setupLayoutParams(config: ActivationConfig) {
        val layoutParams = RelativeLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        layoutParam = LayoutUtils.getLayoutParams(config.activationPosition, layoutParams)
    }

    /**
     * Enables outside click detection on parent view
     * Posts to UI thread to ensure parent is attached
     */
    private fun setupOutsideClickHandling() {
        post {
            val parentView = parent as? ViewGroup
            if (parentView != null) {
                touchOutsideListener = TouchOutsideListener()
                parentView.setOnTouchListener(touchOutsideListener)
            } else {
                Log.w(TAG, "Parent view not found for outside click handling")
            }
        }
    }

    /**
     * Hides the activation view and cleans up resources
     */
    fun hide(){
        cleanup()
    }
    /**
     * Cleans up resources and removes views to prevent memory leaks
     */
    fun cleanup() {
        // Clean up touch listener
        touchOutsideListener?.let { listener ->
            (parent as? ViewGroup)?.setOnTouchListener(null)
        }
        touchOutsideListener = null

        // Clean up div view
        divView?.let { view ->
            LayoutUtils.safeCleanup(TAG, view)
            removeView(view)
        }
        divView = null

        // Clean up click listeners
        setOnClickListener(null)
    }

    override fun onDetachedFromWindow() {
        cleanup()
        super.onDetachedFromWindow()
    }

    /**
     * Handles touch events outside the activation view to trigger dismissal
     * Preserves video control functionality by ignoring touches in control areas
     */
    private inner class TouchOutsideListener : OnTouchListener {

        /**
         * Detects touches outside view bounds and triggers outside click handler
         * @param v Parent view receiving the touch event
         * @param event MotionEvent containing touch coordinates
         * @return false to allow touch event propagation
         */
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                val outRect = Rect()
                getHitRect(outRect)

                if (!outRect.contains(event.x.toInt(), event.y.toInt()) &&
                    !isVideoControlArea(event, v)
                ) {
                    config?.onOutsideClickHandler?.run()
                    return false
                }
            }
            return false
        }

        /**
         * Checks if touch event is within video control area
         * @param event MotionEvent to check
         * @param parentView Parent view for height calculation
         * @return true if touch is in video control area
         */
        private fun isVideoControlArea(event: MotionEvent, parentView: View): Boolean {
            val bottomControlArea = parentView.height - videoControlHeightPx
            return event.y > bottomControlArea
        }
    }
}

// Custom exception for better error handling
class ActivationViewException(message: String, cause: Throwable? = null) : Exception(message, cause)