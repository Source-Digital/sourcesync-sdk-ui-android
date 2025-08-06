package io.sourcesync.sdk.ui.divkit

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.FrameLayout
import com.yandex.div.DivDataTag
import com.yandex.div.core.Div2Context
import com.yandex.div.core.DivConfiguration
import com.yandex.div.core.view2.Div2View
import com.yandex.div2.DivData
import io.sourcesync.sdk.ui.utils.LayoutUtils.forceCleanup
import io.sourcesync.sdk.ui.utils.LayoutUtils.isSafeForCleanup
import io.sourcesync.sdk.ui.utils.LayoutUtils.safeCleanup

@SuppressLint("ViewConstructor")
class ActivationPreview(
    context: Context,
    previewData: DivData,
    config: DivConfiguration
) : FrameLayout(context) {
    private var divView: Div2View? = null

    init {
        initializeView(previewData, config)
    }

    /**
     * Initializes the DivView with the provided data and configuration
     * @param previewData The DivData to display
     * @param config The DivConfiguration to use
     */
    private fun initializeView(previewData: DivData, config: DivConfiguration) {
        try {
            val themedContext = ContextThemeWrapper(
                context,
                context.applicationInfo.theme
            )

            divView = Div2View(
                Div2Context(
                    baseContext = themedContext,
                    configuration = config
                )
            )

            divView?.setData(previewData, DivDataTag("SourceSync-ActivationPreview"))
            divView?.let { addView(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error in initializeView", e)
        }
    }

    override fun onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow called")

        if (isSafeForCleanup(TAG,divView)) {
            safeCleanup(TAG, divView)
        } else {
            forceCleanup(TAG, divView)
        }

        super.onDetachedFromWindow()
    }

    fun safeCleanup() {
        safeCleanup(TAG, divView)
    }

    companion object {
        private const val TAG = "ActivationPreview"
    }
}