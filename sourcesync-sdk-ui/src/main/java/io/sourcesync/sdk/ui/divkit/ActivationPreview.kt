package io.sourcesync.sdk.ui.divkit

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.view.ContextThemeWrapper
import android.widget.FrameLayout
import com.yandex.div.DivDataTag
import com.yandex.div.core.Div2Context
import com.yandex.div.core.DivConfiguration
import com.yandex.div.core.view2.Div2View
import com.yandex.div2.DivData

@SuppressLint("ViewConstructor")
class ActivationPreview
    (context: Context, previewData: DivData, config: DivConfiguration) : FrameLayout(context) {
    private lateinit var divView: Div2View

    init {
        initializeView(previewData, config)
    }

    /**
     * Initializes the DivView with the provided data and configuration
     * @param previewData The DivData to display
     * @param config The DivConfiguration to use
     */

    private fun initializeView(previewData: DivData, config: DivConfiguration) {

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
        divView.setData(previewData, DivDataTag("SourceSync-ActivationPreview"))

        // Add content container to frame layout
        addView(divView)
    }
}