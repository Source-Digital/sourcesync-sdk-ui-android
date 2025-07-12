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

@SuppressLint("ViewConstructor")
class ActivationDetail(
    context: Context,
    detailsData: DivData,
    divConfig: DivConfiguration
) : FrameLayout(context) {
    private lateinit var divView: Div2View

    init {
        initializeView(detailsData, divConfig)
    }

    private fun initializeView(
        detailsData: DivData,
        config: DivConfiguration
    ) {
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


            divView.setData(detailsData, DivDataTag("SourceSync-ActivationDetails"))

            // Add content container to frame layout
            addView(divView)
        } catch (e: Exception) {
            Log.e(TAG, "Error in initializeView", e)
        }
    }

    companion object {
        private const val TAG = "ActivationDetails"
    }
}