package io.sourcesync.sdk.ui.divkit

import android.annotation.SuppressLint
import android.content.Context
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

    private fun initializeView(previewData: DivData, config: DivConfiguration) {

        divView = Div2View(
            Div2Context(
                baseContext = context as ContextThemeWrapper,
                configuration = config
            )
        )
        divView.setData(previewData, DivDataTag("SourceSync-ActivationPreview"))

        // Add content container to frame layout
        addView(divView)

        // Make the preview clickable
        isClickable = true
        isFocusable = true

    }
}