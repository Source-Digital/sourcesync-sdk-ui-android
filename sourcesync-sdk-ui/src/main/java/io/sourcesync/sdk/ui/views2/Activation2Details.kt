package io.sourcesync.sdk.ui.views2

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
 * Standalone details component for activations
 */
class Activation2Details @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var divView: Div2View? = null
    private var config: ActivationConfig? = null
    private var layoutParam: RelativeLayout.LayoutParams? = null
    private var touchOutsideListener: TouchOutsideListener? = null

    init {
        setupOutsideClickOverlay()
    }

    companion object {
        private const val TAG = "ActivationDetails"

        /**
         * Factory method to create details with data
         */
        @JvmStatic
        fun createFromDivData(
            context: Context,
            detailsData: DivData,
            config: ActivationConfig
        ): Activation2Details {
            return Activation2Details(context).apply {
                setConfig(config)
                setData(detailsData)
            }
        }

        /**
         * Factory method to create details from JSON
         */
        @JvmStatic
        fun createFromJson(
            context: Context,
            detailsJson: JSONObject,
            config: ActivationConfig
        ): Activation2Details {
            return Activation2Details(context).apply {
                setConfig(config)
                setDataFromJson(detailsJson)
            }
        }
    }

    /**
     * Set the configuration for this details view
     */
    fun setConfig(config: ActivationConfig) {
        this.config = config
    }

    /**
     * Set details data directly
     */
    fun setData(detailsData: DivData) {
        val cfg = config ?: throw IllegalStateException("Config must be set before data")
        initializeView(detailsData, cfg)
    }

    /**
     * Set details data from JSON
     */
    fun setDataFromJson(jsonObject: JSONObject) {
        try {
            val divData = jsonObject.asTemplateAndCardParsed()
            setData(divData)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON data", e)
        }
    }

    fun getLayoutParam(): RelativeLayout.LayoutParams? = layoutParam

    private fun initializeView(detailsData: DivData, config: ActivationConfig) {
        cleanup()

        try {
            val themedContext = ContextThemeWrapper(
                context,
                context.applicationInfo.theme
            )

            divView = Div2View(
                Div2Context(
                    baseContext = themedContext,
                    configuration = config.divConfiguration
                )
            )

            divView?.setData(detailsData, DivDataTag("SourceSync-ActivationDetails"))
            divView?.let { addView(it) }

            val layoutParams =
                RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)

            layoutParam = LayoutUtils.getLayoutParams(config.activationPosition, layoutParams)

        } catch (e: Exception) {
            Log.e(TAG, "Error in initializeView", e)
        }
    }

    fun setupOutsideClickOverlay() {
        post {
            val parentView = parent as? ViewGroup ?: return@post
            touchOutsideListener = TouchOutsideListener()
            // Create a custom touch delegate that checks bounds
            parentView.setOnTouchListener(touchOutsideListener)
        }
    }

    fun cleanup() {
        divView?.let {
            LayoutUtils.safeCleanup(TAG, it)
            removeView(it)
        }
        touchOutsideListener = null
        divView = null
    }

    override fun onDetachedFromWindow() {
        cleanup()
        super.onDetachedFromWindow()
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
                        config?.onOutsideClickHandler?.run()
                        return false
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