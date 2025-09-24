package io.sourcesync.sdk.ui.views2

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ContextThemeWrapper
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
 * Standalone preview component for activations
 */
class Activation2Preview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var divView: Div2View? = null
    private var config: ActivationConfig? = null
    private var layoutParam: RelativeLayout.LayoutParams? = null

    companion object {
        private const val TAG = "ActivationPreview"

        /**
         * Factory method to create preview with data
         */
        @JvmStatic
        fun create(
            context: Context,
            previewData: DivData,
            config: ActivationConfig?
        ): Activation2Preview {
            return Activation2Preview(context).apply {
                setConfig(config)
                setData(previewData)
            }
        }

        /**
         * Factory method to create preview from JSON
         */
        @JvmStatic
        fun create(
            context: Context,
            previewJson: JSONObject,
            config: ActivationConfig?
        ): Activation2Preview {
            return Activation2Preview(context).apply {
                setConfig(config)
                setDataFromJson(previewJson)
            }
        }
    }

    /**
     * Set the configuration for this preview
     */
    fun setConfig(config: ActivationConfig?) {
        this.config = config
    }


    /**
     * Set preview data directly
     */
    fun setData(previewData: DivData) {
        val cfg = config ?: throw IllegalStateException("Config must be set before data")
        initializeView(previewData, cfg)
    }

    /**
     * Set preview data from JSON
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

    private fun initializeView(previewData: DivData, config: ActivationConfig) {
        cleanup()

        try {
            val themedContext = ContextThemeWrapper(
                context,
                context.applicationInfo.theme
            )

            this.setOnClickListener {
                config.onPreviewClickHandler?.onClick(this)
            }
            divView = Div2View(
                Div2Context(
                    baseContext = themedContext,
                    configuration = config.divConfiguration
                )
            )

            divView?.setData(previewData, DivDataTag("SourceSync-ActivationPreview"))
            divView?.let { addView(it) }

            val layoutParams =
                RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

            layoutParam = LayoutUtils.getLayoutParams(config.activationPosition, layoutParams)
        } catch (e: Exception) {
            Log.e(TAG, "Error in initializeView", e)
        }
    }

    fun cleanup() {
        divView?.let {
            LayoutUtils.safeCleanup(TAG, it)
            removeView(it)
        }
        divView = null
    }

    override fun onDetachedFromWindow() {
        cleanup()
        super.onDetachedFromWindow()
    }
}